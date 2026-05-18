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
import com.vaadin.flow.component.icon.AbstractIcon;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.Registration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
  private Registration rendererRegistration;

  private void setRendererRegistration(Registration registration) {
    if (rendererRegistration != null) {
      rendererRegistration.remove();
    }
    rendererRegistration = registration;
  }

  public RowActionsManager(@NonNull Grid<T> grid) {
    this.grid = grid;
  }

  public <ICON extends AbstractIcon<ICON>> EasyRowAction<T> addRowAction(
      ValueProvider<T, String> labelProvider,
      ValueProvider<T, ICON> iconProvider,
      @NonNull SerializableConsumer<T> handler) {
    EasyRowAction<T> action = new EasyRowAction<T>(labelProvider, iconProvider, handler);
    actions.add(action);
    action.setManager(this);
    setColumnCount(actions.size());
    if (actions.size() == 1 && actionsColumn != null && !actionsColumn.isVisible()) {
      actionsColumn.setVisible(true);
    }
    scheduleRendererUpdate();
    return action;
  }

  /**
   * Removes the specified action from the actions column. If the action is not currently
   * registered, this call is a no-op. The renderer is rebuilt on the next
   * {@code beforeClientResponse} cycle; if no actions remain the column is hidden.
   *
   * @param action the action to remove
   */
  public void removeRowAction(EasyRowAction<T> action) {
    if (!actions.remove(action)) {
      return;
    }
    setColumnCount(actions.size());
    if (actions.isEmpty() && actionsColumn != null) {
      actionsColumn.setVisible(false);
    }
    scheduleRendererUpdate();
  }

  private void setColumnCount(int count) {
    grid.getElement().getStyle().set("--grid-buttons-count", Integer.toString(count));
  }

  private void scheduleRendererUpdate() {
    grid.getUI().ifPresentOrElse(
        ui -> setRendererRegistration(ui.beforeClientResponse(grid, ctx -> updateRenderer())),
        () -> setRendererRegistration(grid.addAttachListener(e ->
            setRendererRegistration(e.getUI().beforeClientResponse(grid, ctx -> updateRenderer()))
        )));
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
   * Schedules a renderer rebuild on the next {@code beforeClientResponse} cycle. Call this after
   * mutating an existing {@link EasyRowAction}'s configuration (e.g. via {@code visibleWhen},
   * {@code enabledWhen}, {@code tooltip}) to make the change visible. Any previously scheduled
   * rebuild is cancelled and replaced.
   */
  public void refresh() {
    scheduleRendererUpdate();
  }

  /**
   * Returns an unmodifiable view of the registered action entries.
   *
   * @return the list of action entries
   */
  public List<EasyRowAction<T>> getRowActions() {
    return Collections.unmodifiableList(actions);
  }

  private void updateRenderer() {
    setRendererRegistration(null);

    var renderer = new LitRendererBuilder<T>("actions");
    renderer.append("<fc-dynamic-buttons>");
    actions.forEach(action -> action.updateRenderer(renderer));
    renderer.append("</fc-dynamic-buttons>");

    if (actionsColumn == null) {
      actionsColumn = grid.addColumn(renderer.build());
      actionsColumn.setAutoWidth(true);
    } else {
      actionsColumn.setRenderer(renderer.build());
    }
  }

  /**
   * Returns the {@link Grid.Column} that hosts the actions, creating it if it has not been added
   * yet. The column is created hidden when no actions have been registered; it becomes visible
   * automatically when the first action is added via {@link #addRowAction}.
   *
   * <p>If a deferred renderer update is pending, it is cancelled and applied immediately so the
   * column reflects the current action list.
   *
   * @return the actions column
   */
  public Grid.Column<T> getActionsColumn() {
    if (actionsColumn == null) {
      updateRenderer();
      if (actions.isEmpty()) {
        actionsColumn.setVisible(false);
      }
    }
    return actionsColumn;
  }


}
