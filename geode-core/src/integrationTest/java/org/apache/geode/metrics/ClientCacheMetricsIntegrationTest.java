/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geode.metrics;

import static org.apache.geode.distributed.ConfigurationProperties.LOG_LEVEL;

import java.util.List;
import java.util.Properties;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.internal.cache.InternalCache;

public class ClientCacheMetricsIntegrationTest {

  private ClientCache clientCache;

  @Before
  public void setUp() {
    Properties configProperties = new Properties();
    configProperties.setProperty(LOG_LEVEL, "WARN");

    clientCache = new ClientCacheFactory(configProperties).create();
  }

  @After
  public void tearDown() {
    clientCache.close();
  }

  @Test
  public void test() {
    MeterRegistry meterRegistry = ((InternalCache) clientCache).getMeterRegistry();

    List<Meter> meters = meterRegistry.getMeters();

    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("Meters for ClientCache: ").append(System.lineSeparator());
    for (Meter meter : meters) {
      stringBuilder.append("\t").append(meter.getId()).append(System.lineSeparator());
    }
    System.out.print(stringBuilder);
  }
}
