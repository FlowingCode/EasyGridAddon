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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import com.flowingcode.vaadin.addons.easygrid.config.InstanceEasyGridConfiguration;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.component.Component;
import org.junit.Test;

public class InstanceFormatterNullRepresentationTest {

  /**
   * When a formatter is registered at instance level for a type, the renderer it produces must
   * still use the effective null representation (including the global fallback). The bug is that
   * {@code ColumnConfigurationLink.setFormatter} used to delegate to
   * {@code primary.setFormatter}, which captured the raw primary as the config for the
   * {@code ColumnConfigurationTextRenderer}, bypassing the link's fallback. As a result, the item
   * label generator returned {@code null} for a null column value — and
   * {@code TextRenderer.createComponent} throws {@code IllegalStateException} on a {@code null}
   * label.
   */
  @Test
  public void formatterOnTypeConfigUsesEffectiveNullRepresentation() {
    var instanceConfig = new InstanceEasyGridConfiguration();

    // Set a formatter for String at instance level; no null representation is configured anywhere
    // in the instance chain. The effective null representation must come from the global fallback
    // (GlobalEasyGridConfiguration registers "" for Object.class).
    instanceConfig.forType(String.class).setFormatter(String::toUpperCase);

    // Resolve to get the per-column configuration, then obtain and apply the renderer factory.
    var factory = instanceConfig.resolve(String.class).<Object>getRendererFactory();
    // The getter returns the column value; null here means the column value itself is null.
    @SuppressWarnings("unchecked")
    var renderer = (ComponentRenderer<Component, Object>) factory.apply(item -> (String) item);

    // createComponent(null): the getter returns null, so the item label generator must return the
    // null representation. With the bug the label generator returned null and TextRenderer threw
    // IllegalStateException. With the fix the label generator returns "" (global fallback).
    var component = renderer.createComponent(null);
    assertThat(component.getElement().getText(), is(""));
  }

}
