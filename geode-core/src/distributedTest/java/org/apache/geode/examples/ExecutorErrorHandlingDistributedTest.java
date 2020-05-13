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

import java.io.Serializable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Rule;
import org.junit.Test;

import org.apache.geode.test.dunit.rules.DistributedExecutorServiceRule;
import org.apache.geode.test.dunit.rules.DistributedRule;

public class ExecutorErrorHandlingDistributedTest implements Serializable {

  private final AtomicBoolean THROW = new AtomicBoolean();

  @Rule
  public DistributedRule distributedRule = new DistributedRule();
  @Rule
  public DistributedExecutorServiceRule executorServiceRule = new DistributedExecutorServiceRule();

  @Test
  public void asyncActionInOtherVmThrows() {
    getVM(0).invoke(() -> {
      THROW.set(true);

      Future<Boolean> result = executorServiceRule.submit(() -> dangerous());

      // ...

      result.get(); // DON'T FORGET TO WAIT ON EVERY FUTURE
    });
  }

  private boolean dangerous() {
    if (THROW.get()) {
      throw new RuntimeException("Danger Will Robinson!");
    }
    return true;
  }
}
