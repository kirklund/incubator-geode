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

import org.junit.Test;

public class RedactionDefaultsTest {

  @Test
  public void sensitiveSubstringsIncludesPassword() {
    assertThat(RedactionDefaults.SENSITIVE_SUBSTRINGS).containsOnly("password");
  }

  @Test
  public void sensitivePrefixesIncludesSyspropHyphen() {
    assertThat(RedactionDefaults.SENSITIVE_PREFIXES).contains("sysprop-");
  }

  @Test
  public void sensitivePrefixesIncludesHyphenDSyspropHyphen() {
    assertThat(RedactionDefaults.SENSITIVE_PREFIXES).contains("-Dsysprop-");
  }

  @Test
  public void sensitivePrefixesIncludesHyphensJDSyspropHyphen() {
    assertThat(RedactionDefaults.SENSITIVE_PREFIXES).contains("--J=-Dsysprop-");
  }

  @Test
  public void sensitivePrefixesIncludesJavaxDotNetDotSsl() {
    assertThat(RedactionDefaults.SENSITIVE_PREFIXES).contains("javax.net.ssl");
  }

  @Test
  public void sensitivePrefixesIncludesHyphenDJavaxDotNetDotSsl() {
    assertThat(RedactionDefaults.SENSITIVE_PREFIXES).contains("-Djavax.net.ssl");
  }

  @Test
  public void sensitivePrefixesIncludesHyphensJDJavaxDotNetDotSsl() {
    assertThat(RedactionDefaults.SENSITIVE_PREFIXES).contains("--J=-Djavax.net.ssl");
  }

  @Test
  public void sensitivePrefixesIncludesSecurityHyphen() {
    assertThat(RedactionDefaults.SENSITIVE_PREFIXES).contains("security-");
  }

  @Test
  public void sensitivePrefixesIncludesHyphenDSecurityHyphen() {
    assertThat(RedactionDefaults.SENSITIVE_PREFIXES).contains("-Dsecurity-");
  }

  @Test
  public void sensitivePrefixesIncludesHyphensJDSecurityHyphen() {
    assertThat(RedactionDefaults.SENSITIVE_PREFIXES).contains("--J=-Dsecurity-");
  }

  @Test
  public void sensitivePrefixesIncludesOnly() {
    assertThat(RedactionDefaults.SENSITIVE_PREFIXES)
        .containsOnly("sysprop-", "javax.net.ssl", "security-",
            "-Dsysprop-", "-Djavax.net.ssl", "-Dsecurity-",
            "--J=-Dsysprop-", "--J=-Djavax.net.ssl", "--J=-Dsecurity-");
  }
}
