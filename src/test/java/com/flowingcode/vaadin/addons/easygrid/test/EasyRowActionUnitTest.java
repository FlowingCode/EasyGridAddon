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
package com.flowingcode.vaadin.addons.easygrid.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.flowingcode.vaadin.addons.easygrid.EasyGrid;
import com.flowingcode.vaadin.addons.easygrid.actions.EasyRowAction;
import com.flowingcode.vaadin.addons.easygrid.actions.RowActionsStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.function.SerializableConsumer;
import java.util.Locale;
import java.util.stream.IntStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Browser-free unit tests for the row-actions column lifecycle: when the actions column is created,
 * shown, hidden, and replaced. {@link EasyGrid#getActionsColumn()} forces the deferred renderer
 * rebuild to run synchronously, so the assertions can read server-side {@link Grid.Column} state
 * without a browser.
 *
 * <p>Assertions that depend on rendered DOM, menu overlays, or pointer/keyboard gestures live in
 * the integration test instead, as they cannot be reproduced without a browser.
 *
 * @author Javier Godoy / Flowing Code
 */
public class EasyRowActionUnitTest {

  private static final SerializableConsumer<Integer> NOP = item -> {};

  private EasyGrid<Integer> grid;

  @Before
  public void setUp() {
    // EasyGrid construction/formatting may consult UI.getCurrent(); mock one as in
    // EasyGridConstructionTest. The grid is never attached, so deferred renderer updates are forced
    // explicitly through getActionsColumn() rather than a beforeClientResponse round trip.
    UI ui = Mockito.mock(UI.class);
    Mockito.when(ui.getLocale()).thenReturn(Locale.ENGLISH);
    UI.setCurrent(ui);

    grid = new EasyGrid<>(Integer.class, false);
    grid.setItems(IntStream.rangeClosed(1, 10).boxed().toList());
    grid.getWrappedGrid().addColumn(x -> x);
  }

  @After
  public void tearDown() {
    UI.setCurrent(null);
  }

  // The actions column exists (non-null) and is shown. getActionsColumn() also forces any pending
  // renderer rebuild to run synchronously, standing in for the beforeClientResponse round trip a
  // live UI would otherwise perform.
  private boolean isActionsColumnVisible() {
    Grid.Column<Integer> column = grid.getActionsColumn();
    return column != null && column.isVisible();
  }

  // Total grid column count, forcing a pending renderer (re)build first so the actions/trigger
  // column is materialized before counting.
  private int columnCount() {
    grid.getActionsColumn();
    return grid.getWrappedGrid().getColumns().size();
  }

  @Test
  public void testActionsColumnVisibility() {
    // column not visible before any action is added
    assertFalse(isActionsColumnVisible());

    // column becomes visible after the first action
    grid.addRowAction(VaadinIcon.VAADIN_H, NOP);
    assertTrue(isActionsColumnVisible());
  }

  @Test
  public void testActionRemove() {
    EasyRowAction<Integer> action = grid.addRowAction(VaadinIcon.VAADIN_H, NOP);
    assertTrue(isActionsColumnVisible());

    // removing the last action hides the column
    action.remove();
    assertFalse(isActionsColumnVisible());
  }

  @Test
  public void testDropdownActionRemove() {
    grid.setRowActionsStyle(RowActionsStyle.DROPDOWN);
    EasyRowAction<Integer> action = grid.addRowAction("Edit", NOP);

    // the trigger column is visible while an action is registered
    assertTrue(isActionsColumnVisible());

    // removing the last action auto-hides the trigger column (as in inline mode)
    action.remove();
    assertFalse(isActionsColumnVisible());
  }

  @Test
  public void testDropdownWithoutActionsDoesNotShowColumn() {
    // selecting the dropdown style with no registered actions must not surface the trigger column
    grid.setRowActionsStyle(RowActionsStyle.DROPDOWN);
    assertFalse(isActionsColumnVisible());

    // the trigger column appears once the first action is registered
    grid.addRowAction("Edit", NOP);
    assertTrue(isActionsColumnVisible());
  }

  @Test
  public void testRefreshWithoutActionsDoesNotShowColumn() {
    // a renderer rebuild with no registered actions must not surface an empty column
    grid.refreshRowActions();
    assertFalse(isActionsColumnVisible());
  }

  @Test
  public void testRendererSwitchWithoutActionsDoesNotShowColumn() {
    // a menu-mode round trip with no registered actions must not surface an empty column
    grid.setRowActionsStyle(RowActionsStyle.CONTEXT_MENU);
    grid.setRowActionsStyle(RowActionsStyle.INLINE_BUTTONS);
    assertFalse(isActionsColumnVisible());
  }

  @Test
  public void testColumnReplacedOnRendererSwitch() {
    grid.setRowActionsStyle(RowActionsStyle.DROPDOWN);
    grid.addRowAction("Edit", NOP);

    // baseline: value column + the dropdown trigger column
    assertEquals(2, columnCount());

    // switch to inline buttons
    grid.setRowActionsStyle(RowActionsStyle.INLINE_BUTTONS);

    // the trigger column was replaced, not left behind: still value + a single actions column
    assertEquals(2, columnCount());
  }

  @Test
  public void testColumnRemovedSwitchingToContextMenu() {
    grid.setRowActionsStyle(RowActionsStyle.DROPDOWN);
    grid.addRowAction("Edit", NOP);

    // baseline: value column + the dropdown trigger column
    assertEquals(2, columnCount());

    // switching to the context-menu style removes the trigger column and adds none in its place
    grid.setRowActionsStyle(RowActionsStyle.CONTEXT_MENU);
    assertEquals(1, columnCount());
  }

}
