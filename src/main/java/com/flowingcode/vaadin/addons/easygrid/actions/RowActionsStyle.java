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
import lombok.NonNull;

/**
 * Identifies how an {@code EasyGrid}'s row actions are presented.
 *
 * @see RowActionsRenderer
 * @author Javier Godoy / Flowing Code
 */
public enum RowActionsStyle {

  /** Actions are presented as inline buttons in a dedicated column. This is the default style. */
  INLINE_BUTTONS,

  /**
   * Actions are presented through an overflow ("⋮") button hosted in a dedicated column; clicking
   * the button opens the menu for that row.
   */
  DROPDOWN,

  /**
   * Actions are presented through the grid's right-click context menu; no dedicated column is
   * created.
   */
  CONTEXT_MENU;

  /**
   * Creates a {@code RowActionsRenderer} that presents row actions in this style.
   *
   * @param <T> the grid bean type
   * @param grid the grid the renderer will decorate
   * @return a new renderer for this style
   */
  <T> RowActionsRenderer<T> createRenderer(@NonNull Grid<T> grid) {
    return switch (this) {
      case INLINE_BUTTONS -> new LitRowActionsRenderer<>(grid);
      case DROPDOWN -> new DropdownMenuRowActionsRenderer<>(grid);
      case CONTEXT_MENU -> new ContextMenuRowActionsRenderer<>(grid);
    };
  }

  /**
   * Returns whether the given renderer is the kind produced by {@link #createRenderer(Grid)} for
   * this style.
   *
   * @param renderer the renderer to test
   * @return {@code true} if {@code renderer} presents actions in this style
   */
  boolean isInstance(RowActionsRenderer<?> renderer) {
    return switch (this) {
      case INLINE_BUTTONS -> renderer instanceof LitRowActionsRenderer;
      case DROPDOWN -> renderer instanceof DropdownMenuRowActionsRenderer;
      case CONTEXT_MENU -> renderer instanceof ContextMenuRowActionsRenderer;
    };
  }

}
