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
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;

/**
 * A {@link RowActionsRenderer} that presents row actions as a right-click context menu using
 * {@link GridContextMenu}. Items are rebuilt dynamically for each row via
 * {@link GridContextMenu#setDynamicContentHandler}, so visibility, enabled state, and labels are
 * evaluated per-item at open time. This renderer does not create a {@link Grid.Column};
 * {@link #getColumn()} always returns {@code null}.
 *
 * @param <T> the grid bean type
 */
@SuppressWarnings("serial")
final class ContextMenuRowActionsRenderer<T> implements RowActionsRenderer<T> {

  private final Grid<T> grid;
  private GridContextMenu<T> contextMenu;
  private List<EasyRowAction<T>> currentActions;

  ContextMenuRowActionsRenderer(@NonNull Grid<T> grid) {
    this.grid = grid;
  }

  @Override
  public void update(List<EasyRowAction<T>> actions) {
    currentActions = new ArrayList<>(actions);
    if (contextMenu == null) {
      var menu = contextMenu = grid.addContextMenu();
      // item is the row bean for which the menu is opening; it is the same object for every
      // menu item built in this handler invocation, so capturing it in the click listeners is safe.
      menu.setDynamicContentHandler(item -> {
        if (item == null) {
          return false;
        }
        menu.removeAll();
        for (EasyRowAction<T> action : currentActions) {
          if (action.isVisible(item)) {
            String label = action.getLabel(item);
            var icon = action.getIcon(item);
            var menuItem = (label != null)
                ? menu.addItem(label, e -> action.execute(item))
                : menu.addItem(icon, e -> action.execute(item));
            menuItem.setEnabled(action.isEnabled(item));
            if (label != null && icon != null) {
              menuItem.addComponentAsFirst(icon);
            }
          }
        }
        return !menu.getItems().isEmpty();
      });
    }
  }

  @Override
  public Grid.Column<T> getColumn() {
    return null;
  }

  @Override
  public void remove() {
    if (contextMenu != null) {
      contextMenu.getElement().removeFromParent();
      contextMenu = null;
    }
    currentActions = List.of();
  }

}
