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
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A {@code ColumnConfiguration} that delegates reads to a primary configuration and, when the
 * primary returns {@code null} for a given property, falls back to a secondary configuration.
 * Writes (setters) are always forwarded to the primary and return {@code this} for fluent chaining.
 */
@SuppressWarnings("serial")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class ColumnConfigurationLink<V> implements ColumnConfiguration<V> {

  @NonNull
  private final ColumnConfiguration<V> primary;

  /** Secondary configuration consulted when the primary returns {@code null} for a property. */
  private final ColumnConfiguration<V> fallback;

  private <T> T get(Function<ColumnConfiguration<V>, T> f) {
    T t = f.apply(primary);
    if (t == null && fallback != null) {
      t = f.apply(fallback);
    }
    return t;
  }

  @Override
  public ColumnConfiguration<V> setNullRepresentation(String nullRepresentation) {
    primary.setNullRepresentation(nullRepresentation);
    return this;
  }

  @Override
  public ColumnConfiguration<V> setTextAlign(ColumnTextAlign textAlign) {
    primary.setTextAlign(textAlign);
    return this;
  }

  @Override
  public ColumnConfiguration<V> setFormatter(SerializableFunction<V, String> formatter) {
    primary.setFormatter(formatter);
    return this;
  }

  @Override
  public <T> ColumnConfiguration<V> setRendererFactory(RendererFactory<T, V> rendererFactory) {
    primary.setRendererFactory(rendererFactory);
    return this;
  }

  @Override
  public ColumnTextAlign getTextAlign() {
    return get(ColumnConfiguration::getTextAlign);
  }

  @Override
  public String getNullRepresentation() {
    return get(ColumnConfiguration::getNullRepresentation);
  }

  @Override
  public <T> RendererFactory<T, V> getRendererFactory() {
    return get(ColumnConfiguration::getRendererFactory);
  }

}
