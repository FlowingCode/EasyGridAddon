package com.flowingcode.vaadin.addons.easygrid.actions;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.AbstractIcon;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.IconFactory;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.ValueProvider;
import lombok.NonNull;

@CssImport(value = "./fc-dynamic-buttons.css")
@JsModule("./fc-icon.ts")
@Uses(Button.class)
public interface HasRowActions<T> {

  /**
   * Returns the {@code RowActionsManager} that manages the actions column for this grid.
   * Implementors must provide this instance; all default methods delegate to it.
   *
   * @return the row actions manager, never {@code null}
   */
  RowActionsManager<T> getRowActionsManager();

  /**
   * Adds a label-only row action that invokes {@code handler} when clicked.
   *
   * @param label the button label
   * @param handler the action to execute when clicked
   * @return the registered action
   */
  default EasyRowAction<T> addRowAction(@NonNull String label,
      @NonNull SerializableConsumer<T> handler) {
    return addRowAction(label, (ValueProvider<T,Icon>) null, handler);
  }

  /**
   * Adds a row action with a static label and icon that invokes {@code handler} when clicked.
   * Either {@code label} or {@code icon} may be {@code null}, but not both.
   *
   * @param label the button label, or {@code null} for icon-only
   * @param icon the button icon, or {@code null} for label-only
   * @param handler the action to execute when clicked
   * @return the registered action
   */
  default EasyRowAction<T> addRowAction(
      String label,
      Icon icon,
      @NonNull SerializableConsumer<T> handler) {
    return addRowAction(label, Constant.ofNullable(icon), handler);
  }

  /**
   * Adds an icon-only row action that invokes {@code handler} when clicked.
   *
   * @param icon the button icon
   * @param handler the action to execute when clicked
   * @return the registered action
   */
  default EasyRowAction<T> addRowAction(
      @NonNull Icon icon,
      @NonNull SerializableConsumer<T> handler) {
    return addRowAction(null, icon, handler);
  }

  /**
   * Adds a row action with a static label and an icon created from {@code iconFactory} that
   * invokes {@code handler} when clicked. {@code label} may be {@code null} for icon-only.
   *
   * @param label the button label, or {@code null} for icon-only
   * @param iconFactory factory used to create the button icon
   * @param handler the action to execute when clicked
   * @return the registered action
   */
  default EasyRowAction<T> addRowAction(
      String label,
      @NonNull IconFactory iconFactory,
      @NonNull SerializableConsumer<T> handler) {
    return addRowAction(label, iconFactory.create(), handler);
  }

  /**
   * Adds an icon-only row action whose icon is created from {@code iconFactory} that invokes
   * {@code handler} when clicked.
   *
   * @param iconFactory factory used to create the button icon
   * @param handler the action to execute when clicked
   * @return the registered action
   */
  default EasyRowAction<T> addRowAction(
      @NonNull IconFactory iconFactory,
      @NonNull SerializableConsumer<T> handler) {
    return addRowAction(null, iconFactory.create(), handler);
  }

  /**
   * Adds an icon-only row action whose icon is computed per row from {@code iconProvider} that
   * invokes {@code handler} when clicked.
   *
   * @param <ICON> the icon type
   * @param iconProvider per-row provider for the button icon
   * @param handler the action to execute when clicked
   * @return the registered action
   */
  default <ICON extends AbstractIcon<ICON>> EasyRowAction<T> addRowAction(
      @NonNull ValueProvider<T, ICON> iconProvider,
      @NonNull SerializableConsumer<T> handler) {
    return addRowAction(null, iconProvider, handler);
  }

  /**
   * Adds a row action with a static label and a per-row icon that invokes {@code handler} when
   * clicked. Either {@code label} or {@code iconProvider} may be {@code null}, but not both.
   *
   * @param <ICON> the icon type
   * @param label the button label, or {@code null} for icon-only
   * @param iconProvider per-row provider for the button icon, or {@code null} for label-only
   * @param handler the action to execute when clicked
   * @return the registered action
   */
  default <ICON extends AbstractIcon<ICON>> EasyRowAction<T> addRowAction(
      String label,
      ValueProvider<T, ICON> iconProvider,
      @NonNull SerializableConsumer<T> handler) {
    return getRowActionsManager().addRowAction(Constant.ofNullable(label), iconProvider, handler);
  }

  /**
   * Sets whether all actions should be rendered as an overflow (context) menu instead of inline
   * buttons.
   *
   * @param asMenu {@code true} to render actions as a menu; {@code false} for inline buttons
   */
  default void setRowActionsAsMenu(boolean asMenu) {
    getRowActionsManager().setRowActionsAsMenu(asMenu);
  }

  /**
   * Replaces the active row-actions renderer. The current renderer is cleaned up before the new
   * one is installed, and a rebuild is scheduled for the next {@code beforeClientResponse} cycle.
   *
   * @param renderer the new renderer to use
   */
  default void setRowActionsRenderer(RowActionsRenderer<T> renderer) {
    getRowActionsManager().setRenderer(renderer);
  }

  /**
   * Returns the {@link Grid.Column} that hosts the actions, or {@code null} if the active renderer
   * does not use a column (e.g. a context-menu renderer). For column-based renderers the column is
   * created on demand, hidden when no actions are registered, and made visible automatically when
   * the first action is added.
   *
   * @return the actions column, or {@code null} if the active renderer does not use a column
   */
  default Grid.Column<T> getActionsColumn() {
    return getRowActionsManager().getActionsColumn();
  }

  /**
   * Schedules a renderer rebuild on the next {@code beforeClientResponse} cycle. Call this after
   * mutating the configuration of any registered {@link EasyRowAction} (e.g. via
   * {@link EasyRowAction#visibleWhen}, {@link EasyRowAction#enabledWhen},
   * {@link EasyRowAction#tooltip}) to have the change reflected in the grid. Any previously
   * scheduled rebuild is cancelled and replaced.
   */
  default void refreshRowActions() {
    getRowActionsManager().refresh();
  }

}
