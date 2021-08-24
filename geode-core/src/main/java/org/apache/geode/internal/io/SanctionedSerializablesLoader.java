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
import java.net.URL;
import java.util.Collection;

import org.apache.geode.internal.InternalDataSerializer;
import org.apache.geode.internal.classloader.ClassPathLoader;

public class SanctionedSerializablesLoader {

  private SanctionedSerializablesLoader() {
    // do not instantiate
  }

  public static Collection<String> loadClassNames(Class<?> classInSamePackage, String resourceName)
      throws IOException {
    URL sanctionedSerializables = ClassPathLoader.getLatest()
        .getResource(classInSamePackage, resourceName);
    return InternalDataSerializer.loadClassNames(sanctionedSerializables);
  }
}
