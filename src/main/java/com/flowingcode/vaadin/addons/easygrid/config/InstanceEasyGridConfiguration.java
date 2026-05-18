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

import com.flowingcode.vaadin.addons.easygrid.EasyGrid;
import java.io.Serializable;

/**
 * Instance-level {@link EasyGrid} configuration. Configurations registered here apply only to the
 * specific {@code EasyGrid} instance that holds this object and take precedence over
 * {@link GlobalEasyGridConfiguration}.
 *
 * <p>See {@link #resolve(Class)} for the full resolution order.
 */
@SuppressWarnings("serial")
public final class InstanceEasyGridConfiguration implements Serializable {

  private final EasyGridConfigurationClassMap byTypeConfigurations =
      new EasyGridConfigurationClassMap();

  /**
   * Returns the {@code ColumnConfiguration} for the given type at the instance level, creating it
   * if it does not yet exist. Modifications to the returned configuration take effect immediately.
   *
   * @param type the column value type
   * @return the {@code ColumnConfiguration} for the given type
   */
  public <V> ColumnConfiguration<V> forType(Class<V> type) {
    return link(byTypeConfigurations.getOrCreate(type), type);
  }

  private <V> ColumnConfiguration<V> link(ColumnConfiguration<V> config, Class<V> type) {
    return new ColumnConfigurationLink<V>(config, GlobalEasyGridConfiguration.resolve(type));
  }

  /**
   * Returns the effective {@code ColumnConfiguration} for the given type, chaining configurations
   * across all levels of the tree. The resolution order, from most to least specific, is:
   * <ol>
   * <li>Type-level configuration registered on this instance via {@link #forType(Class)}.</li>
   * <li>Type-level configuration registered on {@link GlobalEasyGridConfiguration}.</li>
   * </ol>
   * Each level's non-{@code null} fields take precedence over the levels below it.
   *
   * @param type the column value type
   * @return the resolved configuration, never {@code null}
   */
  public <V> ColumnConfiguration<V> resolve(Class<V> type) {
    return forType(type).createNewLayer();
  }

}
