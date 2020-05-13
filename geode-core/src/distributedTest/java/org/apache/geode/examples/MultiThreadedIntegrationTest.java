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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.geode.test.awaitility.GeodeAwaitility.getTimeout;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

public class MultiThreadedIntegrationTest {

  private final ExecutorService executor = Executors.newCachedThreadPool();
  private final CountDownLatch latch = new CountDownLatch(1);
  private final AtomicBoolean fail = new AtomicBoolean();

  @Rule
  public ErrorCollector errorCollector = new ErrorCollector();

  @After
  public void tearDown() throws Exception {
    try {
      executor.shutdownNow();
      assertThat(executor.awaitTermination(getTimeout().toMillis(), MILLISECONDS)).isTrue();
    } finally {
      fail.set(false);
      latch.countDown();
    }
  }

  @Test
  public void threads_complete() throws Exception {
    Future<Void> thread1 = executor.submit(() -> await());
    Future<Void> thread2 = executor.submit(() -> await());

    latch.countDown();

    thread1.get(getTimeout().toMillis(), MILLISECONDS);
    thread2.get(getTimeout().toMillis(), MILLISECONDS);
  }

  @Test
  public void threads_hang() throws Exception {
    Future<Void> thread1 = executor.submit(() -> await());
    Future<Void> thread2 = executor.submit(() -> await());

    // latch.countDown();

    thread1.get(2000, MILLISECONDS);
    thread2.get(2000, MILLISECONDS);
  }

  @Test
  public void threads_throw() throws Exception {
    fail.set(true);

    Future<Void> thread1 = executor.submit(() -> dangerous());
    Future<Void> thread2 = executor.submit(() -> dangerous());

    latch.countDown();

    thread1.get(getTimeout().toMillis(), MILLISECONDS);
    thread2.get(getTimeout().toMillis(), MILLISECONDS);
  }

  private Void await() {
    while (latch.getCount() > 0) {
      try {
        latch.await();
      } catch (InterruptedException e) {
        // break;
        throw new AssertionError(e);
        // errorCollector.addError(e);
      }
    }
    return null;
  }

  private Void dangerous() {
    await();
    if (fail.get()) {
      throw new AssertionError("Danger!");
    }
    return null;
  }
}
