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
import com.flowingcode.vaadin.addons.easygrid.actions.ButtonDefinition;
import com.flowingcode.vaadin.addons.easygrid.actions.DynamicButtons;
import com.flowingcode.vaadin.addons.easygrid.actions.IconDefinition;
import com.flowingcode.vaadin.addons.easygrid.model.Person;
import com.flowingcode.vaadin.addons.easygrid.service.PersonService;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@DemoSource
@PageTitle("Dynamic Buttons")
@SuppressWarnings("serial")
@Route(value = "easy-grid/dynamic-buttons", layout = EasyGridDemoView.class)
public class DynamicButtonsDemo extends Div {

  private final PersonService service = new PersonService();

  public DynamicButtonsDemo() {

    var grid = new EasyGrid<>(Person.class);
    grid.setItems(service.fetchAll());

    grid.addComponentColumn(person -> {
      var buttons = new DynamicButtons(
          ButtonDefinition.ofLabel("Edit")
              .icon(IconDefinition.of(VaadinIcon.EDIT))
              .theme("tertiary"),
          ButtonDefinition.ofIcon(IconDefinition.of(VaadinIcon.TRASH))
              .theme("error tertiary icon"));

      buttons.addButtonClickListener(e -> {
        if (e.getIndex() == 0) {
          Notification.show("Edit: " + person.getFirstName() + " " + person.getLastName());
        } else {
          Notification.show("Delete: " + person.getFirstName() + " " + person.getLastName());
        }
      });

      return buttons;
    }).setHeader("Actions");

    add(grid);
    setSizeFull();
  }
}
