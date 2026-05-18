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
import com.flowingcode.vaadin.addons.easygrid.model.NumberSample;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.math.BigDecimal;
import java.util.List;

@DemoSource
@DemoSource(clazz = NumberSample.class)
@PageTitle("Number Rendering")
@SuppressWarnings("serial")
@Route(value = "easy-grid/number-rendering", layout = EasyGridDemoView.class)
public class NumberRenderingDemo extends Div {

  //#if vaadin eq 0
  private final Html description = new Html("""
      <div>Demonstrates number rendering for all supported numeric types
      using locale-appropriate grouping separators.
      <ul>
      <li>Supported types: <code>BigDecimal</code>, <code>BigInteger</code>, <code>Integer</code>,
          <code>int</code>, <code>Long</code>, <code>Double</code>.</li>
      <li>Values that <code>Double.toString()</code> would display in scientific notation
          (magnitude ≥ 1E7 or absolute value &lt; 1E-3) are rendered in plain decimal notation.</li>
      </ul>
      </div>
      """);
  //#endif

  // Each row holds the same value expressed across all numeric types.
  // Rows are chosen to exercise values that Double.toString() would render in scientific notation
  // (magnitude >= 1E7 or absolute value < 1E-3), as well as small ordinary values.
  private static final List<NumberSample> ITEMS = List.of(
      new NumberSample(new BigDecimal("1234567890123.456")),
      new NumberSample(new BigDecimal("2147483647")),
      new NumberSample(new BigDecimal("-5432100000")),
      new NumberSample(new BigDecimal("0.000012345")),
      new NumberSample(new BigDecimal("42.5")),
      new NumberSample(new BigDecimal("0.42"))
  );

  public NumberRenderingDemo() {

    // All columns use the NumberRenderer wired by GlobalEasyGridConfiguration for Number.class.
    // Values that Double.toString() would show in scientific notation are rendered with
    // locale-appropriate grouping separators instead.
    var grid = new EasyGrid<>(NumberSample.class,
        "bigDecimal", "bigInteger", "integer", "intValue", "longValue", "doubleValue");

    grid.setItems(ITEMS);
    add(grid);
    add(description); //hide-source
    setSizeFull();
  }

}
