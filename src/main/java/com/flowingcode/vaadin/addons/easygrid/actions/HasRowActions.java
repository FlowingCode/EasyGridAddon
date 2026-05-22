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

  default EasyRowAction<T> addRowAction(@NonNull String label,
      @NonNull SerializableConsumer<T> handler) {
    return addRowAction(label, (ValueProvider<T,Icon>) null, handler);
  }

  default EasyRowAction<T> addRowAction(
      String label,
      Icon icon,
      @NonNull SerializableConsumer<T> handler) {
    return addRowAction(label, Constant.ofNullable(icon), handler);
  }

  default EasyRowAction<T> addRowAction(
      @NonNull Icon icon,
      @NonNull SerializableConsumer<T> handler) {
    return addRowAction(null, icon, handler);
  }

  default EasyRowAction<T> addRowAction(
      String label, 
      @NonNull IconFactory iconFactory,
      @NonNull SerializableConsumer<T> handler) {
    return addRowAction(label, iconFactory.create(), handler);
  }

  default EasyRowAction<T> addRowAction(
      @NonNull IconFactory iconFactory,
      @NonNull SerializableConsumer<T> handler) {
    return addRowAction(null, iconFactory.create(), handler);
  }


  default <ICON extends AbstractIcon<ICON>> EasyRowAction<T> addRowAction(
      @NonNull ValueProvider<T, ICON> iconProvider,
      @NonNull SerializableConsumer<T> handler) {
    return addRowAction(null, iconProvider, handler);
  }

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
   * Returns the {@code Grid.Column} that hosts the actions, creating it if it has not been added
   * yet. The column is created hidden when no actions have been registered; it becomes visible
   * automatically when the first action is added.
   *
   * @return the actions column
   */
  default Grid.Column<T> getActionsColumn() {
    return getRowActionsManager().getActionsColumn();
  }

}
