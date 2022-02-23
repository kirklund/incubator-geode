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

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import org.apache.geode.DataSerializer;
import org.apache.geode.internal.serialization.ByteArrayDataInput;
import org.apache.geode.internal.serialization.DSCODE;
import org.apache.geode.internal.serialization.KnownVersion;

@Measurement(iterations = 5, time = 120, timeUnit = SECONDS)
@Warmup(iterations = 1, time = 30, timeUnit = SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(NANOSECONDS)
@State(Scope.Thread)
@SuppressWarnings("all")
public class InternalDataSerializerReadSerializableBenchmark {

  private ByteArrayDataInput dataInput = new ByteArrayDataInput();
  private byte[] serializedBytes;

  @Setup
  public void setUp() throws IOException {
    System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());

    dataInput = new ByteArrayDataInput();

    Exception serializedObject = new Exception("12345678901234567890123456789012345");
    HeapDataOutputStream hdos = new HeapDataOutputStream(KnownVersion.CURRENT);
    DataSerializer.writeObject(serializedObject, hdos);
    byte[] bytes = hdos.toByteArray();
    if (bytes[0] != DSCODE.SERIALIZABLE.toByte()) {
      throw new IllegalStateException(
          "expected first byte to be " + DSCODE.SERIALIZABLE.toByte() + " but it was " + bytes[0]);
    }
    serializedBytes = Arrays.copyOfRange(bytes, 1, bytes.length);
  }

  @Benchmark
  @Threads(1)
  public Serializable readSerializable() throws IOException, ClassNotFoundException {
    dataInput.initialize(serializedBytes, KnownVersion.CURRENT);
    return InternalDataSerializer.readSerializable(dataInput);
  }
}
