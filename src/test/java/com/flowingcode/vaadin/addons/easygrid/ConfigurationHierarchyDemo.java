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
import com.flowingcode.vaadin.addons.easygrid.config.GlobalEasyGridConfiguration;
import com.flowingcode.vaadin.addons.easygrid.model.Address;
import com.flowingcode.vaadin.addons.easygrid.model.Person;
import com.flowingcode.vaadin.addons.easygrid.service.PersonService;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.Getter;

@DemoSource
@PageTitle("Configuration Hierarchy")
@SuppressWarnings("serial")
@Route(value = "easy-grid/config-hierarchy", layout = EasyGridDemoView.class)
@Getter // hide-source
public class ConfigurationHierarchyDemo extends Div {

  private final PersonService service = new PersonService();

  //#if vaadin eq 0
  private final Html description = new Html("""
      <div>Configuration is resolved by precedence: property level (highest) → instance type level → global level (lowest).
      <ul>
      <li>The <code>address</code> column uses the global <code>Address</code> formatter.</li>
      <li>The <code>active</code> column uses the instance-level <code>Boolean</code> formatter.</li>
      <li><code>subscriber</code> overrides both with its own property-level formatter.</li>
      </ul>
      </div>
      """);
  //#endif

  public ConfigurationHierarchyDemo() {

    // Level 3 (lowest) — global: applies to every EasyGrid instance in the application.
    // Registered once at startup; affects all grids that show an Address column.
    GlobalEasyGridConfiguration.forType(Address.class)
        .setFormatter(a -> a.getPostalCode() + " " + a.getCity());

    EasyGrid<Person> grid = new EasyGrid<>(Person.class,
        "firstName", "subscriber", "active", "address");

    // Level 2 — instance type: overrides the global Boolean default ("true"/"false")
    // for all Boolean columns in this EasyGrid only.
    grid.typeConfiguration(Boolean.class)
        .setFormatter(v -> v ? "Yes" : "No");

    // Level 1 (highest) — property: overrides the instance type config for "subscriber" only.
    // Any other Boolean column in this grid would still show "Yes" / "No" (instance level).
    grid.getColumn("subscriber").as(Boolean.class)
        .setFormatter(v -> v ? "Subscribed" : "Not Subscribed");

    // Resolution summary for this grid:
    //   address    → global Address formatter  ("postalCode city")
    //   subscriber → property-level formatter  ("Subscribed" / "Not Subscribed")
    //   (any other Boolean column would use instance-level "Yes" / "No")

    grid.setItems(service.fetchAll());
    add(grid);
    add(description); //hide-source
    setSizeFull();
  }
}
