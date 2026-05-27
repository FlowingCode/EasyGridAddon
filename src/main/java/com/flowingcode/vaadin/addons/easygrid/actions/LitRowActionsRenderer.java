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
import java.util.List;
import lombok.NonNull;

/**
 * A {@link RowActionsRenderer} that renders row actions as inline buttons inside a dedicated
 * {@link Grid.Column}, using a Lit template backed by {@link LitRendererBuilder}. The column is
 * created the first time {@link #update} is called, and its renderer is replaced on every
 * subsequent call.
 *
 * @param <T> the grid bean type
 */
@SuppressWarnings("serial")
final class LitRowActionsRenderer<T> implements RowActionsRenderer<T> {

  private static final String GRID_BUTTONS_COUNT = "--grid-buttons-count";

  private final Grid<T> grid;
  private Grid.Column<T> column;

  LitRowActionsRenderer(@NonNull Grid<T> grid) {
    this.grid = grid;
  }

  @Override
  public void update(List<EasyRowAction<T>> actions) {
    var builder = new LitRendererBuilder<T>("actions");
    builder.append("<fc-dynamic-buttons>");
    actions.forEach(action -> action.updateRenderer(builder));
    builder.append("</fc-dynamic-buttons>");
    var litRenderer = builder.build();
    if (column == null) {
      column = grid.addColumn(litRenderer);
      column.setAutoWidth(true);
    } else {
      column.setRenderer(litRenderer);
    }
    grid.getElement().getStyle().set(GRID_BUTTONS_COUNT, Integer.toString(actions.size()));
  }

  @Override
  public Grid.Column<T> getColumn() {
    return column;
  }

  @Override
  public void remove() {
    if (column != null) {
      grid.removeColumn(column);
      column = null;
    }
    grid.getElement().getStyle().remove(GRID_BUTTONS_COUNT);
  }

}
