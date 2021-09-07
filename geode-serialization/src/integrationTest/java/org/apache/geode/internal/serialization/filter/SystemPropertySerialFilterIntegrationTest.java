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
package org.apache.geode.internal.serialization.filter;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;

public class SystemPropertySerialFilterIntegrationTest {

  @Rule
  public RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

  @Test
  public void settingJdkSerialFilterPropertyStartsFiltering() {
    System.setProperty("jdk.serialFilter", "*;!SerializableClass");

    SerializableClass clone = SerializationUtils.clone(new SerializableClass("hello"));

    assertThat(clone.getMessage()).isEqualTo("hello");
  }

  @Test
  public void stringSerializationSanityCheck() throws IOException, ClassNotFoundException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ObjectOutput objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
    objectOutputStream.writeObject("hello");
    objectOutputStream.flush();

    ObjectInput objectInputStream =
        new ObjectInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));

    assertThat(objectInputStream.readObject()).isEqualTo("hello");
  }

  @Test
  public void serializableClassSanityCheck() throws IOException, ClassNotFoundException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ObjectOutput objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
    objectOutputStream.writeObject(new SerializableClass("hello"));
    objectOutputStream.flush();

    ObjectInputStream objectInputStream =
        new ObjectInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));

    SerializableClass serializableClass = (SerializableClass) objectInputStream.readObject();
    assertThat(serializableClass.getMessage()).isEqualTo("hello");
  }

  @Test
  public void serializableClassWithCustomRejectFilter() throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ObjectOutput objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
    objectOutputStream.writeObject(new SerializableClass("hello"));
    objectOutputStream.flush();
    ObjectInputStream objectInputStream =
        new ObjectInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
    InputStreamFilter inputStreamFilter = new SerializationFilterFactory()
        .create("!" + SerializableClass.class.getName(), emptySet());
    inputStreamFilter.setFilterOn(objectInputStream);

    Throwable thrown = catchThrowable(objectInputStream::readObject);

    assertThat(thrown)
        .isInstanceOf(InvalidClassException.class)
        .hasMessage("filter status: REJECTED");
  }

  @Test
  public void serializableClassWithSystemPropertyRejectFilter()
      throws IOException, ClassNotFoundException {
    System.setProperty("jdk.serialFilter", "!*");

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ObjectOutput objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
    objectOutputStream.writeObject(new SerializableClass("hello"));
    objectOutputStream.flush();
    ObjectInputStream objectInputStream =
        new ObjectInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));

    SerializableClass serializableClass = (SerializableClass) objectInputStream.readObject();
    assertThat(serializableClass.getMessage()).isEqualTo("hello");
  }

  @Test
  public void serializableClassWithProcessWideRejectFilter()
      throws IOException, ClassNotFoundException {
    // TODO

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ObjectOutput objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
    objectOutputStream.writeObject(new SerializableClass("hello"));
    objectOutputStream.flush();
    ObjectInputStream objectInputStream =
        new ObjectInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));

    SerializableClass serializableClass = (SerializableClass) objectInputStream.readObject();
    assertThat(serializableClass.getMessage()).isEqualTo("hello");
  }

  @SuppressWarnings("serial")
  private static class SerializableClass implements Serializable {

    private final String message;

    private SerializableClass(String message) {
      this.message = message;
    }

    private String getMessage() {
      return message;
    }
  }
}
