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
package com.flowingcode.vaadin.addons.easygrid.config;

import com.flowingcode.vaadin.addons.easygrid.EasyGrid;
import com.flowingcode.vaadin.addons.easygrid.renderers.LocalDateRenderers;
import com.flowingcode.vaadin.addons.easygrid.renderers.LocalDateTimeRenderers;
import com.flowingcode.vaadin.addons.easygrid.renderers.LocalTimeRenderers;
import com.flowingcode.vaadin.addons.easygrid.renderers.NumberRenderers;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Getter;
import lombok.experimental.UtilityClass;

/**
 * System-wide {@link EasyGrid} configuration. Configurations registered here apply to all
 * {@code EasyGrid} instances across all sessions.
 *
 * <p>Use {@link #forType(Class)} to obtain a configuration for a given type:
 *
 * <pre>
 * GlobalEasyGridConfiguration.forType(LocalDate.class).setRendererFactory(...);
 * </pre>
 *
 * <p><strong>Startup and thread safety:</strong> This class is a mutable static singleton shared
 * across all user sessions. All calls to {@link #forType(Class)} must be made during application
 * startup, before any session is created, to avoid data races between concurrent sessions. Call
 * {@link #freeze()} at the end of startup to lock the configuration and prevent accidental
 * post-startup modifications. Omitting {@link #freeze()} is a stability risk: any code that runs
 * after startup — including request-handling code — could inadvertently alter rendering for every
 * active session.
 */
@UtilityClass
public class GlobalEasyGridConfiguration {

  private static final EasyGridConfigurationClassMap map = new EasyGridConfigurationClassMap();

  @Getter
  private static volatile boolean frozen;

  static {
    forType(Object.class).setNullRepresentation("");
    forType(Boolean.class).setTextAlign(ColumnTextAlign.CENTER);
    forType(Number.class).setTextAlign(ColumnTextAlign.END);
    forType(Number.class).setRendererFactory(NumberRenderers.of(() -> UI.getCurrent().getLocale()));
    forType(LocalDate.class).setRendererFactory(LocalDateRenderers.of("yyyy-MM-dd"));
    forType(LocalDateTime.class).setRendererFactory(LocalDateTimeRenderers.of("yyyy-MM-dd HH:mm:ss"));
    forType(LocalTime.class).setRendererFactory(LocalTimeRenderers.of("HH:mm:ss"));
  }

  /**
   * Freezes the global configuration, preventing further calls to {@link #forType(Class)}, which
   * will throw {@link IllegalStateException} once frozen. Freezing is irreversible. This method
   * should be called at the end of application startup, after all type configurations have been
   * registered.
   */
  public static void freeze() {
    frozen = true;
  }

  /**
   * Returns the {@code ColumnConfiguration} for the given type at the global level, creating it if
   * it does not yet exist. Modifications to the returned configuration take effect immediately.
   *
   * @param type the column value type
   * @return the {@code ColumnConfiguration} for the given type
   * @throws IllegalStateException if the global configuration has been frozen via {@link #freeze()}
   */
  public static <V> ColumnConfiguration<V> forType(Class<V> type) {
    if (frozen) {
      throw new IllegalStateException("Global configuration is frozen and no longer accepts registrations");
    }
    return map.getOrCreate(type);
  }

  /**
   * Returns the effective {@code ColumnConfiguration} for the given type. When frozen, returns
   * the nearest registered configuration walking the class hierarchy, or {@code null} if none.
   * When not frozen, creates a configuration if one does not yet exist.
   *
   * @param type the column value type
   * @return the resolved configuration, or {@code null} when frozen and no config was registered
   */
  <V> ColumnConfiguration<V> resolve(Class<V> type) {
    return frozen ? map.get(type) : forType(type);
  }

}
