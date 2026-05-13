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

@DemoSource
@PageTitle("Auto Column Discovery")
@SuppressWarnings("serial")
@Route(value = "easy-grid/auto-columns", layout = EasyGridDemoView.class)
public class AutoColumnsDemo extends Div {

  private final PersonService service = new PersonService();

  //#if vaadin eq 0
  private final Html description = new Html("""
      <div>All bean properties of the given class are discovered automatically.
      <ul>
      <li>Column headers are derived from camelCase names (e.g. <code>firstName</code> → "First Name").</li>
      <li>Columns whose type implements <code>Comparable</code>, or that are primitive, are sortable.</li>
      </ul>
      </div>
      """);
  //#endif

  public AutoColumnsDemo() {

    // All bean properties are discovered automatically from the class.
    // Headers are derived from camelCase names ("firstName" → "First Name").
    // Columns whose type implements Comparable, or that are primitive, are made sortable.
    var grid = new EasyGrid<>(Person.class);

    grid.setItems(service.fetchAll());
    add(grid);
    add(description); //hide-source
    setSizeFull();
  }
}
