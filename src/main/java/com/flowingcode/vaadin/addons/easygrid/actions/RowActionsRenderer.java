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

package com.flowingcode.vaadin.addons.easygrid.actions;

import com.vaadin.flow.component.grid.Grid;
import java.io.Serializable;
import java.util.List;

/**
 * Strategy interface for rendering the row actions of an {@code EasyGrid}. Implementations decide
 * how the actions are presented — as inline buttons in a dedicated column, as an overflow/context
 * menu, or any other mechanism.
 *
 * <p>An instance is held by {@link RowActionsManager} and is called whenever the action list
 * changes and a visual refresh is needed.
 *
 * @param <T> the grid bean type
 * @see LitRowActionsRenderer
 */
public interface RowActionsRenderer<T> extends Serializable {

  /**
   * Rebuilds the visual representation to reflect the given action list. Called on every scheduled
   * renderer update. Implementations that use a {@link Grid.Column} should create it on the first
   * call and update its renderer on subsequent calls.
   *
   * @param actions the current list of registered actions (unmodifiable)
   */
  void update(List<EasyRowAction<T>> actions);

  /**
   * Returns the {@link Grid.Column} used to host the actions, if this renderer uses a dedicated
   * column. Renderers that present actions through a context menu or another mechanism not tied to a
   * column should return {@code null}.
   *
   * @return the actions column, or {@code null} if not applicable
   */
  Grid.Column<T> getColumn();

  /**
   * Cleans up all UI elements created by this renderer (columns, context menus, etc.). Called by
   * {@link RowActionsManager} when the renderer is being replaced. The default implementation is a
   * no-op.
   */
  default void remove() {}

}
