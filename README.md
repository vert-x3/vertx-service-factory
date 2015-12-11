# Vert.x Service Factory

[![Build Status](https://vertx.ci.cloudbees.com/buildStatus/icon?job=vert.x3-service-factory)](https://vertx.ci.cloudbees.com/view/vert.x-3/job/vert.x3-service-factory/)

This is a `VerticleFactory` implementation which deploys a verticle given a service name.

The service name is used to lookup a JSON descriptor file which determines the actual verticle that is to be deployed,
and can also contain deployment options for the verticle such as whether it should be run as a worker, and default
config for the service.

The documentation is available [here](src/main/asciidoc/index.ad), or on the the 
[vert.x web site](http://vertx.io/docs/vertx-service-factory).
