package com.flowingcode.vaadin.addons.easygrid.actions;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.AbstractIcon;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.IconFactory;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.ValueProvider;
import lombok.NonNull;

public interface HasRowActions<T> {

  RowActionsManager<T> getRowActionsManager();

  default EasyRowAction<T> addRowAction(String label,
      Icon iconTemplate,
      @NonNull SerializableConsumer<T> handler) {
    return addRowAction(label, Constant.of(iconTemplate), handler);
  }

  default EasyRowAction<T> addRowAction(@NonNull String label,
      @NonNull SerializableConsumer<T> handler) {
    return addRowAction(label, (ValueProvider<T, Icon>) null, handler);
  }

  default EasyRowAction<T> addRowAction(String label, @NonNull IconFactory iconFactory,
      @NonNull SerializableConsumer<T> handler) {
    return addRowAction(label, t -> iconFactory.create(), handler);
  }

  // default EasyRowAction<T> addRowAction(String label,
  // @NonNull SerializableSupplier<Icon> iconFactory,
  // @NonNull SerializableConsumer<T> handler) {
  // return addRowAction(label, Constant.of(iconFactory.get()), handler);
  // }

  default <ICON extends AbstractIcon<ICON>> EasyRowAction<T> addRowAction(String label,
      ValueProvider<T, ICON> iconProvider,
      @NonNull SerializableConsumer<T> handler) {
    return getRowActionsManager().addRowAction(Constant.ofNullable(label), iconProvider, handler);
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
