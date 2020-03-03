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
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.geode.test.awaitility.GeodeAwaitility.await;
import static org.apache.geode.test.awaitility.GeodeAwaitility.getTimeout;
import static org.apache.geode.test.dunit.VM.getController;
import static org.apache.geode.test.dunit.VM.getVM;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import org.apache.geode.test.dunit.AsyncInvocation;
import org.apache.geode.test.dunit.VM;
import org.apache.geode.test.dunit.rules.DistributedRule;

public class SleepDistributedTest {

  private static final AtomicInteger COUNT = new AtomicInteger();
  private static final AtomicReference<CountDownLatch> LATCH = new AtomicReference<>();

  @Rule
  public DistributedRule distributedRule = new DistributedRule();

  @Before
  public void setUp() {
    for (VM vm : asList(getController(), getVM(0))) {
      vm.invoke(() -> {
        COUNT.set(0);
        LATCH.set(new CountDownLatch(1));
      });
    }
  }

  @After
  public void tearDown() {
    for (VM vm : asList(getController(), getVM(0))) {
      vm.invoke(() -> LATCH.get().countDown());
    }
  }

  @Test
  public void sleep() throws Exception {
    getVM(0).invokeAsync(() -> {
      for (int i = 0; i < 100; i++) {
        COUNT.incrementAndGet();
        Thread.sleep(10);
      }
      assertThat(COUNT.get()).isEqualTo(100);
    });

    // fragile wait
    Thread.sleep(2_000);

    assertThat(getVM(0).invoke(() -> COUNT.get())).isEqualTo(100);

    getVM(0).invoke(() -> {
      assertThat(COUNT.get()).isEqualTo(100);
    });
  }

  @Test
  public void awaitility() {
    getVM(0).invokeAsync(() -> {
      while (COUNT.get() < 100) {
        COUNT.incrementAndGet();
        Thread.sleep(10);
      }
      assertThat(COUNT.get()).isEqualTo(100);
    });

    // awaitility
    getVM(0).invoke(() -> {
      await().until(() -> COUNT.get() == 100);
      assertThat(COUNT.get()).isEqualTo(100);
    });
  }

  @Test
  public void awaitility_untilAsserted() {
    getVM(0).invokeAsync(() -> {
      while (COUNT.get() < 100) {
        COUNT.incrementAndGet();
        Thread.sleep(10);
      }
      assertThat(COUNT.get()).isEqualTo(100);
    });

    // awaitility
    getVM(0).invoke(() -> {
      await().untilAsserted(() -> assertThat(COUNT.get()).isEqualTo(100));
    });
  }

  @Test
  public void asyncInvocation() throws Exception {
    AsyncInvocation<Void> waitForLatchInVM0 = getVM(0).invokeAsync(() -> {
      while (COUNT.get() < 100) {
        COUNT.incrementAndGet();
        Thread.sleep(10);
      }
      assertThat(COUNT.get()).isEqualTo(100);
    });

    // AsyncInvocation.await()
    waitForLatchInVM0.await();
  }

  @Test
  public void countDownLatch() {
    getVM(0).invokeAsync(() -> {
      while (COUNT.get() < 100) {
        COUNT.incrementAndGet();
        Thread.sleep(10);
      }
      assertThat(COUNT.get()).isEqualTo(100);
      LATCH.get().countDown();
    });

    getVM(0).invoke(() -> {
      boolean completed = LATCH.get().await(getTimeout().getValueInMS(), MILLISECONDS);
      assertThat(completed).isTrue();
    });
  }

  @Test
  @Ignore
  public void gotcha() {
    getVM(0).invokeAsync(() -> {
      LATCH.set(new CountDownLatch(1));
      Thread.sleep(10);
      LATCH.get().countDown();
    });

    getVM(0).invoke(() -> {
      boolean completed = LATCH.get().await(getTimeout().getValueInMS(), MILLISECONDS);
      assertThat(completed).isTrue();
    });
  }

  @Test
  public void gotcha_fix() {
    getVM(0).invoke(() -> {
      LATCH.set(new CountDownLatch(1));
    });

    getVM(0).invokeAsync(() -> {
      Thread.sleep(10);
      LATCH.get().countDown();
    });

    getVM(0).invoke(() -> {
      boolean completed = LATCH.get().await(getTimeout().getValueInMS(), MILLISECONDS);
      assertThat(completed).isTrue();
    });
  }
}
