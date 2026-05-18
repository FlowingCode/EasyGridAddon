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

import com.vaadin.flow.component.grid.Grid;
import lombok.NonNull;

/**
 * Concrete {@code EasyGridWrapper} that always wraps a plain {@link Grid}, creating it
 * internally. This is the standard entry point for using the Easy Grid Add-on.
 *
 * @param <T> the grid bean type
 */
@SuppressWarnings("serial")
public class EasyGrid<T> extends EasyGridWrapper<T, Grid<T>> {

  /**
   * Creates an {@code EasyGrid} that discovers all top-level bean properties and adds a column for
   * each.
   *
   * @param beanType the bean type to use, not {@code null}
   * @throws NullPointerException if {@code beanType} is {@code null}
   */
  public EasyGrid(@NonNull Class<T> beanType) {
    super(new Grid<T>(), beanType);
  }

  /**
   * Creates an {@code EasyGrid} for the given bean type, optionally auto-creating columns.
   *
   * @param beanType the bean type to use, not {@code null}
   * @param autoCreateColumns when {@code true}, columns are created automatically for the
   *        properties of the bean type
   * @throws NullPointerException if {@code beanType} is {@code null}
   */
  public EasyGrid(@NonNull Class<T> beanType, boolean autoCreateColumns) {
    super(new Grid<T>(), beanType, autoCreateColumns);
  }

  /**
   * Creates an {@code EasyGrid} that adds columns for the specified properties in order.
   *
   * @param beanType the bean type to use, not {@code null}
   * @param propertyNames the names of the properties for which columns are created
   * @throws NullPointerException if {@code beanType} or {@code propertyNames} is {@code null}
   */
  public EasyGrid(@NonNull Class<T> beanType, String... propertyNames) {
    super(new Grid<T>(), beanType, propertyNames);
  }

}
