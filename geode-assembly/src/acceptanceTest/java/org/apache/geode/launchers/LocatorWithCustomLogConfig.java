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

package org.apache.geode.launchers;

import static org.apache.geode.test.junit.rules.serializable.SerializableTemporaryFolder.When.ALWAYS;
import static org.apache.geode.test.util.ResourceUtils.createFileFromResource;
import static org.apache.geode.test.util.ResourceUtils.getResource;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.apache.geode.test.assertj.LogFileAssert;
import org.apache.geode.test.junit.rules.serializable.SerializableTemporaryFolder;

public class LocatorWithCustomLogConfig {
  private static final String CONFIG_FILE_NAME = "LocatorWithCustomLogConfig.xml";
  private static final String LOCATOR_NAME = "the-locator";
  @Rule
  public TemporaryFolder temporaryFolder = new SerializableTemporaryFolder()
      .copyTo(Paths.get("temporary-folder-copy").toFile())
      .when(ALWAYS);
  private String configFilePath;
  private Process locator;

  @Before
  public void setUpLogConfigFile() {
    URL resource = getResource(CONFIG_FILE_NAME);
    configFilePath = createFileFromResource(resource, temporaryFolder.getRoot(), CONFIG_FILE_NAME)
        .getAbsolutePath();
  }

  @After
  public void stopLocator() throws InterruptedException {
    if(locator != null) {
      locator.destroyForcibly().waitFor(4, TimeUnit.SECONDS);
    }
  }

  @Test
  public void foo() throws IOException, InterruptedException {
    File myProcessOutputFile = Paths.get("my-process-output.txt").toFile();
    locator = new ProcessBuilder()
        .redirectErrorStream(true)
        .redirectOutput(myProcessOutputFile)
        .directory(temporaryFolder.getRoot())
        .command("java",
            "-cp", "/Users/demery/workspace/geode/geode-assembly/build/install/apache-geode/lib/geode-dependencies.jar",
            "-Dlog4j.configurationFile=" + configFilePath,
            "-Dgemfire.jmx-manager-start=true",
            "-Djava.awt.headless=true",
            "org.apache.geode.distributed.LocatorLauncher",
            "start",
            LOCATOR_NAME
        ).start();

    assertThat(locator.isAlive()).isTrue();

    Thread.sleep(10_000);
//    locator.destroyForcibly().waitFor(4, TimeUnit.SECONDS);
//    assertThat(locator.getInputStream()).hasContent("monkey");

    File logFile = temporaryFolder.getRoot().toPath().resolve(LOCATOR_NAME + ".log").toFile();
    LogFileAssert.assertThat(logFile).exists();
  }

  /**
   * java \
   *   -cp $CLASSPATH \
   *   ${log4j_config_file_jvm_arg} \
   *   ${logfile_jvm_arg} \
   *   -Djava.awt.headless=true \
   *   -Dgemfire.locators=localhost[10334] \
   *   -Dgemfire.jmx-manager-start=true \
   *   org.apache.geode.distributed.LocatorLauncher start \
   *   geodecluster-sample-locator-0 --port=10334
   */

  private static String textFrom(InputStream stream) throws IOException {
    Reader reader = new InputStreamReader(stream);
    StringBuilder text = new StringBuilder();
    while (reader.ready()) {
      int character = reader.read();
      if (character == -1) {
        break;
      }
      text.append((char) character);
    }
    return text.toString();
  }
}
