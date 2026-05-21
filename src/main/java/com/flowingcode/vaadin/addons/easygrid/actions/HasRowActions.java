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

  RowActionsManager<T> getRowActionsManager();

  default EasyRowAction<T> addRowAction(@NonNull String label,
      @NonNull SerializableConsumer<T> handler) {
    return addRowAction(label, (ValueProvider<T, Icon>) null, handler);
  }

  default EasyRowAction<T> addRowAction(String label,
      Icon iconTemplate,
      @NonNull SerializableConsumer<T> handler) {
    return addRowAction(label, Constant.ofNullable(iconTemplate), handler);
  }

  default EasyRowAction<T> addRowAction(
      @NonNull Icon iconTemplate,
      @NonNull SerializableConsumer<T> handler) {
    return addRowAction(null, iconTemplate, handler);
  }

  default EasyRowAction<T> addRowAction(String label, @NonNull IconFactory iconFactory,
      @NonNull SerializableConsumer<T> handler) {
    return addRowAction(label, Constant.of(iconFactory.create()), handler);
  }

  default EasyRowAction<T> addRowAction(@NonNull IconFactory iconFactory,
      @NonNull SerializableConsumer<T> handler) {
    return addRowAction(null, Constant.of(iconFactory.create()), handler);
  }

  default <ICON extends AbstractIcon<ICON>> EasyRowAction<T> addRowAction(String label,
      ValueProvider<T, ICON> iconProvider,
      @NonNull SerializableConsumer<T> handler) {
    return getRowActionsManager().addRowAction(Constant.ofNullable(label), iconProvider, handler);
  }

  default <ICON extends AbstractIcon<ICON>> EasyRowAction<T> addRowAction(
      ValueProvider<T, ICON> iconProvider,
      @NonNull SerializableConsumer<T> handler) {
    return addRowAction(null, iconProvider, handler);
  }

  // Render all actions as a context menu (overflow menu) instead of inline buttons
  default void setRowActionsAsMenu(boolean asMenu) {
    getRowActionsManager().setRowActionsAsMenu(asMenu);
  }

  // Access the underlying Grid.Column for header, width, freezing, etc.
  default Grid.Column<T> getActionsColumn() {
    return getRowActionsManager().getActionsColumn();
  }

}
