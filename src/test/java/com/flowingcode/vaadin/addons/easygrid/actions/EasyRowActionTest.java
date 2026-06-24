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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.function.SerializableConsumer;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;

/**
 * Tests for EasyRowAction rendering and behavior.
 *
 * @author Javier Godoy / Flowing Code
 */
public class EasyRowActionTest {

  private static final SerializableConsumer<Object> NOP = item -> {};

  private static final String OUTER_PREFIX = "${item.actions ? html`";
  private static final String OUTER_SUFFIX = "` : undefined}";

  private static <T> String templateFor(EasyRowAction<T> action) {
    var builder = new LitRendererBuilder<T>("actions");
    action.updateRenderer(builder);
    String template = builder.getTemplate();
    if (template.contains("item.actions")) {
      assertThat(template, startsWith(OUTER_PREFIX));
      assertThat(template, endsWith(OUTER_SUFFIX));
      return template.substring(OUTER_PREFIX.length(), template.length() - OUTER_SUFFIX.length());
    } else {
      assertThat(template, not(startsWith(OUTER_PREFIX)));
      assertThat(template, not(endsWith(OUTER_SUFFIX)));
      return template;
    }
  }

  // --- label / icon presence ---

  @Test
  public void labelOnly_rendersButtonWithContent() {
    var action = new EasyRowAction<>(null, item -> "Save", null, NOP);
    assertEquals(
        "<vaadin-button @click=${actionsHandler0}>${item.actions[0]}</vaadin-button>",
        templateFor(action));
  }

  @Test
  public void iconOnly_rendersIconChild() {
    var action = new EasyRowAction<>(null, null, Constant.of(new Icon("vaadin", "check")), NOP);
    assertEquals(
        "<vaadin-button theme=\"icon\" @click=${actionsHandler0}>"
            + "<vaadin-icon icon=\"vaadin:check\"></vaadin-icon>"
            + "</vaadin-button>",
        templateFor(action));
  }

  @Test
  public void labelAndIcon_rendersIconChildAndContent() {
    var action =
        new EasyRowAction<>(null, item -> "Save", Constant.of(new Icon("vaadin", "check")), NOP);
    assertEquals(
        "<vaadin-button @click=${actionsHandler0}>"
            + "<vaadin-icon icon=\"vaadin:check\"></vaadin-icon>"
            + "${item.actions[0]}"
            + "</vaadin-button>",
        templateFor(action));
  }

  // --- per-row (dynamic) icon: <fc-icon> branch ---
  // A non-Constant icon provider is evaluated per row, so the icon is rendered as an <fc-icon>
  // whose attributes/properties are spread per item, rather than the static <vaadin-icon> emitted
  // for a Constant icon.

  @Test
  public void iconProviderOnly_rendersFcIconChild() {
    var action = new EasyRowAction<>(null, null, item -> new Icon("vaadin", "check"), NOP);
    assertEquals(
        "<vaadin-button theme=\"icon\" @click=${actionsHandler0}>"
            + "<fc-icon .attr=${item.actions[0]} .prop=${item.actions[1]}></fc-icon>"
            + "</vaadin-button>",
        templateFor(action));
  }

  @Test
  public void labelAndIconProvider_rendersFcIconChildAndContent() {
    var action = new EasyRowAction<>(null, item -> "Save", item -> new Icon("vaadin", "check"), NOP);
    assertEquals(
        "<vaadin-button @click=${actionsHandler0}>"
            + "<fc-icon .attr=${item.actions[0]} .prop=${item.actions[1]}></fc-icon>"
            + "${item.actions[2]}"
            + "</vaadin-button>",
        templateFor(action));
  }

  // --- getTheme ---

  @Test
  public void getTheme_iconOnly_addsIconVariant() {
    var action = new EasyRowAction<>(null, null, Constant.of(new Icon("vaadin", "check")), NOP);
    assertEquals("icon", action.getTheme());
  }

  @Test
  public void getTheme_labelOnly_returnsNull() {
    var action = new EasyRowAction<>(null, Constant.of("Save"), null, NOP);
    assertNull(action.getTheme());
  }

  @Test
  public void getTheme_iconOnly_combinesWithUserVariant() {
    var action = new EasyRowAction<>(null, null, Constant.of(new Icon("vaadin", "check")), NOP);
    action.addThemeVariants(ButtonVariant.LUMO_ERROR);
    assertEquals("error icon", action.getTheme());
  }

  @Test
  public void getTheme_labelAndIcon_noIconVariantAdded() {
    var action = new EasyRowAction<>(null, Constant.of("Save"),
        Constant.of(new Icon("vaadin", "check")), NOP);
    assertNull(action.getTheme());
  }

  // --- baseline ---

  @Test
  public void unconfiguredAction_rendersPlainButton() {
    var action = new EasyRowAction<>(null, Constant.of("X"), null, NOP);
    assertEquals(
        "<vaadin-button @click=${actionsHandler0}>${`X`}</vaadin-button>",
        templateFor(action));
  }

  // --- visibleWhen ---

  @Test
  public void visibleWhen_wrapsInLitConditional() {
    var action = new EasyRowAction<>(null, Constant.of("X"), null, NOP);
    action.visibleWhen(item -> true);
    assertEquals(
        "${item.actions[0] ? html`<vaadin-button @click=${actionsHandler0}>${`X`}</vaadin-button>` : undefined}",
        templateFor(action));
  }

  // --- enabledWhen ---

  @Test
  public void enabledWhen_addsDisabledBinding() {
    var action = new EasyRowAction<>(null, Constant.of("X"), null, NOP);
    action.enabledWhen(item -> true);
    assertEquals(
        "<vaadin-button ?disabled=${item.actions[0]} @click=${actionsHandler0}>${`X`}</vaadin-button>",
        templateFor(action));
  }

  // --- tooltip ---

  @Test
  public void tooltipStatic_emitsLiteralTitleAttribute() {
    var action = new EasyRowAction<>(null, Constant.of("X"), null, NOP);
    action.tooltip("Save item");
    assertEquals(
        "<vaadin-button title=\"Save item\" @click=${actionsHandler0}>${`X`}</vaadin-button>",
        templateFor(action));
  }

  @Test
  public void tooltipDynamic_emitsPerRowTitleBinding() {
    var action = new EasyRowAction<>(null, Constant.of("X"), null, NOP);
    action.tooltip(item -> "Delete " + item);
    assertEquals(
        "<vaadin-button title=${item.actions[0]} @click=${actionsHandler0}>${`X`}</vaadin-button>",
        templateFor(action));
  }

  @Test
  public void manualTitle_preservedWhenNoTooltipProvider() {
    var action = new EasyRowAction<>(null, Constant.of("X"), null, NOP);
    action.getElement().setAttribute("title", "Manual tooltip");
    assertEquals(
        "<vaadin-button title=\"Manual tooltip\" @click=${actionsHandler0}>${`X`}</vaadin-button>",
        templateFor(action));
  }

  @Test
  public void tooltipOverridesManualTitle() {
    var action = new EasyRowAction<>(null, Constant.of("X"), null, NOP);
    action.getElement().setAttribute("title", "Manual tooltip");
    action.tooltip("Provider tooltip");
    assertEquals(
        "<vaadin-button title=\"Provider tooltip\" @click=${actionsHandler0}>${`X`}</vaadin-button>",
        templateFor(action));
  }

  // --- execute: server-side enabledWhen guard ---

  @Test
  public void execute_invokesHandler_whenNoEnabledWhen() {
    var clicked = new AtomicReference<Integer>();
    var action = new EasyRowAction<Integer>(null, Constant.of("X"), null, item -> clicked.set(item));
    action.execute(7);
    assertEquals(Integer.valueOf(7), clicked.get());
  }

  @Test
  public void execute_invokesHandler_whenEnabledWhenTrue() {
    var clicked = new AtomicReference<Integer>();
    var action = new EasyRowAction<Integer>(null, Constant.of("X"), null, item -> clicked.set(item));
    action.enabledWhen(item -> true);
    action.execute(7);
    assertEquals(Integer.valueOf(7), clicked.get());
  }

  @Test
  public void execute_skipsHandler_whenEnabledWhenFalse() {
    var clicked = new AtomicReference<Integer>();
    var action = new EasyRowAction<Integer>(null, Constant.of("X"), null, item -> clicked.set(item));
    action.enabledWhen(item -> false);
    // Server-side guard: the click is rejected without relying on the client ?disabled binding,
    // closing the gap for devtools manipulation and enabledWhen/render races.
    action.execute(7);
    assertNull(clicked.get());
  }

  @Test
  public void execute_appliesEnabledWhenPerItem() {
    var clicked = new AtomicReference<Integer>();
    var action = new EasyRowAction<Integer>(null, Constant.of("X"), null, item -> clicked.set(item));
    action.enabledWhen(item -> item % 2 == 0); // enabled only for even items
    action.execute(3); // odd → rejected
    assertNull(clicked.get());
    action.execute(4); // even → accepted
    assertEquals(Integer.valueOf(4), clicked.get());
  }

  // --- execute: server-side visibleWhen guard ---

  @Test
  public void execute_invokesHandler_whenVisibleWhenTrue() {
    var clicked = new AtomicReference<Integer>();
    var action = new EasyRowAction<Integer>(null, Constant.of("X"), null, item -> clicked.set(item));
    action.visibleWhen(item -> true);
    action.execute(7);
    assertEquals(Integer.valueOf(7), clicked.get());
  }

  @Test
  public void execute_skipsHandler_whenVisibleWhenFalse() {
    var clicked = new AtomicReference<Integer>();
    var action = new EasyRowAction<Integer>(null, Constant.of("X"), null, item -> clicked.set(item));
    action.visibleWhen(item -> false);
    // Server-side guard: visibleWhen omits the button on the client, but the per-row click function
    // is registered for every item, leaving a hidden action reachable via a crafted RPC. execute()
    // must reject it server-side, just as it does for enabledWhen.
    action.execute(7);
    assertNull(clicked.get());
  }

  @Test
  public void execute_appliesVisibleWhenPerItem() {
    var clicked = new AtomicReference<Integer>();
    var action = new EasyRowAction<Integer>(null, Constant.of("X"), null, item -> clicked.set(item));
    action.visibleWhen(item -> item % 2 == 0); // visible only for even items
    action.execute(3); // odd → rejected
    assertNull(clicked.get());
    action.execute(4); // even → accepted
    assertEquals(Integer.valueOf(4), clicked.get());
  }

  @Test
  public void execute_skipsHandler_whenInvisibleButEnabled() {
    var clicked = new AtomicReference<Integer>();
    var action = new EasyRowAction<Integer>(null, Constant.of("X"), null, item -> clicked.set(item));
    action.visibleWhen(item -> false);
    action.enabledWhen(item -> true);
    // Either guard failing must reject the click; an invisible action is never executable even
    // when enabledWhen would allow it.
    action.execute(7);
    assertNull(clicked.get());
  }

  // --- HasStyle forwarding ---

  @Test
  public void className_isForwardedToButton() {
    var action = new EasyRowAction<>(null, Constant.of("X"), null, NOP);
    action.addClassName("danger");
    assertEquals(
        "<vaadin-button class=\"danger\" @click=${actionsHandler0}>${`X`}</vaadin-button>",
        templateFor(action));
  }

  @Test
  public void style_isForwardedToButton() {
    var action = new EasyRowAction<>(null, Constant.of("X"), null, NOP);
    action.getStyle().set("color", "red");
    assertEquals(
        "<vaadin-button style=\"color:red\" @click=${actionsHandler0}>${`X`}</vaadin-button>",
        templateFor(action));
  }

  // --- remove: lifecycle / idempotency ---

  @Test
  public void remove_onUnregisteredAction_isNoOp() {
    var action = new EasyRowAction<Integer>(null, Constant.of("X"), null, item -> {});
    // manager == null (never registered): must be a no-op, not an NPE.
    action.remove();
    action.remove();
  }

  @Test
  public void remove_unregistersFromManager_andIsIdempotent() {
    var manager = new RowActionsManager<Integer>(new Grid<>());
    var action = manager.addRowAction(Constant.of("X"), null, item -> {});
    assertEquals(1, manager.getRowActions().size());

    action.remove();
    assertTrue(manager.getRowActions().isEmpty());

    // a second remove() is a no-op (the manager reference was already cleared) and must not throw
    action.remove();
    assertTrue(manager.getRowActions().isEmpty());
  }

  // --- custom renderer SPI (HasRowActions.setRowActionsRenderer -> manager.setRenderer) ---

  @Test
  public void setRenderer_installsCustomRenderer_delegatesColumn_andCleansUpPrevious() {
    var grid = new Grid<Integer>();
    grid.setItems(1, 2, 3);
    var actionsColumn = grid.addColumn(item -> item);
    var manager = new RowActionsManager<Integer>(grid);

    var custom = new RecordingRenderer<Integer>(actionsColumn);
    manager.setRenderer(custom);

    // resolving the actions column initializes the active renderer (update()) and must delegate
    // to the custom renderer's getColumn()
    assertSame(actionsColumn, manager.getActionsColumn());
    assertTrue("custom renderer should receive update()", custom.updated);
    assertEquals(0, custom.removed);

    // installing another renderer cleans up the custom one exactly once
    manager.setRenderer(new RecordingRenderer<>(null));
    assertEquals(1, custom.removed);
  }

  /** Minimal {@link RowActionsRenderer} that records the SPI calls made by the manager. */
  private static final class RecordingRenderer<T> implements RowActionsRenderer<T> {
    private final Grid.Column<T> column;
    private boolean updated;
    private int removed;

    RecordingRenderer(Grid.Column<T> column) {
      this.column = column;
    }

    @Override
    public void update(List<EasyRowAction<T>> actions) {
      updated = true;
    }

    @Override
    public Grid.Column<T> getColumn() {
      return column;
    }

    @Override
    public void remove() {
      removed++;
    }
  }

}
