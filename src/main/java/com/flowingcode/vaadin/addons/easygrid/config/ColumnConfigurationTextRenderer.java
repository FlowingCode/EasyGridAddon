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

import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.ValueProvider;

/**
 * A {@code TextRenderer} that formats column values using a
 * {@link ColumnConfiguration}. When the value is {@code null}, the configuration's null
 * representation is used. Otherwise, an optional formatter is applied; if none is provided,
 * {@link Object#toString()} is called.
 *
 * @param <T> the grid bean type
 * @param <V> the column value type
 */
@SuppressWarnings("serial")
public final class ColumnConfigurationTextRenderer<T, V> extends TextRenderer<T> {

  /**
   * Creates a renderer that applies the given formatter to non-{@code null} values and returns
   * the configuration's null representation for {@code null} values.
   *
   * @param getter a value provider that extracts the column value from a row item
   * @param config the column configuration supplying the null representation
   * @param formatter a function mapping a value and its configuration to a display string, or
   *        {@code null} to fall back to {@link Object#toString()}
   */
  public ColumnConfigurationTextRenderer(ValueProvider<T, V> getter, ColumnConfiguration<V> config,
      SerializableFunction<V, String> formatter) {
    super(item -> {
      V val = getter.apply(item);
      if (val == null) {
        return config.getNullRepresentation();
      }
      if (formatter != null) {
        return formatter.apply(val);
      }
      return val.toString();
    });
  }

  /**
   * Creates a renderer that falls back to {@link Object#toString()} for non-{@code null} values
   * and returns the configuration's null representation for {@code null} values.
   *
   * @param getter a value provider that extracts the column value from a row item
   * @param config the column configuration supplying the null representation
   */
  public ColumnConfigurationTextRenderer(ValueProvider<T, V> getter, ColumnConfiguration<V> config) {
    this(getter, config, null);
  }

}
