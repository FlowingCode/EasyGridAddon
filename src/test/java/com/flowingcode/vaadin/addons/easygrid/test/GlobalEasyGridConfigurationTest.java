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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import com.flowingcode.vaadin.addons.easygrid.config.ColumnConfiguration;
import com.flowingcode.vaadin.addons.easygrid.config.GlobalEasyGridConfiguration;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import java.lang.reflect.Method;
import org.junit.Test;

public class GlobalEasyGridConfigurationTest {

  @SuppressWarnings("unchecked")
  private static <V> ColumnConfiguration<V> resolve(Class<V> type)
      throws ReflectiveOperationException {
    Method method = GlobalEasyGridConfiguration.class.getDeclaredMethod("resolve", Class.class);
    method.setAccessible(true);
    return (ColumnConfiguration<V>) method.invoke(null, type);
  }

  @Test
  public void getOrCreateWithIntShouldNotThrow() throws ReflectiveOperationException {
    assertNotNull(resolve(int.class));
  }

  @Test
  public void getOrCreateWithBooleanShouldNotThrow() throws ReflectiveOperationException {
    assertNotNull(resolve(boolean.class));
  }

  @Test
  public void booleanPrimitiveParentIsBoolean() throws ReflectiveOperationException {
    // Boolean.class has textAlign=CENTER set in GlobalEasyGridConfiguration static initializer;
    // boolean.class must inherit it, which only happens if Boolean.class is its parent.
    assertSame(ColumnTextAlign.CENTER, resolve(boolean.class).getTextAlign());
  }

}
