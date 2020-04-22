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
import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.geode.internal.inet.LocalHostUtil;
import org.apache.geode.test.junit.rules.ExecutorServiceRule;

/**
 * Examples of using Awaitility and AssertJ with custom alias and failure messages.
 */
public class WithFailMessageTest {

  private static final InetAddress LOCAL_HOST = getLocalHost();

  private final AtomicBoolean running = new AtomicBoolean(true);
  private final View view = new View();
  private final Member member = new Member("member", LOCAL_HOST);

  private Future<Void> viewUpdater;

  @Rule
  public ExecutorServiceRule executorServiceRule = new ExecutorServiceRule();

  /**
   * Sets up a background thread adding a new Member to the View every second.
   */
  @Before
  public void setUp() {
    viewUpdater = executorServiceRule.submit(() -> {
      while (running.get()) {
        view.update();
        Thread.sleep(Duration.ofSeconds(1).toMillis());
      }
    });
  }

  @After
  public void tearDown() throws Exception {
    running.set(false);
    viewUpdater.get(30, SECONDS);
  }

  /** Shows default failure message */
  @Test
  public void defaultFailure() {
    await()
        .atMost(5, SECONDS)
        .untilAsserted(() -> assertThat(view.hasMember(member)).isTrue());
  }

  /** Shows failure message when using Awaitility alias */
  @Test
  public void awaitAlias() {
    await()
        .alias("await until " + view + " has member " + member)
        .atMost(5, SECONDS)
        .untilAsserted(() -> assertThat(view.hasMember(member)).isTrue());
  }

  /** Shows failure message when using AssertJ with alias (as) the assertion */
  @Test
  public void assertionAs() {
    await()
        .atMost(5, SECONDS)
        .untilAsserted(() -> {
          assertThat(view.hasMember(member))
              .as(view + " has member " + member)
              .isTrue();
        });
  }

  /** Shows failure message when using AssertJ withFailMessage */
  @Test
  public void assertionWithFailMessage() {
    await()
        .atMost(5, SECONDS)
        .untilAsserted(() -> {
          assertThat(view.hasMember(member))
              // .as(view + " has member " + member)
              .withFailMessage(view + " is missing member " + member)
              .isTrue();
        });
  }

  /** Shows failure message when using AssertJ on a Collection */
  @Test
  public void assertionOnCollection() {
    await()
        .atMost(5, SECONDS)
        .untilAsserted(() -> {
          assertThat(view.members())
              .withFailMessage(view.members() + " is missing member " + member)
              .contains(member);
        });
  }

  private static InetAddress getLocalHost() {
    try {
      return LocalHostUtil.getLocalHost();
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }

  private static class View {

    private final AtomicInteger memberId = new AtomicInteger();
    private final List<Member> members = new CopyOnWriteArrayList<>();

    private List<Member> members() {
      return Collections.unmodifiableList(members);
    }

    private boolean hasMember(Member member) {
      return members.contains(member);
    }

    private void update() {
      members.add(new Member("member" + memberId.incrementAndGet(), LOCAL_HOST));
    }

    @Override
    public String toString() {
      return "View{members=" + members + '}';
    }
  }

  private static class Member {

    private final String name;
    private final InetAddress inetAddress;

    private Member(String name, InetAddress inetAddress) {
      this.name = name;
      this.inetAddress = inetAddress;
    }

    @Override
    public String toString() {
      return "Member{name='" + name + '\'' + ", inetAddress=" + inetAddress + '}';
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Member member = (Member) o;
      return name.equals(member.name) &&
          inetAddress.equals(member.inetAddress);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, inetAddress);
    }
  }
}
