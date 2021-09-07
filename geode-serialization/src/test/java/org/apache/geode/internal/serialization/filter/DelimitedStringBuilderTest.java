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
package org.apache.geode.internal.serialization.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class DelimitedStringBuilderTest {

  @Test
  public void appendsOneString() {
    DelimitedStringBuilder delimitedStringBuilder = new DelimitedStringBuilder();

    String string = delimitedStringBuilder
        .append("foo")
        .toString();

    assertThat(string)
        .isEqualTo("foo");
  }

  @Test
  public void usesSemicolonDelimiterByDefault() {
    DelimitedStringBuilder delimitedStringBuilder = new DelimitedStringBuilder();

    String string = delimitedStringBuilder
        .append("foo")
        .append("bar")
        .toString();

    assertThat(string)
        .contains(";");
  }

  @Test
  public void appendsTwoStrings() {
    DelimitedStringBuilder delimitedStringBuilder = new DelimitedStringBuilder();

    String string = delimitedStringBuilder
        .append("foo")
        .append("bar")
        .toString();

    assertThat(string)
        .contains("foo")
        .contains("bar")
        .isEqualTo("foo;bar");
  }

  @Test
  public void appendsManyStrings() {
    DelimitedStringBuilder delimitedStringBuilder = new DelimitedStringBuilder();

    String string = delimitedStringBuilder
        .append("foo")
        .append("bar")
        .append("the")
        .append("test")
        .toString();

    assertThat(string)
        .contains("foo")
        .contains("bar")
        .contains("the")
        .contains("test")
        .isEqualTo("foo;bar;the;test");
  }

  @Test
  public void usesSpecifiedDelimiter() {
    DelimitedStringBuilder delimitedStringBuilder = new DelimitedStringBuilder(',');

    String string = delimitedStringBuilder
        .append("foo")
        .append("bar")
        .toString();

    assertThat(string)
        .isEqualTo("foo,bar");
  }

  @Test
  public void appendsManyStringsWithSpecifiedDelimiter() {
    DelimitedStringBuilder delimitedStringBuilder = new DelimitedStringBuilder(' ');

    String string = delimitedStringBuilder
        .append("go")
        .append("ahead")
        .append("make")
        .append("my")
        .append("day")
        .toString();

    assertThat(string)
        .isEqualTo("go ahead make my day");
  }

  @Test
  public void usesSpecifiedStringBuilder() {
    DelimitedStringBuilder delimitedStringBuilder =
        new DelimitedStringBuilder(new StringBuilder("the"), ',', true);

    String string = delimitedStringBuilder
        .append("foo")
        .append("bar")
        .toString();

    assertThat(string)
        .isEqualTo("the,foo,bar");
  }

  @Test
  public void usesSpecifiedBooleanStartedFalse() {
    DelimitedStringBuilder delimitedStringBuilder =
        new DelimitedStringBuilder(new StringBuilder(), ',', false);

    String string = delimitedStringBuilder
        .append("foo")
        .append("bar")
        .toString();

    assertThat(string)
        .isEqualTo("foo,bar");
  }

  @Test
  public void usesSpecifiedBooleanStartedTrue() {
    DelimitedStringBuilder delimitedStringBuilder =
        new DelimitedStringBuilder(new StringBuilder(), ',', true);

    String string = delimitedStringBuilder
        .append("foo")
        .append("bar")
        .toString();

    assertThat(string)
        .isEqualTo(",foo,bar");
  }
}
