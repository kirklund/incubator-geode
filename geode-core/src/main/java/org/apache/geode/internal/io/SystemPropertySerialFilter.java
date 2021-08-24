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

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.geode.internal.io.DoNothing.doNothing;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.logging.log4j.Logger;

import org.apache.geode.annotations.VisibleForTesting;
import org.apache.geode.logging.internal.log4j.api.LogService;

/**
 * Configure the “jmx.remote.rmi.server.serial.filter.pattern” system property if Java version is
 * Java 9 or greater. The serial pattern will be configured to accept only standard JMX open-types.
 * If the system property already has a non-null value, then leave it as is.
 *
 * <p>
 * Configure the {@code jdk.serialFilter} system property if Java version is Java 8. The serial
 * pattern will be configured to accept only geode sanctioned serializables and standard JMX
 * open-types. If the system property already has a non-null value, then leave it as is.
 */
public class SystemPropertySerialFilter implements FilterConfiguration {

  private static final Logger logger = LogService.getLogger();

  private String propertyName;
  private String filterPattern;
  private Supplier<Boolean> condition;
  private Consumer<String> infoLogger = logger::info;

  @Override
  public void configure() {
    Operation operation = condition.get()
        ? new SetSystemProperty(propertyName, filterPattern, infoLogger) : doNothing();
    operation.execute();
  }

  public SystemPropertySerialFilter propertyName(String propertyName) {
    this.propertyName = propertyName;
    return this;
  }

  public SystemPropertySerialFilter filterPattern(String filterPattern) {
    this.filterPattern = filterPattern;
    return this;
  }

  public SystemPropertySerialFilter condition(Supplier<Boolean> condition) {
    this.condition = condition;
    return this;
  }

  @VisibleForTesting
  SystemPropertySerialFilter infoLogger(Consumer<String> infoLogger) {
    this.infoLogger = infoLogger;
    return this;
  }

  private static class SetSystemProperty implements Operation {

    private final String propertyName;
    private final String filterPattern;
    private final Consumer<String> infoLogger;

    private SetSystemProperty(String propertyName, String filterPattern,
        Consumer<String> infoLogger) {
      this.propertyName = propertyName;
      this.filterPattern = filterPattern;
      this.infoLogger = infoLogger;
    }

    @Override
    public void execute() {
      if (isNotEmpty(System.getProperty(propertyName))) {
        // TODO:KIRK: the new Condition prevents this from occurring
        infoLogger.accept("System property " + propertyName + " is already configured.");
        return;
      }

      System.setProperty(propertyName, filterPattern);
      infoLogger.accept("System property " + propertyName + " is now configured with '"
          + filterPattern + "'.");
    }
  }

}
