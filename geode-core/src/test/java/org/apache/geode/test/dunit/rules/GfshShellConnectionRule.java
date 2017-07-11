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
package org.apache.geode.test.dunit.rules;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.geode.management.internal.cli.i18n.CliStrings.CONNECT;
import static org.apache.geode.management.internal.cli.i18n.CliStrings.CONNECT__JMX_MANAGER;
import static org.apache.geode.management.internal.cli.i18n.CliStrings.CONNECT__LOCATOR;
import static org.apache.geode.management.internal.cli.i18n.CliStrings.CONNECT__PASSWORD;
import static org.apache.geode.management.internal.cli.i18n.CliStrings.CONNECT__URL;
import static org.apache.geode.management.internal.cli.i18n.CliStrings.CONNECT__USERNAME;
import static org.apache.geode.management.internal.cli.i18n.CliStrings.CONNECT__USE_HTTP;
import static org.apache.geode.test.dunit.IgnoredException.addIgnoredException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.json.JSONArray;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.Description;

import org.apache.geode.management.cli.Result;
import org.apache.geode.management.internal.cli.HeadlessGfsh;
import org.apache.geode.management.internal.cli.json.GfJsonException;
import org.apache.geode.management.internal.cli.result.CommandResult;
import org.apache.geode.management.internal.cli.util.CommandStringBuilder;
import org.apache.geode.test.dunit.IgnoredException;
import org.apache.geode.test.junit.rules.DescribedExternalResource;

/**
 * JUnit Rule which eases the connection to the locator/jmxManager in Gfsh shell and execute Gfsh
 * commands.
 *
 * <p>
 * If used with {@link ConnectionConfiguration}, you will need to specify a port number when
 * constructing this rule. It will then auto connect for you before running your test.
 *
 * <p>
 * Alternatively, you can call connect with the specific port number yourself in your test. This
 * rule handles closing your connection and Gfsh instance.
 *
 * <p>
 * Usage as {@literal @}Rule or {@literal @}ClassRule:
 *
 * <p>
 * {@literal @}Rule GfshShellConnectionRule rule = new GfshShellConnectionRule();
 *
 * <p>
 * After you connect to a locator, you don't have to call disconnect() or close(), since the rule's
 * tear down takes care of that for you.
 *
 * <p>
 * {@literal @}ClassRule GfshShellConnectionRule rule = new GfshShellConnectionRule();
 *
 * <p>
 * If you call connect in a test when using it as a ClassRule, you will need to explicitly call
 * disconnect after the test. See NetstatDUnitTest for example.
 */
public class GfshShellConnectionRule extends DescribedExternalResource {

  private static final int HEADLESS_GFSH_TIMEOUT_SECONDS = 30;

  private final Supplier<Integer> portSupplier;
  private final PortType portType;
  private final Set<IgnoredException> ignoredExceptions = new HashSet<>();
  private final TemporaryFolder temporaryFolder;

  private volatile HeadlessGfsh gfsh;
  private volatile boolean connected;

  public GfshShellConnectionRule() {
    this(new TemporaryFolder(), null, PortType.JMX_MANGER);
  }

  public GfshShellConnectionRule(Supplier<Integer> portSupplier, PortType portType) {
    this(new TemporaryFolder(), portSupplier, portType);
  }

  private GfshShellConnectionRule(TemporaryFolder temporaryFolder, Supplier<Integer> portSupplier,
      PortType portType) {
    this.temporaryFolder = temporaryFolder;
    this.portSupplier = portSupplier;
    this.portType = portType;

    try {
      temporaryFolder.create();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void before(Description description) throws Throwable {
    this.gfsh = new HeadlessGfsh(getClass().getName(), HEADLESS_GFSH_TIMEOUT_SECONDS,
        temporaryFolder.newFolder("gfsh_files").getAbsolutePath());

    ignoredExceptions
        .add(addIgnoredException("java.rmi.NoSuchObjectException: no such object in table"));

    // do not auto connect if no port initialized
    if (portSupplier == null) {
      return;
    }

    // do not auto connect if it's not used with ConnectionConfiguration
    ConnectionConfiguration config = description.getAnnotation(ConnectionConfiguration.class);
    if (config == null) {
      return;
    }

    connect(portSupplier.get(), portType, CONNECT__USERNAME, config.user(), CONNECT__PASSWORD,
        config.password());
  }

  @Override
  protected void after(Description description) throws Throwable {
    close();

    for (IgnoredException ignoredException : ignoredExceptions) {
      ignoredException.remove();
    }
    ignoredExceptions.clear();
  }

  public void connect(Member locator, String... options)
      throws IOException, ClassNotFoundException, InterruptedException, GfJsonException {
    connect(locator.getPort(), PortType.LOCATOR, options);
  }

  public void connectAndVerify(Member locator, String... options)
      throws IOException, ClassNotFoundException, InterruptedException, GfJsonException {
    connect(locator.getPort(), PortType.LOCATOR, options);
    assertThat(this.connected).isTrue();
  }

  public void connectAndVerify(int port, PortType type, String... options)
      throws IOException, ClassNotFoundException, InterruptedException, GfJsonException {
    connect(port, type, options);
    assertThat(this.connected).isTrue();
  }

  public void secureConnect(int port, PortType type, String username, String password)
      throws IOException, ClassNotFoundException, InterruptedException, GfJsonException {
    connect(port, type, CONNECT__USERNAME, username, CONNECT__PASSWORD, password);
  }

  public void secureConnectAndVerify(int port, PortType type, String username, String password)
      throws IOException, ClassNotFoundException, InterruptedException, GfJsonException {
    connect(port, type, CONNECT__USERNAME, username, CONNECT__PASSWORD, password);
    assertThat(this.connected).isTrue();
  }

  public void connect(int port, PortType type, String... options)
      throws IOException, ClassNotFoundException, InterruptedException, GfJsonException {
    if (gfsh == null) {
      this.gfsh = new HeadlessGfsh(getClass().getName(), HEADLESS_GFSH_TIMEOUT_SECONDS,
          temporaryFolder.newFolder("gfsh_files").getAbsolutePath());
    }

    CommandStringBuilder connectCommand = new CommandStringBuilder(CONNECT);

    String endpoint = "localhost[" + port + "]";
    switch (type) {
      case LOCATOR:
        // port is the locator port
        connectCommand.addOption(CONNECT__LOCATOR, endpoint);
        break;
      case HTTP:
        endpoint = "http://localhost:" + port + "/gemfire/v1";
        connectCommand.addOption(CONNECT__USE_HTTP, Boolean.TRUE.toString());
        connectCommand.addOption(CONNECT__URL, endpoint);
        break;
      default:
        connectCommand.addOption(CONNECT__JMX_MANAGER, endpoint);
        break;
    }

    // add the extra options
    if (options != null) {
      for (int i = 0; i < options.length; i += 2) {
        connectCommand.addOption(options[i], options[i + 1]);
      }
    }

    // Connecting too soon may result in "Failed to retrieve RMIServer stub:
    // javax.naming.CommunicationException [Root exception is java.rmi.NoSuchObjectException: no
    // such object in table]" Exception.

    // throws ConditionTimeoutException if not connected within timeout
    await().atMost(2, MINUTES).until(() -> connect(connectCommand));
  }

  private boolean connect(CommandStringBuilder connectCommand) {
    CommandResult result;
    try {
      result = executeCommand(connectCommand.toString());
    } catch (InterruptedException | GfJsonException e) {
      throw new RuntimeException(e);
    }
    connected = !gfsh.outputString.contains("no such object in table")
        && result.getStatus() == Result.Status.OK;
    return connected;
  }

  public void disconnect() throws GfJsonException, InterruptedException {
    gfsh.clear();
    executeCommand("disconnect");
    connected = false;
  }

  public void close() throws InterruptedException, GfJsonException {
    temporaryFolder.delete();
    if (connected) {
      disconnect();
    }
    gfsh.executeCommand("exit");
    gfsh.terminate();
    gfsh = null;
  }

  public HeadlessGfsh getGfsh() {
    return gfsh;
  }

  public CommandResult executeCommand(String command) throws InterruptedException, GfJsonException {
    gfsh.executeCommand(command);
    CommandResult result = (CommandResult) gfsh.getResult();
    if (isBlank(gfsh.outputString) && result != null && result.getContent() != null) {
      if (result.getStatus() == Result.Status.ERROR) {
        gfsh.outputString = result.toString();
      } else {
        // print out the message body as the command result
        JSONArray messages = (JSONArray) result.getContent().get("message");
        if (messages != null) {
          for (int i = 0; i < messages.length(); i++) {
            gfsh.outputString += messages.getString(i) + "\n";
          }
        }
      }
    }
    System.out.println("Command result for <" + command + ">: \n" + gfsh.outputString);
    return result;
  }

  public String getGfshOutput() {
    return gfsh.outputString;
  }

  public CommandResult executeAndVerifyCommand(String command)
      throws GfJsonException, InterruptedException {
    CommandResult result = executeCommand(command);
    assertThat(result.getStatus()).isEqualTo(Result.Status.OK);
    return result;
  }

  public String execute(String command) throws GfJsonException, InterruptedException {
    executeCommand(command);
    return gfsh.outputString;
  }

  public boolean isConnected() {
    return connected;
  }

  public enum PortType {
    LOCATOR, JMX_MANGER, HTTP
  }
}
