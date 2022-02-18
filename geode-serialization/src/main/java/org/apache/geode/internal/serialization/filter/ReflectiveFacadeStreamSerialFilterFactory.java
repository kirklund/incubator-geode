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

import java.util.Set;

import org.apache.logging.log4j.Logger;

import org.apache.geode.logging.internal.log4j.api.LogService;

/**
 * Creates an instance of {@code ObjectInputFilter} that delegates to {@code ObjectInputFilterApi}
 * to maintain independence from the JRE version.
 */
public class ReflectiveFacadeStreamSerialFilterFactory implements StreamSerialFilterFactory {

  private static final Logger logger = LogService.getLogger();

  public ReflectiveFacadeStreamSerialFilterFactory() {
    logger.info(
        "GEODE-10060: enter/exit ReflectiveFacadeStreamSerialFilterFactory$constructor [{}]",
        identityHashCode(this));
  }

  @Override
  public StreamSerialFilter create(SerializableObjectConfig config, Set<String> sanctionedClasses) {
    logger.info("GEODE-10060: enter ReflectiveFacadeStreamSerialFilterFactory$create [{}]",
        identityHashCode(this));
    ObjectInputFilterApi api =
        new ReflectiveObjectInputFilterApiFactory().createObjectInputFilterApi();

    if (config.getValidateSerializableObjects()) {
      String pattern = new SanctionedSerializablesFilterPattern()
          .append(config.getSerializableObjectFilter())
          .pattern();

      logger.info("GEODE-10060: exit-1 ReflectiveFacadeStreamSerialFilterFactory$create [{}]",
          identityHashCode(this));
      return new ReflectiveFacadeStreamSerialFilter(api, pattern, sanctionedClasses);
    }
    logger.info("GEODE-10060: exit-2 ReflectiveFacadeStreamSerialFilterFactory$create [{}]",
        identityHashCode(this));
    return new NullStreamSerialFilter();
  }
}
