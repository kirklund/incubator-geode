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

/**
 * This list contains classes that Geode's classes subclass, such as antlr AST classes which are
 * used by our Object Query Language. It also contains certain classes that are DataSerializable
 * but end up being serialized as part of other serializable objects. VersionedObjectList, for
 * instance, is serialized as part of a partial putAll exception object.
 *
 * <p>
 * Do not java-serialize objects that Geode does not have complete control over. This leaves us
 * open to security attacks such as Gadget Chains and compromises the ability to do a rolling
 * upgrade from one version of Geode to the next.
 *
 * <p>
 * In general, you shouldn't use java serialization, and you should implement
 * DataSerializableFixedID for internal Geode objects. This gives you better control over
 * backward-compatibility.
 *
 * <p>
 * Do not add to this list unless absolutely necessary. Instead, put your classes either in the
 * sanctionedSerializables file for your module or in its excludedClasses file. Run
 * AnalyzeSerializables to generate the content for the file.
 */
public class SanctionedSerializablesFilterPattern implements FilterPattern {

  private final DelimitedStringBuilder delimitedStringBuilder =
      new DelimitedStringBuilder(dependenciesPattern(), ';');

  @Override
  public String pattern() {
    return delimitedStringBuilder

        // reject all other classes
        .append("!*")

        .toString();
  }

  public SanctionedSerializablesFilterPattern append(String string) {
    delimitedStringBuilder.append(string);
    return this;
  }

  private static String dependenciesPattern() {
    return new DelimitedStringBuilder(';')

        // accept all open MBean data types
        .append("java.**")
        .append("javax.management.**")

        // used for some old enums
        .append("javax.print.attribute.EnumSyntax")

        // query AST objects
        .append("antlr.**")

        // old Admin API
        .append("org.apache.commons.modeler.AttributeInfo")
        .append("org.apache.commons.modeler.FeatureInfo")
        .append("org.apache.commons.modeler.ManagedBean")
        .append("org.apache.geode.distributed.internal.DistributionConfigSnapshot")
        .append("org.apache.geode.distributed.internal.RuntimeDistributionConfigImpl")
        .append("org.apache.geode.distributed.internal.DistributionConfigImpl")

        // WindowedExportFunction, RegionSnapshotService
        .append("org.apache.geode.distributed.internal.membership.InternalDistributedMember")

        // putAll
        .append("org.apache.geode.internal.cache.persistence.PersistentMemberID")
        .append("org.apache.geode.internal.cache.persistence.DiskStoreID")
        .append("org.apache.geode.internal.cache.tier.sockets.VersionedObjectList")

        // security services
        .append("org.apache.shiro.*")
        .append("org.apache.shiro.authz.*")
        .append("org.apache.shiro.authc.*")

        // export logs
        .append("org.apache.logging.log4j.Level")
        .append("org.apache.logging.log4j.spi.StandardLevel")

        // jar deployment
        .append("com.sun.proxy.$Proxy*")
        .append("com.healthmarketscience.rmiio.RemoteInputStream")
        .append("javax.rmi.ssl.SslRMIClientSocketFactory")
        .append("javax.net.ssl.SSLHandshakeException")
        .append("javax.net.ssl.SSLException")
        .append("sun.security.validator.ValidatorException")
        .append("sun.security.provider.certpath.SunCertPathBuilderException")

        // geode-modules
        .append("org.apache.geode.modules.util.SessionCustomExpiry")

        .toString();
  }
}
