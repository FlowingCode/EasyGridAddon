/*-
 * #%L
 * Easy Grid Add-on
 * %%
 * Copyright (C) 2020 - 2026 Flowing Code
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.flowingcode.vaadin.addons.easygrid.test;

import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import com.flowingcode.vaadin.addons.easygrid.config.ColumnConfiguration;
import java.lang.reflect.Method;
import java.util.Collection;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;

/**
 * Verifies that every getter on {@link ColumnConfigurationImpl} delegates to the parent when its
 * local field is {@code null}, and that no non-getter method (setters, etc.) calls into the parent
 * at all.
 *
 * <p>Methods are discovered reflectively from {@link ColumnConfiguration}. Getters (names starting
 * with {@code "get"}) are verified to call through to the parent; all other public methods are
 * verified to call only getter methods on the parent (never setters).
 */
public class ColumnConfigurationImplParentDelegationTest extends DelegationTest {

  public ColumnConfigurationImplParentDelegationTest(String name, Method method) {
    super(method);
  }

  @Parameters(name = "{0}")
  public static Collection<Object[]> parameters() {
    return parameters(ColumnConfiguration.class, m -> true);
  }

  @Override
  protected ColumnConfiguration<?> createDelegate() throws ClassNotFoundException {
    return (ColumnConfiguration<?>) mock(EasyColumnTestHelper.columnConfigurationImplClass());
  }

  @Override
  protected ColumnConfiguration<?> createTarget(Object delegate)
      throws ReflectiveOperationException {
    return EasyColumnTestHelper.newColumnConfigurationImpl((ColumnConfiguration<?>) delegate);
  }

  @Override
  protected void assertDelegated(Object parent, String methodName, Object[] args)
      throws ReflectiveOperationException {
    if (method.getName().startsWith("get")) {
      method.invoke(Mockito.verify(parent));
    } else {
      verifyNoInteractions(parent);
    }
  }

  @Override
  protected void verifyResult(Object target, Object result, String methodName) {
    if (methodName.equals("createNewLayer")) {
      assertNotSame(methodName + " must return a new instance", target, result);
    } else {
      super.verifyResult(target, result, methodName);
    }
  }

}
