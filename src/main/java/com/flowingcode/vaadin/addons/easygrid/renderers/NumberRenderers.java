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

import java.text.NumberFormat;
import java.util.Locale;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.function.SerializableSupplier;
import lombok.experimental.UtilityClass;

/**
 * Factory methods for creating {@link RendererFactory} instances that render {@link Number} values
 * using {@link com.vaadin.flow.data.renderer.NumberRenderer}.
 */
@UtilityClass
public class NumberRenderers {

  /**
   * Returns a factory that formats numbers using the given {@code NumberFormat}.
   *
   * @param numberFormat the format to use
   * @return a {@code RendererFactory} for the given format
   */
  public <T> RendererFactory<T, Number> of(NumberFormat numberFormat) {
    return getter -> new NumberRenderer<>(getter, numberFormat);
  }

  /**
   * Returns a factory that formats numbers using the given {@code NumberFormat} and null
   * representation.
   *
   * @param numberFormat the format to use
   * @param nullRepresentation the string to display for {@code null} values
   * @return a {@code RendererFactory} for the given format and null representation
   */
  public <T> RendererFactory<T, Number> of(NumberFormat numberFormat, String nullRepresentation) {
    return getter -> new NumberRenderer<>(getter, numberFormat, nullRepresentation);
  }

  /**
   * Returns a factory that formats numbers using the default format for the given locale.
   *
   * @param locale the locale to use
   * @return a {@code RendererFactory} using the default format for the given locale
   */
  public <T> RendererFactory<T, Number> of(Locale locale) {
    return getter -> new NumberRenderer<>(getter, locale);
  }

  /**
   * Returns a factory that formats numbers using the default format for the locale supplied at
   * column-creation time.
   *
   * @param locale a supplier of the locale to use
   * @return a {@code RendererFactory} using the default format for the supplied locale
   */
  public <T> RendererFactory<T, Number> of(SerializableSupplier<Locale> locale) {
    return getter -> new NumberRenderer<>(getter, locale.get());
  }

  /**
   * Returns a factory that formats numbers using the default format for the locale supplied at
   * column-creation time, with the given null representation.
   *
   * @param locale a supplier of the locale to use
   * @param nullRepresentation the string to display for {@code null} values
   * @return a {@code RendererFactory} using the default format for the supplied locale and the
   *         given null representation
   */
  public <T> RendererFactory<T, Number> of(SerializableSupplier<Locale> locale,
      String nullRepresentation) {
    return getter -> new NumberRenderer<>(getter, NumberFormat.getInstance(locale.get()),
        nullRepresentation);
  }

  /**
   * Returns a factory that formats numbers using the given {@link java.util.Formatter} format
   * string and the default locale.
   *
   * @param formatString a {@code java.util.Formatter} format string
   * @return a {@code RendererFactory} for the given format string
   */
  public <T> RendererFactory<T, Number> of(String formatString) {
    return getter -> new NumberRenderer<>(getter, formatString);
  }

  /**
   * Returns a factory that formats numbers using the given {@link java.util.Formatter} format
   * string and locale.
   *
   * @param formatString a {@code Formatter} format string
   * @param locale the locale to use
   * @return a {@code RendererFactory} for the given format string and locale
   */
  public <T> RendererFactory<T, Number> of(String formatString, Locale locale) {
    return getter -> new NumberRenderer<>(getter, formatString, locale);
  }

  /**
   * Returns a factory that formats numbers using the given {@link java.util.Formatter} format
   * string, locale, and null representation.
   *
   * @param formatString a {@code Formatter} format string
   * @param locale the locale to use
   * @param nullRepresentation the string to display for {@code null} values
   * @return a {@code RendererFactory} for the given format string, locale, and null representation
   */
  public <T> RendererFactory<T, Number> of(String formatString, Locale locale,
      String nullRepresentation) {
    return getter -> new NumberRenderer<>(getter, formatString, locale, nullRepresentation);
  }

}
