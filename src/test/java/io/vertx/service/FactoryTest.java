package io.vertx.service;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.mymodule.Verticle1;
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
  public void testDeploy() throws Exception {
    testDeploy("my.module", Verticle1.class.getName());
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
    assertWaitUntil(() -> vertx.deploymentIDs().size() == 1);
    awaitLatch(latch);
  }

  @Test
  public void testUndeploy1() throws Exception {
    testUndeploy("my.module", Verticle1.class.getName());
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
      vertx.undeploy(res.result(), res2 -> {
        assertTrue(res2.succeeded());
        latch.countDown();
      });
    });
    assertWaitUntil(() -> vertx.deploymentIDs().size() == 0);
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
    vertx.deployVerticle("service:my.optionsmodule", options, res -> {
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
    JsonObject conf = new JsonObject().put("foo", "wibble").put("quux", "blah");
    vertx.deployVerticle("service:my.confmodule", new DeploymentOptions().setConfig(conf), res -> {
      assertTrue(res.succeeded());
      latch.countDown();
    });
    awaitLatch(latch);
  }

  @Test
  public void testEmptyServiceID() throws Exception {
    vertx.deployVerticle("service:", res -> {
      assertTrue(res.failed());
      assertTrue(res.cause() instanceof IllegalArgumentException);
      assertTrue(res.cause().getMessage().startsWith("Invalid identifier"));
      testComplete();
    });
    await();
  }

  @Test
  public void testNoSuchService() throws Exception {
    vertx.deployVerticle("service:not-exists", res -> {
      assertTrue(res.failed());
      assertTrue(res.cause() instanceof IllegalArgumentException);
      assertTrue(res.cause().getMessage().startsWith("Cannot find service descriptor file not-exists.json"));
      testComplete();
    });
    await();
  }

  @Test
  public void testEmptyDescriptor() throws Exception {
    vertx.deployVerticle("service:my.empty", res -> {
      assertTrue(res.failed());
      assertTrue(res.cause() instanceof IllegalArgumentException);
      assertTrue(res.cause().getMessage().startsWith("my.empty.json is empty"));
      testComplete();
    });
    await();
  }

  @Test
  public void testInvalidJSON() throws Exception {
    vertx.deployVerticle("service:my.invalid", res -> {
      assertTrue(res.failed());
      assertTrue(res.cause() instanceof IllegalArgumentException);
      assertTrue(res.cause().getMessage().startsWith("my.invalid.json contains invalid json"));
      testComplete();
    });
    await();
  }

  @Test
  public void testNoMain() throws Exception {
    vertx.deployVerticle("service:my.nomain", res -> {
      assertTrue(res.failed());
      assertTrue(res.cause() instanceof IllegalArgumentException);
      assertTrue(res.cause().getMessage().startsWith("my.nomain.json does not contain a main field"));
      testComplete();
    });
    await();
  }


}
