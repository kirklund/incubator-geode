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
package org.apache.geode.internal.serialization.filter;

import org.apache.geode.internal.serialization.filter.impl.ConditionalGlobalSerialFilterConfiguration;
import org.apache.geode.internal.serialization.filter.impl.EnableFiltering;

public class ConditionalGlobalSerialFilterConfigurationFactory implements
    GlobalSerialFilterConfigurationFactory {

  private final EnableFiltering enableFiltering;

  public ConditionalGlobalSerialFilterConfigurationFactory() {
    this(() -> false);
  }

  /**
   * Example:
   * {@code
   * () -> isJavaVersionAtLeast(JAVA_1_8) &&
   *       isJavaVersionAtMost(JAVA_1_8) &&
   *       isBlank(System.getProperty("jdk.serialFilter"))
   * }
   */
  private ConditionalGlobalSerialFilterConfigurationFactory(EnableFiltering enableFiltering) {
    this.enableFiltering = enableFiltering;
  }

  @Override
  public FilterConfiguration create(SerializableObjectConfig serializableObjectConfig) {
    if (enableFiltering.isEnabled()) {
      return new ConditionalGlobalSerialFilterConfiguration(serializableObjectConfig);
    }
    return () -> false;
  }
}
