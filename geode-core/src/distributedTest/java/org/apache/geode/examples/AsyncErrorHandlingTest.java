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

import static org.apache.geode.test.dunit.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import org.apache.geode.test.junit.rules.ExecutorServiceRule;

public class AsyncErrorHandlingTest {

  private static final AtomicReference<Exception> ERROR = new AtomicReference<>();

  @Rule
  public ErrorCollector errorCollector = new ErrorCollector();
  @Rule
  public ExecutorServiceRule executorServiceRule = new ExecutorServiceRule();

  @Test
  public void incorrect_1() {
    executorServiceRule.submit(() -> {
      throw new Exception("Bad thing happened");
    });
  }

  @Test
  public void incorrect_2() {
    executorServiceRule.submit(() -> {
      try {
        throw new Exception("Bad thing happened");
      } catch (Exception e) {
        fail(e.getMessage(), e);
      }
    });
  }

  @Test
  public void incorrect_3() {
    executorServiceRule.submit(() -> {
      try {
        throw new Exception("Bad thing happened");
      } catch (Exception e) {
        throw new AssertionError(e);
      }
    });
  }

  @Test
  public void incorrect_4() {
    executorServiceRule.submit(() -> {
      try {
        throw new Exception("Bad thing happened");
      } catch (Exception e) {
        errorCollector.addError(e);
      }
    });
  }

  @Test
  public void anti_pattern() throws Exception {
    Future<Void> future = executorServiceRule.submit(() -> {
      try {
        throw new Exception("Bad thing happened");
      } catch (Exception e) {
        ERROR.set(e); // set some state to assert later
      }
    });

    future.get();

    assertThat(ERROR.get()).isNull();
  }

  @Test
  public void correct() throws Exception {
    Future<Void> future = executorServiceRule.submit(() -> {
      try {
        throw new Exception("Bad thing happened");
      } catch (Exception e) {
        errorCollector.addError(e); // defer
      }
    });

    future.get(); // does NOT throw
  }

  @Test
  public void better() throws Exception {
    Future<Void> future = executorServiceRule.submit(() -> {
      throw new Exception("Bad thing happened");
    });

    future.get(); // throws
  }
}
