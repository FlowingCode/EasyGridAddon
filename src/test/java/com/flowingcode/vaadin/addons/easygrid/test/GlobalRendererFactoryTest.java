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
import static org.hamcrest.Matchers.instanceOf;
import com.flowingcode.vaadin.addons.easygrid.config.InstanceEasyGridConfiguration;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.Test;

public class GlobalRendererFactoryTest {

  @Test
  public void globalLocalDateRendererFactoryIsApplied() {
    // LocalDateRenderer factory is applied from global default
    var config = new InstanceEasyGridConfiguration().resolve(LocalDate.class);
    var renderer = config.<Object>getRendererFactory().apply(v -> (LocalDate) v);
    assertThat(renderer, instanceOf(LocalDateRenderer.class));
  }

  @Test
  public void globalLocalDateTimeRendererFactoryIsApplied() {
    // LocalDateTimeRenderer factory is applied from global default
    var config = new InstanceEasyGridConfiguration().resolve(LocalDateTime.class);
    var renderer = config.<Object>getRendererFactory().apply(v -> (LocalDateTime) v);
    assertThat(renderer, instanceOf(LocalDateTimeRenderer.class));
  }

  @Test
  public void globalLocalTimeRendererFactoryIsApplied() {
    // TextRenderer factory is applied from global default for LocalTime
    var config = new InstanceEasyGridConfiguration().resolve(LocalTime.class);
    var renderer = config.<Object>getRendererFactory().apply(v -> (LocalTime) v);
    assertThat(renderer, instanceOf(TextRenderer.class));
  }

}
