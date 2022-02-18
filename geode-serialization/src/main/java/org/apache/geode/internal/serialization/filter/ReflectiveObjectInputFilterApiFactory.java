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
import static org.apache.commons.lang3.JavaVersion.JAVA_1_8;
import static org.apache.commons.lang3.JavaVersion.JAVA_9;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtLeast;
import static org.apache.geode.internal.serialization.filter.ApiPackage.JAVA_IO;
import static org.apache.geode.internal.serialization.filter.ApiPackage.SUN_MISC;
import static org.apache.geode.internal.serialization.filter.ObjectInputFilterUtils.throwUnsupportedOperationException;

import org.apache.logging.log4j.Logger;

import org.apache.geode.logging.internal.log4j.api.LogService;

/**
 * Implementation of {@code ObjectInputFilterApiFactory} that creates a reflection based
 * {@code ObjectInputFilterApi}.
 */
public class ReflectiveObjectInputFilterApiFactory implements ObjectInputFilterApiFactory {

  private static final Logger logger = LogService.getLogger();

  public ReflectiveObjectInputFilterApiFactory() {
    logger.info("GEODE-10060: enter/exit ReflectiveObjectInputFilterApiFactory#constructor [{}]",
        identityHashCode(this));
  }

  @Override
  public ObjectInputFilterApi createObjectInputFilterApi() {
    logger.info(
        "GEODE-10060: enter ReflectiveObjectInputFilterApiFactory#createObjectInputFilterApi [{}]",
        identityHashCode(this));
    try {
      if (isJavaVersionAtLeast(JAVA_9)) {
        logger.info(
            "GEODE-10060: exit-1 ReflectiveObjectInputFilterApiFactory#createObjectInputFilterApi [{}]",
            identityHashCode(this));
        return new Java9ReflectiveObjectInputFilterApi(JAVA_IO);
      }
      if (isJavaVersionAtLeast(JAVA_1_8)) {
        logger.info(
            "GEODE-10060: exit-2 ReflectiveObjectInputFilterApiFactory#createObjectInputFilterApi [{}]",
            identityHashCode(this));
        return new ReflectiveObjectInputFilterApi(SUN_MISC);
      }
    } catch (ClassNotFoundException | NoSuchMethodException e) {
      logger.info(
          "GEODE-10060: exit-3 ReflectiveObjectInputFilterApiFactory#createObjectInputFilterApi [{}]",
          identityHashCode(this));
      throwUnsupportedOperationException(e);
    }
    logger.info(
        "GEODE-10060: exit-4 ReflectiveObjectInputFilterApiFactory#createObjectInputFilterApi [{}]",
        identityHashCode(this));
    throwUnsupportedOperationException();
    return null; // unreachable
  }
}
