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
import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.concurrent.Future;

import org.junit.Rule;
import org.junit.Test;

import org.apache.geode.test.dunit.AsyncInvocation;
import org.apache.geode.test.dunit.rules.DistributedRule;

public class ExampleDistributedTest implements Serializable {

  @Rule
  public DistributedRule distributedRule = new DistributedRule(1);

  /**
   * Awaits completion of AsyncInvocation with Void type.
   */
  @Test
  public void awaitCompletionOfAsyncInvocation() throws Exception {
    AsyncInvocation<Void> asyncActionInVM0 = getVM(0).invokeAsync(() -> doAsyncAction());

    // do other actions in parallel

    asyncActionInVM0.await();
  }

  /**
   * Awaits completion of AsyncInvocation and then returns its result.
   */
  @Test
  public void getResultOfAsyncInvocation() throws Exception {
    AsyncInvocation<Serializable> asyncActionInVM0 =
        getVM(0).invokeAsync(() -> doAsyncActionWithReturnValue());

    // do other actions in parallel

    Serializable result = asyncActionInVM0.get();
    assertThat(result).isNotNull();
  }

  /**
   * Awaits completion of AsyncInvocation as a Future with Void type.
   */
  @Test
  public void awaitCompletionOfFuture() throws Exception {
    Future<Void> asyncActionInVM0 = getVM(0).invokeAsync(() -> doAsyncAction());

    // do other actions in parallel

    asyncActionInVM0.get();
  }

  /**
   * Awaits completion of AsyncInvocation as a Future and then returns its result.
   */
  @Test
  public void getResultOfFuture() throws Exception {
    Future<Serializable> asyncActionInVM0 =
        getVM(0).invokeAsync(() -> doAsyncActionWithReturnValue());

    // do other actions in parallel

    Serializable result = asyncActionInVM0.get();
    assertThat(result).isNotNull();
  }

  private void doAsyncAction() {
    // do some work that will need to execute asynchronously
  }

  private Serializable doAsyncActionWithReturnValue() {
    // do some work that will need to execute asynchronously
    return "Result";
  }
}
