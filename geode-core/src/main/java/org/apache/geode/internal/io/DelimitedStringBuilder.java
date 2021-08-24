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
package org.apache.geode.internal.io;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.geode.annotations.VisibleForTesting;

public class DelimitedStringBuilder {

  private final AtomicBoolean started = new AtomicBoolean();
  private final char delimiter;
  private final StringBuilder stringBuilder;

  public DelimitedStringBuilder() {
    this(';');
  }

  public DelimitedStringBuilder(char delimiter) {
    this(new StringBuilder(), delimiter, false);
  }

  public DelimitedStringBuilder(String initialValue, char delimiter) {
    this(new StringBuilder(initialValue), delimiter, true);
  }

  @VisibleForTesting
  DelimitedStringBuilder(StringBuilder stringBuilder, char delimiter, boolean started) {
    this.stringBuilder = stringBuilder;
    this.delimiter = delimiter;
    this.started.set(started);
  }

  public DelimitedStringBuilder append(String string) {
    if (isBlank(string)) {
      return this;
    }
    if (started.get()) {
      if (!string.endsWith(";")) {
        stringBuilder.append(delimiter);
      }
    } else {
      started.set(true);
    }
    stringBuilder.append(string);
    return this;
  }

  @Override
  public String toString() {
    return stringBuilder.toString();
  }
}
