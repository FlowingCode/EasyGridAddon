package com.flowingcode.vaadin.addons.easygrid.it;

import com.flowingcode.vaadin.testbench.rpc.RmiCallable;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.IconFactory;
import com.vaadin.flow.function.SerializableConsumer;

public interface EasyRowActionITCallables extends RmiCallable {

  SerializableConsumer<Integer> action(int action);

  Integer getClickedValue();

  Integer getClickedAction();

  void setRowActionsAsMenu(boolean asMenu);

  void setRowActionVariants(ButtonVariant variant);

  void refreshRowActions();

  boolean isActionsColumnVisible();

  RmiEasyRowAction<Integer> addRowAction(String label,
      SerializableConsumer<Integer> handler);
  //
  // RmiEasyRowAction<Integer> addRowAction(
  // String label,
  // Icon icon,
  // SerializableConsumer<Integer> handler);
  //
  // RmiEasyRowAction<Integer> addRowAction(
  // Icon icon,
  // SerializableConsumer<Integer> handler);
  //
  // RmiEasyRowAction<Integer> addRowAction(
  // String label,
  // IconFactory iconFactory,
  // SerializableConsumer<Integer> handler);
  //
  RmiEasyRowAction<Integer> addRowAction(
      IconFactory iconFactory,
      SerializableConsumer<Integer> handler);
  //
  // <ICON extends AbstractIcon<ICON>> RmiEasyRowAction<Integer> addRowAction(
  // ValueProvider<Integer, ICON> iconProvider,
  // SerializableConsumer<Integer> handler);
  //
  // <ICON extends AbstractIcon<ICON>> RmiEasyRowAction<Integer> addRowAction(
  // String label,
  // ValueProvider<Integer, ICON> iconProvider,
  // SerializableConsumer<Integer> handler);
  //
  // void setRowActionsAsMenu(boolean asMenu);
  //
  // // Column<Intger> getActionsColumn();
  //
  // void refreshRowActions();

}
