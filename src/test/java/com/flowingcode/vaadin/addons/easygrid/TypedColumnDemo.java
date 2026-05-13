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
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.temporal.ChronoUnit;
import lombok.Getter;

@DemoSource
@PageTitle("Typed Column")
@SuppressWarnings("serial")
@Route(value = "easy-grid/typed-column", layout = EasyGridDemoView.class)
@Getter // hide-source
public class TypedColumnDemo extends Div {

  private final PersonService service = new PersonService();

  //#if vaadin eq 0
  private final Html description = new Html("""
      <div>Demonstrates <code>addColumn(Class, ValueProvider)</code> for computed values
      with no corresponding bean property.
      <ul>
      <li>The explicit type drives type-aware configuration (renderers, formatters, alignment) automatically.</li>
      <li>No key or header is derived automatically; configure them on the returned <code>EasyColumn</code>.</li>
      </ul>
      </div>
      """);
  //#endif

  public TypedColumnDemo() {

    EasyGrid<Person> grid = new EasyGrid<>(Person.class, false);

    // addColumn(Class, ValueProvider) adds a column for a computed value that has no
    // corresponding bean property. The type drives the type-aware configuration
    // (e.g. LocalDate columns get the configured date renderer automatically).
    // No key or header is set automatically — configure them on the returned EasyColumn.
    grid.addColumn(String.class, p -> p.getFirstName() + " " + p.getLastName())
        .setHeader("Full Name");

    grid.addColumn("birthDate");

    grid.addColumn(Integer.class, p -> {
      LocalDate today = LocalDate.now();
      MonthDay birthMonthDay = MonthDay.from(p.getBirthDate());
      LocalDate nextBirthday = birthMonthDay.atYear(today.getYear());

      if (nextBirthday.isBefore(today)) {
        nextBirthday = birthMonthDay.atYear(today.getYear() + 1);
      }

      return (int) ChronoUnit.DAYS.between(today, nextBirthday);
    }).setHeader("Days Until Birthday");

    grid.setItems(service.fetchAll());
    add(grid);
    add(description); //hide-source
    setSizeFull();
  }
}
