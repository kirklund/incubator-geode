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
package org.apache.geode.management.internal.cli.commands;

import static org.apache.geode.internal.AvailablePortHelper.getRandomAvailableTCPPorts;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import org.apache.geode.test.junit.categories.GfshTest;
import org.apache.geode.test.junit.rules.FolderRule;
import org.apache.geode.test.junit.rules.gfsh.GfshExecution;
import org.apache.geode.test.junit.rules.gfsh.GfshExecutor;
import org.apache.geode.test.junit.rules.gfsh.GfshRule;
import org.apache.geode.test.junit.rules.gfsh.GfshScript;
import org.apache.geode.test.version.TestVersion;
import org.apache.geode.test.version.VersionManager;

@Category(GfshTest.class)
@RunWith(Parameterized.class)
public class ConnectCommandUpgradeTest {

  private static final String HOSTNAME = "localhost";

  private final String oldVersion;

  private GfshExecutor oldGfsh;
  private GfshExecutor gfsh;

  @Parameters(name = "Locator Version: {0}")
  public static Collection<String> data() {
    return VersionManager.getInstance().getVersionsWithoutCurrent();
  }

  public ConnectCommandUpgradeTest(String oldVersion) {
    this.oldVersion = oldVersion;
  }

  @Rule(order = 0)
  public FolderRule folderRule = new FolderRule();
  @Rule(order = 1)
  public GfshRule gfshRule = new GfshRule();

  @Before
  public void setUp() {
    oldGfsh = gfshRule.executor()
        .withGeodeVersion(oldVersion)
        .build(folderRule.getFolder().toPath());
    gfsh = gfshRule.executor()
        .build(folderRule.getFolder().toPath());
  }

  @Test
  public void useCurrentGfshToConnectToOlderLocator()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    int[] ports = getRandomAvailableTCPPorts(3);
    int httpPort = ports[0];
    int locatorPort = ports[1];
    int jmxPort = ports[2];
    GfshScript
        .of(startLocatorCommand("test", locatorPort, jmxPort, httpPort, 0))
        .execute(oldGfsh);

    // New version gfsh could not connect to locators with version below 1.10.0
    if (TestVersion.compare(oldVersion, "1.10.0") < 0) {
      GfshExecution connect = GfshScript
          .of("connect --locator=localhost[" + locatorPort + "]")
          .expectFailure()
          .execute(gfsh);

      assertThat(connect.getOutputText())
          .contains("Cannot use a")
          .contains("gfsh client to connect to")
          .contains("cluster.");

    }

    // From 1.10.0 new version gfsh are able to connect to old version locators
    else {
      GfshExecution connect = GfshScript
          .of("connect --locator=localhost[" + locatorPort + "]")
          .expectExitCode(0)
          .execute(gfsh);

      assertThat(connect.getOutputText())
          .contains("Successfully connected to:");
    }
  }

  @Test
  public void invalidHostname()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    int[] ports = getRandomAvailableTCPPorts(3);
    int httpPort = ports[0];
    int locatorPort = ports[1];
    int jmxPort = ports[2];
    GfshScript
        .of(startLocatorCommand("test", locatorPort, jmxPort, httpPort, 0))
        .execute(oldGfsh);

    GfshExecution connect = GfshScript
        .of("connect --locator=\"invalid host name[52326]\"")
        .expectFailure()
        .execute(gfsh);

    assertThat(connect.getOutputText())
        .doesNotContain("UnknownHostException")
        .doesNotContain("nodename nor servname")
        .contains("can't be reached. Hostname or IP address could not be found.");
  }

  private static String startLocatorCommand(String name, int port, int jmxPort, int httpPort,
      int connectedLocatorPort) {
    String startLocatorCommand =
        "start locator --name=%s --port=%d --http-service-port=%d --J=-Dgemfire.jmx-manager-port=%d";
    if (connectedLocatorPort > 0) {
      return String.format(startLocatorCommand + " --locators=%s[%d]",
          name, port, httpPort, jmxPort, HOSTNAME, connectedLocatorPort);
    }
    return String.format(startLocatorCommand, name, port, httpPort, jmxPort);
  }
}
