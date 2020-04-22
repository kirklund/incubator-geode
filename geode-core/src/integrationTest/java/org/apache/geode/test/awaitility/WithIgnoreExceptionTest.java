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
package org.apache.geode.test.awaitility;

import static org.apache.geode.test.awaitility.GeodeAwaitility.await;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

/**
 * Examples of using Awaitility with ignoreException to consume various throwable types.
 */
public class WithIgnoreExceptionTest {

  private final AtomicInteger count = new AtomicInteger();

  @Test
  public void assertionError() {
    await()
        .untilAsserted(() -> throwUntilCountEquals(new AssertionError(), 10));

    assertThat(count.get())
        .isGreaterThanOrEqualTo(10);
  }

  @Test
  public void ignoreRuntimeException() {
    await()
        .ignoreException(RuntimeException.class)
        .untilAsserted(() -> throwUntilCountEquals(new RuntimeException(), 10));

    assertThat(count.get())
        .isGreaterThanOrEqualTo(10);
  }

  @Test
  public void ignoreCheckedException() {
    await()
        .ignoreException(Exception.class)
        .untilAsserted(() -> throwUntilCountEquals(new Exception(), 10));

    assertThat(count.get())
        .isGreaterThanOrEqualTo(10);
  }

  @Test
  public void ignoreExceptions() {
    await()
        .ignoreExceptions()
        .untilAsserted(() -> throwUntilCountEquals(new NullPointerException(), 10));

    assertThat(count.get())
        .isGreaterThanOrEqualTo(10);
  }

  /**
   * Increments the count and then throws provided throwable when threshold is reached.
   */
  private void throwUntilCountEquals(Throwable throwable, int threshold) throws Throwable {
    int value = count.incrementAndGet();
    if (value < threshold) {
      throw throwable;
    }
  }
}
