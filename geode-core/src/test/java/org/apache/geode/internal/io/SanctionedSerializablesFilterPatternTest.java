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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class SanctionedSerializablesFilterPatternTest {

  private String filterPattern;

  @Before
  public void setUp() {
    filterPattern = new SanctionedSerializablesFilterPattern().pattern();
  }

  @Test
  public void includesJavaWithSubpackages() {
    assertThat(filterPattern).contains("java.**");
  }

  @Test
  public void includesJavaxManagementWithSubpackages() {
    assertThat(filterPattern).contains("javax.management.**");
  }

  @Test
  public void includesEnumSyntax() {
    assertThat(filterPattern).contains("javax.print.attribute.EnumSyntax");
  }

  @Test
  public void includesAntlrWithSubpackages() {
    assertThat(filterPattern).contains("antlr.**");
  }

  @Test
  public void includesCommonsModelerAttributeInfo() {
    assertThat(filterPattern).contains("org.apache.commons.modeler.AttributeInfo");
  }

  @Test
  public void includesCommonsModelerFeatureInfo() {
    assertThat(filterPattern).contains("org.apache.commons.modeler.FeatureInfo");
  }

  @Test
  public void includesCommonsModelerManagedBean() {
    assertThat(filterPattern).contains("org.apache.commons.modeler.ManagedBean");
  }

  @Test
  public void includesDistributionConfigSnapshot() {
    assertThat(filterPattern)
        .contains("org.apache.geode.distributed.internal.DistributionConfigSnapshot");
  }

  @Test
  public void includesRuntimeDistributionConfigImpl() {
    assertThat(filterPattern)
        .contains("org.apache.geode.distributed.internal.RuntimeDistributionConfigImpl");
  }

  @Test
  public void includesDistributionConfigImpl() {
    assertThat(filterPattern)
        .contains("org.apache.geode.distributed.internal.DistributionConfigImpl");
  }

  @Test
  public void includesInternalDistributedMember() {
    assertThat(filterPattern)
        .contains("org.apache.geode.distributed.internal.membership.InternalDistributedMember");
  }

  @Test
  public void includesPersistentMemberID() {
    assertThat(filterPattern)
        .contains("org.apache.geode.internal.cache.persistence.PersistentMemberID");
  }

  @Test
  public void includesDiskStoreID() {
    assertThat(filterPattern).contains("org.apache.geode.internal.cache.persistence.DiskStoreID");
  }

  @Test
  public void includesVersionedObjectList() {
    assertThat(filterPattern)
        .contains("org.apache.geode.internal.cache.tier.sockets.VersionedObjectList");
  }

  @Test
  public void includesShiroPackage() {
    assertThat(filterPattern).contains("org.apache.shiro.*");
  }

  @Test
  public void includesShiroAuthzPackage() {
    assertThat(filterPattern).contains("org.apache.shiro.authz.*");
  }

  @Test
  public void includesShiroAuthcPackage() {
    assertThat(filterPattern).contains("org.apache.shiro.authc.*");
  }

  @Test
  public void includesLog4jLevel() {
    assertThat(filterPattern).contains("org.apache.logging.log4j.Level");
  }

  @Test
  public void includesLog4jStandardLevel() {
    assertThat(filterPattern).contains("org.apache.logging.log4j.spi.StandardLevel");
  }

  @Test
  public void includesSunProxy() {
    assertThat(filterPattern).contains("com.sun.proxy.$Proxy*");
  }

  @Test
  public void includesRmiioRemoteInputStream() {
    assertThat(filterPattern).contains("com.healthmarketscience.rmiio.RemoteInputStream");
  }

  @Test
  public void includesSslRMIClientSocketFactory() {
    assertThat(filterPattern).contains("javax.rmi.ssl.SslRMIClientSocketFactory");
  }

  @Test
  public void includesSSLHandshakeException() {
    assertThat(filterPattern).contains("javax.net.ssl.SSLHandshakeException");
  }

  @Test
  public void includesSSLException() {
    assertThat(filterPattern).contains("javax.net.ssl.SSLException");
  }

  @Test
  public void includesSunValidatorException() {
    assertThat(filterPattern).contains("sun.security.validator.ValidatorException");
  }

  @Test
  public void includesSunSunCertPathBuilderException() {
    assertThat(filterPattern)
        .contains("sun.security.provider.certpath.SunCertPathBuilderException");
  }

  @Test
  public void includesSessionCustomExpiry() {
    assertThat(filterPattern).contains("org.apache.geode.modules.util.SessionCustomExpiry");
  }

  @Test
  public void rejectsAllOtherTypes() {
    assertThat(filterPattern).endsWith(";!*");
  }
}
