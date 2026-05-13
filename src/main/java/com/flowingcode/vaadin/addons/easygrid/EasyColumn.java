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
package com.flowingcode.vaadin.addons.easygrid;

import com.flowingcode.vaadin.addons.easygrid.config.ColumnConfiguration;
import com.flowingcode.vaadin.addons.easygrid.config.ColumnConfigurationTextRenderer;
import com.flowingcode.vaadin.addons.easygrid.renderers.RendererFactory;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.ValueProvider;
import java.io.Serializable;
import java.util.Optional;
import lombok.Getter;

/**
 * Wraps a {@link Grid.Column} and provides type-specific formatting for an EasyGridAddon-managed
 * column.
 *
 * @param <T> the grid bean type
 * @param <V> the column value type
 */
@SuppressWarnings("serial")
public final class EasyColumn<T, V> implements IEasyGridColumn<T, V>, Serializable {

  private final ColumnConfiguration<V> config;

  @Getter
  private final Grid.Column<T> column;

  private final ValueProvider<T, V> getter;

  @Getter
  private final Class<V> type;

  EasyColumn(ColumnConfiguration<V> config, Grid.Column<T> column, ValueProvider<T, V> getter,
      Class<V> type) {
    this.config = config;
    this.column = column;
    this.getter = getter;
    this.type = type;
    ComponentUtil.setData(column, EasyColumn.class, this);
    Optional.ofNullable(config.getTextAlign()).ifPresent(column::setTextAlign);
  }

  @SuppressWarnings("unchecked")
  static <T> EasyColumn<T, ?> getInstance(Grid.Column<T> column) {
    return ComponentUtil.getData(column, EasyColumn.class);
  }

  /**
   * Returns this column cast to value type {@code S}.
   * Succeeds when the column's actual value type is a subtype of {@code S}; throws otherwise.
   *
   * @param <S> the target value type
   * @param type the target value type class
   * @return this column cast to {@code EasyColumn<T, S>}
   * @throws ClassCastException if the column's actual value type is not a subtype of {@code type}
   */
  public <S> EasyColumn<T, S> as(Class<S> type) {
    if (!type.isAssignableFrom(this.type)) {
      throw new ClassCastException(String.format(
          "column \"%s\" has value type %s which cannot be cast to %s",
          column.getKey(), this.type.getName(), type.getName()));
    }
    @SuppressWarnings("unchecked")
    EasyColumn<T, S> result = (EasyColumn<T, S>) this;
    return result;
  }

  static <T, V> Renderer<T> createRenderer(ColumnConfiguration<V> config,
      ValueProvider<T, V> getter) {
    var factory = config.<T>getRendererFactory();
    if (factory != null) {
      return factory.apply(getter);
    }
    return new ColumnConfigurationTextRenderer<T, V>(getter, config);
  }

  private void updateRenderer() {
    column.setRenderer(createRenderer(config, getter));
  }
  
  /**
   * Sets the string to display when the column value is {@code null}. Overrides any null
   * representation inherited from the type or global configuration. Updates the underlying
   * configuration and re-applies the renderer.
   *
   * @param nullRepresentation the string to show for {@code null} values
   * @return this column, for fluent chaining
   */
  public EasyColumn<T, V> setNullRepresentation(String nullRepresentation) {
    config.setNullRepresentation(nullRepresentation);
    updateRenderer();
    return this;
  }

  /**
   * Sets a custom formatter that converts the column value to a display string. Overrides any
   * type-specific or formatter-based renderer inherited from the configuration chain.
   *
   * @param formatter a function mapping a column value to its display string
   * @return this column, for fluent chaining
   */
  public EasyColumn<T, V> setFormatter(SerializableFunction<V, String> formatter) {
    config.setFormatter(formatter);
    updateRenderer();
    return this;
  }

  /**
   * Sets a custom renderer factory for this column. Overrides any type-specific or
   * formatter-based renderer inherited from the configuration chain.
   *
   * @param rendererFactory a factory that creates a renderer from a value provider
   * @return this column, for fluent chaining
   */
  public EasyColumn<T, V> setRendererFactory(RendererFactory<T, V> rendererFactory) {
    config.setRendererFactory(rendererFactory);
    updateRenderer();
    return this;
  }
  
  /**
   * Sets the text alignment for this column's cells. Updates both the underlying
   * {@link Grid.Column} and the {@link ColumnConfiguration}.
   *
   * @param textAlign the text alignment to apply
   * @return this column, for fluent chaining
   */
  @Override
  public EasyColumn<T, V> setTextAlign(ColumnTextAlign textAlign) {
    IEasyGridColumn.super.setTextAlign(textAlign);
    config.setTextAlign(textAlign);
    return this;
  }

}
