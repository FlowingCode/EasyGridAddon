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
import com.flowingcode.vaadin.addons.easygrid.renderers.LocalDateRenderers;
import com.flowingcode.vaadin.addons.easygrid.renderers.LocalDateTimeRenderers;
import com.flowingcode.vaadin.addons.easygrid.service.PersonService;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;

@DemoSource
@PageTitle("Column Configuration")
@SuppressWarnings("serial")
@Route(value = "easy-grid/column-config", layout = EasyGridDemoView.class)
@Getter // hide-source
public class ColumnConfigurationDemo extends Div {

  private final PersonService service = new PersonService();

  //#if vaadin eq 0
  private final Html description = new Html("""
      <div>Demonstrates selective column creation by listing property names in the constructor.
      <ul>
      <li>Column properties are configured via <code>Grid.Column</code> delegation
          (<code>setHeader</code>, <code>setWidth</code>, <code>setFlexGrow</code>).</li>
      <li>The <code>.as(Type.class)</code> method narrows an <code>EasyColumn</code> to a typed handle
          for type-specific configuration such as <code>setRendererFactory</code>,
          <code>setFormatter</code>, and <code>setNullRepresentation</code>.</li>
      </ul>
      </div>
      """);
  //#endif

  public ColumnConfigurationDemo() {

    EasyGrid<Person> grid = new EasyGrid<>(Person.class,
        "firstName", "lastName", "birthDate", "appointmentDateTime", "subscriber", "age");

    // Grid.Column delegation — setHeader, setWidth, setFlexGrow, etc. are forwarded directly
    grid.getColumn("birthDate").setHeader("Date of Birth");
    grid.getColumn("appointmentDateTime").setHeader("Appointment");
    grid.getColumn("lastName").setWidth("180px").setFlexGrow(0);

    // .as(Type.class) narrows the EasyColumn to a typed handle for type-specific methods
    grid.getColumn("birthDate").as(LocalDate.class)
        .setRendererFactory(LocalDateRenderers.<Person>of("dd/MM/yyyy"));

    grid.getColumn("appointmentDateTime").as(LocalDateTime.class)
        .setRendererFactory(LocalDateTimeRenderers.<Person>of("dd/MM/yyyy HH:mm"))
        .setNullRepresentation("—");

    grid.getColumn("subscriber").as(Boolean.class)
        .setFormatter(v -> v ? "Subscribed" : "Not Subscribed");

    // setTextAlign overrides the alignment set by the type-level configuration
    grid.getColumn("age").setTextAlign(ColumnTextAlign.CENTER);

    grid.setItems(service.fetchAll());
    add(grid);
    add(description); //hide-source
    setSizeFull();
  }
}
