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
@PageTitle("Selective Columns")
@SuppressWarnings("serial")
@Route(value = "easy-grid/selective-columns", layout = EasyGridDemoView.class)
@Getter // hide-source
public class SelectiveColumnsDemo extends Div {

  private final PersonService service = new PersonService();

  //#if vaadin eq 0
  private final Html description = new Html("""
      <div>Columns are created for explicit property names passed to the constructor.
      <ul>
      <li>Dot notation accesses nested bean properties (e.g. <code>address.city</code>).</li>
      <li>Columns can be added after construction with <code>addColumn</code>/<code>addColumns</code>.</li>
      <li><code>setColumnOrder</code> reorders columns and hides any not listed.</li>
      <li><code>hideColumns</code> hides specific columns without affecting the rest.</li>
      </ul>
      </div>
      """);
  //#endif

  public SelectiveColumnsDemo() {

    // Pass explicit property names to the constructor to control which columns are created
    // and in what initial order. Dot-notation addresses nested bean properties.
    EasyGrid<Person> grid = new EasyGrid<>(Person.class,
        "firstName", "lastName", "birthDate", "address.city", "address.postalCode");

    // Further columns can be added individually or in bulk after construction.
    grid.addColumn("age");
    grid.addColumns("phoneNumber", "subscriber");

    // setColumnOrder reorders the listed columns and hides all others.
    // phoneNumber and subscriber are not listed, so they are hidden.
    grid.setColumnOrder("firstName", "lastName", "age", "birthDate",
        "address.city", "address.postalCode");

    // hideColumns hides specific columns without affecting the order of the rest.
    grid.hideColumns("address.postalCode");

    grid.setItems(service.fetchAll());
    add(grid);
    add(description); //hide-source
    setSizeFull();
  }
}
