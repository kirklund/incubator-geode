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
package com.gemstone.gemfire.management.internal.cli.functions;

import java.io.File;
import java.util.Properties;

import org.apache.logging.log4j.Logger;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.execute.FunctionAdapter;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.internal.InternalEntity;
import com.gemstone.gemfire.internal.cache.InternalCache;
import com.gemstone.gemfire.internal.logging.LogService;
import com.gemstone.gemfire.internal.util.ArrayUtils;
import com.gemstone.gemfire.management.internal.cli.domain.MemberInspectionDetails;

public class InspectMemberFunction extends FunctionAdapter implements InternalEntity {

  private static final Logger logger = LogService.getLogger();

  protected Cache getCache() {
    return CacheFactory.getAnyInstance();
  }

  public String getId() {
    return getClass().getName();
  }

  @SuppressWarnings("unused")
  public void init(final Properties props) {
  }

  public void execute(final FunctionContext context) {
    Cache cache = getCache();

    try {
      if (cache instanceof InternalCache) {
        InternalCache gemfireCache = (InternalCache) cache;

        DistributedMember member = gemfireCache.getMyId();

        //String diskStoreName = (String) context.getArguments();
        String memberId = member.getId();
        String memberName = member.getName();

        // StatFile[] statFiles =

        MemberInspectionDetails memberInspectionDetails = new MemberInspectionDetails(memberId, memberName);

        // for each statFile
        // add statFile to memberInspectionDetails
        //setStatFileDetails(diskStore, memberInspectionDetails);

          context.getResultSender().lastResult(memberInspectionDetails);
//        }
//        else {
//          context.getResultSender().sendException(new DiskStoreNotFoundException(String.format(
//                  "A disk store with name (%1$s) was not found on member (%2$s).",
//                  diskStoreName, memberName)));
//        }
      }
    }
    catch (Exception e) {
      logger.error("Error occurred while executing 'inspect member': {}!", e.getMessage(), e);
      context.getResultSender().sendException(e);
    }
  }

  private void addStatFileDetailsForFile(final File file, final MemberInspectionDetails memberInspectionDetails) {
    memberInspectionDetails.add(new MemberInspectionDetails.StatFileDetails(file.getAbsolutePath(), file.length()));
  }
}
