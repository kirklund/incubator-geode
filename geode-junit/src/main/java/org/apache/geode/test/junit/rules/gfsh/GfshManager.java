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
package org.apache.geode.test.junit.rules.gfsh;

import static java.util.Collections.synchronizedList;
import static org.apache.geode.internal.lang.ReflectionUtils.readField;
import static org.apache.geode.internal.lang.ReflectionUtils.writeField;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import org.apache.geode.test.junit.rules.Folder;
import org.apache.geode.test.junit.rules.gfsh.ManagedGfshExecutor.Builder;

public class GfshManager implements TestRule {

  private final List<ManagedGfshExecutor> gfshExecutors = synchronizedList(new ArrayList<>());
  private final List<Throwable> errors = synchronizedList(new ArrayList<>());
  private final Object target;

  private Folder folder;

  public GfshManager(Object target) {
    this.target = target;
  }

  @Override
  public Statement apply(Statement base, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        setUp(description, target);
        try {
          base.evaluate();
        } finally {
          tearDown();
        }
      }
    };
  }

  private void setUp(Description description, Object target) throws IOException {
    folder = createFolder(description);
    processAnnotations(target);
  }

  private void tearDown() {

  }

  private static Folder createFolder(Description description) throws IOException {
    String className = description.getTestClass().getSimpleName();
    String methodName = description.getMethodName();
    return new Folder(Paths.get(className, methodName));
  }

  private void processAnnotations(final Object target) {
    try {
      Class<?> clazz = target.getClass();

      Field[] fields = clazz.getDeclaredFields();
      for (Field field : fields) {
        for (Annotation annotation : field.getAnnotations()) {

          if (annotation.annotationType().equals(GfshExecutorBuilder.class)) {
            // annotated with @GfshExecutorBuilder
            throwIfAlreadyAssigned(target, field);
            assignBuilder(target, field);
          }

        }
      }
    } catch (IllegalAccessException e) {
      throw new Error(e);
    }
  }

  private void assignBuilder(final Object target, final Field field)
      throws IllegalAccessException {
    throwIfNotSameType(GfshExecutor.Builder.class, field);

    Builder builder = new Builder(gfshExecutors::add, errors::add, folder.toPath());
    writeField(target, field, builder);
  }

  private static void throwIfNotSameType(final Class<?> clazz, final Field field) {
    if (!field.getType().equals(clazz) && // non-array
        !field.getType().getComponentType().equals(clazz)) { // array
      throw new IllegalArgumentException(
          "Field " + field.getName() + " is not same type as " + clazz.getName());
    }
  }

  private static void throwIfAlreadyAssigned(final Object target, final Field field)
      throws IllegalAccessException {
    if (readField(target, field) != null) {
      throw new IllegalStateException("Field " + field.getName() + " is already assigned");
    }
  }

  public interface GfshRunnerBuilder {

    GfshRunnerBuilder withGeodeVersion();
  }

  public @interface GfshExecutorBuilder {
  }
}
