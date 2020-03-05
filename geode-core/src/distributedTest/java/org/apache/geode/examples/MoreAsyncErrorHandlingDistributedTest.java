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

import static org.apache.geode.test.dunit.VM.getVM;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Rule;
import org.junit.Test;

import org.apache.geode.test.dunit.rules.DistributedExecutorServiceRule;

@SuppressWarnings("serial")
public class MoreAsyncErrorHandlingDistributedTest implements Serializable {

  private final AtomicReference<CountDownLatch> LATCH = new AtomicReference<>();
  private final AtomicBoolean THROW = new AtomicBoolean();

  @Rule
  public DistributedExecutorServiceRule executorServiceRule = new DistributedExecutorServiceRule();

  @Test
  public void asyncActionInOtherVmThrows() {
    getVM(0).invoke(() -> {
      LATCH.set(new CountDownLatch(1));

      Future<Boolean> result = executorServiceRule.submit(() -> dangerous());

      // do some other stuff in parallel to dangerous()
      // ...

      THROW.set(true);
      LATCH.get().countDown();

      // result.get();
    });
  }

  private boolean dangerous() {
    assertThatCode(() -> LATCH.get().await()).doesNotThrowAnyException();
    if (THROW.get()) {
      throw new RuntimeException("Danger Will Robinson!");
    }
    return true;
  }
}
