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

  void setDefaultRowActionVariants(ButtonVariant variant);

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
