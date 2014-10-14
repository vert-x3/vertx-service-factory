package io.vertx.mymodule;

import io.vertx.core.AbstractVerticle;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Verticle1 extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    vertx.eventBus().publish("moduleStarted", Verticle1.class.getName());
  }

  @Override
  public void stop() throws Exception {
    vertx.eventBus().publish("moduleStopped", "whatever");
  }
}
