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
package org.apache.geode.test.junit.rules.gfsh;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.apache.geode.internal.process.ProcessType.LOCATOR;
import static org.apache.geode.internal.process.ProcessType.SERVER;
import static org.apache.geode.test.awaitility.GeodeAwaitility.getTimeout;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.geode.internal.process.ProcessType;
import org.apache.geode.test.junit.rules.gfsh.internal.ProcessLogger;

public class GfshExecution {

  private static final String SCRIPT_TIMEOUT_FAILURE_MESSAGE =
      "Process started by [%s] did not exit after %s %s";
  private static final String SCRIPT_EXIT_VALUE_DESCRIPTION =
      "Exit value from process started by [%s]";

  private static final Predicate<File> IS_RUNNING_SERVER_DIRECTORY =
      directory -> stream(directory.list())
          .anyMatch(filename -> filename.endsWith("server.pid"));

  private static final Predicate<File> IS_SERVER_DIRECTORY = directory -> stream(directory.list())
      .anyMatch(filename -> filename.endsWith("server.pid") || filename.contains("server.status"));

  private static final Predicate<File> IS_RUNNING_LOCATOR_DIRECTORY =
      directory -> stream(directory.list())
          .anyMatch(filename -> filename.endsWith("locator.pid"));

  private static final Predicate<File> IS_LOCATOR_DIRECTORY = directory -> stream(directory.list())
      .anyMatch(
          filename -> filename.endsWith("locator.pid") || filename.contains("locator.status"));

  private final Process process;
  private final File workingDir;
  private final ProcessLogger processLogger;

  protected GfshExecution(Process process, File workingDir) {
    this.process = process;
    this.workingDir = workingDir.getAbsoluteFile();

    processLogger = new ProcessLogger(process, workingDir.getName());
    processLogger.start();
  }

  public String getOutputText() {
    return processLogger.getOutputText();
  }

  /*
   * Note that this is the working directory of gfsh itself. If your script started a server or
   * locator, this will be the parent directory of that member's working directory.
   */
  public File getWorkingDir() {
    return workingDir;
  }

  public Process getProcess() {
    return process;
  }

  public List<Path> getServerDirs() {
    File[] subDirs = workingDir.listFiles(File::isDirectory);

    Predicate<File> isServerDir = dir -> stream(requireNonNull(dir.list()))
        .anyMatch(filename -> filename.endsWith("server.pid"));

    return Arrays.stream(requireNonNull(subDirs)).filter(isServerDir)
        .map(dir -> dir.toPath().toAbsolutePath())
        .collect(toList());
  }

  public List<Path> getLocatorDirs() {
    File[] subDirs = workingDir.listFiles(File::isDirectory);

    Predicate<File> isLocatorDir = dir -> Arrays.stream(requireNonNull(dir.list()))
        .anyMatch(filename -> filename.endsWith("locator.pid"));

    return Arrays.stream(requireNonNull(subDirs)).filter(isLocatorDir)
        .map(dir -> dir.toPath().toAbsolutePath())
        .collect(toList());
  }

  private void printLogFiles() {
    System.out.println(
        "Printing contents of all log files found in " + workingDir.getAbsolutePath());
    List<File> logFiles = findLogFiles();

    for (File logFile : logFiles) {
      System.out.println("Contents of " + logFile.getAbsolutePath());
      try (BufferedReader br =
          new BufferedReader(new InputStreamReader(new FileInputStream(logFile)))) {
        String line;
        while ((line = br.readLine()) != null) {
          System.out.println(line);
        }
      } catch (IOException e) {
        System.out.println("Unable to print log due to: " + getStackTrace(e));
      }
    }
  }

  private List<File> findLogFiles() {
    List<Path> servers = getServerDirs();
    List<Path> locators = getLocatorDirs();

    return concat(servers.stream(), locators.stream())
        .flatMap(this::findLogFiles)
        .collect(toList());
  }

  private Stream<File> findLogFiles(Path memberDir) {
    return stream(requireNonNull(memberDir.toFile().listFiles()))
        .filter(File::isFile)
        .filter(file -> file.getName().toLowerCase().endsWith(".log"));
  }

  void awaitTermination(GfshScript script)
      throws InterruptedException, TimeoutException, ExecutionException {
    boolean exited = process.waitFor(script.getTimeout(), script.getTimeoutTimeUnit());

    try {
      assertThat(exited)
          .withFailMessage(SCRIPT_TIMEOUT_FAILURE_MESSAGE, script, script.getTimeout(),
              script.getTimeoutTimeUnit())
          .isTrue();
      assertThat(process.exitValue())
          .as(SCRIPT_EXIT_VALUE_DESCRIPTION, script)
          .isEqualTo(script.getExpectedExitValue());
    } catch (AssertionError error) {
      printLogFiles();
      throw error;
    } finally {
      processLogger.awaitTermination(script.getTimeout(), script.getTimeoutTimeUnit());
      processLogger.close();
    }
  }

  /**
   * this only kills the process of "gfsh -e command", it does not kill the child processes started
   * by this command.
   */
  void killProcess() throws InterruptedException {
    process.destroyForcibly();
    if (process.isAlive()) {
      // process may not terminate immediately after destroyForcibly
      boolean exited = process.waitFor(getTimeout().toMinutes(), MINUTES);
      if (!exited) {
        throw new IllegalStateException(
            "Failed to destroy Gfsh process started in " + workingDir.getName());
      }
    }
  }

  public Path getSubDir(String subDirName) {
    return getWorkingDir().toPath().resolve(subDirName);
  }

  /*
   * this will stop the server that's been started in this gfsh execution
   */
  public void stopServer(GfshExecutor executor, String serverName) {
    String command = getStopProcessCommand(SERVER, getSubDir(serverName));
    try {
      executor.execute(GfshScript.of(command).withName("Stop-server-" + serverName));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /*
   * this will stop the locator that's been started in this gfsh execution
   */
  public void stopLocator(GfshExecutor executor, String locatorName) {
    String command = getStopProcessCommand(LOCATOR, getSubDir(locatorName));
    try {
      executor.execute(GfshScript.of(command).withName("Stop-locator-" + locatorName));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String getStopProcessCommand(ProcessType processType, Path directory) {
    StringBuilder sb = new StringBuilder("stop ");
    if (processType == SERVER) {
      sb.append("server ");
    } else {
      sb.append("locator ");
    }
    sb.append("--dir=");
    sb.append(directory.toFile());
    return sb.toString();
  }
}
