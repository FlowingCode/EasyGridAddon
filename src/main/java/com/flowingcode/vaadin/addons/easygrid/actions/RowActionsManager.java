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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.ValueProvider;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.NonNull;

/**
 * Manages the row actions column for an {@code EasyGrid}. Maintains the list of registered
 * {@link EasyRowAction} instances, the render mode (inline buttons vs. overflow menu), and the
 * underlying {@link Grid.Column} that hosts the actions.
 *
 * <p>An instance of this class is created lazily the first time a row action is added via
 * {@code EasyGrid.addRowAction(...)}.
 *
 * @param <T> the grid bean type
 */
@SuppressWarnings("serial")
public class RowActionsManager<T> implements Serializable {

  private final Grid<T> grid;
  private final List<EasyRowAction<T>> actions = new ArrayList<>();
  private boolean asMenu = false;
  private Grid.Column<T> actionsColumn;

  public RowActionsManager(@NonNull Grid<T> grid) {
    this.grid = grid;
  }

  public EasyRowAction<T> addRowAction(String label,
      ValueProvider<T, Component> iconProvider,
      @NonNull SerializableConsumer<T> handler) {
    EasyRowAction<T> action = new EasyRowAction<>(label, iconProvider, handler);
    actions.add(action);
    createActionsColumnIfNeeded();
    setColumnCount(actions.size());
    return action;
  }

  private void setColumnCount(int count) {
    grid.getElement().getStyle().set("--grid-buttons-count", Integer.toString(count));
  }

  /**
   * Sets whether all actions should be rendered as an overflow (context) menu instead of inline
   * buttons.
   *
   * @param asMenu {@code true} to render actions as a menu; {@code false} for inline buttons
   */
  public void setRowActionsAsMenu(boolean asMenu) {
    this.asMenu = asMenu;
  }

  /**
   * Returns whether actions are rendered as a menu.
   *
   * @return {@code true} if actions are rendered as a menu
   */
  public boolean isRowActionsAsMenu() {
    return asMenu;
  }

  /**
   * Returns an unmodifiable view of the registered action entries.
   *
   * @return the list of action entries
   */
  public List<EasyRowAction<T>> getRowActions() {
    return Collections.unmodifiableList(actions);
  }

  private void createActionsColumnIfNeeded() {
    if (this.actionsColumn != null) {
      this.actionsColumn = grid.addComponentColumn(row -> {
        HtmlComponent container = new HtmlComponent("grid-buttons");
        actions.stream().map(action -> action.createElement(row))
            .filter(Objects::nonNull)
            .forEach(container.getElement()::appendChild);
        return container;
      });
    }
  }

  /**
   * Returns the {@link Grid.Column} that hosts the actions, or {@code null} if it has not been
   * created yet.
   *
   * @return the actions column, or {@code null}
   */
  public Grid.Column<T> getActionsColumn() {
    return actionsColumn;
  }

}
