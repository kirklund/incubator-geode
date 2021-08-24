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
package org.apache.geode.internal.io;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import org.apache.geode.InternalGemFireException;
import org.apache.geode.annotations.VisibleForTesting;
import org.apache.geode.distributed.internal.DistributedSystemService;
import org.apache.geode.logging.internal.log4j.api.LogService;

public class SanctionedSerializables {
  private static final Logger logger = LogService.getLogger();

  @NotNull
  @VisibleForTesting
  public Set<String> loadSanctionedClassNames(Iterable<DistributedSystemService> services) {
    Set<String> sanctionedClasses = new HashSet<>(650);
    for (DistributedSystemService service : services) {
      try {
        Collection<String> classNames = service.getSerializationAcceptList();
        logger.info("loaded {} sanctioned serializables from {}", classNames.size(),
            service.getClass().getSimpleName());
        sanctionedClasses.addAll(classNames);
      } catch (IOException e) {
        throw new InternalGemFireException(
            "Unable to initialize serialization filter for " + service,
            e);
      }
    }
    return sanctionedClasses;
  }
}
