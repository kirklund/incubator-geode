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
package org.apache.geode.examples;

import static java.util.Arrays.asList;
import static org.apache.geode.test.dunit.VM.getController;
import static org.apache.geode.test.dunit.VM.getVM;
import static org.apache.geode.test.dunit.rules.DistributedRule.getDistributedSystemProperties;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.CacheListener;
import org.apache.geode.cache.EntryEvent;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionShortcut;
import org.apache.geode.test.dunit.VM;
import org.apache.geode.test.dunit.rules.DistributedRule;
import org.apache.geode.test.dunit.rules.SharedErrorCollector;

public class SpyCacheListenerDistributedTest implements Serializable {

  private static final AtomicReference<Cache> cache = new AtomicReference<>();
  private static final AtomicInteger count = new AtomicInteger();

  @Rule
  public DistributedRule distributedRule = new DistributedRule();
  @Rule
  public SharedErrorCollector errorCollector = new SharedErrorCollector();

  @Before
  public void setUp() {
    for (VM vm : asList(getController(), getVM(0), getVM(1))) {
      vm.invoke(() -> {
        cache.set(new CacheFactory(getDistributedSystemProperties()).create());
        count.set(0);
      });
    }
  }

  @After
  public void tearDown() {
    for (VM vm : asList(getController(), getVM(0), getVM(1))) {
      vm.invoke(() -> cache.get().close());
    }
  }

  @Test
  public void correct_count_1() {
    for (VM vm : asList(getController(), getVM(0), getVM(1))) {
      vm.invoke(() -> {
        CacheListener<Object, Object> cacheListener = spy(CacheListener.class); // or mock
        doAnswer(invocation -> {
          count.incrementAndGet();
          return null;
        }).when(cacheListener).afterCreate(any());

        Region<Object, Object> region = cache.get().createRegionFactory(RegionShortcut.LOCAL)
            .addCacheListener(cacheListener)
            .create("region");

        region.create("foo", "bar");

        assertThat(count.get())
            .as("Count in VM-" + vm.getId())
            .isOne();
      });
    }
  }

  @Test
  public void correct_count_2() {
    for (VM vm : asList(getController(), getVM(0), getVM(1))) {
      vm.invoke(() -> {
        CacheListener<Object, Object> cacheListener = spy(CacheListener.class); // or mock
        doAnswer(invocation -> count.incrementAndGet()).when(cacheListener).afterCreate(any());

        Region<Object, Object> region = cache.get().createRegionFactory(RegionShortcut.LOCAL)
            .addCacheListener(cacheListener)
            .create("region");

        region.create("foo", "bar");

        assertThat(count.get())
            .as("Count in VM-" + vm.getId())
            .isOne();
      });
    }
  }

  @Test
  public void correct_count_3() {
    for (VM vm : asList(getController(), getVM(0), getVM(1))) {
      vm.invoke(() -> {
        CacheListener<Object, Object> cacheListener = spy(CacheListener.class); // or mock

        Region<Object, Object> region = cache.get().createRegionFactory(RegionShortcut.LOCAL)
            .addCacheListener(cacheListener)
            .create("region");

        region.create("foo", "bar");

        verify(cacheListener, times(1)
            .description("Count in VM-" + vm.getId()))
                .afterCreate(any());
      });
    }
  }

  @Test
  public void incorrect_errorHandling() {
    for (VM vm : asList(getController(), getVM(0), getVM(1))) {
      vm.invoke(() -> {
        CacheListener<Object, Object> cacheListener = spy(CacheListener.class); // or mock
        doThrow(new Error("Danger in VM-" + vm.getId())).when(cacheListener).afterCreate(any());

        Region<Object, Object> region = cache.get().createRegionFactory(RegionShortcut.LOCAL)
            .addCacheListener(cacheListener)
            .create("region");

        region.create("foo", "bar");
      });
    }
  }

  @Test
  public void correct_errorHandling() {
    for (VM vm : asList(getController(), getVM(0), getVM(1))) {
      vm.invoke(() -> {
        CacheListener<Object, Object> cacheListener = spy(CacheListener.class); // or mock
        doAnswer(invocation -> {
          try {
            throw new Error("Danger in VM-" + vm.getId());
          } catch (Throwable throwable) {
            errorCollector.addError(throwable);
          }
          return null;
        }).when(cacheListener).afterCreate(any());

        Region<Object, Object> region = cache.get().createRegionFactory(RegionShortcut.LOCAL)
            .addCacheListener(cacheListener)
            .create("region");

        region.create("foo", "bar");
      });
    }
  }

  @Test
  public void incorrect_asyncAssertion() {
    for (VM vm : asList(getController(), getVM(0), getVM(1))) {
      vm.invoke(() -> {
        CacheListener<Object, Object> cacheListener = spy(CacheListener.class); // or mock
        doAnswer(invocation -> {
          assertThat((EntryEvent) invocation.getArgument(0))
              .as("Argument in VM-" + vm.getId())
              .isNull();
          return null;
        }).when(cacheListener).afterCreate(any());

        Region<Object, Object> region = cache.get().createRegionFactory(RegionShortcut.LOCAL)
            .addCacheListener(cacheListener)
            .create("region");

        region.create("foo", "bar");
      });
    }
  }

  @Test
  public void correct_asyncAssertion() {
    for (VM vm : asList(getController(), getVM(0), getVM(1))) {
      vm.invoke(() -> {
        CacheListener<Object, Object> cacheListener = spy(CacheListener.class); // or mock
        doAnswer(invocation -> {
          errorCollector
              .checkThat("Argument in VM-" + vm.getId(), invocation.getArgument(0), nullValue());
          return null;
        }).when(cacheListener).afterCreate(any());

        Region<Object, Object> region = cache.get().createRegionFactory(RegionShortcut.LOCAL)
            .addCacheListener(cacheListener)
            .create("region");

        region.create("foo", "bar");
      });
    }
  }
}
