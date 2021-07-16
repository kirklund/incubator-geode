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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Function;

/**
 * TODO:KIRK: delete StringRedactionStrategy
 */
class StringRedactionStrategy implements RedactionStrategy {

  private final Function<String, Boolean> isSensitive;
  private final String redacted;

  StringRedactionStrategy(Function<String, Boolean> isSensitive, String redacted) {
    this.isSensitive = isSensitive;
    this.redacted = redacted;
  }

  @Override
  public String redact(String line) {
    // boolean wasPaddedWithHyphen = false;
    // if (!line.trim().startsWith("-")) {
    // line = "-" + line.trim();
    // wasPaddedWithHyphen = true;
    // }

    System.out.println("line = " + line);

    // TODO: use String and StringTokenizer/StreamTokenizer APIs
    List<String> tokens = new ArrayList<>();
    StringTokenizer tokenizer = new StringTokenizer(line, " -", true);
    while (tokenizer.hasMoreElements()) {
      tokens.add("-" + tokenizer.nextToken());
    }

    for (String token : tokens) {
      System.out.println("token = " + token);
    }

    // if (wasPaddedWithHyphen) {
    // line = line.substring(1);
    // }
    return line;
  }

  static class Item {

    private final String option;
    private final String argument;

    Item(String option, String argument) {
      this.option = option;
      this.argument = argument;
    }


    String getOption() {
      return option;
    }

    String getArgument() {
      return argument;
    }
  }
}
