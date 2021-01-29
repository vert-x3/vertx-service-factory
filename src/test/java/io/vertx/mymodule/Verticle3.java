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

package io.vertx.mymodule;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.impl.Deployment;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonObject;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Verticle3 extends AbstractVerticle {

  @Override
  public void start() {
    if (!Context.isOnWorkerThread()) {
      vertx.eventBus().publish("moduleStarted", false);
      return;
    }
    vertx.runOnContext(v -> {
      List<String> extraCP = Arrays.asList("blah", "wibble");
      DeploymentOptions expected = new DeploymentOptions().setConfig(new JsonObject().put("foo", "bar"))
        .setWorker(true).setIsolationGroup("mygroup").setExtraClasspath(extraCP);
      Deployment dep = ((VertxInternal) vertx).getDeployment(Vertx.currentContext().deploymentID());
      DeploymentOptions options = dep.deploymentOptions();
      boolean val =
        Objects.equals(options.getConfig(), expected.getConfig()) &&
        Objects.equals(options.getIsolatedClasses(), expected.getIsolatedClasses()) &&
        Objects.equals(options.getExtraClasspath(), expected.getExtraClasspath()) &&
        Objects.equals(options.getInstances(), expected.getInstances());
      vertx.eventBus().publish("moduleStarted", val);
    });
  }

}
