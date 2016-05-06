/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gemstone.gemfire.internal;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import com.gemstone.gemfire.test.junit.categories.IntegrationTest;

@Category(IntegrationTest.class)
public class StatArchiveInspectorIntegrationTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }
  
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private File copyResource(String resourceName) throws FileNotFoundException, IOException {
    URL resource = getClass().getResource(resourceName);
    File targetFolder = this.temporaryFolder.getRoot();
    File targetFile = new File(targetFolder, resourceName);
    IOUtils.copy(resource.openStream(), new FileOutputStream(targetFile));
    return targetFile;
  }
  @Test
  public void testJvmPauseDetection() throws FileNotFoundException, IOException {
    File targetFile = copyResource("jvmPauses.gfs");
    File targetFile2 = copyResource("jvmPauses2.gfs");
    File targetFile3 = copyResource("noJvmPause.gfs");
    
    StatArchiveInspector inspector = new StatArchiveInspector(new Properties(), new File[]{targetFile, targetFile2});
    String result = inspector.inspect();
    System.out.println(result);
    assertThat(result).containsPattern("jvmPause detected.*"+targetFile.getAbsolutePath());
    assertThat(result).containsPattern("jvmPause detected.*"+targetFile2.getAbsolutePath());
    assertThat(result).doesNotMatch("jvmPause detected.*"+targetFile3.getAbsolutePath());
  }

}
