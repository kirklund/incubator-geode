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

import static java.lang.System.identityHashCode;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.function.BooleanSupplier;

import org.apache.logging.log4j.Logger;

import org.apache.geode.internal.lang.SystemProperty;
import org.apache.geode.logging.internal.log4j.api.LogService;

/**
 * Creates an instance of {@code GlobalSerialFilterConfiguration} that is enabled only if certain
 * conditions are met. The system property {@code jdk.serialFilter} must be blank, and the system
 * property {@code geode.enableGlobalSerialFilter} must be set to true.
 */
public class SystemPropertyGlobalSerialFilterConfigurationFactory
    implements GlobalSerialFilterConfigurationFactory {

  private static final Logger logger = LogService.getLogger();

  private final boolean enabled;

  {
    logger.info(
        "GEODE-10060: enter/exit SystemPropertyGlobalSerialFilterConfigurationFactory init-block [{}]",
        identityHashCode(this));
  }

  public SystemPropertyGlobalSerialFilterConfigurationFactory() {
    // enable GlobalSerialFilter only under these conditions:
    // (1) JRE supports ObjectInputFilter in either sun.misc. or java.io. package
    // (2) jdk.serialFilter must be blank
    // (3) geode.enableGlobalSerialFilter must be set "true"
    this(ObjectInputFilterUtils::supportsObjectInputFilter);
  }

  SystemPropertyGlobalSerialFilterConfigurationFactory(BooleanSupplier supportsObjectInputFilter) {
    this(supportsObjectInputFilter.getAsBoolean() &&
        isBlank(System.getProperty("jdk.serialFilter")) &&
        SystemProperty
            .getProductBooleanProperty("enableGlobalSerialFilter")
            .orElse(false));
  }

  private SystemPropertyGlobalSerialFilterConfigurationFactory(boolean enabled) {
    logger.info(
        "GEODE-10060: enter main SystemPropertyGlobalSerialFilterConfigurationFactory#constructor enabled = {} [{}]",
        enabled, identityHashCode(this));
    this.enabled = enabled;
    logger.info(
        "GEODE-10060: exit main SystemPropertyGlobalSerialFilterConfigurationFactory#constructor enabled = {} [{}]",
        enabled, identityHashCode(this));
  }

  @Override
  public FilterConfiguration create(SerializableObjectConfig serializableObjectConfig) {
    logger.info(
        "GEODE-10060: enter SystemPropertyGlobalSerialFilterConfigurationFactory#create [{}]",
        identityHashCode(this));
    if (enabled) {
      logger.info(
          "GEODE-10060: exit-1 SystemPropertyGlobalSerialFilterConfigurationFactory#create [{}]",
          identityHashCode(this));
      return new GlobalSerialFilterConfiguration(serializableObjectConfig);
    }
    logger.info(
        "GEODE-10060: exit-2 SystemPropertyGlobalSerialFilterConfigurationFactory#create [{}]",
        identityHashCode(this));
    return new NullFilterConfiguration();
  }

  private static class NullFilterConfiguration implements FilterConfiguration {

    public NullFilterConfiguration() {
      logger.info("GEODE-10060: enter/exit NullFilterConfiguration#constructor [{}]",
          identityHashCode(this));
    }

    @Override
    public boolean configure() {
      logger.info("GEODE-10060: enter/exit NullFilterConfiguration#configure [{}]",
          identityHashCode(this));
      return false;
    }
  }
}
