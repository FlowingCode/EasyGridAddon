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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import com.flowingcode.vaadin.addons.easygrid.EasyColumn;
import com.flowingcode.vaadin.addons.easygrid.config.ColumnConfiguration;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.SerializableBiFunction;
import java.lang.reflect.Method;
import java.util.Collection;
import org.junit.runners.Parameterized.Parameters;

/**
 * Verifies that every chainable setter on {@link ColumnConfiguration} is exposed and delegated by
 * {@link EasyColumn}.
 *
 * <p>Methods are discovered reflectively from {@link ColumnConfiguration}: every public method
 * whose name starts with {@code "set"} and whose return type is {@code IColumnConfiguration} is
 * tested. If {@link EasyColumn} does not expose a corresponding method, the test fails with a
 * clear message, making coverage gaps immediately visible.
 *
 * <p>Delegation is verified via Mockito: the delegate is a mock, and after invoking the
 * corresponding {@link EasyColumn} setter, {@link org.mockito.Mockito#verify verify} asserts that
 * the setter was called on the mock with the expected arguments.
 *
 * @see DelegationTest
 */
public class EasyColumnToConfigurationDelegationTest extends DelegationTest {

  private Grid.Column<?> columnMock;

  public EasyColumnToConfigurationDelegationTest(String name, Method setter) {
    super(setter);
  }

  @Parameters(name = "{0}")
  public static Collection<Object[]> parameters() {
    return parameters(ColumnConfiguration.class, m -> {
      switch (m.getName()) {
        case "setRendererFactory": 
          return false;
        case "setFormatter":
          return m.getParameterTypes()[0] != SerializableBiFunction.class;
        default:
          return m.getName().startsWith("set")
              && m.getReturnType() == ColumnConfiguration.class;
      }
    });
  }

  @Override
  protected ColumnConfiguration<String> createDelegate() throws ClassNotFoundException {
    return EasyColumnTestHelper.mockColumnConfigurationImpl();
  }

  @Override
  protected EasyColumn<?, ?> createTarget(Object delegate) throws ReflectiveOperationException {
    columnMock = mock(Grid.Column.class);
    return EasyColumnTestHelper.newEasyColumn((ColumnConfiguration<?>) delegate, columnMock);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void assertDelegated(Object delegate, String methodName, Object[] args)
      throws ReflectiveOperationException {
    method.invoke(verify(delegate), args);
    if (updatesRenderer(methodName)) {
      verify(columnMock).setRenderer(any(Renderer.class));
    }
  }

  private static boolean updatesRenderer(String methodName) {
    switch (methodName) {
      case "setNullRepresentation":
      case "setFormatter":
        return true;
      default:
        return false;
    }
  }

}
