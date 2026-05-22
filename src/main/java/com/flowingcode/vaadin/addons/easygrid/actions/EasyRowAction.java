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
@SuppressWarnings("serial")
public final class EasyRowAction<T>
    implements Serializable, HasStyle, HasThemeVariant<ButtonVariant> {

  private final ValueProvider<T, String> labelProvider;
  private final ValueProvider<T, AbstractIcon<?>> iconProvider;
  private final SerializableConsumer<T> actionHandler;

  @Getter
  private final Element element = new Element("easy-row-action");

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
      dialog.setConfirmText(confirmText);
      dialog.setCancelable(true);
      dialog.setCancelText(cancelText);
      return dialog;
    };
    return this;
  }

  void updateRenderer(LitRendererBuilder<T> renderer) {

    renderer.withCondition(visibleWhen, () -> {
      int fn = renderer.withFunction((item, args) -> handleClick(item));
      renderer.tag("vaadin-button", () -> {
        if (enabledWhen != null) {
          renderer.bindBoolean("disabled", t -> !enabledWhen.test(t));
        }
        renderer.bind("title", tooltipProvider);
        renderer.copyAllAtttributesAndPropertiesExcept(this, "theme", "title");
        
        renderer.set("theme", getTheme());
        renderer.event("click", fn);

        if (iconProvider != null) {
          renderer.tag("fc-icon", () -> {
            renderer.bindAllAttributesAndProperties(iconProvider);
          });
        }

        renderer.addContent(labelProvider);
      });
    });
  }

  private void handleClick(T item) {
    if (confirmDialogSupplier != null) {
      ConfirmDialog dialog = confirmDialogSupplier.get();
      dialog.addConfirmListener(e -> actionHandler.accept(item));
      dialog.open();
    } else {
      actionHandler.accept(item);
    }
  }

  /**
   * Returns the {@code theme} attribute value for this action's {@code <vaadin-button>}, combining
   * any user-set theme variants (e.g. {@code "primary"}) with {@code "icon"} when the button is
   * icon-only (an icon is configured and no label provider was set). Returns {@code null} when no
   * theme variant applies.
   */
  String getTheme() {
    String theme = getElement().getAttribute("theme");
    if (iconProvider != null && labelProvider == null) {
      theme = theme == null || theme.isEmpty() ? "icon" : theme + " icon";
    }
    return theme;
  }

}
