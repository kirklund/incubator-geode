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
package org.apache.geode.internal.lang;

import java.lang.reflect.Field;

public class ReflectionUtils {

  private ReflectionUtils() {
    // do not instantiate
  }

  public static Object readField(final Class<?> targetClass, final Object targetInstance, final String fieldName)
      throws IllegalAccessException, NoSuchFieldException {
    Field field = targetClass.getDeclaredField(fieldName);
    return readField(targetInstance, field);
  }

  public static Object readField(final Object targetInstance, final Field field)
      throws IllegalAccessException {
    field.setAccessible(true);
    return field.get(targetInstance);
  }

  public static void writeField(final Class<?> targetClass, final Object targetInstance, final String fieldName, final Object value)
      throws IllegalAccessException, NoSuchFieldException {
    Field field = targetClass.getDeclaredField(fieldName);
    writeField(targetInstance, field, value);
  }

  public static void writeField(final Object targetInstance, final Field field, final Object value)
      throws IllegalAccessException {
    field.setAccessible(true);
    field.set(targetInstance, value);
  }
}
