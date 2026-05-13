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
package com.flowingcode.vaadin.addons.easygrid.config;

import com.flowingcode.vaadin.addons.easygrid.renderers.RendererFactory;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.function.SerializableFunction;
import java.io.Serializable;

/**
 * Holds display configuration for a grid column value type, including text alignment, null
 * representation, and renderer factory. Implementations chain through a parent configuration so
 * that non-{@code null} values at a more specific level take precedence over less specific ones.
 *
 * @param <V> the column value type
 */
public sealed interface ColumnConfiguration<V> extends Serializable
    permits ColumnConfigurationImpl, ColumnConfigurationLink {

  /**
   * Sets the string to display when the column value is {@code null}.
   *
   * @param nullRepresentation the null representation string
   * @return this configuration, for fluent chaining
   */
  ColumnConfiguration<V> setNullRepresentation(String nullRepresentation);

  /**
   * Sets the text alignment for cells of this column type.
   *
   * @param textAlign the text alignment to apply
   * @return this configuration, for fluent chaining
   */
  ColumnConfiguration<V> setTextAlign(ColumnTextAlign textAlign);

  /**
   * Returns the effective text alignment, walking the configuration chain until a non-{@code null}
   * value is found, or {@code null} if none is set.
   *
   * @return the effective text alignment, or {@code null}
   */
  ColumnTextAlign getTextAlign();

  /**
   * Returns the effective null representation, walking the configuration chain until a
   * non-{@code null} value is found, or {@code null} if none is set.
   *
   * @return the effective null representation, or {@code null}
   */
  String getNullRepresentation();

  /**
   * Sets a formatter that converts a column value to a display string. Calling this method replaces
   * any previously set renderer factory with a {@link com.vaadin.flow.data.renderer.TextRenderer}
   * that applies the formatter.
   *
   * @param formatter a function mapping a column value to a display string
   * @return this configuration, for fluent chaining
   */
  ColumnConfiguration<V> setFormatter(SerializableFunction<V, String> formatter);

  /**
   * Returns the effective renderer factory, walking the configuration chain until a
   * non-{@code null} value is found, or {@code null} if none is configured.
   *
   * @param <T> the grid bean type
   * @return the effective renderer factory, or {@code null}
   */
  <T> RendererFactory<T, V> getRendererFactory();

  /**
   * Sets a custom renderer factory that creates a renderer from a value provider. Replaces any
   * previously set formatter or renderer factory.
   *
   * @param <T> the grid bean type
   * @param rendererFactory a factory that creates a renderer from a value provider
   * @return this configuration, for fluent chaining
   */
  <T> ColumnConfiguration<V> setRendererFactory(RendererFactory<T, V> rendererFactory);

  /**
   * Returns a new independent configuration node that inherits from this configuration. Values set
   * on the returned node take precedence over this configuration's values; values not set on the
   * returned node fall through to this configuration.
   *
   * @return a new {@code ColumnConfiguration} whose parent is {@code this}
   */
  default ColumnConfiguration<V> createNewLayer() {
    return new ColumnConfigurationImpl<>(this);
  }
}
