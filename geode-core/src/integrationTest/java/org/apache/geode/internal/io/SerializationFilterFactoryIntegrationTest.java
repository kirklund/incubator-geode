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

import static org.apache.commons.lang3.JavaVersion.JAVA_1_8;
import static org.apache.commons.lang3.JavaVersion.JAVA_9;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtLeast;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.junit.Test;

import org.apache.geode.distributed.internal.DistributedSystemService;

public class SerializationFilterFactoryIntegrationTest {

  @Test
  public void createsJava8InputStreamFilter_onJava8() {
    assumeThat(isJavaVersionAtMost(JAVA_1_8)).isTrue();

    SerializationFilterFactory factory = new SerializationFilterFactory();
    String serializationFilterSpec = new SanctionedSerializablesFilterPattern().pattern();
    SanctionedSerializables sanctionedSerializables = mock(SanctionedSerializables.class);

    InputStreamFilter result = factory.create(
        serializationFilterSpec, loadDistributedSystemServices(), sanctionedSerializables);

    assertThat(result).isInstanceOf(Java8InputStreamFilter.class);
  }

  @Test
  public void createsJava9InputStreamFilter_onJava9orGreater() {
    assumeThat(isJavaVersionAtLeast(JAVA_9)).isTrue();

    SerializationFilterFactory factory = new SerializationFilterFactory();
    String serializationFilterSpec = new SanctionedSerializablesFilterPattern().pattern();
    SanctionedSerializables sanctionedSerializables = mock(SanctionedSerializables.class);

    InputStreamFilter result = factory.create(
        serializationFilterSpec, loadDistributedSystemServices(), sanctionedSerializables);

    assertThat(result).isInstanceOf(Java9InputStreamFilter.class);
  }

  private static List<DistributedSystemService> loadDistributedSystemServices() {
    ServiceLoader<DistributedSystemService> loader =
        ServiceLoader.load(DistributedSystemService.class);
    List<DistributedSystemService> services = new ArrayList<>();
    for (DistributedSystemService service : loader) {
      services.add(service);
    }
    return services;
  }
}
