/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.service;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.VerticleFactory;

import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.Callable;

/**
 * This verticle factory looks for a descriptor file on the classpath with a name given by the verticleName
 * (with ':' replaced by '.' as ':' is an illegal file name character in Windows) and
 * with ".json" appended to it.
 * When it finds that file it deploys the actual verticle that's defined by the "main" field in that file.
 * The descriptor file can also contain a field called "options" which is the JSON form of the DeploymentOptions
 * class
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ServiceVerticleFactory implements VerticleFactory {

  @Override
  public boolean requiresResolve() {
    return true;
  }

  @Override
  public void resolve(String identifier, DeploymentOptions deploymentOptions, ClassLoader classLoader, Promise<String> resolution) {
    identifier = VerticleFactory.removePrefix(identifier);
    String descriptorFile = identifier + ".json";
    try {
      JsonObject descriptor;
      String main;
      try (InputStream is = classLoader.getResourceAsStream(descriptorFile)) {
        if (is == null) {
          throw new IllegalArgumentException("Cannot find service descriptor file " + descriptorFile + " on classpath");
        }
        try (Scanner scanner = new Scanner(is, "UTF-8").useDelimiter("\\A")) {
          String conf = scanner.next();
          descriptor = new JsonObject(conf);
        } catch (NoSuchElementException e) {
          throw new IllegalArgumentException(descriptorFile + " is empty");
        } catch (DecodeException e) {
          throw new IllegalArgumentException(descriptorFile + " contains invalid json");
        }
      }
      main = descriptor.getString("main");
      if (main == null) {
        throw new IllegalArgumentException(descriptorFile + " does not contain a main field");
      }

      // Any options specified in the service config will override anything specified at deployment time
      // With the exception of config which can be overridden with config provided at deployment time
      JsonObject depOptions = deploymentOptions.toJson();
      JsonObject depConfig = depOptions.getJsonObject("config", new JsonObject());
      JsonObject serviceOptions = descriptor.getJsonObject("options", new JsonObject());
      JsonObject serviceConfig = serviceOptions.getJsonObject("config", new JsonObject());
      depOptions.mergeIn(serviceOptions);
      serviceConfig.mergeIn(depConfig);
      depOptions.put("config", serviceConfig);
      deploymentOptions.fromJson(depOptions);
      resolution.complete(main);
    } catch (Exception e) {
      resolution.fail(e);
    }
  }

  @Override
  public String prefix() {
    return "service";
  }

  @Override
  public void createVerticle(String verticleName, ClassLoader classLoader, Promise<Callable<Verticle>> promise) {
    promise.fail("Shouldn't be called");
  }
}
