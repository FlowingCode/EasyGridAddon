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

import com.flowingcode.vaadin.addons.easygrid.RuntimeReflectiveOperationException;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.icon.AbstractIcon;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.function.ValueProvider;
import java.io.Serializable;
import java.lang.reflect.Method;
import lombok.Getter;
import lombok.NonNull;

/**
 * Represents a row action registered on an {@code EasyGrid}. Row actions are rendered as buttons
 * (or menu items when {@code setRowActionsAsMenu(true)} is used) in a dedicated actions column.
 *
 * <p>Use the fluent methods to configure conditional visibility, conditional enablement, tooltips,
 * and optional confirmation dialogs before the action is executed. The rendered button can also be
 * styled and themed through the inherited {@link HasStyle} and {@link HasThemeVariant} methods.
 *
 * @param <T> the grid bean type
 */
@SuppressWarnings("serial")
public final class EasyRowAction<T>
    implements Serializable, HasStyle, HasThemeVariant<ButtonVariant> {

  // ConfirmDialog.addOpenedChangeListener was introduced in Vaadin 25; it does not exist in Vaadin 24.
  // Resolved once at class load: non-null means Vaadin 25 (use the Java listener); null means
  // Vaadin 24 (fall back to the DOM opened-changed event).
  private static final Method ADD_OPENED_CHANGE_LISTENER;
  static {
    Method addListener = null;
    try {
      addListener = ConfirmDialog.class.getMethod("addOpenedChangeListener", ComponentEventListener.class);
    } catch (NoSuchMethodException ignored) {}
    ADD_OPENED_CHANGE_LISTENER = addListener;
  }

  private RowActionsManager<T> manager;

  private final ValueProvider<T, String> labelProvider;
  private final ValueProvider<T, AbstractIcon<?>> iconProvider;
  private final SerializableConsumer<T> actionHandler;

  private static final String[] LIFTED_ICON_NAMES =
      {"icon", "src", ".symbol", ".ligature", ".char", ".fontFamily", ".iconClass"};

  /**
   * Backing element for this action. It is never attached to the DOM; it carries the attributes,
   * CSS classes/styles ({@link HasStyle}), and theme variants ({@link HasThemeVariant}) that are
   * forwarded onto the rendered {@code <vaadin-button>} when the actions column is built.
   */
  @Getter
  private final Element element = new Element("easy-row-action");

  @SuppressWarnings("unchecked")
  <ICON extends AbstractIcon<ICON>> EasyRowAction(RowActionsManager<T> manager,
      ValueProvider<T, String> labelProvider,
      ValueProvider<T, ICON> iconProvider,
      @NonNull SerializableConsumer<T> actionHandler) {
    if (labelProvider == null && iconProvider == null) {
      throw new IllegalArgumentException("At least one of label or icon must be non-null");
    }
    this.manager = manager;
    this.labelProvider = labelProvider;
    this.iconProvider = (ValueProvider<T, AbstractIcon<?>>) iconProvider;
    this.actionHandler = actionHandler;
  }
  

  private SerializablePredicate<T> visibleWhen;
  private SerializablePredicate<T> enabledWhen;
  private ValueProvider<T, String> tooltipProvider;
  private SerializableSupplier<ConfirmDialog> confirmDialogSupplier;
  private transient boolean confirmPending;

  private void refresh() {
    if (manager != null) {
      manager.refresh();
    }
  }

  /**
   * Sets a predicate that controls whether this action is visible for a given row item.
   *
   * @param predicate a predicate evaluated for each row item; the action is visible when it returns
   *        {@code true}
   * @return this action, for method chaining
   */
  public EasyRowAction<T> visibleWhen(SerializablePredicate<T> predicate) {
    this.visibleWhen = predicate;
    refresh();
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
    refresh();
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
    refresh();
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
    refresh();
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
    // Render-neutral (the dialog is built at click time), but refreshed so every fluent setter
    // behaves uniformly. The scheduled rebuild is coalesced with any other pending update.
    refresh();
    return this;
  }

  /**
   * Removes this action from the grid's actions column. The column is re-rendered on the next
   * {@code beforeClientResponse} cycle; if no actions remain the column is hidden. Calling
   * {@code remove()} on an action that was never registered, or that has already been removed,
   * is a no-op.
   */
  public void remove() {
    if (manager != null) {
      var manager = this.manager;
      this.manager = null;
      manager.removeRowAction(this);
    }
  }

  boolean isVisible(T item) {
    return visibleWhen == null || visibleWhen.test(item);
  }

  boolean isEnabled(T item) {
    return enabledWhen == null || enabledWhen.test(item);
  }

  String getLabel(T item) {
    return labelProvider != null ? labelProvider.apply(item) : null;
  }

  AbstractIcon<?> getIcon(T item) {
    return iconProvider != null ? iconProvider.apply(item) : null;
  }

  void execute(T item) {
    // Server-side guard: reject the click if the item no longer satisfies enabledWhen.
    // The client-side ?disabled binding prevents most clicks, but this closes the gap
    // for race conditions and devtools manipulation.
    if (enabledWhen != null && !enabledWhen.test(item)) {
      return;
    }
    if (confirmDialogSupplier != null) {
      // Prevent multiple dialogs from stacking on rapid clicks.
      if (confirmPending) {
        return;
      }
      confirmPending = true;
      ConfirmDialog dialog = confirmDialogSupplier.get();
      dialog.addConfirmListener(e -> actionHandler.accept(item));
      // Reset on any close path: confirm, cancel, or programmatic dialog.close()
      if (ADD_OPENED_CHANGE_LISTENER != null) {
        @SuppressWarnings({"rawtypes"})
        ComponentEventListener l = e -> {
          if (!dialog.isOpened()) {
            confirmPending = false;
          }
        };
        try {
          ADD_OPENED_CHANGE_LISTENER.invoke(dialog, l);
        } catch (ReflectiveOperationException ex) {
          throw new RuntimeReflectiveOperationException(ex);
        }
      } else {
        dialog.getElement().addEventListener("opened-changed", e -> confirmPending = false)
            .setFilter("event.detail.value === false");
      }
      dialog.open();
    } else {
      actionHandler.accept(item);
    }
  }

  void updateRenderer(LitRendererBuilder<T> renderer) {

    // Wrap the entire button in a visibility guard; renders nothing when visibleWhen returns false
    renderer.withCondition(visibleWhen, () -> {
      // Register a server-side function; returns its index so the click event can reference it
      int fn = renderer.withFunction((item, args) -> execute(item));
      // Open the <vaadin-button> element that represents this action in the row
      renderer.tag("vaadin-button", () -> {
        if (enabledWhen != null) {
          // Bind disabled to the inverse of enabledWhen so the button grays out when the predicate is false
          renderer.bindBoolean("disabled", t -> !enabledWhen.test(t));
        }

        // Forward all attributes/properties set on this action's element (e.g. CSS classes) except the two handled below
        renderer.copyAllAttributesAndPropertiesExcept(this, "theme", "title");
        // Bind the title attribute per-item so tooltip text can vary by row
        renderer.bind("title", tooltipProvider);
        // Set the theme attribute statically; getTheme() appends "icon" when the button is icon-only
        renderer.set("theme", getTheme());

        // Wire the DOM click event to the registered server function
        renderer.event("click", fn);

        if (iconProvider != null) {
          // Open an <fc-icon> child to render the icon; the icon element is evaluated per item
          renderer.tag("fc-icon", () -> {
            // Spread the icon's relevant attributes/properties (src, ligature, etc.) onto the element
            renderer.spreadAllAttributesAndProperties(iconProvider, LIFTED_ICON_NAMES);
          });
        }

        // Render the label text as button content; no-op when labelProvider is null (icon-only button)
        renderer.addContent(labelProvider);
      });
    });
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
    return theme == null || theme.isEmpty() ? null : theme;
  }

}
