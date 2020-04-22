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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.geode.test.awaitility.GeodeAwaitility.await;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

/**
 * Examples of using Awaitility with atLeast and atMost.
 */
public class WithAtLeastAtMostTest {

  /**
   * Using atLeast and atMost appears to work for expiration but has fragility problems:
   *
   * <pre>
   * * Loss of CPU before awaiting causes failures
   * * Longer running code within until causes failures
   * </pre>
   */
  @Test
  public void expirationOccursWithinRange() {
    Lease lease = new Lease();

    // schedule lease for expiration
    TimerTask timerTask = new TimerTask() {
      @Override
      public void run() {
        lease.expire();
      }
    };

    new Timer().schedule(timerTask, Duration.ofSeconds(10).toMillis());

    // sleep(20, SECONDS); // imitates loss of CPU before await

    await()
        .atLeast(5, SECONDS)
        .atMost(15, SECONDS)
        .until(() -> lease.isExpired());
  }

  private static class Lease {

    private final AtomicBoolean active = new AtomicBoolean();
    private final AtomicReference<TimeStamp> expiration = new AtomicReference<>();

    private Lease() {
      active.set(true);
    }

    private void expire() {
      if (active.compareAndSet(true, false)) {
        expiration.set(new TimeStamp());
      }
    }

    private boolean isExpired() {
      // sleep(20, SECONDS); // imitates
      return !active.get();
    }
  }

  private static void sleep(long timeout, TimeUnit unit) {
    try {
      Thread.sleep(unit.toMillis(timeout));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new Error("Sleep canceled by interrupt", e);
    }
  }

  private static class TimeStamp {

    private final long millis;

    private TimeStamp() {
      this(System.currentTimeMillis());
    }

    private TimeStamp(long millis) {
      this.millis = millis;
    }
  }
}
