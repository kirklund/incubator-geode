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
package org.apache.geode.internal.util.redaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ArgumentValueRedactionTest {

  private static final String REDACTED = "redacted";

  private SensitiveDataDictionary sensitiveDataDictionary;
  private RedactionStrategy redactionStrategy;

  private ArgumentValueRedaction argumentValueRedaction;

  @Before
  public void setUp() {
    sensitiveDataDictionary = mock(SensitiveDataDictionary.class);
    redactionStrategy = mock(RedactionStrategy.class);

    argumentValueRedaction =
        new ArgumentValueRedaction(REDACTED, sensitiveDataDictionary, redactionStrategy);
  }

  @Test
  public void redact_delegatesString() {
    String input = "line";
    String expected = "expected";

    when(redactionStrategy.redact(input)).thenReturn(expected);

    String result = argumentValueRedaction.redact(input);

    verify(redactionStrategy).redact(input);
    assertThat(result).isEqualTo(expected);
  }

  @Test
  public void redact_delegatesNullString() {
    String input = null;

    argumentValueRedaction.redact(input);

    verify(redactionStrategy).redact(input);
  }

  @Test
  public void redact_delegatesEmptyString() {
    String input = "";

    argumentValueRedaction.redact(input);

    verify(redactionStrategy).redact(input);
  }

  @Test
  public void redact_delegatesIterable() {
    String line1 = "line1";
    String line2 = "line2";
    String line3 = "line3";
    Collection<String> input = new ArrayList<>();
    input.add(line1);
    input.add(line2);
    input.add(line3);
    String joinedLine = String.join(" ", input);
    String expected = "expected";

    when(redactionStrategy.redact(joinedLine)).thenReturn(expected);

    String result = argumentValueRedaction.redact(input);

    verify(redactionStrategy).redact(joinedLine);
    assertThat(result).isEqualTo(expected);
  }

  @Test
  public void redact_nullIterable_throwsNullPointerException() {
    Collection<String> input = null;

    Throwable thrown = catchThrowable(() -> {
      argumentValueRedaction.redact(input);
    });

    assertThat(thrown).isInstanceOf(NullPointerException.class);
  }

  @Test
  public void redactArgumentIfNecessary_delegatesTabooOptionToTabooDetection() {
    String option = "option";
    String argument = "argument";

    when(sensitiveDataDictionary.isSensitive(option)).thenReturn(true);

    String result = argumentValueRedaction.redactArgumentIfNecessary(option, argument);

    verify(sensitiveDataDictionary).isSensitive(option);
    assertThat(result).isEqualTo(REDACTED);
  }

  @Test
  public void redactArgumentIfNecessary_delegatesNonTabooOptionToTabooDetection() {
    String option = "option";
    String argument = "argument";

    when(sensitiveDataDictionary.isSensitive(option)).thenReturn(false);

    String result = argumentValueRedaction.redactArgumentIfNecessary(option, argument);

    verify(sensitiveDataDictionary).isSensitive(option);
    assertThat(result).isEqualTo(argument);
  }

  @Test
  public void redactArgumentIfNecessary_delegatesNullOptionToTabooDetection() {
    String option = null;

    argumentValueRedaction.redactArgumentIfNecessary(option, "argument");

    verify(sensitiveDataDictionary).isSensitive(option);
  }

  @Test
  public void redactArgumentIfNecessary_delegatesEmptyOptionToTabooDetection() {
    String option = "";

    argumentValueRedaction.redactArgumentIfNecessary(option, "argument");

    verify(sensitiveDataDictionary).isSensitive(option);
  }

  @Test
  public void redactArgumentIfNecessary_returnsNullStringArgument() {
    String argument = null;

    String result = argumentValueRedaction.redactArgumentIfNecessary("option", argument);

    assertThat(result).isEqualTo(argument);
  }

  @Test
  public void redactArgumentIfNecessary_returnsEmptyStringArgument() {
    String argument = "";

    String result = argumentValueRedaction.redactArgumentIfNecessary("option", argument);

    assertThat(result).isEqualTo(argument);
  }

  @Test
  public void redactEachInList_delegatesCollectionOfLines() {
    String line1 = "line1";
    String line2 = "line2";
    String line3 = "line3";
    List<String> input = new ArrayList<>();
    input.add(line1);
    input.add(line2);
    input.add(line3);

    when(redactionStrategy.redact(anyString())).then(returnsFirstArg());

    List<String> result = argumentValueRedaction.redactEachInList(input);

    verify(redactionStrategy).redact(line1);
    verify(redactionStrategy).redact(line2);
    verify(redactionStrategy).redact(line3);
    assertThat(result).isEqualTo(input);
  }

  @Test
  public void redactEachInList_delegatesEmptyCollectionOfLines() {
    List<String> input = Collections.emptyList();

    when(redactionStrategy.redact(anyString())).then(returnsFirstArg());

    List<String> result = argumentValueRedaction.redactEachInList(input);

    verifyNoInteractions(redactionStrategy);
    assertThat(result).isEqualTo(input);
  }

  @Test
  public void redactEachInList_delegatesNullCollectionOfLines() {
    List<String> input = null;

    when(redactionStrategy.redact(anyString())).then(returnsFirstArg());

    Throwable thrown = catchThrowable(() -> {
      argumentValueRedaction.redactEachInList(input);
    });

    assertThat(thrown).isInstanceOf(NullPointerException.class);
  }

  @Test
  public void isSensitive_delegatesToTabooDetection() {
    String input = "input";

    when(sensitiveDataDictionary.isSensitive(anyString())).thenReturn(true);

    boolean result = argumentValueRedaction.isSensitive(input);

    assertThat(result).isTrue();
  }

  @Test
  public void isSensitive_delegatesNullStringToTabooDetection() {
    String input = null;

    when(sensitiveDataDictionary.isSensitive(isNull())).thenReturn(true);

    boolean result = argumentValueRedaction.isSensitive(input);

    assertThat(result).isTrue();
  }

  @Test
  public void isSensitive_delegatesEmptyStringToTabooDetection() {
    String input = "";

    when(sensitiveDataDictionary.isSensitive(anyString())).thenReturn(true);

    boolean result = argumentValueRedaction.isSensitive(input);

    assertThat(result).isTrue();
  }
}
