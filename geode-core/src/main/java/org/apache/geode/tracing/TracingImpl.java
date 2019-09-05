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
package org.apache.geode.tracing;

import java.io.IOException;
import java.io.UncheckedIOException;

import brave.Tracer;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Sender;
import zipkin2.reporter.okhttp3.OkHttpSender;

public class TracingImpl implements AutoCloseable, Tracing {

  private final Sender sender;
  private final AsyncReporter<Span> spanReporter;
  private final brave.Tracing tracing;
  private final Tracer tracer;

  public TracingImpl(String zipkinUrl) {
    // Configure a reporter, which controls how often spans are sent
    // (the dependency is io.zipkin.reporter2:zipkin-sender-okhttp3)
    sender = OkHttpSender.create(zipkinUrl);
    spanReporter = AsyncReporter.create(sender);

    // Create a tracing component with the service name you want to see in Zipkin.
    tracing = brave.Tracing.newBuilder()
        .localServiceName("my-service")
        .spanReporter(spanReporter)
        .build();

    tracer = tracing.tracer();
  }

  @Override
  public Tracer getTracer() {
    return tracer;
  }

  @Override
  public void close() {
    // Failing to close resources can result in dropped spans! When tracing is no
    // longer needed, close the components you made in reverse order. This might be
    // a shutdown hook for some users.
    tracing.close();
    spanReporter.close();
    try {
      sender.close();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
