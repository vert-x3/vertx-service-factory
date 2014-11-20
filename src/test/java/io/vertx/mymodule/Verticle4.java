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
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Verticle4 extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    JsonObject conf = Vertx.currentContext().config();
    vertx.eventBus().publish("moduleStarted", conf.getString("foo").equals("wibble") &&
                             conf.getString("quux").equals("blah") && conf.getString("socks").equals("eeek"));
  }

}
