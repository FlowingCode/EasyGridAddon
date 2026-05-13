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

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridDataView;
import com.vaadin.flow.component.grid.dataview.GridLazyDataView;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.data.provider.HasDataGenerators;
import com.vaadin.flow.data.provider.HasDataView;
import com.vaadin.flow.data.provider.HasLazyDataView;
import com.vaadin.flow.data.provider.HasListDataView;
import java.util.function.Consumer;
import lombok.NonNull;
import lombok.experimental.Delegate;

/**
 * Base {@code Composite} wrapper around a {@link Grid} that delegates the subset of grid methods
 * defined by {@link IEasyGridComposite} to the wrapped grid instance. Subclasses access the
 * wrapped grid via {@link #getWrappedGrid()} and may add higher-level behaviour on top of this
 * delegation layer.
 *
 * @param <T> the grid bean type
 * @param <GRID> the concrete {@code Grid} subtype being wrapped
 */
@SuppressWarnings("serial")
class EasyGridComposite<T, GRID extends Grid<T>> extends Composite<GRID>
    implements HasStyle, HasSize, HasTheme,
    HasDataGenerators<T>, HasListDataView<T, GridListDataView<T>>,
    HasDataView<T, Void, GridDataView<T>>,
    HasLazyDataView<T, Void, GridLazyDataView<T>> {

  non-sealed abstract class IEasyGridDelegate implements IEasyGridComposite<T> {
  }

  @Delegate(types = IEasyGridDelegate.class)
  private final GRID grid;

  /**
   * Creates a wrapper around the given grid.
   *
   * @param grid the grid to wrap, not {@code null}
   */
  public EasyGridComposite(@NonNull GRID grid) {
    this.grid = grid;
  }

  @Override
  protected GRID initContent() {
    return grid;
  }

  /**
   * Returns the wrapped grid.
   *
   * @return the wrapped grid, never {@code null}
   */
  public final GRID getWrappedGrid() {
    return grid;
  }

  /**
   * Passes the wrapped grid to the given customizer, allowing direct configuration of the grid.
   *
   * @param customizer a consumer that configures the wrapped grid
   */
  public void configure(Consumer<GRID> customizer) {
    customizer.accept(grid);
  }
}
