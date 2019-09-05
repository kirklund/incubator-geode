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

import static org.apache.geode.distributed.ConfigurationProperties.ENABLE_CLUSTER_CONFIGURATION;
import static org.apache.geode.distributed.ConfigurationProperties.NAME;
import static org.apache.geode.test.dunit.VM.getVM;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionFactory;
import org.apache.geode.cache.RegionShortcut;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.cache.server.CacheServer;
import org.apache.geode.distributed.ServerLauncher;
import org.apache.geode.internal.cache.InternalCache;
import org.apache.geode.test.dunit.VM;
import org.apache.geode.test.dunit.rules.DistributedRule;
import org.apache.geode.test.junit.rules.serializable.SerializableTemporaryFolder;

public class TracingDistributedTest implements Serializable {

  @Rule
  public DistributedRule distributedRule = new DistributedRule();

  @Rule
  public SerializableTemporaryFolder temporaryFolder = new SerializableTemporaryFolder();

  private VM serverVM;
  private VM clientVM;

  private static ServerLauncher serverLauncher;
  private static ClientCache clientCache;

  @Before
  public void setUp() {
    serverVM = getVM(-1);
    clientVM = getVM(0);

    int serverPort = serverVM.invoke(() -> {
      Path serverDir = temporaryFolder.getRoot().toPath().toAbsolutePath();

      ServerLauncher.Builder builder = new ServerLauncher.Builder();
      builder.setMemberName("server1");
      builder.setWorkingDirectory(serverDir.toString());
      builder.setServerPort(0);
      builder.set(ENABLE_CLUSTER_CONFIGURATION, "false");

      serverLauncher = builder.build();
      serverLauncher.start();

      InternalCache serverCache = (InternalCache) serverLauncher.getCache();

      RegionFactory regionFactory = serverCache.createRegionFactory(RegionShortcut.REPLICATE);
      regionFactory.create("region1");

      List<CacheServer> cacheServers = serverCache.getCacheServers();
      CacheServer cacheServer = cacheServers.get(0);
      return cacheServer.getPort();
    });

    clientVM.invoke(() -> {
      clientCache = new ClientCacheFactory().addPoolServer("localhost", serverPort)
          .set(NAME, "client1").create();
      ClientRegionFactory regionFactory =
          clientCache.createClientRegionFactory(ClientRegionShortcut.PROXY);
      regionFactory.create("region1");
    });
  }

  @After
  public void tearDown() {
    clientVM.invoke(() -> {
      if (clientCache != null) {
        clientCache.close();
      }
    });
    serverVM.invoke(() -> {
      if (serverLauncher != null) {
        serverLauncher.stop();
      }
    });
  }

  @Test
  public void test() {
    clientVM.invoke(() -> {
      Region region = clientCache.getRegion("region1");

      long putThreadId = Thread.currentThread().getId();

      region.put("key1", "value1");
    });
  }
}
