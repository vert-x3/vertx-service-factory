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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.VerticleFactory;

import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

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
  public void init(Vertx vertx) {
  }

  @Override
  public String prefix() {
    return "service";
  }

  @Override
  public Verticle createVerticle(String verticleName, ClassLoader classLoader) throws Exception {
    String descriptorFile = verticleName.replace(':', '.') + ".json";
    JsonObject descriptor;
    try (InputStream is = classLoader.getResourceAsStream(descriptorFile)) {
      if (is == null) {
        throw new VertxException("Cannot find file " + descriptorFile + " on classpath");
      }
      try (Scanner scanner = new Scanner(is, "UTF-8").useDelimiter("\\A")) {
        String conf = scanner.next();
        descriptor = new JsonObject(conf);
      } catch (NoSuchElementException e) {
        throw new VertxException(descriptorFile + " is empty");
      } catch (DecodeException e) {
        throw new VertxException(descriptorFile + " contains invalid json");
      }
    }
    String main = descriptor.getString("main");
    if (main == null) {
      throw new VertxException(descriptorFile + " does not contain a main field");
    }
    JsonObject joptions = descriptor.getObject("options");
    DeploymentOptions options = joptions == null ? new DeploymentOptions() : new DeploymentOptions(joptions);
    return new ServiceVerticle(main, options);
  }

  @Override
  public void close() {
  }

  private class ServiceVerticle extends AbstractVerticle {

    final String main;
    final DeploymentOptions options;

    private ServiceVerticle(String main, DeploymentOptions options) {
      this.main = main;
      this.options = options;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
      JsonObject conf = vertx.context().config();
      // Merge in the conf provided by the user
      JsonObject serviceConf = options.getConfig() == null ? new JsonObject() : options.getConfig();
      serviceConf.mergeIn(conf);
      options.setConfig(serviceConf);
      vertx.deployVerticle(main, options, res -> {
        if (res.succeeded()) {
          startFuture.complete();
        } else {
          startFuture.fail(res.cause());
        }
      });
    }

    // NOTE
    // No need to override stop and explicitly undeploy as the indirected deployment will be a child
    // deployment of the service deployment so will be automatically undeployed when the parent is
    // undeployed
  }
}
