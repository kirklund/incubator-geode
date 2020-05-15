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
package org.apache.geode.management;

import static java.lang.management.ManagementFactory.getPlatformMBeanServer;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.apache.logging.log4j.Logger;

import org.apache.geode.distributed.DistributedMember;
import org.apache.geode.logging.internal.log4j.api.LogService;
import org.apache.geode.management.internal.ManagementConstants;
import org.apache.geode.management.internal.SystemManagementService;
import org.apache.geode.test.dunit.Wait;
import org.apache.geode.test.dunit.WaitCriterion;

/**
 * Utility test class to get various proxies
 */
public class MBeanUtil {
  private static final Logger logger = LogService.getLogger();

  private static final int MAX_WAIT = 8 * ManagementConstants.REFRESH_TIME;
  private static final MBeanServer mbeanServer = getPlatformMBeanServer();

  /**
   * Utility Method to obtain CacheServerMXBean proxy reference for a particular Member
   *
   * @return a reference to CacheServerMXBean
   */
  static CacheServerMXBean getCacheServerMbeanProxy(DistributedMember member, int port) {
    final SystemManagementService service =
        (SystemManagementService) ManagementTestBase.getManagementService();
    final ObjectName cacheServerMBeanName = service.getCacheServerMBeanName(port, member);

    Wait.waitForCriterion(new WaitCriterion() {

      private CacheServerMXBean bean;

      @Override
      public String description() {
        return "Waiting for the proxy to get reflected at managing node";
      }

      @Override
      public boolean done() {
        bean = service.getMBeanProxy(cacheServerMBeanName, CacheServerMXBean.class);
        return bean != null;
      }

    }, MAX_WAIT, 500, true);

    CacheServerMXBean bean = null;
    try {
      bean = service.getMBeanProxy(cacheServerMBeanName, CacheServerMXBean.class);
    } catch (ManagementException mgz) {
      logger.debug("Undesired Result :CacheServer Proxy Should Not be Empty for : {}",
          cacheServerMBeanName.getCanonicalName());
    }
    return bean;
  }

  /**
   * Utility Method to obtain GatewaySenderMXBean proxy reference for a particular sender id on a
   * member
   *
   * @param member distributed member
   * @param gatewaySenderId sender id
   * @return a reference to GatewaySenderMXBean
   */
  public static GatewaySenderMXBean getGatewaySenderMbeanProxy(DistributedMember member,
      String gatewaySenderId) {
    final SystemManagementService service =
        (SystemManagementService) ManagementTestBase.getManagementService();
    final ObjectName senderMBeanName = service.getGatewaySenderMBeanName(member, gatewaySenderId);

    Wait.waitForCriterion(new WaitCriterion() {

      private GatewaySenderMXBean bean;

      @Override
      public String description() {
        return "Waiting for the proxy to get reflected at managing node";
      }

      @Override
      public boolean done() {
        bean = service.getMBeanProxy(senderMBeanName, GatewaySenderMXBean.class);
        return bean != null;
      }

    }, MAX_WAIT, 500, true);

    GatewaySenderMXBean bean = null;
    try {
      bean = service.getMBeanProxy(senderMBeanName, GatewaySenderMXBean.class);
    } catch (ManagementException mgz) {
      logger.debug("Undesired Result :GatewaySender MBean Proxy Should Not be Empty for : {}",
          senderMBeanName.getCanonicalName());
    }
    return bean;
  }

  /**
   * Utility Method to obtain AsyncEventQueueMXBean proxy reference for a particular queue id on a
   * member
   *
   * @param member distributed member
   * @param queueId Queue id
   * @return a reference to AsyncEventQueueMXBean
   */
  public static AsyncEventQueueMXBean getAsyncEventQueueMBeanProxy(DistributedMember member,
      String queueId) {
    final SystemManagementService service =
        (SystemManagementService) ManagementTestBase.getManagementService();
    final ObjectName queueMBeanName = service.getAsyncEventQueueMBeanName(member, queueId);

    Wait.waitForCriterion(new WaitCriterion() {

      private AsyncEventQueueMXBean bean;

      @Override
      public String description() {
        return "Waiting for the proxy to get reflected at managing node";
      }

      @Override
      public boolean done() {
        bean = service.getMBeanProxy(queueMBeanName, AsyncEventQueueMXBean.class);
        return bean != null;
      }

    }, MAX_WAIT, 500, true);

    AsyncEventQueueMXBean bean = null;
    try {
      bean = service.getMBeanProxy(queueMBeanName, AsyncEventQueueMXBean.class);
    } catch (ManagementException mgz) {
      logger.debug("Undesired Result :Async Event Queue MBean Proxy Should Not be Empty for : {}",
          queueMBeanName.getCanonicalName());
    }
    return bean;
  }

  /**
   * Utility Method to obtain GatewayReceiverMXBean proxy reference for a member
   *
   * @param member distributed member
   * @return a reference to GatewayReceiverMXBean
   */
  public static GatewayReceiverMXBean getGatewayReceiverMbeanProxy(DistributedMember member) {
    final SystemManagementService service =
        (SystemManagementService) ManagementTestBase.getManagementService();
    final ObjectName receiverMBeanName = service.getGatewayReceiverMBeanName(member);

    Wait.waitForCriterion(new WaitCriterion() {

      private GatewayReceiverMXBean bean;

      @Override
      public String description() {
        return "Waiting for the proxy to get reflected at managing node";
      }

      @Override
      public boolean done() {
        bean = service.getMBeanProxy(receiverMBeanName, GatewayReceiverMXBean.class);
        return bean != null;
      }

    }, MAX_WAIT, 500, true);

    GatewayReceiverMXBean bean = null;
    try {
      bean = service.getMBeanProxy(receiverMBeanName, GatewayReceiverMXBean.class);
    } catch (ManagementException mgz) {
      logger.debug("Undesired Result :GatewaySender MBean Proxy Should Not be Empty for : {}",
          receiverMBeanName.getCanonicalName());
    }
    return bean;
  }


  public static void printBeanDetails(ObjectName objectName) {
    MBeanInfo info;
    try {
      info = mbeanServer.getMBeanInfo(objectName);
    } catch (IntrospectionException | ReflectionException | InstanceNotFoundException e) {
      throw new AssertionError("Could not obtain Sender Proxy Details", e);
    }

    MBeanAttributeInfo[] attributeInfos = info.getAttributes();
    for (MBeanAttributeInfo attributeInfo : attributeInfos) {
      try {
        String propertyName = attributeInfo.getName();
        Object propertyValue = mbeanServer.getAttribute(objectName, propertyName);
        logger.info("<ExpectedString>{} = {}</ExpectedString> ", propertyName, propertyValue);
      } catch (Exception e) {
        // ignored
      }
    }
  }
}
