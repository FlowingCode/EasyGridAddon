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

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;

/**
 * Describes a button rendered by the {@code <dynamic-vaadin-buttons>} web component. Mirrors the
 * {@code ButtonDefinition} TypeScript interface.
 *
 * <p>Use one of the static factory methods as the starting point, then chain fluent methods to
 * configure the button further.
 *
 * <pre>{@code
 * // Text-only button
 * ButtonDefinition.ofLabel("Edit")
 *     .theme("primary")
 *
 * // Icon-only button
 * ButtonDefinition.ofIcon(IconDefinition.ofIcon("vaadin:trash"))
 *     .theme("error tertiary icon")
 *
 * // Button with both label and icon
 * ButtonDefinition.ofLabel("Delete")
 *     .icon(IconDefinition.ofIcon("vaadin:trash"))
 *     .theme("error")
 * }</pre>
 */
@SuppressWarnings("serial")
@Getter
public final class ButtonDefinition implements Serializable {

  private String label;
  private String theme;
  private boolean disabled;
  private IconDefinition icon;

  private ButtonDefinition() {}

  /**
   * Creates a button definition with the given text label.
   *
   * @param label the button label
   * @return a new {@code ButtonDefinition}
   */
  public static ButtonDefinition ofLabel(String label) {
    ButtonDefinition def = new ButtonDefinition();
    def.label = label;
    return def;
  }

  /**
   * Creates an icon-only button definition.
   *
   * @param icon the icon configuration
   * @return a new {@code ButtonDefinition}
   */
  public static ButtonDefinition ofIcon(IconDefinition icon) {
    ButtonDefinition def = new ButtonDefinition();
    def.icon = icon;
    return def;
  }

  /**
   * Sets the icon configuration for this button.
   *
   * @param icon the icon configuration
   * @return this definition, for method chaining
   */
  public ButtonDefinition icon(IconDefinition icon) {
    this.icon = icon;
    return this;
  }

  /**
   * Sets the {@code theme} attribute on the rendered {@code <vaadin-button>}.
   *
   * @param theme the theme variant string, e.g. {@code "primary"} or {@code "error tertiary icon"}
   * @return this definition, for method chaining
   */
  public ButtonDefinition theme(String theme) {
    this.theme = theme;
    return this;
  }

  /**
   * Sets whether this button is disabled.
   *
   * @param disabled {@code true} to disable the button
   * @return this definition, for method chaining
   */
  public ButtonDefinition disabled(boolean disabled) {
    this.disabled = disabled;
    return this;
  }

  /**
   * Marks this button as disabled.
   *
   * @return this definition, for method chaining
   */
  public ButtonDefinition disabled() {
    return disabled(true);
  }

  Map<String, Object> getState() {
    Map<String, Object> map = new LinkedHashMap<>();
    if (label != null) {
      map.put("label", label);
    }
    if (theme != null) {
      map.put("theme", theme);
    }
    if (disabled) {
      map.put("disabled", true);
    }
    if (icon != null) {
      map.put("icon", icon.getState());
    }
    return map;
  }

}
