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

import com.vaadin.flow.component.icon.IconFactory;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Describes the icon to render inside a {@link ButtonDefinition}. Mirrors the {@code IconDefinition}
 * TypeScript interface used by the {@code <dynamic-vaadin-buttons>} web component.
 *
 * <p>Use one of the static factory methods to select the icon source, then chain fluent methods for
 * additional options.
 *
 * <pre>{@code
 * // Vaadin iconset
 * IconDefinition.ofIcon("vaadin:edit")
 *
 * // Icon font glyph by CSS class
 * IconDefinition.ofIconClass("fa-solid fa-trash").fontFamily("Font Awesome 6 Free")
 *
 * // External SVG file with a symbol
 * IconDefinition.ofSrc("/icons/sprites.svg").symbol("my-icon")
 * }</pre>
 */
@SuppressWarnings("serial")
public final class IconDefinition implements Serializable {

  private String icon;
  private String src;
  private String symbol;
  private String iconClass;
  private String charCode;
  private String ligature;
  private String fontFamily;
  private Integer size;

  private IconDefinition() {}

  public static IconDefinition of(IconFactory iconFactory) {
    return ofIcon(iconFactory.create().getIcon());
  }

  /**
   * Creates an icon definition using an iconset name in {@code "iconset:name"} format, e.g.
   * {@code "vaadin:check"}.
   *
   * @param icon the icon name
   * @return a new {@code IconDefinition}
   */
  public static IconDefinition ofIcon(String icon) {
    IconDefinition def = new IconDefinition();
    def.icon = icon;
    return def;
  }

  /**
   * Creates an icon definition using a URL to an SVG file or a {@code data:image/svg+xml,...}
   * string.
   *
   * @param src the URL or data URI
   * @return a new {@code IconDefinition}
   */
  public static IconDefinition ofSrc(String src) {
    IconDefinition def = new IconDefinition();
    def.src = src;
    return def;
  }

  /**
   * Creates an icon definition using CSS class names for an icon font glyph, e.g.
   * {@code "fa-solid fa-user"}.
   *
   * @param iconClass the CSS class names
   * @return a new {@code IconDefinition}
   */
  public static IconDefinition ofIconClass(String iconClass) {
    IconDefinition def = new IconDefinition();
    def.iconClass = iconClass;
    return def;
  }

  /**
   * Creates an icon definition using a hex code point for an icon font glyph, e.g. {@code "e001"}.
   *
   * @param charCode the hex code point
   * @return a new {@code IconDefinition}
   */
  public static IconDefinition ofChar(String charCode) {
    IconDefinition def = new IconDefinition();
    def.charCode = charCode;
    return def;
  }

  /**
   * Creates an icon definition using a ligature name for icon fonts that support ligatures.
   *
   * @param ligature the ligature name
   * @return a new {@code IconDefinition}
   */
  public static IconDefinition ofLigature(String ligature) {
    IconDefinition def = new IconDefinition();
    def.ligature = ligature;
    return def;
  }

  /**
   * Sets the symbol ID inside the SVG referenced by {@code src}.
   *
   * @param symbol the symbol ID
   * @return this definition, for method chaining
   */
  public IconDefinition symbol(String symbol) {
    this.symbol = symbol;
    return this;
  }

  /**
   * Sets the font family for icon font glyphs.
   *
   * @param fontFamily the font family name
   * @return this definition, for method chaining
   */
  public IconDefinition fontFamily(String fontFamily) {
    this.fontFamily = fontFamily;
    return this;
  }

  /**
   * Sets the icon size used to set the {@code viewBox}.
   *
   * @param size the icon size in pixels
   * @return this definition, for method chaining
   */
  public IconDefinition size(int size) {
    this.size = size;
    return this;
  }

  Map<String, Object> getState() {
    Map<String, Object> map = new LinkedHashMap<>();
    if (icon != null) {
      map.put("icon", icon);
    }
    if (src != null) {
      map.put("src", src);
    }
    if (symbol != null) {
      map.put("symbol", symbol);
    }
    if (iconClass != null) {
      map.put("iconClass", iconClass);
    }
    if (charCode != null) {
      map.put("char", charCode);
    }
    if (ligature != null) {
      map.put("ligature", ligature);
    }
    if (fontFamily != null) {
      map.put("fontFamily", fontFamily);
    }
    if (size != null) {
      map.put("size", size);
    }
    return map;
  }

}
