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
package org.apache.geode.management.internal.beans;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.geode.cache.RegionShortcut.LOCAL_PERSISTENT;
import static org.apache.geode.distributed.ConfigurationProperties.LOCATORS;
import static org.apache.geode.test.awaitility.GeodeAwaitility.await;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.base.Stopwatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.geode.CancelException;
import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.RegionExistsException;
import org.apache.geode.distributed.internal.InternalDistributedSystem;
import org.apache.geode.test.junit.rules.DiskDirRule;
import org.apache.geode.test.junit.rules.ExecutorServiceRule;
import org.apache.geode.test.junit.rules.serializable.SerializableTestName;

/**
 * Reproduce GEODE-6232: deadlock between destroy region and create region with default disk store.
 *
 * <p>
 * Test loops until the hang is detected.
 *
 * <p>
 * The InternalDistributedSystem shutdown hook is also removed to avoid a 3-way deadlock that will
 * prevent the JVM from exiting. Invoking Cache.close() in tearDown would also add another thread to
 * the deadlock.
 *
 * <p>
 * TODO: After the deadlock is fixed: 1) change the test to confirm that it does not hang without
 * looping, 2) delete the use of Stopwatch, 3) update the javadocs.
 */
public class ManagementListenerDeadlockRegressionTest {

  private static final long TRY_LOCK_TIMEOUT_MILLIS = 10 * 1000;
  private static final long DETECTION_TIMEOUT_MILLIS = 60 * 1000;
  private static final AtomicBoolean HANG_NOT_DETECTED = new AtomicBoolean();

  private static volatile Cache cache;

  private final ReadWriteLock detectHang = new ReentrantReadWriteLock();

  private String regionName;

  @Rule
  public ExecutorServiceRule executorServiceRule = new ExecutorServiceRule();

  @Rule
  public SerializableTestName testName = new SerializableTestName();

  @Rule
  public DiskDirRule diskDirRule = new DiskDirRule();

  @Before
  public void setUp() {
    String uniqueName = getClass().getSimpleName() + "-" + testName.getMethodName();
    regionName = uniqueName + "-region";
    HANG_NOT_DETECTED.set(true);
  }

  @After
  public void tearDown() {
    disableShutdownHook();
    HANG_NOT_DETECTED.set(false);
    executorServiceRule.getExecutorService().shutdownNow();
  }

  @Test
  public void hang() throws Exception {
    // thread-1 is creating a persistent region which will create the default disk store
    executorServiceRule.execute(() -> createRegionWithDefaultDiskStore());

    // thread-2 is closing the cache
    executorServiceRule.execute(() -> closeCache());

    await().untilAsserted(() -> assertThat(executorServiceRule.getThreads()).hasSize(2));

    checkForHang();
  }

  private void createRegionWithDefaultDiskStore() {
    while (HANG_NOT_DETECTED.get()) {
      detectHang.readLock().lock();
      try {
        cache = new CacheFactory().set(LOCATORS, "").create();
        cache.createRegionFactory(LOCAL_PERSISTENT).create(regionName);
      } catch (CancelException | RegionExistsException ignored) {
        // ignore and loop back around to try again
      } finally {
        detectHang.readLock().unlock();
      }
      pause();
    }
  }

  private void closeCache() {
    while (HANG_NOT_DETECTED.get()) {
      Cache cacheToClose = cache;
      if (cacheToClose != null) {
        cache.close();
      }
      pause();
    }
  }

  private void checkForHang() throws InterruptedException {
    Stopwatch stopwatch = Stopwatch.createStarted();
    while (HANG_NOT_DETECTED.get()) {
      boolean locked = detectHang.writeLock().tryLock(TRY_LOCK_TIMEOUT_MILLIS, MILLISECONDS);
      try {
        assertThat(locked)
            .withFailMessage("GEODE-6232: Probable hang detected: " + System.lineSeparator()
                + executorServiceRule.dumpThreads())
            .isTrue();
      } finally {
        if (locked) {
          detectHang.writeLock().unlock();
        }
      }
      if (stopwatch.elapsed(MILLISECONDS) > DETECTION_TIMEOUT_MILLIS) {
        return;
      }
      pause();
    }
  }

  private void pause() {
    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      disableShutdownHook();
      HANG_NOT_DETECTED.set(false);
      Thread.currentThread().interrupt();
    }
  }

  private void disableShutdownHook() {
    // removing shutdownHook to avoid a 3-way deadlock that prevents JVM shutdown
    Runtime.getRuntime().removeShutdownHook(InternalDistributedSystem.shutdownHook);
  }
}
