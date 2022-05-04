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
package org.apache.geode.management;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized.UseParametersRunnerFactory;

import org.apache.geode.internal.UniquePortSupplier;
import org.apache.geode.test.compiler.ClassBuilder;
import org.apache.geode.test.junit.categories.BackwardCompatibilityTest;
import org.apache.geode.test.junit.rules.FolderRule;
import org.apache.geode.test.junit.rules.gfsh.GfshExecution;
import org.apache.geode.test.junit.rules.gfsh.GfshExecutor;
import org.apache.geode.test.junit.rules.gfsh.GfshRule;
import org.apache.geode.test.junit.rules.gfsh.GfshScript;
import org.apache.geode.test.junit.runners.CategoryWithParameterizedRunnerFactory;
import org.apache.geode.test.version.TestVersion;
import org.apache.geode.test.version.VersionManager;

/**
 * This test iterates through the versions of Geode and executes client compatibility with
 * the current version of Geode.
 */
@Category(BackwardCompatibilityTest.class)
@RunWith(Parameterized.class)
@UseParametersRunnerFactory(CategoryWithParameterizedRunnerFactory.class)
public class RollingUpgradeWithGfshDUnitTest {

  private static final String HOSTNAME = "localhost";

  private final UniquePortSupplier portSupplier = new UniquePortSupplier();

  private final String oldVersion;

  @Parameters(name = "{0}")
  public static Collection<String> data() {
    List<String> result = VersionManager.getInstance().getVersionsWithoutCurrent();
    result.removeIf(s -> TestVersion.compare(s, "1.10.0") < 0);
    return result;
  }

  private GfshExecutor currentGfsh;
  private GfshExecutor oldGfsh;

  @Rule(order = 0)
  public FolderRule folderRule = new FolderRule();
  @Rule(order = 1)
  public GfshRule gfshRule = new GfshRule();

  public RollingUpgradeWithGfshDUnitTest(String version) {
    oldVersion = version;
  }

  @Before
  public void setUp() {
    currentGfsh = gfshRule.executor()
        .build(folderRule.getFolder().toPath());
    oldGfsh = gfshRule.executor()
        .withGeodeVersion(oldVersion)
        .build(folderRule.getFolder().toPath());
  }

  @Test
  public void testRollingUpgradeWithDeployment()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    int locatorPort = portSupplier.getAvailablePort();
    int locatorJmxPort = portSupplier.getAvailablePort();
    int locator2Port = portSupplier.getAvailablePort();
    int locator2JmxPort = portSupplier.getAvailablePort();
    int server1Port = portSupplier.getAvailablePort();
    int server2Port = portSupplier.getAvailablePort();

    GfshExecution startupExecution = GfshScript
        .of(startLocatorCommand("loc1", locatorPort, locatorJmxPort, 0, -1))
        .and(startLocatorCommand("loc2", locator2Port, locator2JmxPort, 0, locatorPort))
        .and(startServerCommand("server1", server1Port, locatorPort))
        .and(startServerCommand("server2", server2Port, locatorPort))
        .and(deployDirCommand())
        .execute(oldGfsh);

    // doing rolling upgrades
    startupExecution.stopLocator(oldGfsh, "loc1");
    GfshScript
        .of(startLocatorCommand("loc1", locatorPort, locatorJmxPort, 0, locator2Port))
        .execute(currentGfsh);
    verifyListDeployed(locatorPort);

    startupExecution.stopLocator(oldGfsh, "loc2");
    GfshScript
        .of(startLocatorCommand("loc2", locator2Port, locator2JmxPort, 0, locatorPort))
        .execute(currentGfsh);
    verifyListDeployed(locator2Port);

    // make sure servers can do rolling upgrade too
    startupExecution.stopServer(oldGfsh, "server1");
    GfshScript
        .of(startServerCommand("server1", server1Port, locatorPort))
        .execute(currentGfsh);

    startupExecution.stopServer(oldGfsh, "server2");
    GfshScript
        .of(startServerCommand("server2", server2Port, locatorPort))
        .execute(currentGfsh);
  }

  private void verifyListDeployed(int locatorPort)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    GfshExecution list_deployed = GfshScript
        .of("connect --locator=" + HOSTNAME + "[" + locatorPort + "]")
        .and("list deployed")
        .execute(currentGfsh);
    assertThat(list_deployed.getOutputText())
        .contains("DeployCommandsDUnit1.jar")
        .contains("server1")
        .contains("server2");
    currentGfsh.execute("disconnect");
  }

  private String deployDirCommand() throws IOException {
    ClassBuilder classBuilder = new ClassBuilder();
    File jarsDir = folderRule.getFolder().toPath().toFile();
    String jarName1 = "DeployCommandsDUnit1.jar";
    File jar1 = new File(jarsDir, jarName1);
    String class1 = "DeployCommandsDUnitA";
    classBuilder.writeJarFromName(class1, jar1);
    return "deploy --dir=" + jarsDir.getAbsolutePath();
  }

  private static String startServerCommand(String name, int port, int connectedLocatorPort) {
    return String.format("start server --name=%s --server-port=%d --locators=%s[%d]",
        name, port, HOSTNAME, connectedLocatorPort);
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
