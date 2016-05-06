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
package com.gemstone.gemfire.management.internal.cli.commands;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;

import com.gemstone.gemfire.SystemFailure;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.internal.cache.DiskStoreImpl;
import com.gemstone.gemfire.management.cli.CliMetaData;
import com.gemstone.gemfire.management.cli.Result;
import com.gemstone.gemfire.management.internal.cli.i18n.CliStrings;
import com.gemstone.gemfire.management.internal.cli.result.ResultBuilder;

public class InspectCommands extends AbstractCommandsSupport {

  @CliCommand(value= CliStrings.INSPECT_OFFLINE_MEMBER, help=CliStrings.INSPECT_OFFLINE_MEMBER__HELP)
  @CliMetaData(shellOnly=true, relatedTopic={CliStrings.TOPIC_GEMFIRE_MEMBER})
  public Result describeOfflineDiskStore(
          @CliOption(key=CliStrings.DESCRIBE_OFFLINE_DISK_STORE__DISKSTORENAME,
                  mandatory=true,
                  help=CliStrings.DESCRIBE_OFFLINE_DISK_STORE__DISKSTORENAME__HELP)
                  String diskStoreName,
          @CliOption (key=CliStrings.DESCRIBE_OFFLINE_DISK_STORE__DISKDIRS,
                  mandatory=true,
                  help=CliStrings.DESCRIBE_OFFLINE_DISK_STORE__DISKDIRS__HELP)
          @CliMetaData (valueSeparator = ",")
                  String[] diskDirs,
          @CliOption (key=CliStrings.DESCRIBE_OFFLINE_DISK_STORE__PDX_TYPES,
                  unspecifiedDefaultValue = CliMetaData.ANNOTATION_NULL_VALUE,
                  help=CliStrings.DESCRIBE_OFFLINE_DISK_STORE__PDX_TYPES__HELP)
                  Boolean listPdxTypes,
          @CliOption  (key=CliStrings.DESCRIBE_OFFLINE_DISK_STORE__REGIONNAME,
                  help=CliStrings.DESCRIBE_OFFLINE_DISK_STORE__REGIONNAME__HELP,
                  unspecifiedDefaultValue=CliMetaData.ANNOTATION_NULL_VALUE)
                  String regionName) {

    try {
      final File[] dirs = new File[diskDirs.length];
      for (int i = 0; i < diskDirs.length; i++) {
        dirs[i] = new File((diskDirs[i]));
      }

      if (Region.SEPARATOR.equals(regionName)) {
        return ResultBuilder.createUserErrorResult(CliStrings.INVALID_REGION_NAME);
      }

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      PrintStream printStream = new PrintStream(outputStream);

      DiskStoreImpl.dumpInfo(printStream, diskStoreName, dirs, regionName, listPdxTypes);
      return ResultBuilder.createInfoResult(outputStream.toString());
    } catch (VirtualMachineError e) {
      SystemFailure.initiateFailure(e);
      throw e;
    } catch (Throwable th) {
      SystemFailure.checkFailure();
      if (th.getMessage() == null) {
        return ResultBuilder.createGemFireErrorResult("An error occurred while describing offline disk stores: " + th);
      }
      return ResultBuilder.createGemFireErrorResult("An error occurred while describing offline disk stores: " + th.getMessage());
    }
  }
}
