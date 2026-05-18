package com.flowingcode.vaadin.addons.easygrid.actions;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.IconFactory;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.function.ValueProvider;
import lombok.NonNull;

public interface HasRowActions<T> {

  RowActionsManager<T> getRowActionsManager();

  default EasyRowAction<T> addRowAction(@NonNull String label,
      @NonNull SerializableConsumer<T> handler) {
    return addRowAction(label, (ValueProvider<T, Component>) null, handler);
  }

  default EasyRowAction<T> addRowAction(String label, @NonNull IconFactory iconFactory,
      @NonNull SerializableConsumer<T> handler) {
    return addRowAction(label, t -> iconFactory.create(), handler);
  }

  default EasyRowAction<T> addRowAction(String label,
      @NonNull SerializableSupplier<Component> iconFactory,
      @NonNull SerializableConsumer<T> handler) {
    return addRowAction(label, t -> iconFactory.get(), handler);
  }

  default EasyRowAction<T> addRowAction(String label,
      ValueProvider<T, Component> iconProvider,
      @NonNull SerializableConsumer<T> handler) {
    return getRowActionsManager().addRowAction(label, iconProvider, handler);
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
