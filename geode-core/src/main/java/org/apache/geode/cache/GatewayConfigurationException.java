/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.cache;


/**
 * An exception indicating that a gateway configuration will not work with the remote side of the
 * gateway's configuration.
 * 
 * @since GemFire 6.6
 */
public class GatewayConfigurationException extends GatewayException {
  private static final long serialVersionUID = 3112317608711263007L;

  public GatewayConfigurationException() {
    super();
  }

  public GatewayConfigurationException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public GatewayConfigurationException(String msg) {
    super(msg);
  }

  public GatewayConfigurationException(Throwable cause) {
    super(cause);
  }



}
