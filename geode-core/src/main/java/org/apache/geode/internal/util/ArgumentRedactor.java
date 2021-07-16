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
package org.apache.geode.internal.util;

import java.util.Collection;
import java.util.List;

import org.apache.geode.internal.util.redaction.ArgumentValueRedaction;

public class ArgumentRedactor {

  private static final ArgumentValueRedaction DELEGATE = new ArgumentValueRedaction();

  private ArgumentRedactor() {
    // do not instantiate
  }

  /**
   * Parse a string to find option/argument pairs and redact the arguments if necessary.
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
   *
   * @param line The argument input to be parsed
   *
   * @return A redacted string that has sensitive information obscured.
   */
  public static String redact(String line) {
    return DELEGATE.redact(line);
  }

  public static String redact(Iterable<String> lines) {
    return DELEGATE.redact(lines);
  }

  /**
   * Return the redaction string if the provided option's argument should be redacted.
   * Otherwise, return the provided argument unchanged.
   *
   * @param option A string such as a system property, jvm parameter or command-line option.
   * @param argument A string that is the argument assigned to the option.
   *
   * @return A redacted string if the option indicates it should be redacted, otherwise the
   *         provided argument.
   */
  public static String redactArgumentIfNecessary(String option, String argument) {
    return DELEGATE.redactArgumentIfNecessary(option, argument);
  }

  public static List<String> redactEachInList(Collection<String> lines) {
    return DELEGATE.redactEachInList(lines);
  }

  /**
   * Determine whether an option's argument value should be redacted.
   *
   * @param option The option in question.
   *
   * @return true if the argument's value should be redacted, otherwise false.
   */
  public static boolean isSensitive(String option) {
    return DELEGATE.isSensitive(option);
  }

  public static String getRedacted() {
    return DELEGATE.getRedacted();
  }
}
