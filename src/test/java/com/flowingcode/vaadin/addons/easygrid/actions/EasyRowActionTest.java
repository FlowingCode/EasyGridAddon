package com.flowingcode.vaadin.addons.easygrid.actions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.function.SerializableConsumer;
import org.junit.Test;

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
            + "<fc-icon icon=\"vaadin:check\"></fc-icon>"
            + "</vaadin-button>",
        templateFor(action));
  }

  @Test
  public void labelAndIcon_rendersIconChildAndContent() {
    var action =
        new EasyRowAction<>(null, item -> "Save", Constant.of(new Icon("vaadin", "check")), NOP);
    assertEquals(
        "<vaadin-button @click=${actionsHandler0}>"
            + "<fc-icon icon=\"vaadin:check\"></fc-icon>"
            + "${item.actions[0]}"
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

}
