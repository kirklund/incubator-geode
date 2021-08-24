/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geode.distributed.internal;

import static org.apache.geode.distributed.internal.CoreDistributedSystemService.CORE_SERIALIZABLES_FILE_NAME;
import static org.apache.geode.distributed.internal.CoreDistributedSystemService.MANAGEMENT_SERIALIZABLES_FILE_NAME;
import static org.apache.geode.internal.io.SanctionedSerializablesLoader.loadClassNames;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Collection;

import org.junit.Test;

import org.apache.geode.internal.InternalDataSerializer;

public class CoreDistributedSystemServiceIntegrationTest {

  @Test
  public void loadsCoreSanctionedSerializables() throws IOException {
    Collection<String> coreSerializablesClassNames =
        loadClassNames(InternalDataSerializer.class, CORE_SERIALIZABLES_FILE_NAME);
    DistributedSystemService service = new CoreDistributedSystemService();

    Collection<String> classNames = service.getSerializationAcceptList();

    assertThat(classNames).containsAll(coreSerializablesClassNames);
  }

  @Test
  public void loadsManagementSanctionedSerializables() throws IOException {
    Collection<String> coreSerializablesClassNames =
        loadClassNames(InternalDataSerializer.class, MANAGEMENT_SERIALIZABLES_FILE_NAME);
    DistributedSystemService service = new CoreDistributedSystemService();

    Collection<String> classNames = service.getSerializationAcceptList();

    assertThat(classNames).containsAll(coreSerializablesClassNames);
  }
}
