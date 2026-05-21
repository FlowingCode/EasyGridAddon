package com.flowingcode.vaadin.addons.easygrid.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.dom.Element;
import org.junit.Test;

public class LitRendererBuilderTest {

  private LitRendererBuilder<Object> newBuilder() {
    return new LitRendererBuilder<>("actions");
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

  @Test
  public void appendInsertsRawContent() {
    LitRendererBuilder<Object> b = newBuilder();
    b.append("<wrap>");
    b.tag("x", () -> {});
    b.append("</wrap>");
    assertEquals("<wrap><x></x></wrap>", b.getTemplate());
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
    assertEquals("<x foo=${item.actions[0]}></x>", b.getTemplate());
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
    assertEquals("<x ?disabled=${item.actions[0]}></x>", b.getTemplate());
  }

  @Test
  public void bindObjectRegistersAndEmits() {
    LitRendererBuilder<Object> b = newBuilder();
    b.tag("x", () -> b.bindObject(".descriptor", item -> java.util.Map.of("k", "v")));
    assertEquals("<x .descriptor=${item.actions[0]}></x>", b.getTemplate());
  }

  // --- withCondition ---

  @Test
  public void withConditionWrapsBody() {
    LitRendererBuilder<Object> b = newBuilder();
    b.withCondition(item -> true, () -> b.tag("x", () -> {}));
    assertEquals("${item.actions[0] ? html`<x></x>` : undefined}", b.getTemplate());
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
    assertEquals("<x>${item.actions[0]}</x>", b.getTemplate());
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
    assertEquals("<x foo=${item.actions[0]} .bar=${item.actions[1]}></x>", b.getTemplate());
  }

  @Test
  public void bindAttributesSupportsBooleanPrefix() {
    LitRendererBuilder<Object> b = newBuilder();
    HasElement holder = () -> new Element("test");
    b.tag("x", () -> b.bindAttributes(item -> holder, "?disabled"));
    assertEquals("<x ?disabled=${item.actions[0]}></x>", b.getTemplate());
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

  // --- withFunction ---

  @Test
  public void withFunctionDoesNotAlterTemplate() {
    LitRendererBuilder<Object> b = newBuilder();
    b.withFunction("onClick", (item, args) -> {});
    b.tag("x", () -> {});
    assertEquals("<x></x>", b.getTemplate());
  }

  @Test
  public void withFunctionBuildSucceeds() {
    LitRendererBuilder<Object> b = newBuilder();
    b.withFunction("onClick", (item, args) -> {});
    b.tag("x", () -> {});
    // build() must not throw — the registered configuration callback runs on the LitRenderer.
    org.junit.Assert.assertNotNull(b.build());
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

  @Test
  public void bindingTypeKeyStripsPrefix() {
    assertEquals("foo", LitRendererBuilder.BindingType.ATTRIBUTE.key("foo"));
    assertEquals("foo", LitRendererBuilder.BindingType.PROPERTY.key(".foo"));
    assertEquals("foo", LitRendererBuilder.BindingType.BOOLEAN.key("?foo"));
  }
}
