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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.shared.Registration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Flow wrapper for the {@code <fc-dynamic-buttons>} web component. Renders a row of
 * {@code <vaadin-button>} elements driven by a list of {@link ButtonDefinition} objects.
 *
 * <p>Buttons are configured entirely on the server side and serialized to the {@code buttons}
 * property. A {@link ButtonClickEvent} is fired when the user clicks any button, carrying the
 * zero-based index of the clicked button.
 *
 * <pre>{@code
 * var buttons = new DynamicVaadinButtons(
 *     ButtonDefinition.ofLabel("Edit").theme("primary"),
 *     ButtonDefinition.ofIcon(IconDefinition.ofIcon("vaadin:trash")).theme("error tertiary icon")
 * );
 * buttons.addButtonClickListener(e -> {
 *     if (e.getIndex() == 0) edit(item);
 *     else if (e.getIndex() == 1) delete(item);
 * });
 * }</pre>
 */
@Tag("fc-dynamic-buttons")
@JsModule("./fc-dynamic-buttons.ts")
public class DynamicButtons extends Component {

  private final List<ButtonDefinition> buttons = new ArrayList<>();

  /** Creates a {@code DynamicVaadinButtons} with no buttons. */
  public DynamicButtons() {}

  /**
   * Creates a {@code DynamicVaadinButtons} pre-populated with the given buttons.
   *
   * @param buttons the initial button definitions
   */
  public DynamicButtons(ButtonDefinition... buttons) {
    setButtons(Arrays.asList(buttons));
  }

  /**
   * Replaces all buttons with the given list.
   *
   * @param buttons the new button definitions
   */
  public void setButtons(List<ButtonDefinition> buttons) {
    this.buttons.clear();
    this.buttons.addAll(buttons);
    syncButtons();
  }

  /**
   * Returns an unmodifiable view of the current button definitions.
   *
   * @return the button definitions
   */
  public List<ButtonDefinition> getButtons() {
    return Collections.unmodifiableList(buttons);
  }

  private void syncButtons() {
    var maps = buttons.stream().map(ButtonDefinition::getState).toList();
    getElement().setPropertyList("buttons", maps);
  }

  /**
   * Registers a listener for {@link ButtonClickEvent}, fired when the user clicks any button.
   *
   * @param listener the listener
   * @return a {@link Registration} to remove the listener
   */
  public Registration addButtonClickListener(ComponentEventListener<ButtonClickEvent> listener) {
    return addListener(ButtonClickEvent.class, listener);
  }

  /**
   * Event fired when a button inside a {@link DynamicButtons} is clicked. Carries the
   * zero-based index of the clicked button so callers can correlate it with the
   * {@link ButtonDefinition} list returned by {@link DynamicButtons#getButtons()}.
   */
  @DomEvent("button-click")
  public static class ButtonClickEvent extends ComponentEvent<DynamicButtons> {

    private final int index;

    public ButtonClickEvent(DynamicButtons source, boolean fromClient,
        @EventData("event.detail.index") int index) {
      super(source, fromClient);
      this.index = index;
    }

    /**
     * Returns the zero-based index of the clicked button.
     *
     * @return the button index
     */
    public int getIndex() {
      return index;
    }
  }

}
