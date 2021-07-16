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

import static java.util.stream.Collectors.toList;
import static org.apache.geode.internal.util.redaction.RedactionDefaults.REDACTED;
import static org.apache.geode.internal.util.redaction.RedactionDefaults.SENSITIVE_PREFIXES;
import static org.apache.geode.internal.util.redaction.RedactionDefaults.SENSITIVE_SUBSTRINGS;

import java.util.Collection;
import java.util.List;

import org.apache.geode.annotations.VisibleForTesting;

/**
 * Redacts argument values for options that are detected as sensitive data.
 */
public class ArgumentValueRedaction implements SensitiveDataDictionary {

  private final String redacted;
  private final SensitiveDataDictionary sensitiveDataDictionary;
  private final RedactionStrategy redactionStrategy;

  public ArgumentValueRedaction() {
    this(REDACTED, new CombinedSensitiveDataDictionary(
        new SensitivePrefixDictionary(SENSITIVE_PREFIXES),
        new SensitiveSubstringDictionary(SENSITIVE_SUBSTRINGS)));
  }

  private ArgumentValueRedaction(String redacted, SensitiveDataDictionary sensitiveDataDictionary) {
    this(redacted,
        sensitiveDataDictionary,
        new RegexRedactionStrategy(sensitiveDataDictionary::isSensitive, redacted));
  }

  @VisibleForTesting
  ArgumentValueRedaction(String redacted, SensitiveDataDictionary sensitiveDataDictionary,
      RedactionStrategy redactionStrategy) {
    this.redacted = redacted;
    this.sensitiveDataDictionary = sensitiveDataDictionary;
    this.redactionStrategy = redactionStrategy;
  }

  /**
   * Parse a string to find option/argument pairs and redact the arguments if sensitive.
   *
   * <p>
   * The following format is expected:<br>
   * - Each option/argument pair should be separated by spaces.<br>
   * - The option of each pair must be preceded by at least one hyphen '-'.<br>
   * - Parameters may or may not be wrapped in quotation marks.<br>
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
   * @param string The string input to be parsed
   *
   * @return A redacted string that has sensitive information obscured.
   */
  public String redact(String string) {
    return redactionStrategy.redact(string);
  }

  public String redact(Iterable<String> strings) {
    return redact(String.join(" ", strings));
  }

  /**
   * Return the redaction string if the provided option's argument value should be redacted.
   * Otherwise, return the provided argument unchanged.
   *
   * @param option A string such as a system property, jvm argument or command-line option.
   * @param argument A string that is the argument value for the option.
   *
   * @return A redacted string if the option indicates it should be redacted, otherwise the
   *         provided argument.
   */
  public String redactArgumentIfNecessary(String option, String argument) {
    if (isSensitive(option)) {
      return redacted;
    }
    return argument;
  }

  public List<String> redactEachInList(Collection<String> lines) {
    return lines.stream()
        .map(this::redact)
        .collect(toList());
  }

  @Override
  public boolean isSensitive(String string) {
    return sensitiveDataDictionary.isSensitive(string);
  }

  public String getRedacted() {
    return redacted;
  }
}
