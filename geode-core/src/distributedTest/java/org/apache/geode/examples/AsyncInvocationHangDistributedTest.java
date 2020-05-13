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
import static org.apache.geode.test.dunit.VM.getVM;
import static org.apache.geode.test.dunit.VM.getVMId;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.geode.test.dunit.AsyncInvocation;
import org.apache.geode.test.dunit.rules.DistributedRule;

public class AsyncInvocationHangDistributedTest implements Serializable {

  private static final AtomicReference<CountDownLatch> BEFORE = new AtomicReference<>();
  private static final AtomicReference<CountDownLatch> AFTER = new AtomicReference<>();
  private static final Object LOCK_1 = new Object();
  private static final Object LOCK_2 = new Object();

  @Rule
  public DistributedRule distributedRule = new DistributedRule();

  @Before
  public void setUp() {
    getVM(0).invoke(() -> {
      BEFORE.set(new CountDownLatch(1));
      AFTER.set(new CountDownLatch(1));
    });
  }

  @After
  public void tearDown() {
    getVM(0).invoke(() -> {
      BEFORE.get().countDown();
      AFTER.get().countDown();
    });
  }

  @Test
  public void deadlock() throws Exception {
    AsyncInvocation<Void> thread1 = getVM(0).invokeAsync(() -> {
      synchronized (LOCK_1) {
        BEFORE.get().countDown();
        AFTER.get().await();
        synchronized (LOCK_2) {
          println("got all the locks!");
        }
      }
    });

    AsyncInvocation<Void> thread2 = getVM(0).invokeAsync(() -> {
      BEFORE.get().await();
      synchronized (LOCK_2) {
        AFTER.get().countDown();
        synchronized (LOCK_1) {
          println("got all the locks!");
        }
      }
    });

    thread1.await(2, SECONDS); // GeodeAwaitility.getTimeout().getValueInMS()
    thread2.await(2, SECONDS); // GeodeAwaitility.getTimeout().getValueInMS()
  }

  private void println(String message) {
    int vmId = getVMId();
    long threadId = Thread.currentThread().getId();
    System.out.println("vm-" + vmId + " thread-" + threadId + " " + message);
  }
}
