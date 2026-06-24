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
 * Base class for {@link RowActionsRenderer} implementations that present row actions through a
 * {@link GridContextMenu}. Items are rebuilt dynamically for each row via
 * {@link GridContextMenu#setDynamicContentHandler}, so visibility, enabled state, and labels are
 * evaluated per-item at open time.
 *
 * <p>Subclasses control how the menu is created and triggered by overriding
 * {@link #createContextMenu()} and may host the actions in a {@link Grid.Column} by overriding
 * {@link #getColumn()}.
 *
 * @param <T> the grid bean type
 * @author Javier Godoy / Flowing Code
 */
@SuppressWarnings("serial")
abstract class AbstractContextMenuRowActionsRenderer<T> implements RowActionsRenderer<T> {

  /** The grid this renderer decorates. */
  protected final Grid<T> grid;

  private GridContextMenu<T> contextMenu;

  private List<EasyRowAction<T>> currentActions = List.of();

  AbstractContextMenuRowActionsRenderer(@NonNull Grid<T> grid) {
    this.grid = grid;
  }

  @Override
  public void update(List<EasyRowAction<T>> actions) {
    // action snapshot read by the dynamic content handler on each open
    currentActions = new ArrayList<>(actions);

    if (contextMenu == null) {
      var menu = contextMenu = createContextMenu();
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

  /**
   * Creates the {@link GridContextMenu} that backs this renderer. Invoked once, on the first
   * {@link #update(List)} call, before the dynamic content handler is installed. Subclasses may
   * override it to customize how the menu is triggered (for example, to suppress the default
   * right-click behavior). The default implementation registers a new context menu on the grid via
   * {@link Grid#addContextMenu()}.
   *
   * @return the context menu that backs this renderer
   */
  protected GridContextMenu<T> createContextMenu() {
    return grid.addContextMenu();
  }

  @Override
  public Grid.Column<T> getColumn() {
    return null;
  }

  @Override
  public void remove() {
    if (contextMenu != null) {
      contextMenu.setTarget(null);
      contextMenu.removeFromParent();
      contextMenu = null;
    }
  }

}
