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

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.gemstone.gemfire.internal.StatArchiveReader.ResourceInst;
import com.gemstone.gemfire.internal.StatArchiveReader.StatSpec;
import com.gemstone.gemfire.internal.StatArchiveReader.StatValue;

public class StatArchiveInspector {

  private final Properties config;
  private final StatArchiveReader reader;
  
  public StatArchiveInspector(Properties config, File[] archiveNames) throws IOException {
    if (config == null) {
      throw new NullPointerException("Properties config is required");
    }
    this.reader = new StatArchiveReader(archiveNames, null, true);
    this.config = config;
  }
  
  public String inspect() {
    StringBuilder sb = new StringBuilder();
    StatSpec jvmPauseSpec = new StatSpec() {
      @Override
      public boolean archiveMatches(File archive) {
        return true;
      }
      @Override
      public boolean typeMatches(String typeName) {
        return typeName.equals("StatSampler");
      }
      @Override
      public boolean statMatches(String statName) {
        return statName.equals("jvmPauses");
      }

      @Override
      public boolean instanceMatches(String textId, long numericId) {
        return true;
      }
      @Override
      public int getCombineType() {
        return NONE;
      }
    };
    for (StatValue jvmPauseValue: this.reader.matchSpec(jvmPauseSpec)) {
      if (jvmPauseValue.getSnapshotsMaximum() > 0.0) {
        sb.append("jvmPause detected in: ").append(getArchives(jvmPauseValue)).append(System.lineSeparator());
      }
    }
    try {
      this.reader.close();
    } catch (IOException e) {
      // ignore IOExceptions on close
    }
    return sb.toString();
  }
  
  private Set<File> getArchives(StatValue jvmPauseValue) {
    HashSet<File> result = new HashSet<>();
    for (ResourceInst resource: jvmPauseValue.getResources()) {
      result.add(resource.getArchive().getFile());
    }
    return result;
  }

  public static void main(String[] args) throws IOException {
    File[] files = new File[args.length];
    int i = 0;
    for (String arg: args) {
      files[i] = new File(arg);
      i++;
    }
    StatArchiveInspector inspector = new StatArchiveInspector(new Properties(), files);
    System.out.println(inspector.inspect());
  }

}
