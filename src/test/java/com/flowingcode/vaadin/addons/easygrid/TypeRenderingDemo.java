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
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.Getter;

@DemoSource
@PageTitle("Type Rendering")
@SuppressWarnings("serial")
@Route(value = "easy-grid/type-rendering", layout = EasyGridDemoView.class)
@Getter // hide-source
public class TypeRenderingDemo extends Div {

  private final PersonService service = new PersonService();

  //#if vaadin eq 0
  private final Html description = new Html("""
      <div>Demonstrates default type-driven rendering from <code>GlobalEasyGridConfiguration</code>
      with no per-column configuration.
      <ul>
      <li><code>Number</code> columns are right-aligned.</li>
      <li><code>LocalDate</code> is formatted as <code>yyyy-MM-dd</code>.</li>
      <li><code>LocalDateTime</code> is formatted as <code>yyyy-MM-dd HH:mm:ss</code>.</li>
      <li><code>LocalTime</code> is formatted as <code>HH:mm:ss</code>.</li>
      <li>Null values are rendered as empty strings.</li>
      <li><code>Boolean</code> columns are center-aligned.</li>
      </ul>
      </div>
      """);
  //#endif

  public TypeRenderingDemo() {

    // No per-column configuration — all rendering is driven by GlobalEasyGridConfiguration defaults.
    //
    // age (int / Number)      → right-aligned         (Number.class → END alignment)
    // birthDate (LocalDate)   → "yyyy-MM-dd"          (LocalDate global renderer factory)
    // appointmentDateTime     → "yyyy-MM-dd HH:mm:ss" (LocalDateTime global renderer factory)
    //   when null             → ""                    (Object.class nullRepresentation = "")
    // appointmentTime         → "HH:mm:ss"            (LocalTime global renderer factory, derived from appointmentDateTime)
    //   when null             → ""                    (Object.class nullRepresentation = "")
    // subscriber (boolean)    → "true"/"false", CENTER (Boolean global formatter + alignment)
    var grid = new EasyGrid<>(Person.class,
        "firstName", "age", "birthDate", "appointmentDateTime", "appointmentTime", "subscriber");

    grid.setItems(service.fetchAll());
    add(grid);
    add(description); //hide-source
    setSizeFull();
  }
}
