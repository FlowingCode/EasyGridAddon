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

import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.icon.AbstractIcon;
import com.vaadin.flow.component.icon.FontIcon;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.function.ValueProvider;
import java.io.Serializable;
import lombok.Getter;
import lombok.NonNull;

/**
 * Represents a row action registered on an {@code EasyGrid}. Row actions are rendered as buttons
 * (or menu items when {@code setRowActionsAsMenu(true)} is used) in a dedicated actions column.
 *
 * <p>Use the fluent methods to configure conditional visibility, conditional enablement, tooltips,
 * and optional confirmation dialogs before the action is executed.
 *
 * @param <T> the grid bean type
 */
@Getter
@SuppressWarnings("serial")
public final class EasyRowAction<T>
    implements Serializable, HasStyle, HasThemeVariant<ButtonVariant> {

  private final ValueProvider<T, String> labelProvider;
  private final ValueProvider<T, AbstractIcon<?>> iconProvider;
  private final SerializableConsumer<T>  actionHandler;

  private final Element element = new Element("easy-row-action");
  // private final ThemeList themeList = new ThemeListImpl();
  //
  // private final class ThemeListImpl extends ListImpl {}
  //
  // private static abstract class ListImpl extends AbstractSet<String> implements ThemeList {
  //
  // private final List<String> list = new ArrayList<>();
  //
  // @Override
  // public boolean add(String className) {
  // if (list.contains(className)) {
  // return false;
  // }
  // return list.add(className);
  // }
  //
  // @Override
  // public int size() {
  // return list.size();
  // }
  //
  // @Override
  // public Iterator<String> iterator() {
  // return list.iterator();
  // }
  // }
  
  @SuppressWarnings("unchecked")
  <ICON extends AbstractIcon<ICON>> EasyRowAction(ValueProvider<T, String> labelProvider,
      ValueProvider<T, ICON> iconProvider,
      @NonNull SerializableConsumer<T> actionHandler) {
    if (labelProvider == null && iconProvider == null) {
      throw new IllegalArgumentException("At least one of label or icon must be non-null");
    }
    this.labelProvider = labelProvider;
    this.iconProvider = (ValueProvider<T, AbstractIcon<?>>) iconProvider;
    this.actionHandler = actionHandler;
  }
  
  private SerializablePredicate<T> visibleWhen;
  private SerializablePredicate<T> enabledWhen;
  private ValueProvider<T, String> tooltipProvider;
  private SerializableSupplier<ConfirmDialog> confirmDialogSupplier;

  // @Override
  // public Style getStyle() {
  // if (styles == null) {
  // styles = new ElementStylePropertyMap(new StateNode());
  // }
  // return styles.getStyle();
  // }
  //
  // @Override
  // public Element getElement() {
  // throw new UnsupportedOperationException();
  // }

  /**
   * Sets a predicate that controls whether this action is visible for a given row item.
   *
   * @param predicate a predicate evaluated for each row item; the action is visible when it returns
   *        {@code true}
   * @return this action, for method chaining
   */
  public EasyRowAction<T> visibleWhen(SerializablePredicate<T> predicate) {
    this.visibleWhen = predicate;
    return this;
  }

  /**
   * Sets a predicate that controls whether this action is enabled for a given row item.
   *
   * @param predicate a predicate evaluated for each row item; the action is enabled when it returns
   *        {@code true}
   * @return this action, for method chaining
   */
  public EasyRowAction<T> enabledWhen(SerializablePredicate<T> predicate) {
    this.enabledWhen = predicate;
    return this;
  }

  /**
   * Sets a static tooltip for this action.
   *
   * @param tooltip the tooltip text
   * @return this action, for method chaining
   */
  public EasyRowAction<T> tooltip(String tooltip) {
    this.tooltipProvider = Constant.of(tooltip);
    return this;
  }

  /**
   * Sets a dynamic tooltip for this action, computed from the row item.
   *
   * @param tooltipProvider a function that returns the tooltip text for a given row item
   * @return this action, for method chaining
   */
  public EasyRowAction<T> tooltip(ValueProvider<T, String> tooltipProvider) {
    this.tooltipProvider = tooltipProvider;
    return this;
  }

  /**
   * Configures a confirmation dialog with a message (no title) before the action is executed.
   *
   * @param message the confirmation message
   * @return this action, for method chaining
   */
  public EasyRowAction<T> withConfirmation(String message) {
    return withConfirmation(null, message);
  }

  /**
   * Configures a confirmation dialog with both a title and a message before the action is executed.
   *
   * @param title the dialog title
   * @param message the confirmation message
   * @return this action, for method chaining
   */
  public EasyRowAction<T> withConfirmation(String title, String message) {
    return withConfirmation(title, message, "Ok", "Cancel");
  }

  private EasyRowAction<T> withConfirmation(String title, String message, String confirmText,
      String cancelText) {
    confirmDialogSupplier = () -> {
      var dialog = new ConfirmDialog();
      dialog.setHeader(title);
      dialog.setText(message);
      dialog.setConfirmText(cancelText);
      dialog.setCancelable(true);
      dialog.setCancelText(cancelText);
      return dialog;
    };
    return this;
  }

  /**
   * Returns the visibility predicate, or {@code null} if no visibility condition was set.
   *
   * @return the visibility predicate, or {@code null}
   */
  public SerializablePredicate<T> getVisibleWhen() {
    return visibleWhen;
  }

  /**
   * Returns the enablement predicate, or {@code null} if no enablement condition was set.
   *
   * @return the enablement predicate, or {@code null}
   */
  public SerializablePredicate<T> getEnabledWhen() {
    return enabledWhen;
  }

  /**
   * Returns the tooltip provider function, or {@code null} if no tooltip was set.
   *
   * @return the tooltip provider, or {@code null}
   */
  public SerializableFunction<T, String> getTooltipProvider() {
    return tooltipProvider;
  }

  /**
   * Returns {@code true} if a confirmation dialog is configured for this action.
   *
   * @return {@code true} if confirmation is required before executing the action
   */
  public boolean requiresConfirmation() {
    return confirmDialogSupplier != null;
  }

  void updateRenderer(LitRendererBuilder<T> renderer) {
    
    // if visible?
    // if (visibleWhen != null && !visibleWhen.test(item)) {
    // return null;
    // }

    renderer.append("<vaadin-button");
    renderer.stringAttribute("label", labelProvider);
    
    if (enabledWhen != null) {
      renderer.booleanAttribute("disabled", t -> !enabledWhen.test(t));
    }

    renderer.copyAttributes(this, "class", "style", "theme");
    renderer.stringAttribute("title", tooltipProvider);

    renderer.append(">");
    
    if (iconProvider != null) {
      renderer.append("<vaadin-icon slot=\"prefix\"");
      if (iconProvider instanceof Constant) {
        AbstractIcon<?> icon = iconProvider.apply(null);
        renderer.copyAttributes(icon, "size");
        if (icon instanceof Icon) {
          renderer.copyAttributes(icon, "icon");
        } else if (icon instanceof SvgIcon) {
          renderer.copyAttributes(icon, "src", "symbol");
        } else if (icon instanceof FontIcon) {
          renderer.copyAttributes(icon, "ligature", "char", "fontFamily", "iconClass");
        } else {
          renderer.copyAttributes(icon, "icon");
          renderer.copyAttributes(icon, "src", "symbol");
          renderer.copyAttributes(icon, "ligature", "char", "fontFamily", "iconClass");
        }
      } else {
        // ValueProvider<T, JsonValue> v = item -> {
        // return JsonSerializer.serMap.<T,JsonValue>of();
        // .icon=${btn.icon.icon ?? nothing}
        // .src=${btn.icon.src ?? nothing}
        // .symbol=${btn.icon.symbol ?? nothing}
        // .iconClass=${btn.icon.iconClass ?? nothing}
        // .char=${btn.icon.char ?? nothing}
        // .ligature=${btn.icon.ligature ?? nothing}
        // .fontFamily=${btn.icon.fontFamily ?? nothing}
        // .size=${btn.icon.size ?? nothing}
        // };

        ValueProvider<T, Element> z = item -> iconProvider.apply(item).getElement();
        renderer.stringAttribute("icon", item -> z.apply(item).getAttribute("icon"));
        renderer.stringAttribute("size", item -> z.apply(item).getAttribute("size"));
        renderer.stringAttribute("src", item -> z.apply(item).getAttribute("src"));
        renderer.stringAttribute("symbol", item -> z.apply(item).getAttribute("symbol"));
        renderer.stringAttribute("ligature", item -> z.apply(item).getAttribute("ligature"));
        renderer.stringAttribute("char", item -> z.apply(item).getAttribute("char"));
        renderer.stringAttribute("fontFamily", item -> z.apply(item).getAttribute("fontFamily"));
        renderer.stringAttribute("iconClass", item -> z.apply(item).getAttribute("iconClass"));
      }
      renderer.append(">");
    }
    
    renderer.append("</vaadin-button>");
  }

}
