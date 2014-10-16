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

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ServiceIndentifier {

  private final String owner;
  private final String serviceName;
  private final String version;

  public ServiceIndentifier(String owner, String serviceName, String version) {
    this.owner = owner;
    this.serviceName = serviceName;
    this.version = version;
  }

  public ServiceIndentifier(String idString) {
    String[] split = idString.split(":");
    if (split.length != 2 && split.length != 3) {
      throw new IllegalArgumentException("Invalid service identifier: " + idString);
    }
    owner = split[0];
    serviceName = split[1];
    version = split.length == 3 ? split[2] : null;
  }

  public String owner() {
    return owner;
  }

  public String serviceName() {
    return serviceName;
  }

  public String version() {
    return version;
  }

  public String descriptorFilename() {
    return owner + "." + serviceName + ".json";
  }
}
