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

import java.util.function.Function;
import java.util.regex.Matcher;

class RegexRedactionStrategy implements RedactionStrategy {

  private static final boolean PERMIT_FIRST_PAIR_WITHOUT_HYPHEN = true;

  private final Function<String, Boolean> isSensitive;
  private final String redacted;

  RegexRedactionStrategy(Function<String, Boolean> isSensitive, String redacted) {
    this.isSensitive = isSensitive;
    this.redacted = redacted;
  }

  /**
   * Parse a string to find option/argument pairs and redact the arguments if necessary.
   *
   * <p>
   * Prepends the line with a "-", which is later removed. This allows the use on lines such as
   * "password=secret" rather than just "--password=secret"
   *
   * <p>
   * The following format is expected:<br>
   * - Each option/argument pair should be separated by spaces.<br>
   * - The option of each pair must be preceded by at least one hyphen '-'.<br>
   * - Arguments may or may not be wrapped in quotation marks.<br>
   * - Options and arguments may be separated by an equals sign '=' or any number of spaces.<br>
   *
   * <p>
   * Examples:
   * <ol>
   * <li>"--password=secret"</li>
   * <li>"--user me --password secret"</li>
   * <li>"-Dflag -Dopt=arg"</li>
   * <li>"--classpath=."</li>
   * <li>"password=secret"</li>
   * </ol>
   */
  @Override
  public String redact(String line) {
    // boolean wasPaddedWithHyphen = false;
    // if (!line.trim().startsWith("-") && PERMIT_FIRST_PAIR_WITHOUT_HYPHEN) {
    // line = "-" + line.trim();
    // wasPaddedWithHyphen = true;
    // }

    Matcher matcher = SensitiveDataRegex.getPattern().matcher(line);
    while (matcher.find()) {
      String option = matcher.group(2);
      if (!isSensitive.apply(option)) {
        continue;
      }

      String leadingBoundary = matcher.group(1);
      String separator = matcher.group(3);
      String withRedaction = leadingBoundary + option + separator + redacted;
      line = line.replace(matcher.group(), withRedaction);
    }

    // if (wasPaddedWithHyphen) {
    // line = line.substring(1);
    // }
    return line;
  }
}
