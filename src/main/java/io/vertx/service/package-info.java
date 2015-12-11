/**
 * # Vert.x Service Factory
 *
 * This is a `VerticleFactory` implementation which deploys a verticle given a service name. Notice that this factory
 * is not related to http://vertx.io/docs/vertx-service-proxy/[vert.x service proxies], but is about facilities to
 * deploy components.
 *
 * The service name is used to lookup a JSON descriptor file which determines the actual verticle that is to be deployed,
 * and can also contain deployment options for the verticle such as whether it should be run as a worker, and default
 * config for the service.
 *
 * It is useful as it decouples the service user from the actual verticle that is deployed and it allows the service
 * to provide default deployment options and configuration.
 *
 * ## Service identifier
 *
 * The service name is simply a string - you can use anything you want, but it's a good convention to use a reverse
 * domain name (rather like a Java package name) so not to clash with other similar named services that might be on your
 * classpath. E.g:
 *
 * * Good names: `com.mycompany.services.clever-db-service`, `org.widgets.widget-processor`
 * * Poor names (but they are still valid): `accounting-service`, `foo`
 *
 * ## Usage
 *
 * When deploying the service use the prefix `service:`, this selects the service verticle factory.
 * The verticle can be deployed programmatically e.g.:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example1(io.vertx.core.Vertx, io.vertx.core.DeploymentOptions)}
 * ----
 *
 * Or can be deployed on the command line with:
 *
 * [source]
 * ----
 * vertx run service:com.mycompany-clever-db-service
 * ----
 *
 * ## Making it available
 *
 * Vert.x picks up `VerticleFactory` implementations from the classpath, so you just need to make sure the
 * ${maven.artifactId} jar file on the classpath. First you need to add the Maven verticle factory to your application's
 * classpath. If you are using a fat jar, you can use the following dependencies:
 *
 * * Maven (in your `pom.xml`):
 *
 * [source,xml,subs="+attributes"]
 * ----
 * <dependency>
 *   <groupId>${maven.groupId}</groupId>
 *   <artifactId>${maven.artifactId}</artifactId>
 *   <version>${maven.version}</version>
 * </dependency>
 * ----
 *
 * * Gradle (in your `build.gradle` file):
 *
 * [source,groovy,subs="+attributes"]
 * ----
 * compile '${maven.groupId}:${maven.artifactId}:${maven.version}'
 * ----
 *
 * You can also register a `VerticleFactory` instances programmatically on your `vertx` instance using the
 * {@link io.vertx.core.Vertx#registerVerticleFactory(io.vertx.core.spi.VerticleFactory)} method:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example2(io.vertx.core.Vertx)}
 * ----
 *
 * ## Service descriptor
 *
 * When you ask to deploy a service, the service factory first looks for a descriptor file on the classpath.
 * The descriptor file name is given by the service name concatenated with the `.json` file extension.
 *
 * E.g. for a service name of `com.mycompany.clever-db-service` it would like for a descriptor file called
 * `com.mycompany.clever-db-service.json` on the classpath.
 *
 * The descriptor file is simply a text file which must contain a valid JSON object. At minimum the JSON must provide
 * a `main` field which determines the actual verticle that will be deployed, e.g:
 *
 * [source]
 * ----
 * {
 *  "main": "com.mycompany.cleverdb.MainVerticle"
 * }
 * ----
 *
 * or
 *
 * [source]
 * ----
 * {
 *   "main": "app.js"
 * }
 * ----
 *
 * or you could even redirect to a different verticle factory e.g. the Maven verticle factory to dynamically load the
 * service from Maven at run-time:
 *
 * [source]
 * ----
 * {
 *  "main": "maven:com.mycompany:clever-db:1,2::clever-db-service"
 * }
 * ----
 *
 * The JSON can also provide an `options` field which maps exactly to a {@link io.vertx.core.DeploymentOptions} object.
 *
 * [source]
 * ----
 * {
 *   "main": "com.mycompany.cleverdb.MainVerticle",
 *   "options": {
 *     "config" : {
 *      "foo": "bar"
 *     },
 *     "worker": true,
 *     "isolationGroup": "mygroup"
 *   }
 * }
 * ----
 *
 * When deploying a service from a service descriptor, any fields that are specified in the descriptor, such as `worker`,
 * `isolationGroup`, etc cannot be overridden by any deployment options passed in at deployment time.
 *
 * The exception is `config`. Any configuration passed in at deploy time will override any corresponding fields in any
 * config present in the descriptor file.
 */
@Document(fileName = "index.ad")
package io.vertx.service;

import io.vertx.docgen.Document;