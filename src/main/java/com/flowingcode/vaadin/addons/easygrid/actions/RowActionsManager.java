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

import com.vaadin.flow.component.button.ButtonVariant;
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
 * {@link EasyRowAction} instances, the render mode (inline buttons vs. overflow menu), and
 * delegates all visual concerns to a {@link RowActionsRenderer}.
 *
 * <p>A manager instance is created eagerly together with its grid. The instance itself is
 * lightweight; the actions {@link Grid.Column} it manages is created lazily — on the first renderer
 * update or {@link #getActionsColumn()} call — and stays hidden until the first action is added.
 *
 * @param <T> the grid bean type
 */
@SuppressWarnings("serial")
public class RowActionsManager<T> implements Serializable {

  private final Grid<T> grid;
  private final List<EasyRowAction<T>> actions = new ArrayList<>();
  private RowActionsRenderer<T> renderer;
  private ButtonVariant[] defaultVariants = {ButtonVariant.LUMO_TERTIARY_INLINE};
  private boolean rendererInitialized = false;
  private Registration rendererRegistration;

  private void setRendererRegistration(Registration registration) {
    if (rendererRegistration != null) {
      rendererRegistration.remove();
    }
    rendererRegistration = registration;
  }

  /**
   * Creates a new {@code RowActionsManager} for the given grid.
   *
   * @param grid the grid to manage row actions for
   */
  public RowActionsManager(@NonNull Grid<T> grid) {
    this.grid = grid;
    this.renderer = new LitRowActionsRenderer<>(grid);
  }

  /**
   * Creates and registers a new row action, applying the current default theme variants and
   * scheduling a renderer rebuild. At least one of {@code labelProvider} or {@code iconProvider}
   * must be non-{@code null}.
   *
   * @param <ICON> the icon type
   * @param labelProvider per-row label provider, or {@code null} for icon-only
   * @param iconProvider per-row icon provider, or {@code null} for label-only
   * @param handler the action to execute when the action is clicked
   * @return the registered action
   */
  <ICON extends AbstractIcon<ICON>> EasyRowAction<T> addRowAction(
      ValueProvider<T, String> labelProvider,
      ValueProvider<T, ICON> iconProvider,
      @NonNull SerializableConsumer<T> handler) {
    EasyRowAction<T> action = new EasyRowAction<T>(this, labelProvider, iconProvider, handler);
    if (defaultVariants != null) {
      action.addThemeVariants(defaultVariants);
    }
    actions.add(action);
    var column = renderer.getColumn();
    if (column != null && actions.size() == 1 && !column.isVisible()) {
      column.setVisible(true);
    }
    scheduleRendererUpdate();
    return action;
  }

  /**
   * Removes the specified action. If the action is not currently registered, this call is a no-op.
   * The renderer is rebuilt on the next {@code beforeClientResponse} cycle; if no actions remain
   * and the active renderer uses a column, that column is hidden.
   *
   * @param action the action to remove
   */
  void removeRowAction(EasyRowAction<T> action) {
    if (!actions.remove(action)) {
      return;
    }
    var column = renderer.getColumn();
    if (column != null && actions.isEmpty()) {
      column.setVisible(false);
    }
    scheduleRendererUpdate();
    action.remove();
  }

  private void scheduleRendererUpdate() {
    grid.getUI().ifPresentOrElse(
        ui -> setRendererRegistration(ui.beforeClientResponse(grid, ctx -> updateRenderer())),
        () -> setRendererRegistration(grid.addAttachListener(e -> setRendererRegistration(
            e.getUI().beforeClientResponse(grid, ctx -> updateRenderer())))));
  }

  /**
   * Sets the theme variants that are applied to every action upon creation.
   *
   * @param variants the variants to apply
   */
  void setDefaultRowActionVariants(ButtonVariant... variants) {
    this.defaultVariants = variants != null && variants.length > 0 ? variants : null;
  }

  /**
   * Sets whether all actions should be rendered as an overflow (context) menu instead of inline
   * buttons.
   *
   * @param asMenu {@code true} to render actions as a menu; {@code false} for inline buttons
   */
  void setRowActionsAsMenu(boolean asMenu) {
    if (asMenu != (renderer instanceof ContextMenuRowActionsRenderer)) {
      setRenderer(asMenu ? new ContextMenuRowActionsRenderer<>(grid) : new LitRowActionsRenderer<>(grid));
    }
  }

  /**
   * Replaces the active renderer. The current renderer is cleaned up via {@link
   * RowActionsRenderer#remove()} before the new one is installed. A renderer rebuild is scheduled
   * for the next {@code beforeClientResponse} cycle.
   *
   * @param renderer the new renderer to use
   */
  public void setRenderer(@NonNull RowActionsRenderer<T> renderer) {
    this.renderer.remove();
    rendererInitialized = false;
    this.renderer = renderer;
    scheduleRendererUpdate();
  }

  /**
   * Schedules a renderer rebuild on the next {@code beforeClientResponse} cycle. The fluent
   * configuration methods of {@link EasyRowAction} schedule this automatically; an explicit call is
   * only needed after changing an action's styling or theme variants, which are not applied
   * automatically. Any previously scheduled rebuild is cancelled and replaced.
   */
  void refresh() {
    scheduleRendererUpdate();
  }

  /**
   * Returns an unmodifiable view of the registered action entries.
   *
   * @return the list of action entries
   */
  List<EasyRowAction<T>> getRowActions() {
    return Collections.unmodifiableList(actions);
  }

  private void updateRenderer() {
    setRendererRegistration(null);
    renderer.update(actions);
    rendererInitialized = true;
  }

  /**
   * Returns the {@link Grid.Column} that hosts the actions, or {@code null} if the active renderer
   * does not use a column (e.g. a context-menu renderer).
   *
   * <p>For column-based renderers: the column is created on demand if it does not exist yet, hidden
   * when no actions are registered, and made visible automatically when the first action is added.
   * If a deferred renderer update is pending it is cancelled and applied immediately so the column
   * reflects the current action list.
   */
  Grid.Column<T> getActionsColumn() {
    if (!rendererInitialized) {
      updateRenderer();
      if (actions.isEmpty()) {
        var column = renderer.getColumn();
        if (column != null) {
          column.setVisible(false);
        }
      }
    }
    return renderer.getColumn();
  }

}
