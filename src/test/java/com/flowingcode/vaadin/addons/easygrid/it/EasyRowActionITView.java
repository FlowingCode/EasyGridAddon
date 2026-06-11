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

import com.flowingcode.vaadin.addons.easygrid.EasyGrid;
import com.flowingcode.vaadin.jsonmigration.InstrumentedRoute;
import com.flowingcode.vaadin.jsonmigration.LegacyClientCallable;
import com.flowingcode.vaadin.testbench.rpc.RmiRemote;
import com.flowingcode.vaadin.testbench.rpc.Version;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.IconFactory;
import com.vaadin.flow.component.internal.AllowInert;
import com.vaadin.flow.function.SerializableConsumer;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.experimental.Delegate;

@SuppressWarnings("serial")
@InstrumentedRoute(EasyRowActionITView.ROUTE)
public class EasyRowActionITView extends Div implements EasyRowActionITCallables {

  public static final String ROUTE = "it/EasyRowAction";
  private EasyGrid<Integer> grid;

  public static interface IState extends RmiRemote {
    Integer getClickedValue();

    Integer getClickedAction();

    default SerializableConsumer<Integer> action(int action) {
      return value -> {
        ((State) this).clickedValue = value;
        ((State) this).clickedAction = action;
      };
    }
  }

  @Getter
  private static class State implements IState {
    private Integer clickedValue;
    private Integer clickedAction;
  };

  @Delegate
  private State state = new State();

  public EasyRowActionITView() {
    grid = new EasyGrid<>(Integer.class, false);
    grid.setItems(IntStream.rangeClosed(1, 10).boxed().toList());
    grid.getWrappedGrid().addColumn(x -> x);
    add(grid);
  }

  @Override
  public SerializableConsumer<Integer> action(int action) {
    return state.action(action);
  }

  @Override
  @AllowInert
  @LegacyClientCallable
  public JsonValue $call(JsonObject invocation) {
    return EasyRowActionITCallables.super.$call(invocation);
  }

  @Override
  public Version getVersion() {
    return new Version();
  }

  @Override
  public RmiEasyRowAction<Integer> addRowAction(String label,
      SerializableConsumer<Integer> handler) {
    return RmiEasyRowAction.of(grid.addRowAction(label, handler));
  }

  @Override
  public void setRowActionsAsMenu(boolean asMenu) {
    grid.setRowActionsAsMenu(asMenu);
  }

  @Override
  public void setDefaultRowActionVariants(ButtonVariant variant) {
    grid.setDefaultRowActionVariants(variant);
  }

  @Override
  public void refreshRowActions() {
    grid.refreshRowActions();
  }

  @Override
  public boolean isActionsColumnVisible() {
    var column = grid.getActionsColumn();
    return column != null && column.isVisible();
  }

  @Override
  public RmiEasyRowAction<Integer> addRowAction(
      IconFactory iconFactory,
      SerializableConsumer<Integer> handler) {
    var action = grid.addRowAction(iconFactory, handler);
    return RmiEasyRowAction.of(action);
  }

}
