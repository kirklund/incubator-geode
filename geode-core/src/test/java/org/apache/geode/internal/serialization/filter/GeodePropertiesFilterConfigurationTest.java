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

import static org.apache.geode.internal.serialization.filter.GeodePropertiesFilterConfiguration.loadSanctionedSerializablesService;
import static org.apache.geode.internal.serialization.filter.SerialFilterAssertions.assertThatSerialFilterIsNotNull;
import static org.apache.geode.internal.serialization.filter.SerialFilterAssertions.assertThatSerialFilterIsNull;
import static org.mockito.Mockito.mock;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;

import org.apache.geode.internal.serialization.filter.GeodePropertiesFilterConfiguration.SerializableObjectConfig;

public class GeodePropertiesFilterConfigurationTest {

  @Test
  public void configuresJdkSerialFilter() throws InvocationTargetException, IllegalAccessException {
    assertThatSerialFilterIsNull();

    SerializableObjectConfig config = new SerializableObjectConfig(new Properties());
    Set<SanctionedSerializablesService> services = loadSanctionedSerializablesService();
    ReflectionGlobalSerialFilter filter = new ReflectionGlobalSerialFilter()
        .globalSerialFilter(mock(GlobalSerialFilter.class));

    GeodePropertiesFilterConfiguration configuration =
        new GeodePropertiesFilterConfiguration(config, services, filter);

    configuration.configureJdkSerialFilter();

    assertThatSerialFilterIsNotNull();
  }
}
