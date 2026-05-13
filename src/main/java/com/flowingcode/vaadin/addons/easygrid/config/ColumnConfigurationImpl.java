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
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.ValueProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Holds display configuration for a grid column, including text alignment, null representation,
 * and renderer factory.
 *
 * <p>Instances are obtained through the configuration-tree classes:
 * {@link GlobalEasyGridConfiguration} and {@link InstanceEasyGridConfiguration}.
 *
 * <p>Configurations form a chain: each getter returns this instance's own value when
 * non-{@code null}, otherwise delegates to the parent.
 *
 * @param <V> the column value type
 */
@SuppressWarnings("serial")
@Setter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Accessors(chain = true)
final class ColumnConfigurationImpl<V> implements ColumnConfiguration<V> {

  private final ColumnConfiguration<V> parent;

  private ColumnTextAlign textAlign;

  private String nullRepresentation;
  private RendererFactory<ValueProvider<?, V>, Renderer<?>> rendererFactory;

  @Override
  public ColumnTextAlign getTextAlign() {
    return textAlign != null ? textAlign
        : (parent != null ? parent.getTextAlign() : null);
  }

  @Override
  public String getNullRepresentation() {
    return nullRepresentation != null ? nullRepresentation
        : (parent != null ? parent.getNullRepresentation() : null);
  }

  @Override
  public <T> RendererFactory<T, V> getRendererFactory() {
    @SuppressWarnings({"unchecked", "rawtypes"})
    RendererFactory<T, V> factory = (RendererFactory) rendererFactory;
    if (factory == null && parent != null) {
      factory = parent.getRendererFactory();
    }
    return factory;
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public <T> ColumnConfiguration<V> setRendererFactory(RendererFactory<T, V> rendererFactory) {
    this.rendererFactory = (RendererFactory) rendererFactory;
    return this;
  }

  @Override
  public ColumnConfiguration<V> setFormatter(SerializableFunction<V, String> formatter) {
    setRendererFactory(getter -> new ColumnConfigurationTextRenderer<>(getter, this, formatter));
    return this;
  }

}
