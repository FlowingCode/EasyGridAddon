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

/*
 * Portions of this interface are derived from the Vaadin Flow Grid API,
 * Copyright 2000-2025 Vaadin Ltd., licensed under the Apache License, Version 2.0.
 * See https://github.com/vaadin/flow-components for the original source.
 */

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.SortOrderProvider;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.ValueProvider;
import java.util.Comparator;

/**
 * Defines the chainable column API exposed by {@link EasyColumn}, providing fluent delegating
 * setters for the underlying {@link Grid.Column}.
 *
 * @param <T> the grid bean type
 * @param <V> the column value type
 */
sealed interface IEasyGridColumn<T, V> permits EasyColumn {

  /**
   * Returns the underlying {@code Grid.Column}.
   *
   * @return the underlying {@code Grid.Column}
   */
  Grid.Column<T> getColumn();

  /**
   * Sets the text alignment for this column's cells.
   *
   * @param textAlign the text alignment to apply
   * @return this column, for fluent chaining
   */
  default EasyColumn<T, V> setTextAlign(ColumnTextAlign textAlign) {
    getColumn().setTextAlign(textAlign);
    return (EasyColumn<T, V>) this;
  }

  /**
   * Sets whether this column's width is automatically determined from its content.
   *
   * @param autoWidth {@code true} to enable auto-width, {@code false} to use a fixed width
   * @return this column, for fluent chaining
   */
  default EasyColumn<T, V> setAutoWidth(boolean autoWidth) {
    getColumn().setAutoWidth(autoWidth);
    return (EasyColumn<T, V>) this;
  }

  /**
   * Sets a comparator to use for in-memory sorting for this column.
   *
   * @param comparator the comparator to use
   * @return this column, for fluent chaining
   */
  default EasyColumn<T, V> setComparator(Comparator<T> comparator) {
    getColumn().setComparator(comparator);
    return (EasyColumn<T, V>) this;
  }

  /**
   * Sets a comparator to use for in-memory sorting for this column based on a value provider.
   *
   * @param <C> the comparable value type
   * @param valueProvider a value provider that extracts the comparable sort key from a row item
   * @return this column, for fluent chaining
   */
  default <C extends Comparable<? super C>> EasyColumn<T, V> setComparator(
      ValueProvider<T, C> valueProvider) {
    getColumn().setComparator(valueProvider);
    return (EasyColumn<T, V>) this;
  }

  /**
   * Sets the editor component for this column.
   *
   * @param component the editor component
   * @return this column, for fluent chaining
   */
  default EasyColumn<T, V> setEditorComponent(Component component) {
    getColumn().setEditorComponent(component);
    return (EasyColumn<T, V>) this;
  }

  /**
   * Sets a generator that provides the editor component for each row in this column.
   *
   * @param componentFunction a function that returns the editor component for a given row item
   * @return this column, for fluent chaining
   */
  default EasyColumn<T, V> setEditorComponent(
      SerializableFunction<T, ? extends Component> componentFunction) {
    getColumn().setEditorComponent(componentFunction);
    return (EasyColumn<T, V>) this;
  }

  /**
   * Sets the flex grow ratio for this column.
   *
   * @param flexGrow the flex grow ratio
   * @return this column, for fluent chaining
   */
  default EasyColumn<T, V> setFlexGrow(int flexGrow) {
    getColumn().setFlexGrow(flexGrow);
    return (EasyColumn<T, V>) this;
  }

  /**
   * Sets a text footer for this column.
   *
   * @param footerText the footer text
   * @return this column, for fluent chaining
   */
  default EasyColumn<T, V> setFooter(String footerText) {
    getColumn().setFooter(footerText);
    return (EasyColumn<T, V>) this;
  }

  /**
   * Sets a component footer for this column.
   *
   * @param footerComponent the footer component
   * @return this column, for fluent chaining
   */
  default EasyColumn<T, V> setFooter(Component footerComponent) {
    getColumn().setFooter(footerComponent);
    return (EasyColumn<T, V>) this;
  }

  /**
   * Sets the part name for the footer cell of this column.
   *
   * @param footerPartName the part name for the footer cell
   * @return this column, for fluent chaining
   */
  default EasyColumn<T, V> setFooterPartName(String footerPartName) {
    getColumn().setFooterPartName(footerPartName);
    return (EasyColumn<T, V>) this;
  }

  /**
   * Sets whether this column is frozen, locking it to the start of the grid.
   *
   * @param frozen {@code true} to freeze this column, {@code false} to unfreeze it
   * @return this column, for fluent chaining
   */
  default EasyColumn<T, V> setFrozen(boolean frozen) {
    getColumn().setFrozen(frozen);
    return (EasyColumn<T, V>) this;
  }

  /**
   * Sets whether this column is frozen to the end of the grid.
   *
   * @param frozenToEnd {@code true} to freeze this column to the end, {@code false} to unfreeze it
   * @return this column, for fluent chaining
   */
  default EasyColumn<T, V> setFrozenToEnd(boolean frozenToEnd) {
    getColumn().setFrozenToEnd(frozenToEnd);
    return (EasyColumn<T, V>) this;
  }

  /**
   * Sets a text header for this column.
   *
   * @param headerText the header text
   * @return this column, for fluent chaining
   */
  default EasyColumn<T, V> setHeader(String headerText) {
    getColumn().setHeader(headerText);
    return (EasyColumn<T, V>) this;
  }

  /**
   * Sets a component header for this column.
   *
   * @param headerComponent the header component
   * @return this column, for fluent chaining
   */
  default EasyColumn<T, V> setHeader(Component headerComponent) {
    getColumn().setHeader(headerComponent);
    return (EasyColumn<T, V>) this;
  }

  /**
   * Sets the part name for the header cell of this column.
   *
   * @param headerPartName the part name for the header cell
   * @return this column, for fluent chaining
   */
  default EasyColumn<T, V> setHeaderPartName(String headerPartName) {
    getColumn().setHeaderPartName(headerPartName);
    return (EasyColumn<T, V>) this;
  }

  /**
   * Sets the key for this column, used to retrieve it via {@link Grid#getColumnByKey(String)}.
   *
   * @param key the column key
   * @return this column, for fluent chaining
   */
  default EasyColumn<T, V> setKey(String key) {
    getColumn().setKey(key);
    return (EasyColumn<T, V>) this;
  }

  /**
   * Sets a generator that returns a part name for a given row item in this column.
   *
   * @param partNameGenerator a function that returns the part name for a given row item
   * @return this column, for fluent chaining
   */
  default EasyColumn<T, V> setPartNameGenerator(SerializableFunction<T, String> partNameGenerator) {
    getColumn().setPartNameGenerator(partNameGenerator);
    return (EasyColumn<T, V>) this;
  }

  /**
   * Sets whether this column is user-resizable.
   *
   * @param resizable {@code true} to make this column resizable, {@code false} to prevent resizing
   * @return this column, for fluent chaining
   */
  default EasyColumn<T, V> setResizable(boolean resizable) {
    getColumn().setResizable(resizable);
    return (EasyColumn<T, V>) this;
  }

  /**
   * Sets whether this column represents a row header for accessibility purposes.
   *
   * @param rowHeader {@code true} to mark this column as a row header, {@code false} otherwise
   * @return this column, for fluent chaining
   */
  default EasyColumn<T, V> setRowHeader(boolean rowHeader) {
    getColumn().setRowHeader(rowHeader);
    return (EasyColumn<T, V>) this;
  }

  /**
   * Sets whether this column is user-sortable.
   *
   * @param sortable {@code true} to make this column sortable, {@code false} to disable sorting
   * @return this column, for fluent chaining
   */
  default EasyColumn<T, V> setSortable(boolean sortable) {
    getColumn().setSortable(sortable);
    return (EasyColumn<T, V>) this;
  }

  /**
   * Sets the sort order provider for this column.
   *
   * @param provider the sort order provider
   * @return this column, for fluent chaining
   */
  default EasyColumn<T, V> setSortOrderProvider(SortOrderProvider provider) {
    getColumn().setSortOrderProvider(provider);
    return (EasyColumn<T, V>) this;
  }

  /**
   * Sets the backend properties used for sorting when this column is sorted.
   *
   * @param properties the backend property names used for sorting
   * @return this column, for fluent chaining
   */
  default EasyColumn<T, V> setSortProperty(String... properties) {
    getColumn().setSortProperty(properties);
    return (EasyColumn<T, V>) this;
  }

  /**
   * Sets a generator that produces a tooltip for a given row item in this column.
   *
   * @param tooltipGenerator a function that returns the tooltip text for a given row item
   * @return this column, for fluent chaining
   */
  default EasyColumn<T, V> setTooltipGenerator(SerializableFunction<T, String> tooltipGenerator) {
    getColumn().setTooltipGenerator(tooltipGenerator);
    return (EasyColumn<T, V>) this;
  }

  /**
   * Sets whether this column is visible.
   *
   * @param visible {@code true} to show this column, {@code false} to hide it
   * @return this column, for fluent chaining
   */
  default EasyColumn<T, V> setVisible(boolean visible) {
    getColumn().setVisible(visible);
    return (EasyColumn<T, V>) this;
  }

  /**
   * Sets the width of this column as a CSS value.
   *
   * @param width the column width as a CSS value
   * @return this column, for fluent chaining
   */
  default EasyColumn<T, V> setWidth(String width) {
    getColumn().setWidth(width);
    return (EasyColumn<T, V>) this;
  }

}
