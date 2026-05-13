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

import static org.mockito.Mockito.mock;
import com.flowingcode.vaadin.addons.easygrid.EasyColumn;
import com.flowingcode.vaadin.addons.easygrid.config.ColumnConfiguration;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.function.ValueProvider;
import java.lang.reflect.Constructor;
import org.mockito.Mockito;

final class EasyColumnTestHelper {

  private EasyColumnTestHelper() {}

  static Class<?> columnConfigurationImplClass() throws ClassNotFoundException {
    return Class.forName("com.flowingcode.vaadin.addons.easygrid.config.ColumnConfigurationImpl");
  }

  static Class<?> columnConfigurationLinkClass() throws ClassNotFoundException {
    return Class.forName("com.flowingcode.vaadin.addons.easygrid.config.ColumnConfigurationLink");
  }

  @SuppressWarnings("unchecked")
  static <V> ColumnConfiguration<V> mockColumnConfigurationImpl() throws ClassNotFoundException {
    var mock = (ColumnConfiguration<V>) mock(columnConfigurationImplClass());
    Mockito.when(mock.getRendererFactory())
        .thenReturn(getter -> new com.vaadin.flow.data.renderer.TextRenderer<>(Object::toString));
    return mock;
  }

  @SuppressWarnings("unchecked")
  static <V> ColumnConfiguration<V> newColumnConfigurationImpl(ColumnConfiguration<V> parent)
      throws ReflectiveOperationException {
    var ctor = columnConfigurationImplClass().getDeclaredConstructor(ColumnConfiguration.class);
    ctor.setAccessible(true);
    return (ColumnConfiguration<V>) ctor.newInstance(parent);
  }

  @SuppressWarnings("unchecked")
  static <V> ColumnConfiguration<V> newColumnConfigurationLink(ColumnConfiguration<?> primary,
      ColumnConfiguration<?> fallback) throws ReflectiveOperationException {
    var ctor = columnConfigurationLinkClass().getDeclaredConstructor(ColumnConfiguration.class,
        ColumnConfiguration.class);
    ctor.setAccessible(true);
    return (ColumnConfiguration<V>) ctor.newInstance(primary, fallback);
  }

  @SuppressWarnings("rawtypes")
  static EasyColumn<?, ?> newEasyColumn(ColumnConfiguration<?> config, Grid.Column<?> column)
      throws ReflectiveOperationException {
    Constructor<EasyColumn> ctor = EasyColumn.class.getDeclaredConstructor(
        ColumnConfiguration.class, Grid.Column.class, ValueProvider.class, Class.class);
    ctor.setAccessible(true);
    ValueProvider<?, ?> getter = x -> x;
    return ctor.newInstance(config, column, getter, Object.class);
  }

}
