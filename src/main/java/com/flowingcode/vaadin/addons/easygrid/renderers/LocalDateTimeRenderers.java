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
package com.flowingcode.vaadin.addons.easygrid.renderers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.function.SerializableSupplier;
import lombok.experimental.UtilityClass;

/**
 * Factory methods for creating {@link RendererFactory} instances that render {@link LocalDateTime}
 * values using {@link com.vaadin.flow.data.renderer.LocalDateTimeRenderer}.
 */
@UtilityClass
public class LocalDateTimeRenderers {

  /**
   * Returns a factory that uses the default locale and format.
   *
   * @return a {@code RendererFactory} using the default locale and format
   */
  public <T> RendererFactory<T, LocalDateTime> of() {
    return getter -> new LocalDateTimeRenderer<>(getter);
  }

  /**
   * Returns a factory that formats date-times with the given pattern using the default locale.
   *
   * @param formatPattern a {@link DateTimeFormatter} pattern
   * @return a {@code RendererFactory} for the given pattern
   */
  public <T> RendererFactory<T, LocalDateTime> of(String formatPattern) {
    return getter -> new LocalDateTimeRenderer<>(getter, formatPattern);
  }

  /**
   * Returns a factory that formats date-times with the given pattern and locale.
   *
   * @param formatPattern a {@link DateTimeFormatter} pattern
   * @param locale the locale to use
   * @return a {@code RendererFactory} for the given pattern and locale
   */
  public <T> RendererFactory<T, LocalDateTime> of(String formatPattern, Locale locale) {
    return getter -> new LocalDateTimeRenderer<>(getter, formatPattern, locale);
  }

  /**
   * Returns a factory that formats date-times with the given pattern, locale, and null
   * representation.
   *
   * @param formatPattern a {@link DateTimeFormatter} pattern
   * @param locale the locale to use
   * @param nullRepresentation the string to display for {@code null} values
   * @return a {@code RendererFactory} for the given pattern, locale, and null representation
   */
  public <T> RendererFactory<T, LocalDateTime> of(String formatPattern, Locale locale,
      String nullRepresentation) {
    return getter -> new LocalDateTimeRenderer<>(getter, formatPattern, locale, nullRepresentation);
  }

  /**
   * Returns a factory that formats date-times using the given {@link DateTimeFormatter} supplier.
   *
   * @param formatter a supplier of the formatter to use
   * @return a {@code RendererFactory} using the given formatter supplier
   */
  public <T> RendererFactory<T, LocalDateTime> of(
      SerializableSupplier<DateTimeFormatter> formatter) {
    return getter -> new LocalDateTimeRenderer<>(getter, formatter);
  }

  /**
   * Returns a factory that formats date-times using the given {@link DateTimeFormatter} supplier
   * and null representation.
   *
   * @param formatter a supplier of the formatter to use
   * @param nullRepresentation the string to display for {@code null} values
   * @return a {@code RendererFactory} using the given formatter supplier and null representation
   */
  public <T> RendererFactory<T, LocalDateTime> of(
      SerializableSupplier<DateTimeFormatter> formatter, String nullRepresentation) {
    return getter -> new LocalDateTimeRenderer<>(getter, formatter, nullRepresentation);
  }

}
