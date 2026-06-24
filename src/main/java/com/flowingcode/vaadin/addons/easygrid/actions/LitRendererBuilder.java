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

import com.flowingcode.vaadin.jsonmigration.JsonMigration;
import com.flowingcode.vaadin.jsonmigration.JsonSerializer;
import com.flowingcode.vaadin.jsonmigration.LitRendererMigrationExtension;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.ValueProvider;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;

/**
 * Builds the Lit template and backing {@link LitRenderer} for an {@code EasyGrid} row-actions
 * column. Callers open elements with {@link #tag(String, Runnable)} and add attribute/property
 * bindings, content, and per-row server functions; all per-row values are exposed to the template
 * through a single {@code item.<property>} object, addressed by index.
 *
 * <p>The builder is single-use: after {@link #build()} (or {@link #getTemplate()}) it is closed and
 * further mutation throws {@link IllegalStateException}. When at least one per-row value is
 * registered, the whole template is wrapped in a presence guard so it renders nothing until the
 * backing property object is populated on the client.
 *
 * @param <T> the grid bean type
 * @author Javier Godoy / Flowing Code
 */
@ExtensionMethod(value = LitRendererMigrationExtension.class, suppressBaseMethods = true)
final class LitRendererBuilder<T> {

  private static final Pattern PROPERTY_PATTERN = Pattern.compile("[A-Za-z][A-Za-z0-9]*");
  private static final Pattern TAG_NAME_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9-]*");

  private final String property;
  private final StringBuilder template = new StringBuilder();
  private final List<SerializableBiConsumer<T, JsonArray>> functionHandlers = new ArrayList<>();
  private final List<ValueProvider<T, ?>> properties = new ArrayList<>();
  private boolean tagOpen = false;
  private boolean closed = false;

  /**
   * Creates a builder for a <em>static-only</em> template, one that emits no per-row values and so
   * needs no backing {@code item.<property>} object. Only build-time literals are permitted (e.g.
   * {@link #set}, or {@link #bind}/{@link #addContent} with a {@link Constant}); any operation that
   * would register a per-row value ({@code bind} with a dynamic provider, {@link #withCondition},
   * {@link #bindBoolean}, {@link #spreadAllAttributesAndProperties}, {@link #withFunction}, or the
   * presence guard added by {@link #build()}) throws {@link IllegalStateException}.
   *
   * <p>For a template that binds per-row values, use {@link #LitRendererBuilder(String)} instead.
   *
   * @param <T> the grid bean type
   * @return a builder that accepts only build-time literals (no {@code item.<property>} object)
   */
  public static <T> LitRendererBuilder<T> staticOnly() {
    return new LitRendererBuilder<>();
  }

  private LitRendererBuilder() {
    this.property = null;
  }

  /**
   * Creates a builder for a template that binds per-row values through a backing
   * {@code item.<property>} object addressed by index. For a template that emits only build-time
   * literals, use {@link #staticOnly()} instead.
   *
   * @param property the name of the backing per-row property object; must be an alphanumeric
   *        identifier starting with a letter
   * @throws IllegalArgumentException if {@code property} is not a valid identifier
   */
  public LitRendererBuilder(@NonNull String property) {
    if (!PROPERTY_PATTERN.matcher(property).matches()) {
      throw new IllegalArgumentException(
          "Property must be an alphanumeric identifier starting with a letter: " + property);
    }
    this.property = property;
  }

  private String getFunctionName(int index) {
    // LitRenderer.withFunction requires alphanumeric names with no underscores.
    requireProperty();
    return property + "Handler" + index;
  }

  private void close() {
    if (!closed) {
      if (!properties.isEmpty()) {
        requireProperty();
        template.insert(0, "${item.%s ? html`".formatted(property));
        template.append("` : undefined}");
      }
      closed = true;
    }
  }

  private void requireProperty() {
    if (property == null) {
      throw new IllegalStateException(
          "Property name is required to register per-row values");
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

  /** Finalizes the template and builds the {@code LitRenderer}. */
  public LitRenderer<T> build() {
    close();

    LitRenderer<T> renderer = LitRenderer.of(template.toString());
    if (!properties.isEmpty()) {
      requireProperty();
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
        return JsonMigration.convertToClientCallableResult(obj);
      });
    }

    for (int i = 0; i < functionHandlers.size(); i++) {
      renderer.withFunction(getFunctionName(i), functionHandlers.get(i));
    }
    return renderer;
  }



  /**
   * Opens an element: appends {@code <name} (the closing {@code >} is emitted later), runs
   * {@code body}, then closes with {@code </name>}. The body should add
   * attribute bindings first (via {@link #set}, {@link #bind}, {@link #bindBoolean},
   * {@link #copyAttributes}, {@link #spreadAllAttributesAndProperties}) and then content (via
   * nested {@link #tag},
   * {@link #withCondition}, or {@link #addContent}). The opening {@code >} is
   * emitted automatically the first time the body adds content, or at body-end for an empty tag.
   * Tags nest to arbitrary depth.
   *
   * @param name the tag name; must match {@code [a-zA-Z][a-zA-Z0-9-]*}
   * @param body adds the tag's attributes and content
   * @throws IllegalArgumentException if {@code name} is not a valid tag name
   */
  public void tag(String name, Runnable body) {
    requireNotClosed();
    if (name == null || !TAG_NAME_PATTERN.matcher(name).matches()) {
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
   *
   * @param predicate evaluated for each row item; the body renders only when it returns
   *        {@code true}, or always when {@code null}
   * @param body adds the conditionally-rendered content
   */
  public void withCondition(SerializablePredicate<T> predicate, Runnable body) {
    requireNotClosed();
    if (predicate == null) {
      body.run();
      return;
    }
    requireProperty();
    finishOpeningTag();
    template.append("${item.%s[%s] ? html`".formatted(property, register(predicate::test)));
    body.run();
    finishOpeningTag();
    template.append("` : undefined}");
  }

  /**
   * Emits an attribute or property with a literal value. {@code null} is a no-op; otherwise the
   * value is inlined at build time with prefix-aware dispatch.
   *
   * @param name the attribute or property name, optionally with a {@code .} or {@code ?} binding
   *        prefix
   * @param value the literal value, or {@code null} to emit nothing
   */
  public void set(String name, String value) {
    requireNotClosed();
    bind(name, Constant.ofNullable(value));
  }

  /**
   * Binds an attribute or property to a per-row value. A {@code Constant} value is inlined at
   * build time; {@code null} is a no-op.
   *
   * @param name the attribute or property name, optionally with a {@code .} or {@code ?} binding
   *        prefix
   * @param value per-row provider for the value, or {@code null} to emit nothing
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
      requireProperty();
      template.append(" %s=${item.%s[%s]}".formatted(name, property, register(value)));
    }
  }

  /**
   * Binds the current tag's content to a per-row value. A {@code Constant} value is inlined at
   * build time; {@code null} provider is a no-op. The opening tag's {@code >} is closed first if
   * needed.
   *
   * @param value per-row provider for the content, or {@code null} to emit nothing
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
      requireProperty();
      finishOpeningTag();
      template.append("${item.%s[%s]}".formatted(property, register(value)));
    }
  }

  /**
   * Emits a literal attribute, dispatching on the Lit binding prefix in {@code name}:
   * <ul>
   * <li>No prefix: emits an HTML attribute {@code name="value"}.</li>
   * <li>{@code .} prefix (property binding): emits <code>.name=${`value`}</code>.</li>
   * <li>{@code ?} prefix (boolean attribute binding): emits <code>?name=${true}</code> unless
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
   * Emits a Lit event listener binding <code>@eventName=${functionName}</code>. {@code functionName}
   * must reference a function previously registered via {@link #withFunction}.
   *
   * @param eventName the DOM event name (without the {@code @} prefix)
   * @param functionIndex the index of a function previously registered via {@link #withFunction}
   */
  public void event(String eventName, int functionIndex) {
    requireNotClosed();
    requireTagOpen();
    template.append(" @%s=${%s}".formatted(eventName, getFunctionName(functionIndex)));
  }

  /**
   * Emits a Lit event listener binding <code>@eventName=${handler}</code> where {@code handler} is
   * an inline client-side expression rather than a registered function reference.
   *
   * @param eventName the DOM event name (without the {@code @} prefix)
   * @param handler the client-side event handler expression
   */
  public void event(String eventName, String handler) {
    requireNotClosed();
    requireTagOpen();
    template.append(" @%s=${%s}".formatted(eventName, handler));
  }

  /**
   * Binds a Lit boolean attribute <code>?name=${...}</code> to a per-row predicate. {@code null} is a
   * no-op.
   */
  public void bindBoolean(String name, SerializablePredicate<T> predicate) {
    requireNotClosed();
    if (predicate != null) {
      requireTagOpen();
      requireProperty();
      template.append(" ?%s=${item.%s[%s]}".formatted(name, property, register(predicate::test)));
    }
  }

  /**
   * Snapshots the named attributes from {@code component}'s element and delegates each non-empty
   * value to {@link #set(String, String)}. Names with {@code .} or {@code ?} prefixes are
   * read via {@link Element#getProperty(String)} on the stripped name; names with no prefix are
   * read via {@link Element#getAttribute(String)}. Emission (HTML attribute vs. property vs.
   * boolean binding) follows {@code set(String, String)}'s dispatch rules.
   */
  public void copyAttributes(HasElement component, String... names) {
    requireNotClosed();
    requireTagOpen();

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
   * Snapshots all attributes and properties from {@code component}'s element (excluding names
   * listed in {@code names}) and delegates each to {@link #set(String, String)}. Properties are
   * emitted with a {@code .} prefix; attributes are emitted as-is. An empty {@code names} array
   * copies everything.
   *
   * <p>Exclusions are matched against the <em>emitted</em> binding name, prefix included: a plain
   * name (e.g. {@code "theme"}) excludes only the attribute of that name, while a {@code .}-prefixed
   * name (e.g. {@code ".theme"}) excludes only the property. To exclude both the attribute and the
   * property of the same name, list both forms.
   */
  public void copyAllAttributesAndPropertiesExcept(HasElement component, String... names) {
    requireNotClosed();
    requireTagOpen();

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
    Predicate<? super String> excludingAttribute = excludingAttribute(names);
    return name -> excludingAttribute.test("."+name);
  }

  /**
   * Binds per-row {@code .attr} and {@code .prop} object properties on the current open tag,
   * populated from all attributes and properties of the component returned by
   * {@code componentProvider}. Attributes are collected into a map bound to {@code .attr};
   * properties are collected into a map bound to {@code .prop}. Empty maps are passed as
   * {@code null}; a {@code null} component maps to {@code null} for both.
   *
   * @param <C> the component type
   * @param componentProvider provides the source component for each row item
   */
  public <C extends HasElement> void spreadAllAttributesAndProperties(
      ValueProvider<T, C> componentProvider) {
    requireNotClosed();
    requireTagOpen();
    requireProperty();

    // Evaluate componentProvider once per item across all lambdas.
    // Cache<T,C> intentionally shadows the enclosing method's T and C type parameters:
    // local records are implicitly static and cannot capture method type parameters directly.
    record Cache<T, C>(T item, C component) {}
    Ref<Cache<T, C>> cacheRef = new Ref<>();

    ValueProvider<T, C> once = item -> {
      if (cacheRef.value == null || cacheRef.value.item != item) {
        cacheRef.value = new Cache<>(item, componentProvider.apply(item));
      }
      return cacheRef.value.component;
    };
    
    int attrIdx = register(item -> {
      C component = once.apply(item);
      if (component == null) {
        return null;
      }
      Element el = component.getElement();
      JsonObject obj = Json.createObject();
      el.getAttributeNames().forEach(name -> obj.put(name, el.getAttribute(name)));
      return obj.keys().length == 0 ? null : obj;
    });

    int propIdx = register(item -> {
      C component = once.apply(item);
      if (component == null) {
        return null;
      }
      Element el = component.getElement();
      JsonObject obj = Json.createObject();
      el.getPropertyNames().forEach(name -> obj.put(name, el.getProperty(name)));
      return obj.keys().length == 0 ? null : obj;
    });

    template.append(" .attr=${item.%s[%d]} .prop=${item.%s[%d]}".formatted(property, attrIdx, property, propIdx));
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
    requireProperty();
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

  @SuppressWarnings("serial")
  private static final class Ref<V> implements Serializable {
    transient V value;
  }

  enum BindingType {
    ATTRIBUTE, PROPERTY, BOOLEAN;

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
