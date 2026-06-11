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
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@DemoSource
@PageTitle("Conditional Actions")
@SuppressWarnings("serial")
@Route(value = "easy-grid/row-actions", layout = EasyGridDemoView.class)
public class RowActionsDemo extends Div {

  private final PersonService service = new PersonService();

  public RowActionsDemo() {
    var grid = new EasyGrid<>(Person.class);
    grid.hideColumns("phoneNumber", "appointmentDateTime", "appointmentTime", "subscriber");
    grid.setItems(service.fetchAll());

    // Always visible; static tooltip
    grid.addRowAction("Edit", VaadinIcon.EDIT.create(), person ->
        Notification.show("Edit: " + person.getFirstName() + " " + person.getLastName()))
        .tooltip("Edit person details");

    // Always visible; enabled only when a phone number is set;
    // tooltip changes dynamically to show the number or explain the disabled state
    grid.addRowAction(VaadinIcon.PHONE, person ->
        Notification.show("Calling " + person.getPhoneNumber()))
        .enabledWhen(person -> person.getPhoneNumber() != null)
        .tooltip(person -> person.getPhoneNumber() != null
            ? "Call " + person.getPhoneNumber()
            : "No phone number on record");

    // Visible only for active persons; requires confirmation before executing
    grid.addRowAction("Deactivate", VaadinIcon.CLOSE_CIRCLE.create(), person ->
        Notification.show("Deactivated: " + person.getFirstName() + " " + person.getLastName()))
        .visibleWhen(Person::isActive)
        .withConfirmation("Deactivate person",
            "Are you sure you want to deactivate this person?")
        .addThemeVariants(ButtonVariant.LUMO_ERROR);

    // Visible only for inactive persons
    grid.addRowAction("Activate", VaadinIcon.CHECK_CIRCLE.create(), person ->
        Notification.show("Activated: " + person.getFirstName() + " " + person.getLastName()))
        .visibleWhen(person -> !person.isActive());

    // Per-row dynamic icon: filled star for subscribers, outline star for non-subscribers
    grid.addRowAction(
        person -> person.isSubscriber() ? VaadinIcon.STAR.create() : VaadinIcon.STAR_O.create(),
        person -> Notification.show(
            (person.isSubscriber() ? "Unsubscribe" : "Subscribe") + ": " + person.getFirstName()));

    grid.getActionsColumn().setHeader("Actions");
    add(grid);
    setSizeFull();
  }
}
