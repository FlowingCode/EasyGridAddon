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
import lombok.NonNull;

/**
 * A {@link RowActionsRenderer} that presents row actions as a right-click context menu using
 * {@link GridContextMenu}. The menu opens on the grid's default right-click gesture. This renderer
 * does not create a {@link Grid.Column}; {@link #getColumn()} always returns {@code null}.
 *
 * @param <T> the grid bean type
 * @author Javier Godoy / Flowing Code
 */
@SuppressWarnings("serial")
final class ContextMenuRowActionsRenderer<T> extends AbstractContextMenuRowActionsRenderer<T> {

  ContextMenuRowActionsRenderer(@NonNull Grid<T> grid) {
    super(grid);
  }

}
