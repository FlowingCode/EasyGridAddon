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
package com.flowingcode.vaadin.addons.easygrid.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.flowingcode.vaadin.addons.easygrid.EasyColumn;
import com.flowingcode.vaadin.addons.easygrid.EasyGrid;
import com.flowingcode.vaadin.addons.easygrid.model.Person;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

public class EasyGridConstructionTest {

  @After
  public void tearDownUI() {
    UI.setCurrent(null);
  }

  @Test
  public void autoDiscoveryCreatesColumnsForBeanProperties() {
    UI ui = Mockito.mock(UI.class);
    Mockito.when(ui.getLocale()).thenReturn(Locale.ENGLISH);
    UI.setCurrent(ui);
    assertNotNull("UI must be set by @Before before EasyGrid construction", UI.getCurrent());
    EasyGrid<Person> grid = new EasyGrid<>(Person.class);
    List<String> keys = grid.getWrappedGrid().getColumns().stream()
        .map(Grid.Column::getKey)
        .collect(Collectors.toList());
    assertThat(keys, hasItems("firstName", "lastName", "age", "birthDate", "subscriber", "active"));
  }

  @Test
  public void explicitPropertyConstructorCreatesOnlyListedColumns() {
    EasyGrid<Person> grid = new EasyGrid<>(Person.class, "firstName", "lastName");
    List<String> keys = grid.getWrappedGrid().getColumns().stream()
        .map(Grid.Column::getKey)
        .collect(Collectors.toList());
    assertThat(keys, hasSize(2));
    assertThat(keys, hasItems("firstName", "lastName"));
  }

  @Test
  public void addColumnWithTypeAndGetterHasNoKey() {
    EasyGrid<Person> grid = new EasyGrid<>(Person.class, false);
    EasyColumn<Person, String> col =
        grid.addColumn(String.class, p -> p.getFirstName() + " " + p.getLastName());
    assertNull(col.getColumn().getKey());
    assertThat(grid.getWrappedGrid().getColumns(), hasSize(1));
  }

  @Test
  public void setColumnOrderShowsOnlyListedColumns() {
    EasyGrid<Person> grid = new EasyGrid<>(Person.class, "firstName", "lastName", "address");
    grid.setColumnOrder("lastName", "firstName");
    assertTrue(grid.getWrappedGrid().getColumnByKey("lastName").isVisible());
    assertTrue(grid.getWrappedGrid().getColumnByKey("firstName").isVisible());
    assertFalse(grid.getWrappedGrid().getColumnByKey("address").isVisible());
  }

  @Test
  public void hideColumnsHidesSpecifiedColumns() {
    EasyGrid<Person> grid = new EasyGrid<>(Person.class, "firstName", "lastName", "address");
    grid.hideColumns("address", "firstName");
    assertFalse(grid.getWrappedGrid().getColumnByKey("address").isVisible());
    assertFalse(grid.getWrappedGrid().getColumnByKey("firstName").isVisible());
    assertTrue(grid.getWrappedGrid().getColumnByKey("lastName").isVisible());
  }

}
