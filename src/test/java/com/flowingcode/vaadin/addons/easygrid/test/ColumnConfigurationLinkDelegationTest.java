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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;

/**
 * Verifies that every getter on {@link ColumnConfigurationLink} checks the primary config first
 * and falls back to the fallback config, and that no non-getter method calls into the fallback.
 *
 * <p>Methods are discovered reflectively from {@link ColumnConfiguration}. Each getter is
 * exercised twice: once with the primary unstubbed (returns {@code null}), in which case the
 * link must fall back to the fallback config; and once with the primary stubbed to return a
 * non-{@code null} value, in which case the fallback must not be called. For non-getter methods,
 * no interaction with the fallback is expected.
 */
public class ColumnConfigurationLinkDelegationTest extends DelegationTest {

  private final boolean stubbed;
  private ColumnConfiguration<?> fallbackMock;

  public ColumnConfigurationLinkDelegationTest(String name, Method method, boolean stubbed) {
    super(method);
    this.stubbed = stubbed;
  }

  @Parameters(name = "{0}")
  public static Collection<Object[]> parameters() {
    List<Object[]> result = new ArrayList<>();
    for (Object[] row : parameters(ColumnConfiguration.class, m -> true)) {
      Method m = (Method) row[1];
      if (m.getName().startsWith("get")) {
        result.add(new Object[] {row[0], m, false});
        result.add(new Object[] {row[0] + " [primary stubbed]", m, true});
      } else {
        result.add(new Object[] {row[0], m, false});
      }
    }
    return result;
  }

  @Override
  protected ColumnConfiguration<?> createDelegate() throws ClassNotFoundException {
    return (ColumnConfiguration<?>) mock(EasyColumnTestHelper.columnConfigurationImplClass());
  }

  @Override
  protected ColumnConfiguration<?> createTarget(Object primary)
      throws ReflectiveOperationException {
    fallbackMock =
        (ColumnConfiguration<?>) mock(EasyColumnTestHelper.columnConfigurationImplClass());
    if (stubbed) {
      stubGetter(method, (ColumnConfiguration<?>) primary);
    }
    return EasyColumnTestHelper.newColumnConfigurationLink((ColumnConfiguration<?>) primary,
        fallbackMock);
  }

  @Override
  protected void assertDelegated(Object primary, String methodName, Object[] args)
      throws ReflectiveOperationException {
    if (method.getName().startsWith("get") && !stubbed) {
      // primary returned null, so the link fell back to fallbackMock
      method.invoke(Mockito.verify(fallbackMock));
    } else {
      // primary was stubbed non-null (getter) or setter went to primary — fallbackMock untouched
      verifyNoInteractions(fallbackMock);
    }
  }

  /**
   * Stubs {@code mock.getter()} to return a non-{@code null} value so that
   * {@link ColumnConfigurationLink}'s getter short-circuits without reaching the fallback.
   */
  @SuppressWarnings("rawtypes")
  private void stubGetter(Method getter, ColumnConfiguration<?> mock)
      throws ReflectiveOperationException {
    Class<?> returnType = getter.getReturnType();
    Object value;
    if (returnType == String.class) {
      value = "";
    } else if (returnType.isEnum()) {
      value = returnType.getEnumConstants()[0];
    } else {
      value = Mockito.mock(returnType);
    }
    ColumnConfiguration proxy = Mockito.doReturn(value).when(mock);
    getter.invoke(proxy);
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
