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

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;
import static org.apache.geode.internal.serialization.filter.DoNothing.doNothing;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.logging.log4j.Logger;

import org.apache.geode.annotations.VisibleForTesting;
import org.apache.geode.logging.internal.log4j.api.LogService;

public class ReflectionGlobalSerialFilter implements FilterConfiguration {

  private static final Logger logger = LogService.getLogger();

  private GlobalSerialFilter globalSerialFilter;
  private Supplier<Boolean> condition;
  private Consumer<String> infoLogger = logger::info;

  @Override
  public void configure() {
    Operation operation = condition.get()
        ? new SetGlobalSerialFilter(globalSerialFilter, infoLogger) : doNothing();
    operation.execute();
  }

  public ReflectionGlobalSerialFilter globalSerialFilter(GlobalSerialFilter globalSerialFilter) {
    this.globalSerialFilter = globalSerialFilter;
    return this;
  }

  public ReflectionGlobalSerialFilter condition(Supplier<Boolean> condition) {
    this.condition = condition;
    return this;
  }

  @VisibleForTesting
  ReflectionGlobalSerialFilter infoLogger(Consumer<String> infoLogger) {
    this.infoLogger = infoLogger;
    return this;
  }

  private static class SetGlobalSerialFilter implements Operation {

    private static final String ALREADY_SET_MESSAGE = "Serial filter can only be set once";

    private final GlobalSerialFilter globalSerialFilter;
    private final Consumer<String> infoLogger;

    private SetGlobalSerialFilter(GlobalSerialFilter globalSerialFilter,
        Consumer<String> infoLogger) {
      this.globalSerialFilter = globalSerialFilter;
      this.infoLogger = infoLogger;
    }

    @Override
    public void execute() {
      try {
        globalSerialFilter.setFilter();
      } catch (UnsupportedOperationException e) {
        if (hasRootCauseWithMessage(e, IllegalStateException.class, ALREADY_SET_MESSAGE)) {
          infoLogger.accept("Global serial filter is already configured.");
        }
      }
    }

    private static boolean hasRootCauseWithMessage(Throwable throwable,
        Class<? extends Throwable> causeClass, String message) {
      Throwable rootCause = getRootCause(throwable);
      return isInstanceOf(rootCause, causeClass) && hasMessage(rootCause, message);
    }

    private static boolean isInstanceOf(Throwable throwable,
        Class<? extends Throwable> causeClass) {
      return nonNull(throwable) && throwable.getClass().equals(causeClass);
    }

    private static boolean hasMessage(Throwable throwable, String message) {
      return nonNull(throwable) && throwable.getMessage().equalsIgnoreCase(message);
    }
  }
}
