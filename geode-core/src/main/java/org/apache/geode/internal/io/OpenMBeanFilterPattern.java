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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.rmi.MarshalledObject;

import javax.management.ObjectName;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularType;

/**
 * Defines a serial filter pattern that accepts all open MBean data types and rejects everything
 * not included in the pattern.
 */
public class OpenMBeanFilterPattern implements FilterPattern {

  @Override
  public String pattern() {
    // note: java.util.* may also be needed
    return new DelimitedStringBuilder(';')

        // accept all open MBean data types
        .append(Boolean.class.getName())
        .append(Byte.class.getName())
        .append(Character.class.getName())
        .append(Short.class.getName())
        .append(Integer.class.getName())
        .append(Long.class.getName())
        .append(Float.class.getName())
        .append(Double.class.getName())
        .append(String.class.getName())
        .append(BigInteger.class.getName())
        .append(BigDecimal.class.getName())
        .append(ObjectName.class.getName())
        .append(OpenType.class.getName())
        .append(CompositeData.class.getName())
        .append(TabularData.class.getName())
        .append(SimpleType.class.getName())
        .append(CompositeType.class.getName())
        .append(TabularType.class.getName())
        .append(ArrayType.class.getName())
        .append(MarshalledObject.class.getName())

        // reject all other classes
        .append("!*")
        .toString();
  }
}
