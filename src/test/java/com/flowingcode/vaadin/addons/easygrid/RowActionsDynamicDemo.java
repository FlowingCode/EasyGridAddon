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
package com.flowingcode.vaadin.addons.easygrid;

import com.flowingcode.vaadin.addons.demo.DemoSource;
import com.flowingcode.vaadin.addons.easygrid.model.Person;
import com.flowingcode.vaadin.addons.easygrid.service.PersonService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@DemoSource
@PageTitle("Runtime Action Changes")
@SuppressWarnings("serial")
@Route(value = "easy-grid/row-actions-dynamic", layout = EasyGridDemoView.class)
public class RowActionsDynamicDemo extends Div {

  private final PersonService service = new PersonService();

  public RowActionsDynamicDemo() {
    var grid = new EasyGrid<>(Person.class);
    grid.hideColumns("phoneNumber", "appointmentDateTime", "appointmentTime", "subscriber");
    grid.setItems(service.fetchAll());

    var editAction = grid.addRowAction("Edit", VaadinIcon.EDIT.create(), person ->
        Notification.show("Edit: " + person.getFirstName() + " " + person.getLastName()));
    var deleteAction = grid.addRowAction(VaadinIcon.TRASH, person ->
        Notification.show("Delete: " + person.getFirstName() + " " + person.getLastName()));
    deleteAction.addThemeVariants(ButtonVariant.LUMO_ERROR);

    // Changing visibleWhen after construction requires refreshRowActions() to take effect.
    var restrictCheckbox = new Checkbox("Show edit only for active persons");
    restrictCheckbox.addValueChangeListener(e -> {
      editAction.visibleWhen(e.getValue() ? Person::isActive : null);
    });

    // Removes the delete action from the grid entirely at runtime.
    var removeButton = new Button("Remove delete action");
    removeButton.addClickListener(e -> {
      deleteAction.remove();
      removeButton.setEnabled(false);
    });

    add(new HorizontalLayout(restrictCheckbox, removeButton), grid);
    setSizeFull();
  }
}
