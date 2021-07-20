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

import static org.apache.geode.internal.util.redaction.RedactionDefaults.SENSITIVE_PREFIXES;
import static org.apache.geode.internal.util.redaction.RedactionDefaults.SENSITIVE_SUBSTRINGS;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class RegexRedactionStrategyTest {

  private static final String REDACTED = "redacted";

  private RegexRedactionStrategy regexRedactionStrategy;

  @Before
  public void setUp() {
    SensitiveDataDictionary sensitiveDataDictionary = new CombinedSensitiveDictionary(
        new SensitivePrefixDictionary(SENSITIVE_PREFIXES),
        new SensitiveSubstringDictionary(SENSITIVE_SUBSTRINGS));

    regexRedactionStrategy =
        new RegexRedactionStrategy(sensitiveDataDictionary::isSensitive, REDACTED);
  }

  @Test
  public void redact_endsWithPassword_afterHyphenD_isRedacted() {
    String string = "-Dgemfire.password=%s";
    String sensitive = "__this_should_be_redacted__";
    String input = String.format(string, sensitive);
    String expected = String.format(string, REDACTED);

    String output = regexRedactionStrategy.redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .doesNotContain(sensitive)
        .isEqualTo(expected);
  }

  @Test
  public void redact_password_afterHyphens_isRedacted() {
    String string = "--password=%s";
    String sensitive = "__this_should_be_redacted__";
    String input = String.format(string, sensitive);
    String expected = String.format(string, REDACTED);

    String output = regexRedactionStrategy.redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .doesNotContain(sensitive)
        .isEqualTo(expected);
  }

  @Test
  public void redact_endsWithPassword_afterHyphensJD_isRedacted() {
    String string = "--J=-Dgemfire.some.very.qualified.item.password=%s";
    String sensitive = "__this_should_be_redacted__";
    String input = String.format(string, sensitive);
    String expected = String.format(string, REDACTED);

    String output = regexRedactionStrategy.redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .doesNotContain(sensitive)
        .isEqualTo(expected);
  }

  @Test
  public void redact_beginsWithSyspropHyphen_afterHyphensJD_isRedacted() {
    String string = "--J=-Dsysprop-secret.information=%s";
    String sensitive = "__this_should_be_redacted__";
    String input = String.format(string, sensitive);
    String expected = String.format(string, REDACTED);

    String output = regexRedactionStrategy.redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .doesNotContain(sensitive)
        .isEqualTo(expected);
  }

  @Test
  public void redact_gemfireSecurityPassword_afterHyphenD_isRedacted() {
    String string = "-Dgemfire.security-password=%s";
    String sensitive = "secret";
    String input = String.format(string, sensitive);
    String expected = String.format(string, REDACTED);

    String output = regexRedactionStrategy.redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .isEqualTo(expected);
  }

  @Test
  public void redact_endsWithPassword_afterHyphensJD_isRedacted2() {
    String string = "--J=-Dsome.highly.qualified.password=%s";
    String sensitive = "secret";
    String input = String.format(string, sensitive);
    String expected = String.format(string, REDACTED);

    String output = regexRedactionStrategy.redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .isEqualTo(expected);
  }

  @Test
  public void redact_gemfireSecurityProperties_afterHyphenD_isNotRedacted() {
    String input = "-Dgemfire.security-properties=\"c:\\Program Files (x86)\\My Folder\"";

    String output = regexRedactionStrategy.redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .isEqualTo(input);
  }

  @Test
  public void redact_gemfireSecurityProperties_afterHyphenD_isNotRedacted2() {
    String input = "-Dgemfire.security-properties=./security-properties";

    String output = regexRedactionStrategy.redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .isEqualTo(input);
  }

  @Test
  public void redact_containsSecurityHyphen_afterHyphensJD_isNotRedacted() {
    String input = "--J=-Dgemfire.sys.security-option=someArg";

    String output = regexRedactionStrategy.redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .isEqualTo(input);
  }

  @Test
  public void redact_gemfireOption_afterHyphenD_isNotRedacted() {
    String input = "-Dgemfire.sys.option=printable";

    String output = regexRedactionStrategy.redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .isEqualTo(input);
  }

  @Test
  public void redact_gemfireUseClusterConfiguration_afterHyphenD_isNotRedacted() {
    String input = "-Dgemfire.use-cluster-configuration=true";

    String output = regexRedactionStrategy.redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .isEqualTo(input);
  }

  @Test
  public void redact_miscOption_isNotRedacted() {
    String input = "someotherstringoption";

    String output = regexRedactionStrategy.redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .isEqualTo(input);
  }

  @Test
  public void redact_hyphensClasspath_isNotRedacted() {
    String input = "--classpath=.";

    String output = regexRedactionStrategy.redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .isEqualTo(input);
  }

  @Test
  public void redact_endsWithPassword_afterHyphenD_isRedacted_whileOtherOptions_areNotRedacted() {
    String string = "-DmyArg -Duser-password=%s --classpath=.";
    String sensitive = "foo";
    String input = String.format(string, sensitive);
    String expected = String.format(string, REDACTED);

    String output = regexRedactionStrategy.redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .doesNotContain(sensitive)
        .isEqualTo(expected);
  }

  @Test
  public void redact_twoEndsWithPasswordOptions_areRedacted_whileOtherOptions_areNotRedacted() {
    String string = "-DmyArg -Duser-password=%s -DOtherArg -Dsystem-password=%s";
    String sensitive1 = "foo";
    String sensitive2 = "bar";
    String input = String.format(string, sensitive1, sensitive2);
    String expected = String.format(string, REDACTED, REDACTED);

    String output = regexRedactionStrategy.redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .doesNotContain(sensitive1)
        .doesNotContain(sensitive2)
        .isEqualTo(expected);
  }

  @Test
  public void redact_manyEndsWithPasswordOptions_areRedacted_whileOtherOption_isNotRedacted() {
    String string =
        "-Dlogin-password=%s -Dlogin-name=%s -Dgemfire-password = %s --geode-password= %s --J=-Dsome-other-password =%s";
    String sensitive1 = "secret";
    String nonSensitive = "admin";
    String sensitive2 = "super-secret";
    String sensitive3 = "confidential";
    String sensitive4 = "shhhh";
    String input = String.format(
        string, sensitive1, nonSensitive, sensitive2, sensitive3, sensitive4);
    String expected = String.format(
        string, REDACTED, nonSensitive, REDACTED, REDACTED, REDACTED);

    String output = regexRedactionStrategy.redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .doesNotContain(sensitive1)
        .contains(nonSensitive)
        .doesNotContain(sensitive2)
        .doesNotContain(sensitive3)
        .doesNotContain(sensitive4)
        .isEqualTo(expected);
  }

  @Test
  public void redact_password_afterHyphens_isRedacted_butReusedForOption_isNotRedacted() {
    String string = "connect --password=%s --user=%s";
    String reusedSensitive = "test";
    String input = String.format(string, reusedSensitive, reusedSensitive);
    String expected = String.format(string, REDACTED, reusedSensitive);

    String output = regexRedactionStrategy.redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .contains(reusedSensitive)
        .isEqualTo(expected);
  }

  @Test
  public void redact_twoEndsWithPasswordOptions_areRedacted_butReusedForOption_andOtherOption_areNotRedacted() {
    String string = "connect --%s-password=%s --product-password=%s";
    String reusedSensitive = "test";
    String sensitive = "test1";
    String input = String.format(string, reusedSensitive, reusedSensitive, sensitive);
    String expected = String.format(string, reusedSensitive, REDACTED, REDACTED);

    String output = regexRedactionStrategy.redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .contains(reusedSensitive)
        .doesNotContain(sensitive)
        .isEqualTo(expected);
  }

  @Test
  public void redact_gemfireSslTruststorePassword_isRedacted() {
    String string = "-Dgemfire.ssl-truststore-password=%s";
    String sensitive = "gibberish";
    String input = String.format(string, sensitive);
    String expected = String.format(string, REDACTED);

    String output = regexRedactionStrategy.redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .doesNotContain(sensitive)
        .isEqualTo(expected);
  }

  @Test
  public void redact_gemfireSslKeystorePassword_isRedacted() {
    String string = "-Dgemfire.ssl-keystore-password=%s";
    String sensitive = "gibberish";
    String input = String.format(string, sensitive);
    String expected = String.format(string, REDACTED);

    String output = regexRedactionStrategy.redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .doesNotContain(sensitive)
        .isEqualTo(expected);
  }

  @Test
  public void redact_sensitiveEndsWithHyphen_isRedacted() {
    String string = "-Dgemfire.ssl-keystore-password=%s";
    String sensitive = "supersecret-";
    String input = String.format(string, sensitive);
    String expected = String.format(string, REDACTED);

    String output = regexRedactionStrategy.redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .doesNotContain(sensitive)
        .isEqualTo(expected);
  }

  @Test
  public void redact_sensitiveContainsHyphen_isRedacted() {
    String string = "-Dgemfire.ssl-keystore-password=%s";
    String sensitive = "super-secret";
    String input = String.format(string, sensitive);
    String expected = String.format(string, REDACTED);

    String output = regexRedactionStrategy.redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .doesNotContain(sensitive)
        .isEqualTo(expected);
  }

  @Test
  public void redact_sensitiveContainsManyHyphens_isRedacted() {
    String string = "-Dgemfire.ssl-keystore-password=%s";
    String sensitive = "this-is-super-secret";
    String input = String.format(string, sensitive);
    String expected = String.format(string, REDACTED);

    String output = regexRedactionStrategy.redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .doesNotContain(sensitive)
        .isEqualTo(expected);
  }

  @Test
  public void redact_sensitiveBeginsWithHyphen_isRedacted_fixMe() {
    String string = "-Dgemfire.ssl-keystore-password=%s";
    String sensitive = "-supersecret";
    String input = String.format(string, sensitive);
    String expected = String.format(string, REDACTED);

    String output = regexRedactionStrategy.redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .doesNotContain(sensitive)
        .isEqualTo(expected);
  }

  @Test
  public void redact_quotedSensitiveBeginsWithHyphen_isRedacted() {
    String string = "-Dgemfire.ssl-keystore-password=%s";
    String sensitive = "\"-supersecret\"";
    String input = String.format(string, sensitive);
    String expected = String.format(string, REDACTED);

    String output = regexRedactionStrategy.redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .doesNotContain(sensitive)
        .isEqualTo(expected);
  }
}
