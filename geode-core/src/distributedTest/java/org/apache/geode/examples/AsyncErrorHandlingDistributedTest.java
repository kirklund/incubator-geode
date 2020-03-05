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

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Rule;
import org.junit.Test;

import org.apache.geode.cache.CacheListener;
import org.apache.geode.cache.EntryEvent;
import org.apache.geode.cache.util.CacheListenerAdapter;
import org.apache.geode.test.dunit.rules.DistributedExecutorServiceRule;
import org.apache.geode.test.dunit.rules.SharedErrorCollector;

public class AsyncErrorHandlingDistributedTest {

  private final AtomicBoolean THROW = new AtomicBoolean(true);

  @Rule
  public SharedErrorCollector errorCollector = new SharedErrorCollector();

  @Rule
  public DistributedExecutorServiceRule executorServiceRule = new DistributedExecutorServiceRule();

  @Test
  public void incorrect() throws Exception {
    // add slow receiver CacheListener (from SlowRecDUnitTest)
    CacheListener cacheListener = new CacheListenerAdapter() {
      @Override
      public void afterUpdate(EntryEvent event) {
        // make the slow receiver event slower!
        try {
          doSleep(500);
        } catch (InterruptedException e) {
          fail("interrupted");
        }
      }
    };

    Thread thread = new Thread(() -> {
      Thread.currentThread().interrupt();
      cacheListener.afterUpdate(mock(EntryEvent.class));
    });
    thread.start();
    thread.join();
  }

  @Test
  public void correct() throws Exception {
    // add slow receiver CacheListener (from SlowRecDUnitTest)
    CacheListener cacheListener = new CacheListenerAdapter() {
      @Override
      public void afterUpdate(EntryEvent event) {
        // make the slow receiver event slower!
        try {
          doSleep(500);
        } catch (InterruptedException e) {
          errorCollector.addError(e);
        }
      }
    };

    Thread thread = new Thread(() -> {
      cacheListener.afterUpdate(mock(EntryEvent.class));
    });
    thread.start();
    thread.join();
  }

  @Test
  public void better() throws Exception {
    // add slow receiver CacheListener (from SlowRecDUnitTest)
    CacheListener cacheListener = new CacheListenerAdapter() {
      @Override
      public void afterUpdate(EntryEvent event) {
        // make the slow receiver event slower!
        try {
          doSleep(500);
        } catch (InterruptedException e) {
          errorCollector.addError(e);
        }
      }
    };

    Future<Void> completed = executorServiceRule.submit(() -> {
      cacheListener.afterUpdate(mock(EntryEvent.class));
    });

    completed.get();
  }

  private void doSleep(long millis) throws InterruptedException {
    if (THROW.get()) {
      throw new InterruptedException("Interrupt because THROW is true");
    }
    Thread.sleep(millis);
  }
}
