/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geode.pmd;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

/**
 * PMD rule that flags usage of {@code Thread.sleep(millis)} in test code as a violation.
 *
 * <p>
 * By default, all existing uses of {@code Thread.sleep(millis)} within test code will be annotated
 * with {@code @RemoveThreadSleep}, indicating that we should remove the sleep and replace it with
 * something more precise such as {@code GeodeAwaitility.await()}, {@code AsyncInvocation.await()},
 * or {@code CountDownLatch.await()}.
 *
 * <p>
 * Test usage of {@code Thread.sleep(millis)} for purposes other than waiting on asynchronous
 * behavior, such as slowing down region operations with a CacheListener, can be overridden as
 * allowed by annotating with {@code @AllowThreadSleep}.
 */
public class AvoidThreadSleepRule extends AbstractJavaRule {

  @Override
  public Object visit(ASTPrimaryPrefix node, Object data) {
    if (isThreadSleep(node)) {
      addViolation(data, node);
    }
    return super.visit(node, data);
  }

  private boolean isThreadSleep(ASTPrimaryPrefix node) {
    Node name = node.jjtGetChild(0);
    if ("Thread.sleep".equals(name.getImage())) {
      ASTMethodDeclaration parentMethod = node.getFirstParentOfType(ASTMethodDeclaration.class);
      return !parentMethod.isAnnotationPresent("AllowThreadSleep") &&
          !parentMethod.isAnnotationPresent("RemoveThreadSleep");
    }
    return false;
  }
}
