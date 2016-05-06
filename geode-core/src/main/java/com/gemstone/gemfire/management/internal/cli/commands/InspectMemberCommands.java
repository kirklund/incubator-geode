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
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;

import com.gemstone.gemfire.SystemFailure;
import com.gemstone.gemfire.cache.execute.FunctionInvocationTargetException;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.gemstone.gemfire.cache.operations.OperationContext;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.internal.lang.ClassUtils;
import com.gemstone.gemfire.management.cli.CliMetaData;
import com.gemstone.gemfire.management.cli.ConverterHint;
import com.gemstone.gemfire.management.cli.Result;
import com.gemstone.gemfire.management.internal.cli.CliUtil;
import com.gemstone.gemfire.management.internal.cli.domain.DiskStoreDetails;
import com.gemstone.gemfire.management.internal.cli.domain.MemberInspectionDetails;
import com.gemstone.gemfire.management.internal.cli.functions.DescribeDiskStoreFunction;
import com.gemstone.gemfire.management.internal.cli.functions.InspectMemberFunction;
import com.gemstone.gemfire.management.internal.cli.i18n.CliStrings;
import com.gemstone.gemfire.management.internal.cli.result.CompositeResultData;
import com.gemstone.gemfire.management.internal.cli.result.ResultBuilder;
import com.gemstone.gemfire.management.internal.cli.result.TabularResultData;
import com.gemstone.gemfire.management.internal.cli.util.DiskStoreNotFoundException;
import com.gemstone.gemfire.management.internal.cli.util.MemberNotFoundException;
import com.gemstone.gemfire.management.internal.inspector.Inspector;
import com.gemstone.gemfire.management.internal.security.ResourceOperation;

public class InspectMemberCommands extends AbstractCommandsSupport {

  @CliCommand(value= CliStrings.INSPECT_OFFLINE_MEMBER, help=CliStrings.INSPECT_OFFLINE_MEMBER__HELP)
  @CliMetaData(shellOnly=true, relatedTopic={CliStrings.TOPIC_GEMFIRE_MEMBER})
  public Result inspectOfflineMember(
          @CliOption(key=CliStrings.INSPECT_OFFLINE_MEMBER__STATFILES,
                  mandatory=true,
                  help=CliStrings.INSPECT_OFFLINE_MEMBER__STATFILES__HELP)
                  @CliMetaData (valueSeparator = ",")
                  final String[] statFiles,
          @CliOption (key=CliStrings.INSPECT_OFFLINE_MEMBER__STATDIRS,
                  mandatory=false,
                  help=CliStrings.INSPECT_OFFLINE_MEMBER__STATDIRS__HELP)
                  @CliMetaData (valueSeparator = ",")
                  final String[] statDirs) {

    try {
//      final File[] dirs = new File[diskDirs.length];
//      for (int i = 0; i < diskDirs.length; i++) {
//        dirs[i] = new File((diskDirs[i]));
//      }

//      if (Region.SEPARATOR.equals(regionName)) {
//        return ResultBuilder.createUserErrorResult(CliStrings.INVALID_REGION_NAME);
//      }

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      PrintStream printStream = new PrintStream(outputStream);

      String[] logFiles = null;

      inspectOfflineMember(printStream, statFiles, statDirs, logFiles);

      return ResultBuilder.createInfoResult(outputStream.toString());

    } catch (VirtualMachineError e) {
      SystemFailure.initiateFailure(e);
      throw e;

    } catch (Throwable t) {
      SystemFailure.checkFailure();
      if (t.getMessage() == null) {
        return ResultBuilder.createGemFireErrorResult("An error occurred while inspecting offline member: " + t);
      }
      return ResultBuilder.createGemFireErrorResult("An error occurred while inspecting offline member: " + t.getMessage());
    }
  }

  @CliCommand(value = CliStrings.INSPECT_MEMBER, help = CliStrings.INSPECT_MEMBER__HELP)
  @CliMetaData(shellOnly = false, relatedTopic = { CliStrings.TOPIC_GEMFIRE_MEMBER })
  @ResourceOperation(resource = OperationContext.Resource.CLUSTER, operation = OperationContext.OperationCode.READ)
  public Result inspectMember(
          @CliOption(key = CliStrings.DESCRIBE_DISK_STORE__MEMBER,
                  mandatory = true,
                  optionContext = ConverterHint.MEMBERIDNAME,
                  help = CliStrings.DESCRIBE_DISK_STORE__MEMBER__HELP)
                  final String memberName) {
    try {
      return toCompositeResult(getMemberInspectionDetails(memberName));

    } catch (MemberNotFoundException e) {
      return ResultBuilder.createShellClientErrorResult(e.getMessage());

    } catch (FunctionInvocationTargetException ignore) {
      return ResultBuilder.createGemFireErrorResult(CliStrings.format(CliStrings.COULD_NOT_EXECUTE_COMMAND_TRY_AGAIN, CliStrings.INSPECT_MEMBER));

    } catch (VirtualMachineError e) {
      SystemFailure.initiateFailure(e);
      throw e;

    } catch (Throwable t) {
      SystemFailure.checkFailure();
      return ResultBuilder.createGemFireErrorResult(String.format(CliStrings.INSPECT_MEMBER__MSG__INFO_FOR__0__COULD_NOT_BE_RETRIEVED, memberName, toString(t, isDebugging())));
    }
  }

  @CliAvailabilityIndicator({CliStrings.INSPECT_OFFLINE_MEMBER})
  public boolean offlineInspectMemberCommandsAvailable() {
    return true;
  }

  @CliAvailabilityIndicator({CliStrings.INSPECT_MEMBER})
  public boolean inspectMemberCommandsAvailable() {
    return (!CliUtil.isGfshVM() || (getGfsh() != null && getGfsh().isConnectedAndReady()));
  }

  private void inspectOfflineMember(PrintStream printStream, String[] statFiles, String[] statDirs, String[] logFiles) {
    // TODO: inspect the stat files

    Set<File> allStatFiles = new TreeSet<>();

    if (statFiles != null && statFiles.length > 0) {
      if (!"null".equals(statFiles[0])) {
        for (String statFile : statFiles) {
          allStatFiles.add(new File(statFile));
        }
      }
    }

    if (statDirs != null && statDirs.length > 0) {
      for (String statDir : statDirs) {
        File dir = new File(statDir);
        File[] files = dir.listFiles(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.endsWith(".gfs");
          }
        });
        for (File file : files) {
          allStatFiles.add(file);
        }
      }
    }

    if (allStatFiles.isEmpty()) {
      // TODO: error?
    }

    Inspector inspector = new Inspector(allStatFiles.toArray(new File[allStatFiles.size()]), new File[] {});

    printInspectOfflineMemberResults(printStream, statFiles, inspector);
  }

  private void printInspectOfflineMemberResults(PrintStream printStream, String[] statFiles, Inspector inspector) {
    // TODO: print real results of inspection

    boolean comma = false;
    StringBuilder sb = new StringBuilder();
    for (String statFile : statFiles) {
      if (comma) {
        sb.append(",");
      }
      sb.append(statFile);
      comma = true;
    }

    printStream.println("Inspecting member with stat files: " + sb.toString());
    printStream.println(inspector.inspect());
  }

  private MemberInspectionDetails getMemberInspectionDetails(final String memberName) {
    final DistributedMember member = getMember(getCache(), memberName); // may throw a MemberNotFoundException

    final ResultCollector<?, ?> resultCollector = getMembersFunctionExecutor(Collections.singleton(member))
            .execute(new InspectMemberFunction());

    final Object result = ((List<?>) resultCollector.getResult()).get(0);

    if (result instanceof MemberInspectionDetails) { // member inspection details in hand...
      return (MemberInspectionDetails) result;

    } else { // unknown and unexpected return type...
      final Throwable cause = (result instanceof Throwable ? (Throwable) result : null);

      if (isLogging()) {
        if (cause != null) {
          getGfsh().logSevere(
                  String.format(
                          "Exception (%1$s) occurred while executing '%2$s' on member (%3$s).",
                          ClassUtils.getClassName(cause),
                          CliStrings.DESCRIBE_DISK_STORE,
                          memberName),
                  cause);
        }
        else {
          getGfsh().logSevere(
                  String.format(
                          "Received an unexpected result of type (%1$s) while executing '%2$s' on member (%3$s).",
                          ClassUtils.getClassName(result),
                          CliStrings.DESCRIBE_DISK_STORE,
                          memberName),
                  null);
        }
      }

      throw new RuntimeException(
                  CliStrings.format(
                          CliStrings.UNEXPECTED_RETURN_TYPE_EXECUTING_COMMAND_ERROR_MESSAGE,
                          ClassUtils.getClassName(result),
                          CliStrings.DESCRIBE_DISK_STORE),
                  cause);
    }
  }

  protected Result toCompositeResult(final MemberInspectionDetails memberInspectionDetails) {
    final CompositeResultData memberInspectionData = ResultBuilder.createCompositeResultData();

    final CompositeResultData.SectionResultData memberInspectionSection = memberInspectionData.addSection();

    memberInspectionSection.addData("Member ID", memberInspectionDetails.getMemberId());
    memberInspectionSection.addData("Member Name", memberInspectionDetails.getMemberName());

    final TabularResultData statFileTable = memberInspectionData.addSection().addTable();

    for (MemberInspectionDetails.StatFileDetails statFileDetails : memberInspectionDetails) {
      statFileTable.accumulate("Stat File", statFileDetails.getAbsolutePath());
      statFileTable.accumulate("Size", statFileDetails.getSize());
    }

    return ResultBuilder.buildResult(memberInspectionData);
  }

}
