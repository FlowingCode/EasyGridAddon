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

  private record Property<T>(int index, ValueProvider<T, ?> provider) {
  }

  private final List<Property<T>> properties = new ArrayList<>();
  private int propertyIndex;

  public LitRendererBuilder(@NonNull String property) {
    this.property = property;
  }

  public void append(String s) {
    template.append(s);
  }

  private int withProperty(ValueProvider<T, ?> provider) {
    properties.add(new Property<T>(propertyIndex, provider));
    return propertyIndex++;
  }

  public void withFunction(String functionName, SerializableBiConsumer<T, JsonArray> handler) {
    configuration.add(lit -> lit.withFunction(functionName, handler));
  }

  public LitRenderer<T> build() {
    LitRenderer<T> renderer = LitRenderer.of(template.toString());
    if (!properties.isEmpty()) {
      renderer.withProperty("actions", t -> {
        var obj = Json.createObject();
        properties.forEach(property -> {
          var k = Integer.toString(property.index);
          var v = property.provider.apply(t);
          obj.put(k, JsonSerializer.toJson(v));
        });
        return obj;
      });
    }

    configuration.forEach(c -> c.accept(renderer));
    return renderer;
  }

  public int addObjectProperty(ValueProvider<T, ?> provider) {
    return withProperty(provider);
  }

  public void nestedStringAttribute(String attrName, int idx, String key) {
    template.append(" %s=${item.%s[%d]?.%s}".formatted(attrName, property, idx, key));
  }

  public void booleanAttribute(String name, ValueProvider<T, Boolean> predicate) {
    if (predicate != null) {
      template.append(" ?%s=${item.%s[%s]}".formatted(name, property, withProperty(predicate)));
    }
  }

  public void stringAttribute(String name, ValueProvider<T, String> value) {
    if (value != null) {
      if (value instanceof Constant) {
        String constant = value.apply(null);
        template.append(" %s=%s".formatted(name, wrapAndEscapeTemplateCharacters(constant)));
      } else {
        template.append(" %s=${item.%s[%s]}".formatted(name, property, withProperty(value)));
      }
    }
  }

  public void copyAttributes(HasElement component, String... names) {
    Element element = component.getElement();
    for (String name : names) {
      String value = element.getAttribute(name);
      if (value != null && !value.isEmpty()) {
        template.append(" %s=%s".formatted(name, wrapAndEscapeTemplateCharacters(value)));
      }
    }
  }

  private static String wrapAndEscapeTemplateCharacters(String value) {
    if (value.indexOf('"') < 0) {
      return '"' + value + '"';
    } else {
      return "${`" + value.replace("`", "\\`").replace("$", "\\$") + "`}";
    }
  }

  public void beginCondition(SerializablePredicate<T> predicate) {
    template.append("${item.%s[%s] ? html`".formatted(property, withProperty(predicate::test)));
  }

  public void endCondition() {
    template.append("` : undefined}");
  }

}
