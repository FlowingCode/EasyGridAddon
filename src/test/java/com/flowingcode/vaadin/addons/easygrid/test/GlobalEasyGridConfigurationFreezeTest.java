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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.flowingcode.vaadin.addons.easygrid.config.ColumnConfiguration;
import com.flowingcode.vaadin.addons.easygrid.config.GlobalEasyGridConfiguration;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Verifies {@link GlobalEasyGridConfiguration#freeze()} behaviour. */
public class GlobalEasyGridConfigurationFreezeTest {

  private static void setFrozen(boolean value) throws ReflectiveOperationException {
    Field field = GlobalEasyGridConfiguration.class.getDeclaredField("frozen");
    field.setAccessible(true);
    field.set(null, value);
  }

  @SuppressWarnings("unchecked")
  private static <V> ColumnConfiguration<V> resolve(Class<V> type)
      throws ReflectiveOperationException {
    Method method = GlobalEasyGridConfiguration.class.getDeclaredMethod("resolve", Class.class);
    method.setAccessible(true);
    return (ColumnConfiguration<V>) method.invoke(null, type);
  }

  @Before
  public void ensureUnfrozen() throws ReflectiveOperationException {
    setFrozen(false);
  }

  @After
  public void restoreUnfrozen() throws ReflectiveOperationException {
    setFrozen(false);
  }

  @Test
  public void resolveBeforeFreezeCreatesEntry() throws ReflectiveOperationException {
    assertNotNull(resolve(LocalDate.class));
  }

  @Test
  public void afterFreezeIsFrozen() {
    GlobalEasyGridConfiguration.freeze();
    assertTrue(GlobalEasyGridConfiguration.isFrozen());
  }

  @Test(expected = IllegalStateException.class)
  public void forTypeAfterFreezeThrows() {
    GlobalEasyGridConfiguration.freeze();
    GlobalEasyGridConfiguration.forType(String.class);
  }

  @Test
  public void resolveAfterFreezeReturnsRegisteredConfig() throws ReflectiveOperationException {
    GlobalEasyGridConfiguration.freeze();
    // LocalDate was registered in the static initializer
    assertNotNull(resolve(LocalDate.class));
  }

  @Test
  public void resolveAfterFreezeReturnsNullForUnregisteredType()
      throws ReflectiveOperationException {
    GlobalEasyGridConfiguration.freeze();
    // No config registered for this marker interface
    assertNull(resolve(Runnable.class));
  }

}
