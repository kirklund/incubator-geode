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

import org.apache.geode.internal.serialization.filter.impl.DelegatingObjectInputFilterFactory;
import org.apache.geode.internal.serialization.filter.impl.SanctionedSerializablesFilterPattern;

public class ImplObjectInputFilterFactory implements ObjectInputFilterFactory {

  private final Runnable precondition;

  public ImplObjectInputFilterFactory(Runnable precondition) {
    this.precondition = precondition;
  }

  @Override
  public ObjectInputFilter create(SerializableObjectConfig config, Set<String> sanctionedClasses) {
    if (config.getValidateSerializableObjects()) {
      precondition.run();
      String filterPattern = new SanctionedSerializablesFilterPattern()
          .append(config.getSerializableObjectFilter())
          .pattern();
      return new DelegatingObjectInputFilterFactory()
          .create(filterPattern, sanctionedClasses);
    }
    return new EmptyObjectInputFilter();
  }
}
