# Vert.x Service Factory

[![Build Status](https://github.com/vert-x3/vertx-service-factory/workflows/CI/badge.svg?branch=master)](https://github.com/vert-x3/vertx-service-factory/actions?query=workflow%3ACI)

This is a `VerticleFactory` implementation which deploys a verticle given a service name.

The service name is used to lookup a JSON descriptor file which determines the actual verticle that is to be deployed,
and can also contain deployment options for the verticle such as whether it should be run as a worker, and default
config for the service.

Please see the main documentation on the web-site for a full description:

* [Web-site documentation](https://vertx.io/docs/vertx-service-factory/java/)
