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
package org.apache.geode.internal.io;

import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;
import static org.apache.commons.lang3.JavaVersion.JAVA_1_8;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtLeast;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.apache.geode.distributed.ConfigurationProperties.SERIALIZABLE_OBJECT_FILTER;
import static org.apache.geode.distributed.ConfigurationProperties.VALIDATE_SERIALIZABLE_OBJECTS;

import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.geode.annotations.VisibleForTesting;
import org.apache.geode.distributed.internal.DistributedSystemService;

public class GeodePropertiesFilterConfiguration {

  private final SerializableObjectConfig config;
  private final Set<DistributedSystemService> services;
  private final ReflectionGlobalSerialFilter filter;

  private static Set<DistributedSystemService> loadDistributedSystemServices() {
    return stream(ServiceLoader.load(DistributedSystemService.class).spliterator(), false)
        .collect(Collectors.toSet());
  }

  public GeodePropertiesFilterConfiguration(Properties configProperties) {
    this(new SerializableObjectConfig(configProperties),
        loadDistributedSystemServices(),
        new ReflectionGlobalSerialFilter());
  }

  @VisibleForTesting
  GeodePropertiesFilterConfiguration(SerializableObjectConfig config,
      Set<DistributedSystemService> services, ReflectionGlobalSerialFilter filter) {
    this.config = config;
    this.services = unmodifiableSet(services);
    this.filter = filter;
  }

  public void configureJdkSerialFilter() {
    filter
        .globalSerialFilter(new GlobalSerialFilterFactory().create(
            new SanctionedSerializablesFilterPattern()
                .append(config.getFilterPatternIfEnabled())
                .pattern(),
            new SanctionedSerializables()
                .loadSanctionedClassNames(services)))
        .condition(() -> isJavaVersionAtLeast(JAVA_1_8) && isJavaVersionAtMost(JAVA_1_8) &&
            isBlank(System.getProperty("jdk.serialFilter")))
        .configure();
  }

  @VisibleForTesting
  static class SerializableObjectConfig {

    private final Properties config;

    SerializableObjectConfig(Properties config) {
      this.config = requireNonNull(config);
    }

    String getFilterPatternIfEnabled() {
      return isValidateSerializableObjectsEnabled() ? getSerializableObjectFilter() : null;
    }

    private boolean isValidateSerializableObjectsEnabled() {
      return "true".equalsIgnoreCase(config.getProperty(VALIDATE_SERIALIZABLE_OBJECTS));
    }

    private String getSerializableObjectFilter() {
      return config.getProperty(SERIALIZABLE_OBJECT_FILTER);
    }
  }
}
