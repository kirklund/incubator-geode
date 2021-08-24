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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.rmi.MarshalledObject;

import javax.management.ObjectName;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularType;

import org.junit.Before;
import org.junit.Test;

public class OpenMBeanFilterPatternTest {

  private String filterPattern;

  @Before
  public void setUp() {
    filterPattern = new OpenMBeanFilterPattern().pattern();
  }

  @Test
  public void includesBoolean() {
    assertThat(filterPattern).contains(Boolean.class.getName());
  }

  @Test
  public void includesByte() {
    assertThat(filterPattern).contains(Byte.class.getName());
  }

  @Test
  public void includesCharacter() {
    assertThat(filterPattern).contains(Character.class.getName());
  }

  @Test
  public void includesShort() {
    assertThat(filterPattern).contains(Short.class.getName());
  }

  @Test
  public void includesInteger() {
    assertThat(filterPattern).contains(Integer.class.getName());
  }

  @Test
  public void includesLong() {
    assertThat(filterPattern).contains(Long.class.getName());
  }

  @Test
  public void includesFloat() {
    assertThat(filterPattern).contains(Float.class.getName());
  }

  @Test
  public void includesDouble() {
    assertThat(filterPattern).contains(Double.class.getName());
  }

  @Test
  public void includesString() {
    assertThat(filterPattern).contains(String.class.getName());
  }

  @Test
  public void includesBigInteger() {
    assertThat(filterPattern).contains(BigInteger.class.getName());
  }

  @Test
  public void includesBigDecimal() {
    assertThat(filterPattern).contains(BigDecimal.class.getName());
  }

  @Test
  public void includesObjectName() {
    assertThat(filterPattern).contains(ObjectName.class.getName());
  }

  @Test
  public void includesCompositeData() {
    assertThat(filterPattern).contains(CompositeData.class.getName());
  }

  @Test
  public void includesTabularData() {
    assertThat(filterPattern).contains(TabularData.class.getName());
  }

  @Test
  public void includesSimpleType() {
    assertThat(filterPattern).contains(SimpleType.class.getName());
  }

  @Test
  public void includesCompositeType() {
    assertThat(filterPattern).contains(CompositeType.class.getName());
  }

  @Test
  public void includesTabularType() {
    assertThat(filterPattern).contains(TabularType.class.getName());
  }

  @Test
  public void includesArrayType() {
    assertThat(filterPattern).contains(ArrayType.class.getName());
  }

  @Test
  public void includesMarshalledObject() {
    assertThat(filterPattern).contains(MarshalledObject.class.getName());
  }

  @Test
  public void rejectsAllOtherTypes() {
    assertThat(filterPattern).endsWith(";!*");
  }
}
