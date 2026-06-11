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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.ValueProvider;
import elemental.json.JsonObject;
import lombok.Builder;
import lombok.Value;
import org.junit.Test;

public class LitRendererBuilderTest {

  private LitRendererBuilder<Object> newBuilder() {
    return new LitRendererBuilder<>("actions");
  }

  private LitRendererBuilder<Pojo> newPojoBuilder() {
    return new LitRendererBuilder<>("actions");
  }

  private static JsonObject actionsFor(LitRenderer<Pojo> renderer, Pojo row) {
    ValueProvider<Pojo, ?> vp = renderer.getValueProviders().get("actions");
    assertNotNull("expected an 'actions' value provider", vp);
    return (JsonObject) vp.apply(row);
  }

  /**
   * Wraps {@code inner} in the {@code item.actions} presence guard the builder emits whenever at
   * least one per-row property is registered.
   */
  private static String guarded(String inner) {
    return "${item.actions ? html`" + inner + "` : undefined}";
  }

  // --- structural emission ---

  @Test
  public void emptyTagOpensAndCloses() {
    LitRendererBuilder<Object> b = newBuilder();
    b.tag("vaadin-button", () -> {});
    assertEquals("<vaadin-button></vaadin-button>", b.getTemplate());
  }

  @Test
  public void nestedTags() {
    LitRendererBuilder<Object> b = newBuilder();
    b.tag("outer", () -> {
      b.set("a", "1");
      b.tag("inner", () -> b.set("b", "2"));
    });
    assertEquals("<outer a=\"1\"><inner b=\"2\"></inner></outer>", b.getTemplate());
  }

  // --- set: literal attribute / property / boolean dispatch ---

  @Test
  public void setEmitsHtmlAttribute() {
    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> b.set("foo", "bar"));
    assertEquals("<x foo=\"bar\"></x>", b.getTemplate());
  }

  @Test
  public void setDotPrefixEmitsPropertyBinding() {
    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> b.set(".prop", "hello"));
    assertEquals("<x .prop=${`hello`}></x>", b.getTemplate());
  }

  @Test
  public void setQuestionPrefixTrueEmitsBooleanAttribute() {
    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> b.set("?disabled", "true"));
    assertEquals("<x ?disabled=${true}></x>", b.getTemplate());
  }

  @Test
  public void setQuestionPrefixFalseSkips() {
    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> b.set("?disabled", "false"));
    assertEquals("<x></x>", b.getTemplate());
  }

  @Test
  public void setNullValueIsNoOp() {
    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> b.set("foo", null));
    assertEquals("<x></x>", b.getTemplate());
  }

  // --- bind: dynamic vs Constant ---

  @Test
  public void bindRegistersProviderAndEmitsReference() {
    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> b.bind("foo", item -> "value"));
    assertEquals(guarded("<x foo=${item.actions[0]}></x>"), b.getTemplate());
  }

  @Test
  public void bindWithConstantDelegatesToLiteralPath() {
    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> b.bind("foo", Constant.of("bar")));
    assertEquals("<x foo=\"bar\"></x>", b.getTemplate());
  }

  @Test
  public void bindNullProviderIsNoOp() {
    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> b.bind("foo", null));
    assertEquals("<x></x>", b.getTemplate());
  }

  @Test
  public void bindBooleanRegistersAndEmits() {
    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> b.bindBoolean("disabled", item -> true));
    assertEquals(guarded("<x ?disabled=${item.actions[0]}></x>"), b.getTemplate());
  }

  // --- withCondition ---

  @Test
  public void withConditionWrapsBody() {
    LitRendererBuilder<Object> b = newBuilder();
    b.withCondition(item -> true, () -> b.tag("x", () -> {}));
    assertEquals(guarded("${item.actions[0] ? html`<x></x>` : undefined}"), b.getTemplate());
  }

  @Test
  public void withConditionNullPredicateIsPassThrough() {
    LitRendererBuilder<Object> b = newBuilder();
    b.withCondition(null, () -> b.tag("x", () -> {}));
    assertEquals("<x></x>", b.getTemplate());
  }

  // --- content ---

  @Test
  public void addContentLiteral() {
    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> b.addContent(Constant.of("Hello")));
    assertEquals("<x>${`Hello`}</x>", b.getTemplate());
  }

  @Test
  public void addContentDynamic() {
    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> b.addContent(item -> "label"));
    assertEquals(guarded("<x>${item.actions[0]}</x>"), b.getTemplate());
  }

  @Test
  public void addContentNullProviderIsNoOp() {
    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> b.addContent(null));
    assertEquals("<x></x>", b.getTemplate());
  }

  @Test
  public void addContentConstantOfNullIsNoOp() {
    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> b.addContent(Constant.of(null)));
    assertEquals("<x></x>", b.getTemplate());
  }

  // --- tagOpen guard ---

  @Test
  public void attributeOutsideTagThrows() {
    LitRendererBuilder<Object> b = newBuilder();
    assertThrows(IllegalStateException.class, () -> b.set("foo", "bar"));
  }

  @Test
  public void attributeAfterNestedContentThrows() {
    LitRendererBuilder<Object> b = newBuilder();
    assertThrows(IllegalStateException.class, () -> {
      b.tag("outer", () -> {
        b.tag("inner", () -> {});
        b.set("foo", "bar");
      });
    });
  }

  @Test
  public void bindAttributesEmptyNamesThrows() {
    LitRendererBuilder<Object> b = newBuilder();
    assertThrows(IllegalArgumentException.class,
        () -> b.tag("x", () -> b.bindAttributes(item -> null)));
  }

  @Test
  public void requireTagOpenExceptionMessageMentionsAttribute() {
    LitRendererBuilder<Object> b = newBuilder();
    IllegalStateException ex =
        assertThrows(IllegalStateException.class, () -> b.set("foo", "bar"));
    assertEquals("Attribute can only be added inside a start tag, before any content",
        ex.getMessage());
  }

  // --- bindAttributes happy path ---

  @Test
  public void bindAttributesEmitsPerNameTemplateReferences() {
    LitRendererBuilder<Object> b = newBuilder();
    HasElement holder = () -> new Element("test");
    b.tag("x", () -> b.bindAttributes(item -> holder, "foo", ".bar"));
    assertEquals(guarded("<x foo=${item.actions[0]} .bar=${item.actions[1]}></x>"), b.getTemplate());
  }

  @Test
  public void bindAttributesSupportsBooleanPrefix() {
    LitRendererBuilder<Object> b = newBuilder();
    HasElement holder = () -> new Element("test");
    b.tag("x", () -> b.bindAttributes(item -> holder, "?disabled"));
    assertEquals(guarded("<x ?disabled=${item.actions[0]}></x>"), b.getTemplate());
  }

  // --- copyAttributes BOOLEAN branch ---

  @Test
  public void copyAttributesQuestionPrefixWithTrue() {
    Element el = new Element("test");
    el.setProperty("disabled", true);
    HasElement holder = () -> el;

    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> b.copyAttributes(holder, "?disabled"));
    assertEquals("<x ?disabled=${true}></x>", b.getTemplate());
  }

  @Test
  public void copyAttributesQuestionPrefixWithFalseSkips() {
    Element el = new Element("test");
    el.setProperty("disabled", false);
    HasElement holder = () -> el;

    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> b.copyAttributes(holder, "?disabled"));
    assertEquals("<x></x>", b.getTemplate());
  }

  @Test
  public void copyAttributesQuestionPrefixMissingPropertySkips() {
    Element el = new Element("test");
    HasElement holder = () -> el;

    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> b.copyAttributes(holder, "?disabled"));
    assertEquals("<x></x>", b.getTemplate());
  }

  // --- withFunction / event ---

  @Test
  public void withFunctionDoesNotAlterTemplate() {
    LitRendererBuilder<Object> b = newBuilder();
    b.withFunction((item, args) -> {});
    b.tag("x", () -> {});
    assertEquals("<x></x>", b.getTemplate());
  }

  @Test
  public void withFunctionReturnsSequentialIndices() {
    LitRendererBuilder<Object> b = newBuilder();
    assertEquals(0, b.withFunction((item, args) -> {}));
    assertEquals(1, b.withFunction((item, args) -> {}));
    assertEquals(2, b.withFunction((item, args) -> {}));
  }

  @Test
  public void withFunctionBuildSucceeds() {
    LitRendererBuilder<Object> b = newBuilder();
    b.withFunction((item, args) -> {});
    b.tag("x", () -> {});
    // build() must not throw — the registered handlers are bound to the LitRenderer.
    assertNotNull(b.build());
  }

  @Test
  public void eventEmitsListenerBinding() {
    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> {
      int idx = b.withFunction((item, args) -> {});
      b.event("click", idx);
    });
    assertEquals("<x @click=${actionsHandler0}></x>", b.getTemplate());
  }

  @Test
  public void eventUsesPropertyAsFunctionPrefix() {
    LitRendererBuilder<Object> b = new LitRendererBuilder<>("widgets");
    b.tag("x", () -> {
      int idx = b.withFunction((item, args) -> {});
      b.event("click", idx);
    });
    assertEquals("<x @click=${widgetsHandler0}></x>", b.getTemplate());
  }

  @Test
  public void eventMultipleFunctionsKeepDistinctIndices() {
    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> {
      int a = b.withFunction((item, args) -> {});
      int c = b.withFunction((item, args) -> {});
      b.event("click", a);
      b.event("dblclick", c);
    });
    assertEquals(
        "<x @click=${actionsHandler0} @dblclick=${actionsHandler1}></x>", b.getTemplate());
  }

  @Test
  public void eventRequiresOpenTag() {
    LitRendererBuilder<Object> b = newBuilder();
    int idx = b.withFunction((item, args) -> {});
    assertThrows(IllegalStateException.class, () -> b.event("click", idx));
  }

  // --- escape behavior ---

  @Test
  public void quoteValueSwitchesToBacktickForm() {
    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> b.set("foo", "a\"b"));
    assertEquals("<x foo=${`a\"b`}></x>", b.getTemplate());
  }

  @Test
  public void backtickValueSwitchesToBacktickForm() {
    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> b.set("foo", "a`b"));
    // backtick is escaped to \` inside the template literal
    assertEquals("<x foo=${`a\\`b`}></x>", b.getTemplate());
  }

  @Test
  public void dollarBraceValueSwitchesToBacktickForm() {
    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> b.set("foo", "a${x}"));
    // $ is escaped to \$ so it isn't interpreted as JS interpolation
    assertEquals("<x foo=${`a\\${x}`}></x>", b.getTemplate());
  }

  @Test
  public void backslashValueSwitchesToBacktickForm() {
    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> b.set("foo", "a\\b"));
    // backslash is escaped to \\
    assertEquals("<x foo=${`a\\\\b`}></x>", b.getTemplate());
  }

  // --- copyAttributes ---

  @Test
  public void copyAttributesReadsAttributeAndProperty() {
    Element el = new Element("test");
    el.setAttribute("foo", "fooValue");
    el.setProperty("bar", "barValue");

    HasElement holder = () -> el;

    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> b.copyAttributes(holder, "foo", ".bar"));
    assertEquals("<x foo=\"fooValue\" .bar=${`barValue`}></x>", b.getTemplate());
  }

  @Test
  public void copyAttributesSkipsMissingValues() {
    Element el = new Element("test");
    HasElement holder = () -> el;

    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> b.copyAttributes(holder, "foo", ".bar"));
    assertEquals("<x></x>", b.getTemplate());
  }

  // --- BindingType ---

  @Test
  public void bindingTypeDispatchesOnPrefix() {
    assertEquals(LitRendererBuilder.BindingType.ATTRIBUTE,
        LitRendererBuilder.BindingType.of("foo"));
    assertEquals(LitRendererBuilder.BindingType.PROPERTY,
        LitRendererBuilder.BindingType.of(".foo"));
    assertEquals(LitRendererBuilder.BindingType.BOOLEAN,
        LitRendererBuilder.BindingType.of("?foo"));
  }

  // --- build() per case ---

  @Test
  public void buildLiteralOnlyHasNoActionsProvider() {
    LitRendererBuilder<Pojo> b = newPojoBuilder();
    b.tag("vaadin-button", () -> b.set("label", "OK"));
    LitRenderer<Pojo> r = b.build();
    assertNotNull(r);
    assertFalse("no registered providers expected for a literal-only template",
        r.getValueProviders().containsKey("actions"));
  }

  @Test
  public void buildBindEmitsStringPerRow() {
    LitRendererBuilder<Pojo> b = newPojoBuilder();
    b.tag("vaadin-button", () -> b.bind("label", Pojo::getName));

    JsonObject actions = actionsFor(b.build(), Pojo.builder().name("hello").build());
    assertEquals("hello", actions.getString("0"));
  }

  @Test
  public void buildBindBooleanEmitsBooleanPerRow() {
    LitRendererBuilder<Pojo> b = newPojoBuilder();
    b.tag("vaadin-button", () -> b.bindBoolean("disabled", Pojo::getActive));

    JsonObject actions = actionsFor(b.build(), Pojo.builder().active(true).build());
    assertTrue(actions.getBoolean("0"));
  }

  @Test
  public void buildAddContentDynamicEmitsPerRowString() {
    LitRendererBuilder<Pojo> b = newPojoBuilder();
    b.tag("vaadin-button", () -> b.addContent(Pojo::getName));

    JsonObject actions = actionsFor(b.build(), Pojo.builder().name("Click").build());
    assertEquals("Click", actions.getString("0"));
  }

  @Test
  public void buildWithConditionRegistersPredicateAsFirstProperty() {
    LitRendererBuilder<Pojo> b = newPojoBuilder();
    b.withCondition(Pojo::getActive, () -> b.tag("vaadin-button", () -> {}));

    JsonObject actions = actionsFor(b.build(), Pojo.builder().active(true).build());
    assertTrue(actions.getBoolean("0"));
  }

  @Test
  public void buildBindAttributesRegistersOnePerName() {
    LitRendererBuilder<Pojo> b = newPojoBuilder();
    b.tag("fc-icon", () -> b.bindAttributes(row -> {
      Element el = new Element("vaadin-icon");
      el.setAttribute("icon", row.getIconName());
      el.setProperty("symbol", row.getName());
      return (HasElement) () -> el;
    }, "icon", ".symbol"));

    JsonObject actions = actionsFor(b.build(),
        Pojo.builder().iconName("vaadin:check").name("done").build());
    assertEquals("vaadin:check", actions.getString("0"));
    assertEquals("done", actions.getString("1"));
  }

  @Test
  public void buildMixesLiteralAndDynamicProperties() {
    LitRendererBuilder<Pojo> b = newPojoBuilder();
    b.tag("vaadin-button", () -> {
      b.set("theme", "primary");
      b.bind("label", Pojo::getName);
      b.bindBoolean("disabled", Pojo::getActive);
    });
    JsonObject actions = actionsFor(b.build(),
        Pojo.builder().name("Save").active(false).build());
    // literal "theme" doesn't register a provider — only label (idx 0) and disabled (idx 1)
    assertEquals("Save", actions.getString("0"));
    assertFalse(actions.getBoolean("1"));
  }

  // --- spreadAllAttributesAndProperties ---

  @Test
  public void spreadConstant_nullComponent_isNoOp() {
    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> b.spreadAllAttributesAndProperties(Constant.of((HasElement) null), "icon"));
    assertEquals("<x></x>", b.getTemplate());
  }

  @Test
  public void spreadConstant_liftedAttributeEmittedDirectly() {
    Element el = new Element("test");
    el.setAttribute("icon", "vaadin:check");
    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> b.spreadAllAttributesAndProperties(Constant.of((HasElement) () -> el), "icon"));
    assertEquals("<x icon=\"vaadin:check\"></x>", b.getTemplate());
  }

  @Test
  public void spreadConstant_separatesLiftedFromNonLifted() {
    Element el = new Element("test");
    el.setAttribute("icon", "vaadin:check");
    el.setAttribute("extra", "val");
    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> b.spreadAllAttributesAndProperties(Constant.of((HasElement) () -> el), "icon"));
    assertEquals("<x extra=\"val\" icon=\"vaadin:check\"></x>", b.getTemplate());
  }

  @Test
  public void spreadDynamic_emitsAttrPropMapsAndLiftedBindings() {
    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> b.spreadAllAttributesAndProperties(
        item -> (HasElement) () -> new Element("test"), "icon"));
    assertEquals(
        guarded("<x .attr=${item.actions[0]} .prop=${item.actions[1]} icon=${item.actions[2]}></x>"),
        b.getTemplate());
  }

  @Test
  public void spreadDynamic_build_attrMapContainsNonLiftedAttributes() {
    LitRendererBuilder<Pojo> b = newPojoBuilder();
    b.tag("fc-icon", () -> b.spreadAllAttributesAndProperties(row -> {
      Element el = new Element("vaadin-icon");
      el.setAttribute("icon", row.getIconName());
      el.setAttribute("extra", "val");
      return (HasElement) () -> el;
    }, "icon"));

    JsonObject actions = actionsFor(b.build(), Pojo.builder().iconName("vaadin:check").build());
    JsonObject attrMap = actions.getObject("0");
    assertEquals("val", attrMap.getString("extra"));
    assertEquals("vaadin:check", actions.getString("2"));
  }

  @Value
  @Builder
  static class Pojo {
    String name;
    Boolean active;
    String iconName;
  }
}
