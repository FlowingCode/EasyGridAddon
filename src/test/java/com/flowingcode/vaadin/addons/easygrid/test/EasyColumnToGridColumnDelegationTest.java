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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import com.flowingcode.vaadin.addons.easygrid.EasyColumn;
import com.flowingcode.vaadin.addons.easygrid.config.ColumnConfiguration;
import com.vaadin.flow.component.grid.Grid;
import java.lang.reflect.Method;
import java.util.Collection;
import org.junit.runners.Parameterized.Parameters;

/**
 * Verifies that every chainable setter on {@link Grid.Column} is exposed and delegated by
 * {@link EasyColumn}.
 *
 * <p>Methods are discovered reflectively from {@link Grid.Column}: every public method whose name
 * starts with {@code "set"} and is declared on {@code Grid.Column} or an interface is tested. If
 * {@link EasyColumn} does not expose a corresponding method, the test fails with a clear message,
 * making coverage gaps immediately visible.
 *
 * <p>Delegation is verified via Mockito: the delegate is a mock {@link Grid.Column}, and after
 * invoking the corresponding {@link EasyColumn} setter, {@link org.mockito.Mockito#verify verify}
 * asserts that the setter was called on the mock with the expected arguments.
 *
 * @see DelegationTest
 */
public class EasyColumnToGridColumnDelegationTest extends DelegationTest {

  public EasyColumnToGridColumnDelegationTest(String name, Method setter) {
    super(setter);
  }

  @Parameters(name = "{0}")
  public static Collection<Object[]> parameters() {
    return parameters(Grid.Column.class, m -> {
      switch (m.getName()) {
        case "setClassName":
        case "setClassNameGenerator":
        case "setRenderer":
          return false;
        default:
          return (m.getDeclaringClass() == Grid.Column.class
              || m.getDeclaringClass().isInterface()) && m.getName().startsWith("set");
      }
    });
  }

  @Override
  protected Grid.Column<?> createDelegate() throws ReflectiveOperationException {
    return mock(Grid.Column.class);
  }

  @Override
  protected EasyColumn<?, ?> createTarget(Object delegate) throws ReflectiveOperationException {
    return EasyColumnTestHelper.newEasyColumn(
        EasyColumnTestHelper.mockColumnConfigurationImpl(), (Grid.Column<?>) delegate);
  }

  @Override
  protected void verifyMethod(Method targetMethod) {
    assertEquals(EasyColumn.class, targetMethod.getReturnType());
  }

  @Override
  protected void assertDelegated(Object delegate, String methodName, Object[] args)
      throws ReflectiveOperationException {
    method.invoke(verify(delegate), args);
  }

}
