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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.CacheListener;
import org.apache.geode.cache.EntryEvent;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionShortcut;

public class SpyCacheListenerIntegrationTest {

  private final AtomicReference<Cache> cache = new AtomicReference<>();
  private final AtomicInteger count = new AtomicInteger();

  @Rule
  public ErrorCollector errorCollector = new ErrorCollector();

  @Before
  public void setUp() {
    cache.set(new CacheFactory().create());
    count.set(0);
  }

  @After
  public void tearDown() {
    cache.get().close();
  }

  @Test
  public void correct_count_1() {
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
        .as("Count")
        .isOne();
  }

  @Test
  public void correct_count_2() {
    CacheListener<Object, Object> cacheListener = spy(CacheListener.class); // or mock
    doAnswer(invocation -> count.incrementAndGet()).when(cacheListener).afterCreate(any());

    Region<Object, Object> region = cache.get().createRegionFactory(RegionShortcut.LOCAL)
        .addCacheListener(cacheListener)
        .create("region");

    region.create("foo", "bar");

    assertThat(count.get())
        .as("Count")
        .isOne();
  }

  @Test
  public void correct_count_3() {
    CacheListener<Object, Object> cacheListener = spy(CacheListener.class); // or mock

    Region<Object, Object> region = cache.get().createRegionFactory(RegionShortcut.LOCAL)
        .addCacheListener(cacheListener)
        .create("region");

    region.create("foo", "bar");

    verify(cacheListener, times(1)
        .description("Count"))
            .afterCreate(any());
  }

  @Test
  public void incorrect_errorHandling() {
    CacheListener<Object, Object> cacheListener = spy(CacheListener.class); // or mock
    doThrow(new Error("Danger!")).when(cacheListener).afterCreate(any());

    Region<Object, Object> region = cache.get().createRegionFactory(RegionShortcut.LOCAL)
        .addCacheListener(cacheListener)
        .create("region");

    region.create("foo", "bar");
  }

  @Test
  public void correct_errorHandling() {
    CacheListener<Object, Object> cacheListener = spy(CacheListener.class); // or mock
    doAnswer(invocation -> {
      try {
        throw new Error("Danger!");
      } catch (Throwable throwable) {
        errorCollector.addError(throwable);
      }
      return null;
    }).when(cacheListener).afterCreate(any());

    Region<Object, Object> region = cache.get().createRegionFactory(RegionShortcut.LOCAL)
        .addCacheListener(cacheListener)
        .create("region");

    region.create("foo", "bar");
  }

  @Test
  public void incorrect_asyncAssertion() {
    CacheListener<Object, Object> cacheListener = spy(CacheListener.class); // or mock
    doAnswer(invocation -> {
      assertThat((EntryEvent) invocation.getArgument(0))
          .as("Argument")
          .isNull();
      return null;
    }).when(cacheListener).afterCreate(any());

    Region<Object, Object> region = cache.get().createRegionFactory(RegionShortcut.LOCAL)
        .addCacheListener(cacheListener)
        .create("region");

    region.create("foo", "bar");
  }

  @Test
  public void correct_asyncAssertion() {
    CacheListener<Object, Object> cacheListener = spy(CacheListener.class); // or mock
    doAnswer(invocation -> {
      errorCollector.checkThat("Argument", invocation.getArgument(0), nullValue());
      return null;
    }).when(cacheListener).afterCreate(any());

    Region<Object, Object> region = cache.get().createRegionFactory(RegionShortcut.LOCAL)
        .addCacheListener(cacheListener)
        .create("region");

    region.create("foo", "bar");
  }
}
