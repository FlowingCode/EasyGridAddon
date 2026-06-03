package com.flowingcode.vaadin.addons.easygrid.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.flowingcode.vaadin.testbench.rpc.HasRpcSupport;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import com.vaadin.flow.component.contextmenu.testbench.ContextMenuOverlayElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

public class EasyRowActionIT extends AbstractViewTest implements HasRpcSupport {

  private EasyRowActionITCallables $server = createCallableProxy(EasyRowActionITCallables.class);

  private GridElement grid;

  public EasyRowActionIT() {
    super(EasyRowActionITView.ROUTE);
  }

  @Before
  public void before() {
    grid = $(GridElement.class).waitForFirst();
  }

  @Test
  public void testActionInvocation() {
    $server.addRowAction(VaadinIcon.VAADIN_H, $server.action(1));

    assertNull($server.getClickedValue());

    // Items are 1-10; row index 2 = item 3 (value intentionally differs from row index)
    grid.getCell(2, 1).$("vaadin-button").first().click();

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
    assertNotNull(grid.getCell(0, 1).$("vaadin-button").first().getAttribute("disabled"));

    // clicking enabled button fires the handler (row 1 = item 2)
    grid.getCell(1, 1).$("vaadin-button").first().click();
    assertEquals(Integer.valueOf(2), $server.getClickedValue());

    // clicking disabled button does not fire the handler
    grid.getCell(0, 1).$("vaadin-button").first().click();
    assertEquals(Integer.valueOf(2), $server.getClickedValue()); // unchanged
  }

  @Test
  public void testConfirmation() {
    var action = $server.addRowAction(VaadinIcon.VAADIN_H, $server.action(1));
    action.withConfirmation("Confirm", "Proceed?");

    // cancel does not fire the handler
    grid.getCell(0, 1).$("vaadin-button").first().click();
    $(ConfirmDialogElement.class).waitForFirst().getCancelButton().click();
    assertNull($server.getClickedValue());

    // click opens the dialog; confirming fires the handler
    grid.getCell(0, 1).$("vaadin-button").first().click();
    $(ConfirmDialogElement.class).waitForFirst();
    assertNull($server.getClickedValue());
    $(ConfirmDialogElement.class).waitForFirst().getConfirmButton().click();
    assertEquals(Integer.valueOf(1), $server.getClickedValue());

    // second click while dialog is open does not open a second dialog
    grid.getCell(0, 1).$("vaadin-button").first().click();
    $(ConfirmDialogElement.class).waitForFirst(); // ensure dialog is open
    grid.getCell(0, 1).$("vaadin-button").first().click();
    assertEquals(1, $(ConfirmDialogElement.class).all().size());
  }

  @Test
  public void testContextMenu() {
    $server.setRowActionsAsMenu(true);
    $server.addRowAction("Edit", $server.action(1));

    // no inline buttons (no actions column)
    assertTrue(grid.getCell(0, 0).$("vaadin-button").all().isEmpty());

    // right-click opens the context menu
    grid.getCell(0, 0).contextClick();
    var overlay = $(ContextMenuOverlayElement.class).waitForFirst();

    // one menu item for the registered action
    assertEquals(1, overlay.getMenuItems().size());

    // clicking the item fires the handler with the correct item (row 0 = item 1)
    overlay.getMenuItems().get(0).click();
    assertEquals(Integer.valueOf(1), $server.getClickedValue());
  }

  @Test
  public void testContextMenuVisibleWhen() {
    $server.setRowActionsAsMenu(true);
    $server.addRowAction("Edit", $server.action(1));
    $server.addRowAction("Delete", $server.action(2))
        .visibleWhen(x -> x % 2 == 0); // visible only for even items

    // Delete absent for odd row (row 0 = item 1); only Edit shown
    grid.getCell(0, 0).contextClick();
    var overlay = $(ContextMenuOverlayElement.class).waitForFirst();
    assertEquals(1, overlay.getMenuItems().size());
    new org.openqa.selenium.interactions.Actions(getDriver()).sendKeys(Keys.ESCAPE).perform();

    // even row (row 1 = item 2): both items shown
    grid.getCell(1, 0).contextClick();
    assertEquals(2, $(ContextMenuOverlayElement.class).waitForFirst().getMenuItems().size());
  }

  @Test
  public void testContextMenuEnabledWhen() {
    $server.setRowActionsAsMenu(true);
    $server.addRowAction("Edit", $server.action(1))
        .enabledWhen(x -> x % 2 == 0); // enabled only for even items

    // menu item disabled for odd row (row 0 = item 1)
    grid.getCell(0, 0).contextClick();
    var item = $(ContextMenuOverlayElement.class).waitForFirst().getMenuItems().get(0);
    assertNotNull(item.getAttribute("disabled"));
  }

  @Test
  public void testActionRemove() {
    var action = $server.addRowAction(VaadinIcon.VAADIN_H, $server.action(1));
    assertTrue($server.isActionsColumnVisible());

    // removing the last action hides the column
    action.remove();
    assertFalse($server.isActionsColumnVisible());
  }

  @Test
  public void testRefreshRowActions() {
    var action = $server.addRowAction(VaadinIcon.VAADIN_H, $server.action(1));

    // baseline: the button carries only the default variant, not "error"
    assertFalse(
        grid.getCell(0, 1).$("vaadin-button").first().getAttribute("theme").contains("error"));

    // An element-level change (a theme variant) is NOT applied automatically — unlike the fluent
    // mutators (visibleWhen/enabledWhen/tooltip), which self-refresh.
    action.addThemeVariants(ButtonVariant.LUMO_ERROR);
    assertFalse(
        grid.getCell(0, 1).$("vaadin-button").first().getAttribute("theme").contains("error"));

    // It takes effect only after an explicit refreshRowActions().
    $server.refreshRowActions();
    assertTrue(
        grid.getCell(0, 1).$("vaadin-button").first().getAttribute("theme").contains("error"));
  }

  @Test
  public void testThemeVariants() {
    $server.addRowAction(VaadinIcon.VAADIN_H, $server.action(1));

    // default tertiary-inline variant applied
    var theme = grid.getCell(0, 1).$("vaadin-button").first().getAttribute("theme");
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
  public void testActionsColumnVisibility() {
    // column not visible before any action is added
    assertFalse($server.isActionsColumnVisible());

    // column becomes visible (non-null) after the first action
    $server.addRowAction(VaadinIcon.VAADIN_H, $server.action(1));
    assertTrue($server.isActionsColumnVisible());
  }

}
