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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.flowingcode.vaadin.addons.easygrid.actions.RowActionsStyle;
import com.flowingcode.vaadin.testbench.rpc.HasRpcSupport;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import com.vaadin.flow.component.contextmenu.testbench.ContextMenuElement;
import com.vaadin.flow.component.contextmenu.testbench.ContextMenuItemElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;
import java.time.Duration;
import java.util.List;
import lombok.experimental.ExtensionMethod;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

class ElementQueryExtension {
  public static <T extends TestBenchElement> T waitForSingle(ElementQuery<T> q) {
    // https://github.com/vaadin/testbench/issues/2189
    q.waitForFirst();
    return q.single();
  }
}

/**
 * Integration tests for EasyRowAction.
 *
 * @author Javier Godoy / Flowing Code
 */
@ExtensionMethod(ElementQueryExtension.class)
public class EasyRowActionIT extends AbstractViewTest implements HasRpcSupport {

  private EasyRowActionITCallables $server = createCallableProxy(EasyRowActionITCallables.class);

  private GridElement grid;

  public EasyRowActionIT() {
    super(EasyRowActionITView.ROUTE);
  }

  @Before
  public void before() {
    grid = $(GridElement.class).waitForSingle();
  }

  // In V24 the menu items are read from the <vaadin-context-menu-overlay> via
  // ContextMenuOverlayElement; in V25 that overlay element is gone (ContextMenuOverlayElement is
  // deprecated and now maps to <vaadin-context-menu>) and items are read from ContextMenuElement.
  // Resolve the version-appropriate, non-deprecated element class by name and invoke its
  // getMenuItems() reflectively so this compiles and runs against either Vaadin version.
  private List<ContextMenuItemElement> getContextMenuItems() {
    int major = $server.getVersion().getMajorVersion();
    TestBenchElement element = major >= 25
        ? $("body").waitForSingle().findElement(By.tagName("vaadin-context-menu"))
        : $("vaadin-context-menu-overlay").waitForSingle();

    return element.$(ContextMenuItemElement.class).all();
  }

  // Whether a context menu overlay is currently open. In V24 the open overlay is a separate
  // <vaadin-context-menu-overlay> element, while in V25 it is a child of <vaadin-context-menu>;
  // rather than probe for either DOM shape, rely on the "opened" attribute exposed through
  // ContextMenuElement.isOpen(), which maps to <vaadin-context-menu> in both versions.
  // In V24, a stale element reference may occur during menu closure as the overlay is detached;
  // treat such an exception as "not open" since the element is being disposed.
  private boolean isContextMenuOpen() {
    try {
      return $(ContextMenuElement.class).all().stream()
            .anyMatch(e -> e.isOpen() || e.getPropertyBoolean("opened") == Boolean.TRUE);
    } catch (StaleElementReferenceException e) {
      return false;
    }
  }

  // Opens the DROPDOWN overflow menu for the given row by clicking its trigger button and
  // waits until the menu overlay is open. The dropdown renderer hosts the trigger in a dedicated
  // column (index 1, after the value column at index 0); unlike CONTEXT_MENU, the menu opens only
  // from this button, never from a row right-click. The backing GridContextMenu is the same kind
  // used by CONTEXT_MENU, so items are read through getContextMenuItems().
  private void openDropdownMenu(int rowIndex) {
    grid.getCell(rowIndex, 1).$("vaadin-button").single().click();
    waitUntil(d -> isContextMenuOpen());
  }

  // Closes any open menu overlay via ESCAPE and waits until it is gone, so the next open starts
  // from a known-closed state.
  private void closeMenu() {
    new org.openqa.selenium.interactions.Actions(getDriver()).sendKeys(Keys.ESCAPE).perform();
    waitUntil(d -> !isContextMenuOpen());
  }

  // Grace period during which an overlay that must never appear would have appeared. Asserting
  // absence has no positive signal to wait on, so a short fixed sleep is the pragmatic choice.
  private void sleepGrace() {
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    }
  }

  @Test
  public void testActionInvocation() {
    $server.addRowAction(VaadinIcon.VAADIN_H, $server.action(1));

    assertNull($server.getClickedValue());

    // Items are 1-10; row index 2 = item 3 (value intentionally differs from row index)
    grid.getCell(2, 1).$("vaadin-button").single().click();

    assertEquals(Integer.valueOf(3), $server.getClickedValue());
  }

  @Test
  public void testVisibleWhen() {
    var action = $server.addRowAction(VaadinIcon.VAADIN_H, $server.action(1));
    action.visibleWhen(x -> x % 2 == 0); // visible only for even items

    // button absent in an odd-item row (row 0 = item 1)
    assertTrue(grid.getCell(0, 1).$("vaadin-button").all().isEmpty());

    // button present in an even-item row of the same grid (row 1 = item 2)
    assertFalse(grid.getCell(1, 1).$("vaadin-button").all().isEmpty());

    // clearing visibleWhen makes the button visible in every row
    action.visibleWhen(null);
    assertFalse(grid.getCell(0, 1).$("vaadin-button").all().isEmpty());
  }

  @Test
  public void testMultipleActionsInvocation() {
    $server.addRowAction(VaadinIcon.VAADIN_H, $server.action(1));
    $server.addRowAction(VaadinIcon.VAADIN_H, $server.action(2));

    // first action in row 2 (item 3)
    grid.getCell(2, 1).$("vaadin-button").get(0).click();
    assertEquals(Integer.valueOf(3), $server.getClickedValue());
    assertEquals(Integer.valueOf(1), $server.getClickedAction());

    // second action in row 4 (item 5)
    grid.getCell(4, 1).$("vaadin-button").get(1).click();
    assertEquals(Integer.valueOf(5), $server.getClickedValue());
    assertEquals(Integer.valueOf(2), $server.getClickedAction());
  }

  @Test
  public void testEnabledWhen() {
    var action = $server.addRowAction(VaadinIcon.VAADIN_H, $server.action(1));
    action.enabledWhen(x -> x % 2 == 0); // enabled only for even items
    $server.refreshRowActions();

    // disabled attribute present in odd row (row 0 = item 1)
    assertNotNull(grid.getCell(0, 1).$("vaadin-button").single().getAttribute("disabled"));

    // clicking enabled button fires the handler (row 1 = item 2)
    grid.getCell(1, 1).$("vaadin-button").single().click();
    assertEquals(Integer.valueOf(2), $server.getClickedValue());

    // clicking disabled button does not fire the handler
    grid.getCell(0, 1).$("vaadin-button").single().click();
    assertEquals(Integer.valueOf(2), $server.getClickedValue()); // unchanged
  }

  @Test
  public void testConfirmation() {
    var action = $server.addRowAction(VaadinIcon.VAADIN_H, $server.action(1));
    action.withConfirmation("Confirm", "Proceed?");

    // cancel does not fire the handler
    grid.getCell(0, 1).$("vaadin-button").single().click();
    $(ConfirmDialogElement.class).waitForSingle().getCancelButton().click();
    assertNull($server.getClickedValue());

    // click opens the dialog; confirming fires the handler
    grid.getCell(0, 1).$("vaadin-button").single().click();
    $(ConfirmDialogElement.class).waitForSingle();
    assertNull($server.getClickedValue());
    $(ConfirmDialogElement.class).waitForSingle().getConfirmButton().click();
    assertEquals(Integer.valueOf(1), $server.getClickedValue());

    // second click while dialog is open does not open a second dialog
    grid.getCell(0, 1).$("vaadin-button").single().click();
    $(ConfirmDialogElement.class).waitForSingle(); // ensure dialog is open
    grid.getCell(0, 1).$("vaadin-button").single().click();
    assertEquals(1, $(ConfirmDialogElement.class).all().size());
  }

  @Test
  public void testContextMenu() {
    $server.setRowActionsStyle(RowActionsStyle.CONTEXT_MENU);
    $server.addRowAction("Edit", $server.action(1));

    // no inline buttons (no actions column)
    assertTrue(grid.getCell(0, 0).$("vaadin-button").all().isEmpty());

    // right-click opens the context menu
    grid.getCell(0, 0).contextClick();
    var items = getContextMenuItems();

    // one menu item for the registered action
    assertEquals(1, items.size());

    // clicking the item fires the handler with the correct item (row 0 = item 1)
    items.get(0).click();
    assertEquals(Integer.valueOf(1), $server.getClickedValue());
  }

  @Test
  public void testContextMenuVisibleWhen() {
    $server.setRowActionsStyle(RowActionsStyle.CONTEXT_MENU);
    $server.addRowAction("Edit", $server.action(1));
    $server.addRowAction("Delete", $server.action(2))
        .visibleWhen(x -> x % 2 == 0); // visible only for even items

    // Delete absent for odd row (row 0 = item 1); only Edit shown
    grid.getCell(0, 0).contextClick();
    assertEquals(1, getContextMenuItems().size());
    closeMenu();

    // even row (row 1 = item 2): both items shown
    grid.getCell(1, 0).contextClick();
    assertEquals(2, getContextMenuItems().size());
  }

  @Test
  public void testContextMenuEnabledWhen() {
    $server.setRowActionsStyle(RowActionsStyle.CONTEXT_MENU);
    $server.addRowAction("Edit", $server.action(1))
        .enabledWhen(x -> x % 2 == 0); // enabled only for even items

    // menu item disabled for odd row (row 0 = item 1)
    grid.getCell(0, 0).contextClick();
    var item = getContextMenuItems().get(0);
    assertNotNull(item.getAttribute("disabled"));
  }

  @Test
  public void testContextMenuConfirmation() throws InterruptedException {
    $server.setRowActionsStyle(RowActionsStyle.CONTEXT_MENU);
    $server.addRowAction("Edit", $server.action(1)).withConfirmation("Confirm", "Proceed?");

    // selecting the menu item opens the confirmation dialog; cancelling does not fire the handler
    grid.getCell(0, 0).contextClick();
    getContextMenuItems().get(0).click();
    var dialog = $(ConfirmDialogElement.class).waitForSingle();
    dialog.getCancelButton().click();
    assertNull($server.getClickedValue());

    new WebDriverWait(getDriver(),
        Duration.ofSeconds(1))
            .until(ExpectedConditions
                .numberOfElementsToBe(By.tagName("vaadin-confirm-dialog-overlay"), 0));

    // selecting it again and confirming fires the handler (row 0 = item 1)
    grid.getCell(0, 0).contextClick();
    getContextMenuItems().get(0).click();
    $(ConfirmDialogElement.class).waitForSingle().getConfirmButton().click();
    assertEquals(Integer.valueOf(1), $server.getClickedValue());
  }

  @Test
  public void testDropdown() {
    $server.setRowActionsStyle(RowActionsStyle.DROPDOWN);
    $server.addRowAction("Edit", $server.action(1));

    // a single overflow trigger button is rendered in the actions column — the actions
    // themselves surface through the menu, not as inline buttons
    assertEquals(1, grid.getCell(2, 1).$("vaadin-button").all().size());

    // clicking the trigger opens the menu bound to that row (row 2 = item 3)
    openDropdownMenu(2);
    var items = getContextMenuItems();

    // one menu item for the registered action
    assertEquals(1, items.size());

    // clicking the item fires the handler with the trigger row's item
    items.get(0).click();
    assertEquals(Integer.valueOf(3), $server.getClickedValue());
  }

  @Test
  public void testDropdownVisibleWhen() {
    $server.setRowActionsStyle(RowActionsStyle.DROPDOWN);
    $server.addRowAction("Edit", $server.action(1));
    $server.addRowAction("Delete", $server.action(2))
        .visibleWhen(x -> x % 2 == 0); // visible only for even items

    // regardless of how many actions are registered, the column shows exactly one trigger button
    // (unlike inline mode, which renders one button per action)
    assertEquals(1, grid.getCell(0, 1).$("vaadin-button").all().size());

    // Delete absent for odd row (row 0 = item 1); only Edit shown
    openDropdownMenu(0);
    assertEquals(1, getContextMenuItems().size());
    closeMenu();

    // even row (row 1 = item 2): both items shown
    openDropdownMenu(1);
    assertEquals(2, getContextMenuItems().size());
  }

  @Test
  public void testDropdownEnabledWhen() {
    $server.setRowActionsStyle(RowActionsStyle.DROPDOWN);
    $server.addRowAction("Edit", $server.action(1))
        .enabledWhen(x -> x % 2 == 0); // enabled only for even items

    // menu item disabled for odd row (row 0 = item 1)
    openDropdownMenu(0);
    var item = getContextMenuItems().get(0);
    assertNotNull(item.getAttribute("disabled"));
  }

  @Test
  public void testDropdownConfirmation() {
    $server.setRowActionsStyle(RowActionsStyle.DROPDOWN);
    $server.addRowAction("Edit", $server.action(1)).withConfirmation("Confirm", "Proceed?");

    // selecting the overflow menu item opens the confirmation dialog; cancelling does not fire
    openDropdownMenu(0);
    getContextMenuItems().get(0).click();
    $(ConfirmDialogElement.class).waitForSingle().getCancelButton().click();
    assertNull($server.getClickedValue());

    // selecting it again and confirming fires the handler (row 0 = item 1)
    openDropdownMenu(0);
    getContextMenuItems().get(0).click();
    $(ConfirmDialogElement.class).waitForSingle().getConfirmButton().click();
    assertEquals(Integer.valueOf(1), $server.getClickedValue());
  }

  @Test
  public void testDropdownSuppressesContextMenu() {
    $server.setRowActionsStyle(RowActionsStyle.DROPDOWN);
    $server.addRowAction("Edit", $server.action(1));

    // the default right-click gesture stays suppressed: right-clicking a data cell opens nothing
    grid.getCell(0, 0).contextClick();
    sleepGrace();
    assertFalse(isContextMenuOpen());

    // nor does a left click anywhere else in the row open the menu
    grid.getCell(0, 0).click();
    sleepGrace();
    assertFalse(isContextMenuOpen());

    // positive control: the overflow trigger button is the only thing that opens the menu
    openDropdownMenu(0);
    assertEquals(1, getContextMenuItems().size());
  }

  @Test
  public void testDropdownRemovedOnRendererSwitch() {
    $server.setRowActionsStyle(RowActionsStyle.DROPDOWN);
    $server.addRowAction("Edit", $server.action(1));

    // baseline: value column + the dropdown trigger column
    assertEquals(2, $server.getColumnCount());

    // positive control: the trigger opens the menu while in dropdown mode
    openDropdownMenu(0);
    closeMenu();

    // switch to inline buttons and wait for the actions column to render
    $server.setRowActionsStyle(RowActionsStyle.INLINE_BUTTONS);
    waitUntil(d -> {
      try {
        return !grid.getCell(0, 1).$("vaadin-button").all().isEmpty();
      } catch (RuntimeException e) {
        return false;
      }
    });

    // the trigger column was replaced, not left behind: still value + a single actions column
    assertEquals(2, $server.getColumnCount());

    // and that column now hosts a working inline action button (row 0 = item 1), not the old trigger
    grid.getCell(0, 1).$("vaadin-button").single().click();
    assertEquals(Integer.valueOf(1), $server.getClickedValue());
  }

  @Test
  public void testDropdownToContextMenuSwitch() {
    $server.setRowActionsStyle(RowActionsStyle.DROPDOWN);
    $server.addRowAction("Edit", $server.action(1));

    // baseline: value column + the dropdown trigger column
    assertEquals(2, $server.getColumnCount());

    // switch to the context-menu style
    $server.setRowActionsStyle(RowActionsStyle.CONTEXT_MENU);

    // the trigger column is removed and nothing replaces it: only the value column remains
    assertEquals(1, $server.getColumnCount());

    // the actions now open the context-menu way, on right-click
    grid.getCell(0, 0).contextClick();
    var items = getContextMenuItems();
    assertEquals(1, items.size());
    items.get(0).click();
    assertEquals(Integer.valueOf(1), $server.getClickedValue());
  }

  @Test
  public void testRefreshRowActions() {
    var action = $server.addRowAction(VaadinIcon.VAADIN_H, $server.action(1));

    // baseline: the button carries only the default variant, not "error"
    assertFalse(
        grid.getCell(0, 1).$("vaadin-button").single().getAttribute("theme").contains("error"));

    // An element-level change (a theme variant) is NOT applied automatically — unlike the fluent
    // mutators (visibleWhen/enabledWhen/tooltip), which self-refresh.
    action.addThemeVariants(ButtonVariant.LUMO_ERROR);
    assertFalse(
        grid.getCell(0, 1).$("vaadin-button").single().getAttribute("theme").contains("error"));

    // It takes effect only after an explicit refreshRowActions().
    $server.refreshRowActions();
    assertTrue(
        grid.getCell(0, 1).$("vaadin-button").single().getAttribute("theme").contains("error"));
  }

  @Test
  public void testThemeVariants() {
    $server.addRowAction(VaadinIcon.VAADIN_H, $server.action(1));

    // default tertiary-inline variant applied
    var theme = grid.getCell(0, 1).$("vaadin-button").single().getAttribute("theme");
    assertNotNull(theme);
    assertTrue(theme.contains("tertiary-inline"));

    // extra variant combined with the default
    var action2 = $server.addRowAction(VaadinIcon.VAADIN_H, $server.action(2));
    action2.addThemeVariants(ButtonVariant.LUMO_ERROR);
    $server.refreshRowActions();
    var theme2 = grid.getCell(0, 1).$("vaadin-button").get(1).getAttribute("theme");
    assertNotNull(theme2);
    assertTrue(theme2.contains("tertiary-inline"));
    assertTrue(theme2.contains("error"));

    // setDefaultRowActionVariants overrides the default for subsequently added actions
    $server.setDefaultRowActionVariants(ButtonVariant.LUMO_ERROR);
    $server.addRowAction(VaadinIcon.VAADIN_H, $server.action(3));
    var theme3 = grid.getCell(0, 1).$("vaadin-button").get(2).getAttribute("theme");
    assertNotNull(theme3);
    assertTrue(theme3.contains("error"));
    assertFalse(theme3.contains("tertiary-inline"));
  }

  @Test
  public void testContextMenuRemovedOnRendererSwitch() {
    $server.setRowActionsStyle(RowActionsStyle.CONTEXT_MENU);
    $server.addRowAction("Edit", $server.action(1));

    // positive control: the context menu opens while in menu mode
    grid.getCell(0, 0).contextClick();
    waitUntil(d -> isContextMenuOpen());
    new org.openqa.selenium.interactions.Actions(getDriver()).sendKeys(Keys.ESCAPE).perform();
    waitUntil(d -> !isContextMenuOpen());

    // switch back to inline buttons and wait for the actions column to render
    $server.setRowActionsStyle(RowActionsStyle.INLINE_BUTTONS);
    waitUntil(d -> {
      try {
        return !grid.getCell(0, 1).$("vaadin-button").all().isEmpty();
      } catch (RuntimeException e) {
        return false;
      }
    });

    // the replaced renderer must have unwired the context menu from the grid
    grid.getCell(0, 0).contextClick();
    sleepGrace();
    assertFalse(isContextMenuOpen());
  }

}
