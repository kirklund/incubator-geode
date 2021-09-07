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
package org.apache.geode.internal.serialization.filter;

import java.util.Set;


public class SerializationFilterFactory {

  public InputStreamFilter create(String pattern,
      Iterable<SanctionedSerializablesService> services,
      SanctionedSerializables sanctionedSerializables) {
    return create(pattern,
        sanctionedSerializables.loadSanctionedClassNames(services));
  }

  public InputStreamFilter create(String pattern, Set<String> sanctionedClasses) {
    ObjectInputFilterApi api = new ObjectInputFilterApiFactory().createObjectInputFilterApi();
    return new DelegatingInputStreamFilter(api, pattern, sanctionedClasses);
    // if (isJavaVersionAtLeast(JAVA_9)) {
    // return new Java9InputStreamFilter(pattern, sanctionedClasses);
    // }
    // if (isJavaVersionAtLeast(JavaVersion.JAVA_1_8)) {
    // return new Java8InputStreamFilter(pattern, sanctionedClasses);
    // }
    // throw new UnsupportedOperationException("ObjectInputFilter is not supported in JRE version");
  }
}
