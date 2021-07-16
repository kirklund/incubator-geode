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

import static org.apache.geode.internal.util.ArgumentRedactor.getRedacted;
import static org.apache.geode.internal.util.ArgumentRedactor.isSensitive;
import static org.apache.geode.internal.util.ArgumentRedactor.redact;
import static org.apache.geode.internal.util.ArgumentRedactor.redactEachInList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

public class ArgumentRedactorTest {

  @Test
  public void isTaboo_endsWithSecurityHyphenPassword_isTaboo() {
    String input = "gemfire.security-password";

    boolean output = isSensitive(input);

    assertThat(output)
        .as("output of isTaboo(" + input + ")")
        .isTrue();
  }

  @Test
  public void isTaboo_password_isTaboo() {
    String input = "password";

    boolean output = isSensitive(input);

    assertThat(output)
        .as("output of isTaboo(" + input + ")")
        .isTrue();
  }

  @Test
  public void isTaboo_containsPassword_isTaboo() {
    String input = "other-password-option";

    boolean output = isSensitive(input);

    assertThat(output)
        .as("output of isTaboo(" + input + ")")
        .isTrue();
  }

  @Test
  public void isTaboo_clusterSslTruststorePassword_isTaboo() {
    String input = "cluster-ssl-truststore-password";

    boolean output = isSensitive(input);

    assertThat(output)
        .as("output of isTaboo(" + input + ")")
        .isTrue();
  }

  @Test
  public void isTaboo_gatewaySslTruststorePassword_isTaboo() {
    String input = "gateway-ssl-truststore-password";

    boolean output = isSensitive(input);

    assertThat(output)
        .as("output of isTaboo(" + input + ")")
        .isTrue();
  }

  @Test
  public void isTaboo_serverSslKeystorePassword_isTaboo() {
    String input = "server-ssl-keystore-password";

    boolean output = isSensitive(input);

    assertThat(output)
        .as("output of isTaboo(" + input + ")")
        .isTrue();
  }

  @Test
  public void isTaboo_securityUsername_isTaboo() {
    String input = "security-username";

    boolean output = isSensitive(input);

    assertThat(output)
        .as("output of isTaboo(" + input + ")")
        .isTrue();
  }

  @Test
  public void isTaboo_securityManager_isTaboo() {
    String input = "security-manager";

    boolean output = isSensitive(input);

    assertThat(output)
        .as("output of isTaboo(" + input + ")")
        .isTrue();
  }

  @Test
  public void isTaboo_beginsWithSecurityHyphen_isTaboo() {
    String input = "security-important-property";

    boolean output = isSensitive(input);

    assertThat(output)
        .as("output of isTaboo(" + input + ")")
        .isTrue();
  }

  @Test
  public void isTaboo_javaxNetSslKeyStorePassword_isTaboo() {
    String input = "javax.net.ssl.keyStorePassword";

    boolean output = isSensitive(input);

    assertThat(output)
        .as("output of isTaboo(" + input + ")")
        .isTrue();
  }

  @Test
  public void isTaboo_beginsWithJavaxNetSsl_isTaboo() {
    String input = "javax.net.ssl.some.security.item";

    boolean output = isSensitive(input);

    assertThat(output)
        .as("output of isTaboo(" + input + ")")
        .isTrue();
  }

  @Test
  public void isTaboo_javaxNetSslKeyStoreType_isTaboo() {
    String input = "javax.net.ssl.keyStoreType";

    boolean output = isSensitive(input);

    assertThat(output)
        .as("output of isTaboo(" + input + ")")
        .isTrue();
  }

  @Test
  public void isTaboo_beginsWithSyspropHyphen_isTaboo() {
    String input = "sysprop-secret-prop";

    boolean output = isSensitive(input);

    assertThat(output)
        .as("output of isTaboo(" + input + ")")
        .isTrue();
  }

  @Test
  public void isTaboo_gemfireSecurityManager_isNotTaboo() {
    String input = "gemfire.security-manager";

    boolean output = isSensitive(input);

    assertThat(output)
        .as("output of isTaboo(" + input + ")")
        .isFalse();
  }

  @Test
  public void isTaboo_clusterSslEnabled_isNotTaboo() {
    String input = "cluster-ssl-enabled";

    boolean output = isSensitive(input);

    assertThat(output)
        .as("output of isTaboo(" + input + ")")
        .isFalse();
  }

  @Test
  public void isTaboo_conserveSockets_isNotTaboo() {
    String input = "conserve-sockets";

    boolean output = isSensitive(input);

    assertThat(output)
        .as("output of isTaboo(" + input + ")")
        .isFalse();
  }

  @Test
  public void isTaboo_username_isNotTaboo() {
    String input = "username";

    boolean output = isSensitive(input);

    assertThat(output)
        .as("output of isTaboo(" + input + ")")
        .isFalse();
  }

  @Test
  public void isTaboo_justAnOption_isNotTaboo() {
    String input = "just-an-option";

    boolean output = isSensitive(input);

    assertThat(output)
        .as("output of isTaboo(" + input + ")")
        .isFalse();
  }

  @Test
  public void redact_endsWithPassword_afterHyphenD_isRedacted() {
    String string = "-Dgemfire.password=%s";
    String sensitive = "__this_should_be_redacted__";
    String input = String.format(string, sensitive);
    String expected = String.format(string, getRedacted());

    String output = redact(input);

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
    String expected = String.format(string, getRedacted());

    String output = redact(input);

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
    String expected = String.format(string, getRedacted());

    String output = redact(input);

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
    String expected = String.format(string, getRedacted());

    String output = redact(input);

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
    String expected = String.format(string, getRedacted());

    String output = redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .isEqualTo(expected);
  }

  @Test
  public void redact_endsWithPassword_afterHyphensJD_isRedacted2() {
    String string = "--J=-Dsome.highly.qualified.password=%s";
    String sensitive = "secret";
    String input = String.format(string, sensitive);
    String expected = String.format(string, getRedacted());

    String output = redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .isEqualTo(expected);
  }

  @Test
  public void redact_gemfireSecurityProperties_afterHyphenD_isNotRedacted() {
    String input = "-Dgemfire.security-properties=\"c:\\Program Files (x86)\\My Folder\"";

    String output = redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .isEqualTo(input);
  }

  @Test
  public void redact_gemfireSecurityProperties_afterHyphenD_isNotRedacted2() {
    String input = "-Dgemfire.security-properties=./security-properties";

    String output = redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .isEqualTo(input);
  }

  @Test
  public void redact_containsSecurityHyphen_afterHyphensJD_isNotRedacted() {
    String input = "--J=-Dgemfire.sys.security-option=someArg";

    String output = redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .isEqualTo(input);
  }

  @Test
  public void redact_gemfireOption_afterHyphenD_isNotRedacted() {
    String input = "-Dgemfire.sys.option=printable";

    String output = redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .isEqualTo(input);
  }

  @Test
  public void redact_gemfireUseClusterConfiguration_afterHyphenD_isNotRedacted() {
    String input = "-Dgemfire.use-cluster-configuration=true";

    String output = redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .isEqualTo(input);
  }

  @Test
  public void redact_miscOption_isNotRedacted() {
    String input = "someotherstringoption";

    String output = redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .isEqualTo(input);
  }

  @Test
  public void redact_hyphensClasspath_isNotRedacted() {
    String input = "--classpath=.";

    String output = redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .isEqualTo(input);
  }

  @Test
  public void redact_endsWithPassword_afterHyphenD_isRedacted_whileOtherOptions_areNotRedacted() {
    String string = "-DmyArg -Duser-password=%s --classpath=.";
    String sensitive = "foo";
    String input = String.format(string, sensitive);
    String expected = String.format(string, getRedacted());

    String output = redact(input);

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
    String expected = String.format(string, getRedacted(), getRedacted());

    String output = redact(input);

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
        string, getRedacted(), nonSensitive, getRedacted(), getRedacted(), getRedacted());

    String output = redact(input);

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
    String expected = String.format(string, getRedacted(), reusedSensitive);

    String output = redact(input);

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
    String expected = String.format(string, reusedSensitive, getRedacted(), getRedacted());

    String output = redact(input);

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
    String expected = String.format(string, getRedacted());

    String output = redact(input);

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
    String expected = String.format(string, getRedacted());

    String output = redact(input);

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
    String expected = String.format(string, getRedacted());

    String output = redact(input);

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
    String expected = String.format(string, getRedacted());

    String output = redact(input);

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
    String expected = String.format(string, getRedacted());

    String output = redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .doesNotContain(sensitive)
        .isEqualTo(expected);
  }

  @Test
  public void redact_sensitiveBeginsWithHyphen_isRedacted() {
    String string = "-Dgemfire.ssl-keystore-password=%s";
    String sensitive = "-supersecret";
    String input = String.format(string, sensitive);
    String expected = String.format(string, getRedacted());

    String output = redact(input);

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
    String expected = String.format(string, getRedacted());

    String output = redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .doesNotContain(sensitive)
        .isEqualTo(expected);
  }

  @Test
  public void redact_collectionOfSensitiveOptions_areAllRedacted() {
    String sensitive1 = "secret";
    String sensitive2 = "super-secret";
    String sensitive3 = "confidential";
    String sensitive4 = "shhhh";
    String sensitive5 = "failed";

    Collection<String> input = new ArrayList<>();
    input.add("--gemfire.security-password=" + sensitive1);
    input.add("--login-password=" + sensitive1);
    input.add("--gemfire-password = " + sensitive2);
    input.add("--geode-password= " + sensitive3);
    input.add("--some-other-password =" + sensitive4);
    input.add("--justapassword =" + sensitive5);

    String output = redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .doesNotContain(sensitive1)
        .doesNotContain(sensitive2)
        .doesNotContain(sensitive3)
        .doesNotContain(sensitive4)
        .doesNotContain(sensitive5)
        .contains("--gemfire.security-password=" + getRedacted())
        .contains("--login-password=" + getRedacted())
        .contains("--gemfire-password = " + getRedacted())
        .contains("--geode-password= " + getRedacted())
        .contains("--some-other-password =" + getRedacted())
        .contains("--justapassword =" + getRedacted());
  }

  @Test
  public void redact_collectionOfNonSensitiveOptions_areNotRedacted() {
    Collection<String> input = new ArrayList<>();
    input.add("--gemfire.security-properties=./security.properties");
    input.add("--gemfire.sys.security-option=someArg");
    input.add("--gemfire.use-cluster-configuration=true");
    input.add("--someotherstringoption");
    input.add("--login-name=admin");
    input.add("--myArg --myArg2 --myArg3=-arg4");
    input.add("--myArg --myArg2 --myArg3=\"-arg4\"");

    String output = redact(input);

    assertThat(output)
        .as("output of redact(" + input + ")")
        .contains("--gemfire.security-properties=./security.properties")
        .contains("--gemfire.sys.security-option=someArg")
        .contains("--gemfire.use-cluster-configuration=true")
        .contains("--someotherstringoption")
        .contains("--login-name=admin")
        .contains("--myArg --myArg2 --myArg3=-arg4")
        .contains("--myArg --myArg2 --myArg3=\"-arg4\"");
  }

  @Test
  public void redactEachInList_collectionOfSensitiveOptions_areAllRedacted() {
    String sensitive1 = "secret";
    String sensitive2 = "super-secret";
    String sensitive3 = "confidential";
    String sensitive4 = "shhhh";
    String sensitive5 = "failed";

    Collection<String> input = new ArrayList<>();
    input.add("--gemfire.security-password=" + sensitive1);
    input.add("--login-password=" + sensitive1);
    input.add("--gemfire-password = " + sensitive2);
    input.add("--geode-password= " + sensitive3);
    input.add("--some-other-password =" + sensitive4);
    input.add("--justapassword =" + sensitive5);

    List<String> output = redactEachInList(input);

    assertThat(output)
        .as("output of redactEachInList(" + input + ")")
        .doesNotContain(sensitive1)
        .doesNotContain(sensitive2)
        .doesNotContain(sensitive3)
        .doesNotContain(sensitive4)
        .doesNotContain(sensitive5)
        .contains("--gemfire.security-password=" + getRedacted())
        .contains("--login-password=" + getRedacted())
        .contains("--gemfire-password = " + getRedacted())
        .contains("--geode-password= " + getRedacted())
        .contains("--some-other-password =" + getRedacted())
        .contains("--justapassword =" + getRedacted());
  }
}
