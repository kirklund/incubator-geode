/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geode.examples;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.geode.test.awaitility.GeodeAwaitility.getTimeout;
import static org.apache.geode.test.dunit.VM.getController;
import static org.apache.geode.test.dunit.VM.getVM;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.geode.test.dunit.AsyncInvocation;
import org.apache.geode.test.dunit.VM;
import org.apache.geode.test.dunit.rules.DistributedRule;

public class AsyncInvocationDistributedTest {

  private static final AtomicReference<CountDownLatch> LATCH = new AtomicReference<>();

  @Rule
  public DistributedRule distributedRule = new DistributedRule();

  @Before
  public void setUp() {
    for (VM vm : asList(getController(), getVM(0))) {
      vm.invoke(() -> LATCH.set(new CountDownLatch(1)));
    }
  }

  @Test
  public void example() throws Exception {
    AsyncInvocation<Void> waitForLatchInVM0 = getVM(0).invokeAsync(() -> Thread.sleep(1_000));

    waitForLatchInVM0.await();
  }

  @Test
  public void await() throws Exception {
    AsyncInvocation<Void> waitForLatchInVM0 = getVM(0).invokeAsync(() -> doVoid());

    getVM(0).invoke(() -> LATCH.get().countDown());

    waitForLatchInVM0.await();
  }

  @Test
  public void get() throws Exception {
    AsyncInvocation<Boolean> waitForLatchInVM0 = getVM(0).invokeAsync(() -> doBoolean());

    getVM(0).invoke(() -> LATCH.get().countDown());

    boolean value = waitForLatchInVM0.get();
    assertThat(value).isTrue();
  }

  @Test
  public void await_withTimeout() throws Exception {
    AsyncInvocation<Void> waitForLatchInVM0 = getVM(0).invokeAsync(() -> doVoid());

    waitForLatchInVM0.await(1_000, MILLISECONDS);
  }

  @Test
  public void get_withTimeout() throws Exception {
    AsyncInvocation<Boolean> waitForLatchInVM0 = getVM(0).invokeAsync(() -> doBoolean());

    boolean value = waitForLatchInVM0.get(1_000, MILLISECONDS);
    assertThat(value).isTrue();
  }

  private static void doVoid() throws InterruptedException {
    LATCH.get().await();
  }

  private static boolean doBoolean() throws InterruptedException {
    return LATCH.get().await(getTimeout().getValueInMS(), MILLISECONDS);
  }
}
