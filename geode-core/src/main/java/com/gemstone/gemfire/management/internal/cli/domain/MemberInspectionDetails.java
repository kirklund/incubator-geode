/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gemstone.gemfire.management.internal.cli.domain;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.gemstone.gemfire.internal.lang.ObjectUtils;

public class MemberInspectionDetails implements Comparable<MemberInspectionDetails>, Iterable<MemberInspectionDetails.StatFileDetails>, Serializable {

  private final String memberId;
  private final String memberName;

  private Set<StatFileDetails> statFileDetailsSet = new TreeSet<>();

  public MemberInspectionDetails(final String memberId) {
    this(memberId, null);
  }

  public MemberInspectionDetails(final String memberId, final String memberName) {
    assertNotNull(memberId, "The id of the member to inspect cannot be null!");
    this.memberId = memberId;
    this.memberName = memberName;
  }

  public String getMemberId() {
    return memberId;
  }

  public String getMemberName() {
    return memberName;
  }

  public boolean add(final StatFileDetails statFileDetails) {
    assertNotNull(statFileDetails, "Stat file details for (%1$s) cannot be null!", this.memberId);
    return statFileDetailsSet.add(statFileDetails);
  }

  @Override
  public Iterator<StatFileDetails> iterator() {
    return Collections.unmodifiableSet(this.statFileDetailsSet).iterator();
  }

  @Override
  public int compareTo(final MemberInspectionDetails memberInspectionDetails) {
    int comparisonValue = compare(getMemberName(), memberInspectionDetails.getMemberName());
    comparisonValue = (comparisonValue != 0 ? comparisonValue : compare(getMemberId(), memberInspectionDetails.getMemberId()));
    return (comparisonValue != 0 ? comparisonValue : getMemberName().compareTo(memberInspectionDetails.getMemberName()));
  }

  private static <T extends Comparable<T>> int compare(final T obj1, final T obj2) {
    return (obj1 == null && obj2 == null ? 0 : (obj1 == null ? 1 : (obj2 == null ? -1 : obj1.compareTo(obj2))));
  }

  private static void assertNotNull(final Object obj, final String message, final Object... args) {
    if (obj == null) {
      throw new NullPointerException(String.format(message, args));
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MemberInspectionDetails that = (MemberInspectionDetails) o;

    return memberId != null ? memberId.equals(that.memberId) : that.memberId == null;
  }

  @Override
  public int hashCode() {
    return memberId != null ? memberId.hashCode() : 0;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(getClass().getSimpleName());
    sb.append(" {memberId = ").append(this.memberId);
    sb.append(", memberName = ").append(this.memberName);
    sb.append(", statFileDetailsSet = ").append(this.statFileDetailsSet);
    sb.append("}");
    return sb.toString();
  }

  public static class StatFileDetails implements Comparable<MemberInspectionDetails.StatFileDetails>, Serializable {

    private final String absolutePath;

    private final long size;

    public StatFileDetails(final String absolutePath) {
      this(absolutePath, 0);
    }

    public StatFileDetails(final String absolutePath, final long size) {
      assertNotNull(absolutePath, "The absolute path of the stat file cannot be null!");
      this.absolutePath = absolutePath;
      this.size = size;
    }

    public String getAbsolutePath() {
      return this.absolutePath;
    }

    public long getSize() {
      return this.size;
    }

    public int compareTo(final MemberInspectionDetails.StatFileDetails statFileDetails) {
      return getAbsolutePath().compareTo(statFileDetails.getAbsolutePath());
    }

    @Override
    public boolean equals(final Object obj) {
      if (obj == this) {
        return true;
      }

      if (!(obj instanceof StatFileDetails)) {
        return false;
      }

      final StatFileDetails that = (StatFileDetails) obj;

      return ObjectUtils.equals(getAbsolutePath(), that.getAbsolutePath());
    }

    @Override
    public int hashCode() {
      int hashValue = 17;
      hashValue = 37 * hashValue + ObjectUtils.hashCode(getAbsolutePath());
      return hashValue;
    }

    @Override
    public String toString() {
      final StringBuilder buffer = new StringBuilder(getClass().getSimpleName());
      buffer.append(" {absolutePath = ").append(getAbsolutePath());
      buffer.append(", size = ").append(getSize());
      buffer.append("}");
      return buffer.toString();
    }
  }

}
