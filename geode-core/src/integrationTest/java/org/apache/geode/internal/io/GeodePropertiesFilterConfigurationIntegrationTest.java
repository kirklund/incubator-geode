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

import static org.apache.geode.internal.io.SerialFilterAssertions.assertThatSerialFilterIsNotNull;
import static org.apache.geode.internal.io.SerialFilterAssertions.assertThatSerialFilterIsNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

/**
 * Global serial filter can only be set once within a JVM.
 */
public class GeodePropertiesFilterConfigurationIntegrationTest {

  @Before
  public void serialFilterIsNullBeforeTest()
      throws InvocationTargetException, IllegalAccessException {
    assertThatSerialFilterIsNull();
  }

  @Test
  public void configuresSerialFilter()
      throws InvocationTargetException, IllegalAccessException {
    GeodePropertiesFilterConfiguration configuration =
        new GeodePropertiesFilterConfiguration(new Properties());

    configuration.configureJdkSerialFilter();

    assertThatSerialFilterIsNotNull();
  }
}
