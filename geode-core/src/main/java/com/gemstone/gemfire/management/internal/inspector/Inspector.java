/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gemstone.gemfire.management.internal.inspector;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Inspector {

  private final Properties config;

  public Inspector() {
    this.config = readDefaultProperties();
  }

  Properties getConfig() {
    return this.config;
  }

  private Properties readDefaultProperties() {
    try {
      Properties props = new Properties();
      InputStream in = getClass().getResourceAsStream("/com/gemstone/gemfire/management/internal/inspector/inspector-default.properties");
      props.load(in);
      in.close();
      return props;
    } catch (IOException e) {
      throw new Error("Unable to read default properties", e);
    }
  }

}
