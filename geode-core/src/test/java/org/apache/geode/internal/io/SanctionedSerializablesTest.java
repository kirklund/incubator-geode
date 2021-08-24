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

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import org.apache.geode.distributed.internal.DistributedSystemService;

public class SanctionedSerializablesTest {

  @Test
  public void returnsAcceptListFromDistributedSystemService() {
    SanctionedSerializables sanctionedSerializables = new SanctionedSerializables();
    Collection<DistributedSystemService> services = new HashSet<>();
    services.add(distributedSystemService("foo", "bar"));

    Set<String> result = sanctionedSerializables.loadSanctionedClassNames(services);

    assertThat(result).containsExactlyInAnyOrder("foo", "bar");
  }

  @Test
  public void returnsAcceptListsFromManyDistributedSystemServices() {
    SanctionedSerializables sanctionedSerializables = new SanctionedSerializables();
    Collection<DistributedSystemService> services = new HashSet<>();
    services.add(distributedSystemService("foo", "bar"));
    services.add(distributedSystemService("the", "fox"));
    services.add(distributedSystemService("a", "bear"));

    Set<String> result = sanctionedSerializables.loadSanctionedClassNames(services);

    assertThat(result).containsExactlyInAnyOrder("foo", "bar", "the", "fox", "a", "bear");
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  public void nullThrowsNullPointerException() {
    SanctionedSerializables sanctionedSerializables = new SanctionedSerializables();

    Throwable thrown = catchThrowable(() -> {
      sanctionedSerializables.loadSanctionedClassNames(null);
    });
  }

  @Test
  public void emptyServicesReturnsEmptySet() {
    SanctionedSerializables sanctionedSerializables = new SanctionedSerializables();

    Set<String> result = sanctionedSerializables.loadSanctionedClassNames(emptySet());

    assertThat(result).isEmpty();
  }

  @Test
  public void servicesWithEmptyAcceptListsReturnsEmptySet() {
    SanctionedSerializables sanctionedSerializables = new SanctionedSerializables();
    Collection<DistributedSystemService> services = new HashSet<>();
    services.add(distributedSystemService());
    services.add(distributedSystemService());
    services.add(distributedSystemService());

    Set<String> result = sanctionedSerializables.loadSanctionedClassNames(services);

    assertThat(result).isEmpty();
  }

  private DistributedSystemService distributedSystemService(String... classNames) {
    try {
      DistributedSystemService service = mock(DistributedSystemService.class);
      when(service.getSerializationAcceptList()).thenReturn(asList(classNames));
      return service;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
