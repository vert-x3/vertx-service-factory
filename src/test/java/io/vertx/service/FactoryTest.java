package io.vertx.service;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.mymodule.Verticle1;
import io.vertx.mymodule.Verticle2;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class FactoryTest extends VertxTestBase {

  @Test
  public void testDeploy1() throws Exception {
    testDeploy("my:module:1.0", Verticle1.class.getName());
  }

  @Test
  public void testDeploy2() throws Exception {
    testDeploy("somename", Verticle2.class.getName());
  }

  private void testDeploy(String serviceName, String verticleClassName) throws Exception {
    CountDownLatch latch = new CountDownLatch(2);
    vertx.eventBus().localConsumer("moduleStarted").handler(message -> {
      assertEquals(verticleClassName, message.body());
      latch.countDown();
    });
    vertx.deployVerticle("service:" + serviceName, res -> {
      assertTrue(res.succeeded());
      latch.countDown();
    });
    waitUntil(() -> vertx.deployments().size() == 1);
    awaitLatch(latch);
  }

  @Test
  public void testUndeploy1() throws Exception {
    testUndeploy("my:module:1.0", Verticle1.class.getName());
  }

  @Test
  public void testUndeploy2() throws Exception {
    testUndeploy("somename", Verticle2.class.getName());
  }

  private void testUndeploy(String serviceName, String verticleClassName) throws Exception {
    CountDownLatch latch = new CountDownLatch(3);
    vertx.eventBus().localConsumer("moduleStarted").handler(message -> {
      assertEquals(verticleClassName, message.body());
      latch.countDown();
    });
    vertx.eventBus().localConsumer("moduleStopped").handler(message -> {
      latch.countDown();
    });
    vertx.deployVerticle("service:" + serviceName, res -> {
      assertTrue(res.succeeded());
      vertx.undeployVerticle(res.result(), res2 -> {
        assertTrue(res2.succeeded());
        latch.countDown();
      });
    });
    waitUntil(() -> vertx.deployments().size() == 0);
    awaitLatch(latch);
  }

  @Test
  public void testOptions() throws Exception {
    CountDownLatch latch = new CountDownLatch(2);
    vertx.eventBus().<Boolean>localConsumer("moduleStarted").handler(message -> {
      assertTrue(message.body());
      latch.countDown();
    });
    List<String> extraCP = Arrays.asList("foo");
    // These options should be overridden by those in the service descriptor
    DeploymentOptions options = new DeploymentOptions().setIsolationGroup("othergroup").setWorker(false).setExtraClasspath(extraCP);
    vertx.deployVerticle("service:my:optionsmodule:1.0", options, res -> {
      assertTrue(res.succeeded());
      latch.countDown();
    });
    awaitLatch(latch);
  }

  @Test
  public void testMergeConfig() throws Exception {
    CountDownLatch latch = new CountDownLatch(2);
    vertx.eventBus().<Boolean>localConsumer("moduleStarted").handler(message -> {
      assertTrue(message.body());
      latch.countDown();
    });
    // We override the config - this overrides any config fields in the service descriptor config
    JsonObject conf = new JsonObject().putString("foo", "wibble").putString("quux", "blah");
    vertx.deployVerticle("service:my:confmodule:1.0", new DeploymentOptions().setConfig(conf), res -> {
      assertTrue(res.succeeded());
      latch.countDown();
    });
    awaitLatch(latch);
  }


}
