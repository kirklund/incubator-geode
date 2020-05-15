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
package org.apache.geode.management.internal;

import static java.lang.management.ManagementFactory.getPlatformMBeanServer;
import static java.util.Collections.unmodifiableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.geode.annotations.VisibleForTesting;
import org.apache.geode.distributed.internal.InternalDistributedSystem;
import org.apache.geode.management.ManagementException;

/**
 * This class acts as a central point hub for collecting all notifications originated from VM and
 * sending across to Managing Node
 */
public class NotificationHub {

  /**
   * This is a single window to manipulate region resources for management
   */
  protected ManagementResourceRepo repo;

  /**
   * Platform MBean Server
   */
  private final MBeanServer mbeanServer = getPlatformMBeanServer();

  private final Map<ObjectName, NotificationHubListener> listenerObjectMap;

  /**
   * Member Name
   */
  private final String memberSource;

  /**
   * public constructor
   *
   * @param repo Resource repo for this member
   */
  public NotificationHub(ManagementResourceRepo repo) {
    this.repo = repo;
    listenerObjectMap = new HashMap<>();
    memberSource = MBeanJMXAdapter
        .getMemberNameOrUniqueId(
            InternalDistributedSystem.getConnectedInstance().getDistributedMember());
  }

  /**
   * Adds a NotificationHubListener
   */
  public void addHubNotificationListener(String memberName, ObjectName objectName) {
    try {
      synchronized (listenerObjectMap) {
        NotificationHubListener listener = listenerObjectMap.get(objectName);
        if (listener == null) {
          listener = new NotificationHubListener(objectName);
          listener.incNumCounter();
          mbeanServer.addNotificationListener(objectName, listener, null, null);
          listenerObjectMap.put(objectName, listener);
        } else {
          listener.incNumCounter();
        }
      }

    } catch (InstanceNotFoundException e) {
      throw new ManagementException(e);
    }
  }

  /**
   * Removes a NotificationHubListener
   */
  public void removeHubNotificationListener(String memberName, ObjectName objectName) {
    try {
      synchronized (listenerObjectMap) {
        if (listenerObjectMap.get(objectName) != null) {
          NotificationHubListener listener = listenerObjectMap.get(objectName);
          if (listener.decNumCounter() == 0) {
            listenerObjectMap.remove(objectName);
            // The MBean might have been un registered if the resource is
            // removed from cache.
            // The below method is to ensure clean up of user defined MBeans
            mbeanServer.removeNotificationListener(objectName, listener);
          }
        }
      }
    } catch (ListenerNotFoundException | InstanceNotFoundException e) {
      // No op
    }
  }

  /**
   * This method is basically to cleanup resources which might cause leaks if the same VM is used
   * again for cache creation.
   */
  public void cleanUpListeners() {
    synchronized (listenerObjectMap) {
      for (ObjectName objectName : listenerObjectMap.keySet()) {

        NotificationHubListener listener = listenerObjectMap.get(objectName);

        if (listener != null) {
          try {
            mbeanServer.removeNotificationListener(objectName, listener);
          } catch (ListenerNotFoundException | InstanceNotFoundException e) {
            // Do nothing. Already have been un-registered ( For listeners which
            // are on other MBeans apart from MemberMXBean)
          }
        }
      }
    }

    listenerObjectMap.clear();
  }

  @VisibleForTesting
  public Map<ObjectName, NotificationHubListener> getListenerObjectMap() {
    return unmodifiableMap(listenerObjectMap);
  }

  /**
   * This class is the managed node counterpart to listen to notifications from MBeans for which it
   * is registered
   */
  public class NotificationHubListener implements NotificationListener {

    /**
     * MBean for which this listener is added
     */
    private final ObjectName name;

    /**
     * Counter to indicate how many listener are attached to this MBean
     */
    private final AtomicInteger numCounter = new AtomicInteger();

    protected NotificationHubListener(ObjectName name) {
      this.name = name;
    }

    public int incNumCounter() {
      return numCounter.incrementAndGet();
    }

    public int decNumCounter() {
      return numCounter.decrementAndGet();
    }

    public int getNumCounter() {
      return numCounter.get();
    }

    @Override
    public void handleNotification(Notification notification, Object handback) {
      NotificationKey key = new NotificationKey(name);
      notification.setUserData(memberSource);
      repo.putEntryInLocalNotificationRegion(key, notification);
    }
  }
}
