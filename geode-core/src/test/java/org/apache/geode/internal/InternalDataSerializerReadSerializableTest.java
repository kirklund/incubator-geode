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
package org.apache.geode.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

import org.apache.geode.internal.serialization.ByteArrayDataInput;
import org.apache.geode.internal.serialization.KnownVersion;

public class InternalDataSerializerReadSerializableTest {

  @Test
  public void readSerializableDeserializesException() throws IOException, ClassNotFoundException {
    String message = "12345678901234567890123456789012345";
    Serializable serializable = new Exception(message);
    byte[] serialized = SerializationUtils.serialize(serializable);
    ByteArrayDataInput dataInput = new ByteArrayDataInput();
    dataInput.initialize(serialized, KnownVersion.CURRENT);

    Serializable deserialized = InternalDataSerializer.readSerializable(dataInput);

    assertThat(deserialized).isInstanceOf(serializable.getClass());
    Exception deserializedException = (Exception) deserialized;
    assertThat(deserializedException).hasMessage(message);
  }
}
