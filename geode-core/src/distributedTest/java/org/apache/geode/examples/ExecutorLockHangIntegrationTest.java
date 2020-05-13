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

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.geode.test.junit.rules.ExecutorServiceRule;

public class ExecutorLockHangIntegrationTest {

  private static final AtomicReference<CountDownLatch> READY = new AtomicReference<>();
  private static final AtomicReference<CountDownLatch> GO = new AtomicReference<>();
  private final Lock lock1 = new ReentrantLock();
  private final Lock lock2 = new ReentrantLock();

  @Rule
  public ExecutorServiceRule executorServiceRule = new ExecutorServiceRule();

  @Before
  public void setUp() {
    READY.set(new CountDownLatch(1));
    GO.set(new CountDownLatch(1));
  }

  @After
  public void tearDown() {
    System.out.println();
    System.out.println(executorServiceRule.dumpThreads());
    READY.get().countDown();
  }

  @Test
  public void deadlock() throws Exception {
    Future<Void> future1 = executorServiceRule.submit(() -> {
      println("running");

      println("locking lock1");
      lock1.lockInterruptibly();
      println("locked lock1");

      READY.get().countDown();
      GO.get().await();

      println("locking lock2 - hang");
      lock2.lockInterruptibly();
      println("locked lock2");

      println("exiting");
    });

    Future<Void> future2 = executorServiceRule.submit(() -> {
      println("running");

      READY.get().await();

      println("locking lock2");
      lock2.lockInterruptibly();
      println("locked lock2");

      GO.get().countDown();

      println("locking lock1 - hang");
      lock1.lockInterruptibly();
      println("locked lock1");

      println("exiting");
    });

    future1.get(2, SECONDS); // GeodeAwaitility.getTimeout().getValueInMS()
    future1.get(2, SECONDS); // GeodeAwaitility.getTimeout().getValueInMS()
    future2.get(2, SECONDS); // GeodeAwaitility.getTimeout().getValueInMS()
  }

  private void println(String message) {
    System.out.println(Thread.currentThread().getName() + " " + message);
  }
}
