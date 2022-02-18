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

import static java.lang.System.identityHashCode;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Objects.requireNonNull;

import java.io.InvalidClassException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;

import org.apache.logging.log4j.Logger;

import org.apache.geode.logging.internal.log4j.api.LogService;

class ObjectInputFilterInvocationHandler implements InvocationHandler {

  private static final Logger logger = LogService.getLogger();

  private final Method ObjectInputFilter_checkInput;
  private final Method ObjectInputFilter_FilterInfo_serialClass;
  private final Object ObjectInputFilter_Status_ALLOWED;
  private final Object ObjectInputFilter_Status_REJECTED;

  private final Object objectInputFilter;
  private final Collection<String> sanctionedClasses;

  ObjectInputFilterInvocationHandler(
      Method ObjectInputFilter_checkInput,
      Method ObjectInputFilter_FilterInfo_serialClass,
      Object ObjectInputFilter_Status_ALLOWED,
      Object ObjectInputFilter_Status_REJECTED,
      Object objectInputFilter,
      Collection<String> sanctionedClasses) {
    logger.info("GEODE-10060: enter ObjectInputFilterInvocationHandler#constructor [{}]",
        identityHashCode(this));
    this.ObjectInputFilter_checkInput = ObjectInputFilter_checkInput;
    this.ObjectInputFilter_FilterInfo_serialClass = ObjectInputFilter_FilterInfo_serialClass;
    this.ObjectInputFilter_Status_ALLOWED = ObjectInputFilter_Status_ALLOWED;
    this.ObjectInputFilter_Status_REJECTED = ObjectInputFilter_Status_REJECTED;
    this.objectInputFilter = objectInputFilter;
    this.sanctionedClasses = unmodifiableCollection(sanctionedClasses);
    logger.info("GEODE-10060: exit ObjectInputFilterInvocationHandler#constructor [{}]",
        identityHashCode(this));
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args)
      throws IllegalAccessException, IllegalArgumentException,
      java.lang.reflect.InvocationTargetException {
    logger.info(
        "GEODE-10060: enter ObjectInputFilterInvocationHandler#invoke method name is {} [{}]",
        method.getName(), identityHashCode(this));
    if (!"checkInput".equals(method.getName())) {
      // delegate to the actual objectInputFilter instance for any method other than checkInput
      logger.info(
          "GEODE-10060: exit-1 ObjectInputFilterInvocationHandler#invoke method name is {} [{}]",
          method.getName(), identityHashCode(this));
      return method.invoke(objectInputFilter, args);
    }

    requireNonNull(args, "Single argument FilterInfo is null");

    if (args.length != 1) {
      throw new IllegalArgumentException("Single argument FilterInfo is required");
    }

    // fetch the class of the serialized instance
    Object objectInputFilter_filterInfo = args[0];
    Class<?> serialClass =
        (Class<?>) ObjectInputFilter_FilterInfo_serialClass.invoke(objectInputFilter_filterInfo);
    if (serialClass == null) { // no class to check, so nothing to accept-list
      logger.info(
          "GEODE-10060: exit-2 ObjectInputFilterInvocationHandler#invoke invoking ObjectInputFilter_checkInput with null serialClass [{}]",
          identityHashCode(this));
      return ObjectInputFilter_checkInput.invoke(objectInputFilter, objectInputFilter_filterInfo);
    }

    // check sanctionedClasses to determine if the name of the class is ALLOWED
    String serialClassName = serialClass.getName();
    if (serialClass.isArray()) {
      serialClassName = serialClass.getComponentType().getName();
    }
    logger.info(
        "GEODE-10060: ObjectInputFilterInvocationHandler#invoke checking sanctionedClasses collection [{}]",
        identityHashCode(this));
    if (sanctionedClasses.contains(serialClassName)) {
      logger.info(
          "GEODE-10060: exit-3 ObjectInputFilterInvocationHandler#invoke allowing class from sanctionedClasses collection [{}]",
          identityHashCode(this));
      return ObjectInputFilter_Status_ALLOWED;
    }

    // check the filter to determine if the class is ALLOWED
    logger.info(
        "GEODE-10060: ObjectInputFilterInvocationHandler#invoke invoking ObjectInputFilter_checkInput for {} [{}]",
        serialClassName, identityHashCode(this));
    Object objectInputFilter_Status =
        ObjectInputFilter_checkInput.invoke(objectInputFilter, objectInputFilter_filterInfo);
    if (objectInputFilter_Status == ObjectInputFilter_Status_REJECTED) {
      logger.fatal("Serialization filter is rejecting class {}", serialClassName,
          new InvalidClassException(serialClassName));
    }
    logger.info(
        "GEODE-10060: exit-4 ObjectInputFilterInvocationHandler#invoke JDK filter is allowing class [{}]",
        identityHashCode(this));
    return objectInputFilter_Status;
  }

  @Override
  public String toString() {
    return new StringBuilder(getClass().getSimpleName())
        .append("@")
        .append(Integer.toHexString(hashCode()))
        .append('{')
        .append("objectInputFilter=").append(objectInputFilter)
        .append(", sanctionedClassesCount=").append(sanctionedClasses.size())
        .append('}')
        .toString();
  }
}
