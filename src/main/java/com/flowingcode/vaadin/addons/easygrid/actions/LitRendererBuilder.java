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
import elemental.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import lombok.NonNull;

final class LitRendererBuilder<T> {

  private final String property;
  private final StringBuilder template = new StringBuilder();
  private final List<SerializableBiConsumer<T, JsonArray>> functionHandlers = new ArrayList<>();
  private final List<ValueProvider<T, ?>> properties = new ArrayList<>();
  private boolean tagOpen = false;
  private boolean closed = false;

  public LitRendererBuilder(@NonNull String property) {
    if (!property.matches("[A-Za-z][A-Za-z0-9]*")) {
      throw new IllegalArgumentException(
          "property must be an alphanumeric identifier starting with a letter: " + property);
    }
    this.property = property;
  }

  private String getFunctionName(int index) {
    // LitRenderer.withFunction requires alphanumeric names — no underscores.
    return property + "Handler" + index;
  }

  private void close() {
    if (!closed) {
      if (!properties.isEmpty()) {
        template.insert(0, "${item.%s ? html`".formatted(property));
        template.append("` : undefined}");
      }
      closed = true;
    }
  }

  private void requireNotClosed() {
    if (closed) {
      throw new IllegalStateException("Builder has already been closed");
    }
  }

  /** For testing only. */
  String getTemplate() {
    close();
    return template.toString();
  }

  /** Finalizes the template and builds the {@link LitRenderer}. */
  public LitRenderer<T> build() {
    close();

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

    for (int i = 0; i < functionHandlers.size(); i++) {
      renderer.withFunction(getFunctionName(i), functionHandlers.get(i));
    }
    return renderer;
  }

  /**
   * Opens {@code <name}, runs {@code body}, then closes with {@code </name>}. The body should add
   * attribute bindings first (via {@link #set}, {@link #bind}, {@link #bindBoolean},
   * {@link #bindObject}, {@link #copyAttributes}, {@link #bindAttributes},
   * {@link #bindAllAttributesAndProperties}) and then content (via nested {@link #tag},
   * {@link #withCondition}, {@link #addContent}, or {@link #append}). The opening {@code >} is
   * emitted automatically the first time the body adds content, or at body-end for an empty tag.
   * Tags nest to arbitrary depth.
   */
  public void tag(String name, Runnable body) {
    requireNotClosed();
    if (name == null || !name.matches("[a-zA-Z][a-zA-Z0-9-]*")) {
      throw new IllegalArgumentException("Invalid tag name: " + name);
    }
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
    requireNotClosed();
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
    requireNotClosed();
    finishOpeningTag();
    template.append(s);
  }

  /**
   * Emits an attribute or property with a literal value. {@code null} is a no-op; otherwise the
   * value is inlined at build time with prefix-aware dispatch.
   */
  public void set(String name, String value) {
    requireNotClosed();
    bind(name, Constant.ofNullable(value));
  }

  /**
   * Binds an attribute or property to a per-row value. A {@code Constant} value is inlined at
   * build time; {@code null} is a no-op.
   */
  public void bind(String name, ValueProvider<T, String> value) {
    requireNotClosed();
    if (value == null) {
      return;
    }
    if (value instanceof Constant) {
      emitLiteral(name, value.apply(null));
    } else {
      requireTagOpen();
      template.append(" %s=${item.%s[%s]}".formatted(name, property, register(value)));
    }
  }

  /**
   * Binds the current tag's content to a per-row value. A {@code Constant} value is inlined at
   * build time; {@code null} provider is a no-op. The opening tag's {@code >} is closed first if
   * needed.
   */
  public void addContent(ValueProvider<T, String> value) {
    requireNotClosed();
    if (value == null) {
      return;
    }
    if (value instanceof Constant) {
      String str = value.apply(null);
      if (str == null) {
        return;
      }
      finishOpeningTag();
      template.append("${`").append(escapeTemplateLiteral(str)).append("`}");
    } else {
      finishOpeningTag();
      template.append("${item.%s[%s]}".formatted(property, register(value)));
    }
  }

  /**
   * Binds an attribute or property to a per-row object value. The value is JSON-serialized at
   * render time and passed through to Lit as-is — no {@code Constant} inlining and no
   * prefix-aware dispatch beyond what the receiving element expects. {@code null} provider is a
   * no-op.
   */
  public void bindObject(String name, ValueProvider<T, ?> value) {
    requireNotClosed();
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
  private void emitLiteral(String name, String value) {
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
   * Emits a Lit event listener binding {@code @eventName=${functionName}}. {@code functionName}
   * must reference a function previously registered via {@link #withFunction}.
   */
  public void event(String eventName, int functionIndex) {
    requireNotClosed();
    requireTagOpen();
    template.append(" @%s=${%s}".formatted(eventName, getFunctionName(functionIndex)));
  }

  /**
   * Binds a Lit boolean attribute {@code ?name=${...}} to a per-row predicate. {@code null} is a
   * no-op.
   */
  public void bindBoolean(String name, SerializablePredicate<T> predicate) {
    requireNotClosed();
    if (predicate != null) {
      requireTagOpen();
      template.append(" ?%s=${item.%s[%s]}".formatted(name, property, register(predicate::test)));
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
    requireNotClosed();
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
   * Snapshots all attributes and properties from {@code component}'s element — excluding names
   * listed in {@code names} — and delegates each to {@link #set(String, String)}. Properties are
   * emitted with a {@code .} prefix; attributes are emitted as-is. An empty {@code names} array
   * copies everything.
   */
  public void copyAllAtttributesAndPropertiesExcept(HasElement component, String... names) {
    requireNotClosed();
    Element element = component.getElement();

    element.getAttributeNames().filter(excludingAttribute(names)).forEach(name->{
      set(name, element.getAttribute(name));
    });
    
    element.getPropertyNames().filter(excludingProperty(names)).forEach(name->{
      set("."+name, element.getProperty(name));
    });
  }

  private Predicate<? super String> excludingAttribute(String[] names) {
    return name -> {
      for (String n : names) {
        if (n.equals(name)) {
          return false;
        }
      } 
      return true;
    };
  }
  
  private Predicate<? super String> excludingProperty(String[] names) {
    return name -> excludingAttribute(names).test("."+name);
  }

  /**
   * For each name in {@code names}, emits a per-row attribute or property binding that reads the
   * corresponding value from the component returned by {@code componentProvider}, using
   * prefix-aware dispatch.
   */
  public <C extends HasElement> void bindAttributes(ValueProvider<T, C> componentProvider,
      String... names) {
    requireNotClosed();
    if (names.length == 0) {
      throw new IllegalArgumentException("at least one attribute name is required");
    }
    requireTagOpen();
    for (String name : names) {
      BindingType type = BindingType.of(name);
      int idx = register(item -> {
        C component = componentProvider.apply(item);
        return component == null ? null : type.read(component.getElement(), name);
      });
      template.append(" %s=${item.%s[%d]}".formatted(name, property, idx));
    }
  }

  /**
   * Binds per-row {@code .attr} and {@code .prop} object properties on the current open tag,
   * populated from all attributes and properties of the component returned by
   * {@code componentProvider}. Attributes are collected into a map bound to {@code .attr};
   * properties are collected into a map bound to {@code .prop}. Empty maps are passed as
   * {@code null}; a {@code null} component maps to {@code null} for both.
   *
   * <p>Names listed in {@code liftedNames} are excluded from the maps and instead bound directly
   * as individual attribute or property bindings on the target element (using the same
   * prefix-aware dispatch as {@link #copyAttributes}). This allows elements with dedicated named
   * properties (such as {@code <fc-icon>}) to receive well-known fields by name rather than
   * through the generic spread maps. For a {@code Constant} provider, lifting is performed via
   * {@link #copyAttributes} and the remainder via {@link #copyAllAtttributesAndPropertiesExcept}.
   *
   * @param <C> the component type
   * @param componentProvider provides the source component for each row item
   * @param liftedNames attribute/property names (with optional {@code .} or {@code ?} prefix) to
   *        bind directly instead of through the maps
   */
  public <C extends HasElement> void spreadAllAttributesAndProperties(
      ValueProvider<T, C> componentProvider, String... liftedNames) {
    requireNotClosed();
    requireTagOpen();

    if (componentProvider instanceof Constant) {
      C component = componentProvider.apply(null);
      if (component != null) {
        copyAllAtttributesAndPropertiesExcept(component, liftedNames);
        copyAttributes(component, liftedNames);
      }
      return;
    }
    
    // Evaluate componentProvider once per item across all lambdas.
    // Cache<T,C> intentionally shadows the enclosing method's T and C type parameters:
    // local records are implicitly static and cannot capture method type parameters directly.
    record Cache<T, C>(T item, C component) {}
    @SuppressWarnings("unchecked")
    Cache<T, C> cache[] = new Cache[1];

    ValueProvider<T, C> once = item -> {
      if (cache[0] == null || cache[0].item != item) {
        cache[0] = new Cache<>(item, componentProvider.apply(item));
      }
      return cache[0].component;
    };
    
    int attrIdx = register(item -> {
      C component = once.apply(item);
      if (component == null) {
        return null;
      }
      Element el = component.getElement();
      JsonObject obj = Json.createObject();
      el.getAttributeNames().filter(excludingAttribute(liftedNames))
          .forEach(name -> obj.put(name, el.getAttribute(name)));
      return obj.keys().length == 0 ? null : obj;
    });

    int propIdx = register(item -> {
      C component = once.apply(item);
      if (component == null) {
        return null;
      }
      Element el = component.getElement();
      JsonObject obj = Json.createObject();
      el.getPropertyNames().filter(excludingProperty(liftedNames))
          .forEach(name -> obj.put(name, el.getProperty(name)));
      return obj.keys().length == 0 ? null : obj;
    });

    template.append(" .attr=${item.%s[%d]} .prop=${item.%s[%d]}".formatted(property, attrIdx, property, propIdx));
    if (liftedNames.length > 0) {
      bindAttributes(once, liftedNames);
    }
  }

  /**
   * Registers a server-side function handler and returns its index for use with
   * {@link #event(String, int)}.
   *
   * @param handler the function handler invoked when the client fires the event
   * @return the index of the registered function
   */
  public int withFunction(SerializableBiConsumer<T, JsonArray> handler) {
    requireNotClosed();
    functionHandlers.add(handler);
    return functionHandlers.size() - 1;
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
    if (value.indexOf('"') < 0 && value.indexOf('`') < 0 && value.indexOf('\\') < 0
        && !value.contains("${")) {
      return '"' + value + '"';
    }
    return "${`" + escapeTemplateLiteral(value) + "`}";
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
