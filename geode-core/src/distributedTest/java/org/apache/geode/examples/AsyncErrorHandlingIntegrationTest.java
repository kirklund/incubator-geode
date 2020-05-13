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

import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.EntryEvent;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionShortcut;
import org.apache.geode.cache.util.CacheListenerAdapter;

public class AsyncErrorHandlingIntegrationTest {

  private final AtomicReference<Cache> cache = new AtomicReference<>();

  @Rule
  public ErrorCollector errorCollector = new ErrorCollector();

  @Before
  public void setUp() {
    cache.set(new CacheFactory().create());
  }

  @After
  public void tearDown() {
    cache.get().close();
  }

  @Test
  public void incorrect() {
    Region<Object, Object> region = cache.get().createRegionFactory(RegionShortcut.LOCAL)
        .addCacheListener(new CacheListenerAdapter<Object, Object>() {
          @Override
          public void afterCreate(EntryEvent<Object, Object> event) {
            try {
              throw new Exception("Danger!");
            } catch (Exception e) {
              throw new AssertionError(e);
            }
          }
        })
        .create("region");

    region.create("foo", "bar");
  }

  @Test
  public void correct() {
    Region<Object, Object> region = cache.get().createRegionFactory(RegionShortcut.LOCAL)
        .addCacheListener(new CacheListenerAdapter<Object, Object>() {
          @Override
          public void afterCreate(EntryEvent<Object, Object> event) {
            try {
              throw new Exception("Danger!");
            } catch (Exception e) {
              errorCollector.addError(e);
            }
          }
        })
        .create("region");

    region.create("foo", "bar");
  }
}
