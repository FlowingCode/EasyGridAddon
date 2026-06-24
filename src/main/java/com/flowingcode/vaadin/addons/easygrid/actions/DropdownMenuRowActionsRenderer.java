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
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.function.ValueProvider;
import java.util.List;
import lombok.NonNull;

/**
 * A {@link RowActionsRenderer} that presents row actions through an overflow ("⋮") menu. A
 * dedicated {@link Grid.Column} hosts a trigger button in each row; clicking it opens a
 * {@link GridContextMenu} whose items are rebuilt dynamically per row. The default right-click
 * gesture is suppressed so the menu opens only from the trigger button. {@link #getColumn()}
 * returns the trigger column.
 *
 * @param <T> the grid bean type
 * @author Javier Godoy / Flowing Code
 */
@SuppressWarnings("serial")
final class DropdownMenuRowActionsRenderer<T> extends AbstractContextMenuRowActionsRenderer<T> {

  private Grid.Column<T> column;

  DropdownMenuRowActionsRenderer(@NonNull Grid<T> grid) {
    super(grid);
  }

  @Override
  public void update(List<EasyRowAction<T>> actions) {
    if (column == null) {
      var builder = LitRendererBuilder.<T>staticOnly();
      ValueProvider<T, Icon> dots = Constant.of(VaadinIcon.ELLIPSIS_DOTS_V.create());
      // The trigger button must open the GridContextMenu programmatically, but GridContextMenu
      // exposes no public API to open it at a given pointer event. Re-route the click into the
      // connector's own open path ($contextMenuTargetConnector.openOnHandler), after clearing
      // preventContextMenu so the open is allowed. This relies on Vaadin connector internals;
      // validated against Vaadin 24.10.x and 25.1.x and covered end-to-end by
      // EasyRowActionIT.testDropdown, which fails loudly if the connector contract changes.
      String handler = """
          ev=>{const grid = ev.composedPath()
                              .find(el => el.matches?.('vaadin-grid-cell-content')).parentElement
               grid.preventContextMenu=false;
               grid.$contextMenuTargetConnector.openOnHandler.call(grid,ev);
              }
          """;
      var button = new EasyRowAction<T>(null, null, dots, handler);
      button.updateRenderer(builder);
      column = grid.addColumn(builder.build());
      column.setAutoWidth(true);
      column.setFlexGrow(0);
    }

    super.update(actions);
  }

  @Override
  protected GridContextMenu<T> createContextMenu() {
    var menu = super.createContextMenu();
    // DROPDOWN opens only from the trigger button, never from a row right-click, so drop the
    // connector's default contextmenu listener. The removal is deferred with setTimeout on
    // purpose: createContextMenu() runs in the same round-trip that wires up that listener, so a
    // synchronous removeListener() would run before the listener exists and remove nothing. The
    // macrotask delay guarantees the connector has finished init first. Same internal-API caveat
    // and version validation as the trigger handler; covered by
    // EasyRowActionIT.testDropdownSuppressesContextMenu.
    grid.getElement().executeJs("""
        setTimeout(()=>this.$contextMenuTargetConnector.removeListener());
        """);
    return menu;
  }

  @Override
  public Grid.Column<T> getColumn() {
    return column;
  }

  @Override
  public void remove() {
    // Tear down the trigger column in addition to the backing context menu, so switching away from
    // this renderer (or removing it) does not leave an orphaned column behind.
    super.remove();
    if (column != null) {
      grid.removeColumn(column);
      column = null;
    }
  }

}
