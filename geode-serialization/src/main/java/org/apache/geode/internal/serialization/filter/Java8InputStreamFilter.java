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

import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;

import org.apache.logging.log4j.Logger;

import org.apache.geode.logging.internal.log4j.api.LogService;

public class Java8InputStreamFilter implements InputStreamFilter {
  private static final Logger logger = LogService.getLogger();

  private Object serializationFilter;

  // ObjectInputFilter$Config
  private Class<?> configClass;
  // method on ObjectInputFilter$Config or ObjectInputStream
  private Method setObjectInputFilterMethod;
  // method on ObjectInputFilter$Config
  private Method createFilterMethod;

  // ObjectInputFilter
  private Class<?> filterClass;
  // field on ObjectInputFilter
  private Object ALLOWED;
  // field on ObjectInputFilter
  private Object REJECTED;
  // method on ObjectInputFilter
  private Method checkInputMethod;

  // ObjectInputFilter$FilterInfo
  private Class<?> filterInfoClass;
  // method on ObjectInputFilter$FilterInfo
  private Method serialClassMethod;

  public Java8InputStreamFilter(String serializationFilterSpec,
      Collection<String> sanctionedClasses) {
    createJava8Filter(serializationFilterSpec, sanctionedClasses);
  }

  @Override
  public void setFilterOn(ObjectInputStream objectInputStream) {
    try {
      setObjectInputFilterMethod.invoke(configClass, objectInputStream, serializationFilter);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new UnsupportedOperationException("Unable to filter serialization", e);
    }
  }

  /**
   * java8 has sun.misc.ObjectInputFilter and uses ObjectInputFilter$Config.setObjectInputFilter()
   */
  private void createJava8Filter(String serializationFilterSpec,
      Collection<String> sanctionedClasses) {
    try {
      filterInfoClass = Class.forName("sun.misc.ObjectInputFilter$FilterInfo");
      serialClassMethod = filterInfoClass.getDeclaredMethod("serialClass");

      filterClass = Class.forName("sun.misc.ObjectInputFilter");
      checkInputMethod = filterClass.getDeclaredMethod("checkInput", filterInfoClass);

      Class<?> statusClass = Class.forName("sun.misc.ObjectInputFilter$Status");
      ALLOWED = statusClass.getEnumConstants()[1];
      REJECTED = statusClass.getEnumConstants()[2];
      if (!ALLOWED.toString().equals("ALLOWED") || !REJECTED.toString().equals("REJECTED")) {
        throw new UnsupportedOperationException(
            "ObjectInputFilter$Status enumeration in this JDK is not as expected");
      }

      configClass = Class.forName("sun.misc.ObjectInputFilter$Config");
      setObjectInputFilterMethod = configClass.getDeclaredMethod("setObjectInputFilter",
          ObjectInputStream.class, filterClass);
      createFilterMethod = configClass.getDeclaredMethod("createFilter", String.class);

      serializationFilter = createSerializationFilter(serializationFilterSpec, sanctionedClasses);
    } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException
        | NoSuchMethodException e) {
      throw new UnsupportedOperationException(
          "A serialization filter has been specified but Geode was unable to configure a filter",
          e);
    }
  }

  private Object createSerializationFilter(String serializationFilterSpec,
      Collection<String> sanctionedClasses)
      throws InvocationTargetException, IllegalAccessException {
    /*
     * create a user filter with the serialization acceptlist/denylist. This will be wrapped
     * by a filter that accept-lists sanctioned classes
     */
    Object userFilter = createFilterMethod.invoke(null, serializationFilterSpec);

    InvocationHandler handler = (proxy, method, args) -> {
      if ("checkInput".equals(method.getName())) {
        Object filterInfo = args[0];
        Class<?> serialClass = (Class<?>) serialClassMethod.invoke(filterInfo);
        if (serialClass == null) { // no class to check, so nothing to accept-list
          return checkInputMethod.invoke(userFilter, filterInfo);
        }
        String className = serialClass.getName();
        if (serialClass.isArray()) {
          className = serialClass.getComponentType().getName();
        }
        if (sanctionedClasses.contains(className)) {
          return ALLOWED;
        }
        Object status = checkInputMethod.invoke(userFilter, filterInfo);
        if (status == REJECTED) {
          logger.fatal("Serialization filter is rejecting class {}", className,
              new InvalidClassException(className));
        }
        return status;
      }
      throw new UnsupportedOperationException(
          "ObjectInputFilter." + method.getName() + " is not implemented");
    };

    return Proxy.newProxyInstance(filterClass.getClassLoader(), new Class[] {filterClass},
        handler);
  }
}
