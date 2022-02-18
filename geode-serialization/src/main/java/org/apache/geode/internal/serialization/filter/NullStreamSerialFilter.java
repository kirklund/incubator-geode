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

import java.io.ObjectInputStream;

import org.apache.logging.log4j.Logger;

import org.apache.geode.logging.internal.log4j.api.LogService;

/**
 * Implementation of {@code StreamSerialFilter} that does nothing.
 */
public class NullStreamSerialFilter implements StreamSerialFilter {

  private static final Logger logger = LogService.getLogger();

  public NullStreamSerialFilter() {
    logger.info("GEODE-10060: enter/exit NullStreamSerialFilter#constructor [{}]",
        identityHashCode(this));
  }

  @Override
  public void setFilterOn(ObjectInputStream objectInputStream) {
    logger.info("GEODE-10060: enter/exit NullStreamSerialFilter#setFilterOn [{}]",
        identityHashCode(this));
    // Do nothing, this is the case where we don't filter.
  }
}
