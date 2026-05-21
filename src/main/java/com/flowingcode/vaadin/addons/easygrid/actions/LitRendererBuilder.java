package com.flowingcode.vaadin.addons.easygrid.actions;

import com.flowingcode.vaadin.jsonmigration.JsonSerializer;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.ValueProvider;
import elemental.json.Json;
import elemental.json.JsonArray;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.NonNull;

final class LitRendererBuilder<T> {

  private final String property;
  private final StringBuilder template = new StringBuilder();
  private final List<Consumer<LitRenderer<T>>> configuration = new ArrayList<>();
  private final List<ValueProvider<T, ?>> properties = new ArrayList<>();
  private boolean tagOpen = false;

  public LitRendererBuilder(@NonNull String property) {
    this.property = property;
  }

  public LitRenderer<T> build() {
    LitRenderer<T> renderer = LitRenderer.of(template.toString());
    if (!properties.isEmpty()) {
      int n = properties.size();
      String[] keys = new String[n];
      for (int i = 0; i < n; i++) {
        keys[i] = Integer.toString(i);
      }
      @SuppressWarnings("unchecked")
      ValueProvider<T, ?>[] providers = properties.toArray(new ValueProvider[n]);
      renderer.withProperty(property, t -> {
        var obj = Json.createObject();
        for (int i = 0; i < n; i++) {
          obj.put(keys[i], JsonSerializer.toJson(providers[i].apply(t)));
        }
        return obj;
      });
    }
    configuration.forEach(c -> c.accept(renderer));
    return renderer;
  }

  /**
   * Opens {@code <name}, runs {@code body}, then closes with {@code </name>}. The body should add
   * attribute bindings first (via {@link #attribute}, {@link #bindAttribute}, {@link #bindBoolean},
   * {@link #copy}, {@link #bindAttributes}) and then content (via nested {@link #tag},
   * {@link #withCondition}, or {@link #append}). The opening {@code >} is emitted automatically
   * the first time the body adds content, or at body-end for an empty tag. Tags nest to arbitrary
   * depth.
   */
  public void tag(String name, Runnable body) {
    finishOpeningTag();
    template.append('<').append(name);
    tagOpen = true;
    body.run();
    finishOpeningTag();
    template.append("</").append(name).append('>');
  }

  /**
   * Wraps {@code body} in a Lit conditional that renders only when {@code predicate} is
   * {@code true} for the current row. A {@code null} predicate is treated as always-true and
   * {@code body} is invoked directly with no surrounding conditional.
   */
  public void withCondition(SerializablePredicate<T> predicate, Runnable body) {
    if (predicate == null) {
      body.run();
      return;
    }
    finishOpeningTag();
    template.append("${item.%s[%s] ? html`".formatted(property, register(predicate::test)));
    body.run();
    finishOpeningTag();
    template.append("` : undefined}");
  }

  /** Appends raw template content. Closes any pending opening tag first. */
  public void append(String s) {
    finishOpeningTag();
    template.append(s);
  }

  /**
   * Emits an attribute or property with a literal value. {@code null} is a no-op; otherwise the
   * value is inlined at build time with prefix-aware dispatch.
   */
  public void set(String name, String value) {
    bind(name, Constant.ofNullable(value));
  }

  /**
   * Binds an attribute or property to a per-row value. A {@link Constant} {@code value} is inlined
   * at build time; {@code null} is a no-op.
   */
  public void bind(String name, ValueProvider<T, String> value) {
    if (value == null) {
      return;
    }
    if (value instanceof Constant) {
      emitLiteralAttribute(name, value.apply(null));
    } else {
      requireTagOpen();
      template.append(" %s=${item.%s[%s]}".formatted(name, property, register(value)));
    }
  }

  /**
   * Binds an attribute or property to a per-row object value. The value is JSON-serialized at
   * render time and passed through to Lit as-is — no {@link Constant} inlining and no
   * prefix-aware dispatch beyond what the receiving element expects. {@code null} provider is a
   * no-op.
   */
  public void bindObject(String name, ValueProvider<T, ?> value) {
    if (value == null) {
      return;
    }
    requireTagOpen();
    template.append(" %s=${item.%s[%s]}".formatted(name, property, register(value)));
  }

  /**
   * Emits a literal attribute, dispatching on the Lit binding prefix in {@code name}:
   * <ul>
   * <li>No prefix: emits an HTML attribute {@code name="value"}.</li>
   * <li>{@code .} prefix (property binding): emits {@code .name=${`value`}}.</li>
   * <li>{@code ?} prefix (boolean attribute binding): emits {@code ?name=${true}} unless
   * {@code value} is {@code "false"}; in that case the attribute is omitted.</li>
   * </ul>
   * {@code null} values are no-ops.
   */
  private void emitLiteralAttribute(String name, String value) {
    if (value == null) {
      return;
    }
    if (name.startsWith("?")) {
      if ("false".equals(value)) {
        return;
      }
      requireTagOpen();
      template.append(" %s=${true}".formatted(name));
    } else if (name.startsWith(".")) {
      requireTagOpen();
      template.append(" %s=${`%s`}".formatted(name, escapeTemplateLiteral(value)));
    } else {
      requireTagOpen();
      template.append(" %s=%s".formatted(name, wrapAndEscapeTemplateCharacters(value)));
    }
  }

  /**
   * Binds a Lit boolean attribute {@code ?name=${...}} to a per-row predicate. {@code null} is a
   * no-op.
   */
  public void bindBoolean(String name, ValueProvider<T, Boolean> predicate) {
    if (predicate != null) {
      requireTagOpen();
      template.append(" ?%s=${item.%s[%s]}".formatted(name, property, register(predicate)));
    }
  }

  /**
   * Snapshots the named attributes from {@code component}'s element and delegates each non-empty
   * value to {@link #set(String, String)}. Names with {@code .} or {@code ?} prefixes are
   * read via {@link Element#getProperty(String)} on the stripped name; names with no prefix are
   * read via {@link Element#getAttribute(String)}. Emission (HTML attribute vs. property vs.
   * boolean binding) follows {@link #set(String, String)}'s dispatch rules.
   */
  public void copyAttributes(HasElement component, String... names) {
    Element element = component.getElement();
    for (String name : names) {
      String value = switch (BindingType.of(name)) {
        case ATTRIBUTE -> element.getAttribute(name);
        default -> element.getProperty(name.substring(1));
      };
      set(name, value);
    }
  }

  /**
   * For each name, emits an HTML attribute bound per-row to the same-named attribute of the
   * component returned by {@code componentProvider}.
   */
  public <C extends HasElement> void bindAttributes(ValueProvider<T, C> componentProvider,
      String... names) {
    if (names.length == 0) {
      throw new IllegalArgumentException();
    }
    requireTagOpen();
    for (String name : names) {
      BindingType type = BindingType.of(name);
      int idx = register(item -> type.read(componentProvider.apply(item).getElement(), name));
      template.append(" %s=${item.%s[%d]}".formatted(name, property, idx));
    }
  }

  public void withFunction(String functionName, SerializableBiConsumer<T, JsonArray> handler) {
    configuration.add(lit -> lit.withFunction(functionName, handler));
  }

  private int register(ValueProvider<T, ?> provider) {
    int index = properties.size();
    properties.add(provider);
    return index;
  }

  private void finishOpeningTag() {
    if (tagOpen) {
      template.append('>');
      tagOpen = false;
    }
  }

  private void requireTagOpen() {
    if (!tagOpen) {
      throw new IllegalStateException(
          "Attribute can only be added inside a start tag, before any content");
    }
  }

  private static String wrapAndEscapeTemplateCharacters(String value) {
    if (value.indexOf('"') < 0) {
      return '"' + value + '"';
    } else {
      return "${`" + escapeTemplateLiteral(value) + "`}";
    }
  }

  private static String escapeTemplateLiteral(String value) {
    return value.replace("\\", "\\\\").replace("`", "\\`").replace("$", "\\$");
  }

  enum BindingType {
    ATTRIBUTE {
      @Override Object read(Element el, String name) {
        return el.getAttribute(name);
      }
      @Override String key(String name) {
        return name;
      }
    },
    PROPERTY {
      @Override Object read(Element el, String name) {
        return el.getProperty(name.substring(1));
      }
      @Override String key(String name) {
        return name.substring(1);
      }
    },
    BOOLEAN {
      @Override Object read(Element el, String name) {
        return el.getProperty(name.substring(1), false);
      }
      @Override String key(String name) {
        return name.substring(1);
      }
    };

    abstract Object read(Element el, String name);
    abstract String key(String name);

    static BindingType of(String name) {
      if (name.startsWith("?")) {
        return BOOLEAN;
      }
      if (name.startsWith(".")) {
        return PROPERTY;
      }
      return ATTRIBUTE;
    }
  }

}
