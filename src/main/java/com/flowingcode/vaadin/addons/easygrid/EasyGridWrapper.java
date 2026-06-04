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

import com.flowingcode.vaadin.addons.easygrid.actions.HasRowActions;
import com.flowingcode.vaadin.addons.easygrid.actions.RowActionsManager;
import com.flowingcode.vaadin.addons.easygrid.config.ColumnConfiguration;
import com.flowingcode.vaadin.addons.easygrid.config.InstanceEasyGridConfiguration;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.binder.BeanPropertySet;
import com.vaadin.flow.data.binder.PropertyDefinition;
import com.vaadin.flow.data.binder.PropertySet;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.util.SharedUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NonNull;

/**
 * Wraps a {@link Grid} to provide bean property-based column creation and type-aware
 * {@link ColumnConfiguration} resolution. Columns can be created automatically from all top-level
 * bean properties, from an explicit list of property names, or individually via
 * {@link #addColumn(String)} and {@link #addColumn(Class, ValueProvider)}.
 *
 * <p>{@link EasyGrid} is the standard concrete subclass for use with a plain {@code Grid}.
 *
 * @param <T> the grid bean type
 * @param <GRID> the concrete {@code Grid} subtype being wrapped
 */
@SuppressWarnings("serial")
public class EasyGridWrapper<T, GRID extends Grid<T>> extends EasyGridComposite<T, GRID>
    implements HasRowActions<T> {

  @Getter
  private final Class<T> beanType;
  private transient PropertySet<T> propertySet;

  private final InstanceEasyGridConfiguration configuration = new InstanceEasyGridConfiguration();

  /**
   * Creates an {@code EasyGridWrapper} that discovers all top-level bean properties and adds a
   * column for each.
   *
   * @param grid the grid to configure, not {@code null}
   * @param beanType the bean type to use, not {@code null}
   * @throws NullPointerException if either {@code grid} or {@code beanType} is {@code null}
   */
  public EasyGridWrapper(@NonNull GRID grid, @NonNull Class<T> beanType) {
    this(grid, beanType, true);
  }

  /**
   * Creates an {@code EasyGridWrapper} for the given bean type, optionally auto-creating columns.
   *
   * @param grid the grid to configure, not {@code null}
   * @param beanType the bean type to use, not {@code null}
   * @param autoCreateColumns when {@code true}, columns are created automatically for the
   *        properties of the bean type
   * @throws NullPointerException if either {@code grid} or {@code beanType} is {@code null}
   */
  public EasyGridWrapper(@NonNull GRID grid, @NonNull Class<T> beanType,
      boolean autoCreateColumns) {
    super(grid);
    this.beanType = beanType;
    if (autoCreateColumns) {
      getPropertySet().getProperties().filter(p -> !p.isSubProperty())
          .map(PropertyDefinition::getName).forEach(this::addColumn);
    }
  }

  /**
   * Creates an {@code EasyGridWrapper} that adds columns for the specified properties in order.
   *
   * @param grid the grid to configure, not {@code null}
   * @param beanType the bean type to use, not {@code null}
   * @param propertyNames the names of the properties for which columns are created
   * @throws NullPointerException if either {@code grid} or {@code beanType} is {@code null}, or if
   *         {@code propertyNames} is {@code null}
   */
  public EasyGridWrapper(@NonNull GRID grid, @NonNull Class<T> beanType, String... propertyNames) {
    super(grid);
    this.beanType = beanType;
    addColumns(propertyNames);
  }

  private PropertySet<T> getPropertySet() {
    if (propertySet == null) {
      propertySet = BeanPropertySet.get(beanType);
    }
    return propertySet;
  }

  /**
   * Returns the {@code EasyColumn} for the given property name, or {@code null} if no column with
   * that key exists or the column was not created through the EasyGrid API.
   *
   * @param propertyName the property name used as the column key
   * @return the {@code EasyColumn} for the given key, or {@code null}
   */
  public EasyColumn<T, ?> getColumn(String propertyName) {
    Grid.Column<T> column = getWrappedGrid().getColumnByKey(propertyName);
    return column != null ? EasyColumn.getInstance(column) : null;
  }

  /**
   * Adds a column for the given bean property. The column header is derived from the property name,
   * and the column is made sortable if the property type implements {@link Comparable} or is a
   * primitive type.
   *
   * @param propertyName the name of the bean property
   * @return the {@code EasyColumn} for the added column
   * @throws IllegalArgumentException if the property cannot be resolved or a column for it already
   *         exists
   */
  public EasyColumn<T, ?> addColumn(String propertyName) {
    return createEasyColumn(resolveProperty(propertyName));
  }

  /**
   * Sets the order of the columns shown in the grid. Only the listed columns are visible after this
   * call; all other columns are hidden. The columns are shown in the order they are listed.
   *
   * @param propertyNames the property names of the columns to show, in the desired order
   * @throws NullPointerException if {@code propertyNames} is {@code null}
   */
  public void setColumnOrder(@NonNull String... propertyNames) {
    Set<String> visible = new LinkedHashSet<>(Arrays.asList(propertyNames));
    List<Grid.Column<T>> ordered = new ArrayList<>();
    for (String name : visible) {
      Grid.Column<T> column = getWrappedGrid().getColumnByKey(name);
      if (column != null) {
        column.setVisible(true);
        ordered.add(column);
      }
    }
    getWrappedGrid().getColumns().stream()
        .filter(c -> !visible.contains(c.getKey()))
        .peek(c -> c.setVisible(false))
        .forEach(ordered::add);
    getWrappedGrid().setColumnOrder(ordered);
  }

  /**
   * Hides the columns with the given property names.
   *
   * @param propertyNames the property names of the columns to hide
   * @throws NullPointerException if {@code propertyNames} is {@code null}
   */
  public void hideColumns(@NonNull String... propertyNames) {
    Stream.of(propertyNames).map(getWrappedGrid()::getColumnByKey).filter(Objects::nonNull)
        .forEach(c -> c.setVisible(false));
  }

  /**
   * Adds columns for the given bean properties in order.
   *
   * @param propertyNames the names of the bean properties
   * @throws NullPointerException if {@code propertyNames} is {@code null}
   * @throws IllegalArgumentException if any property cannot be resolved or already has a column
   */
  public void addColumns(@NonNull String... propertyNames) {
    Stream.of(propertyNames).forEach(this::addColumn);
  }

  /**
   * Adds a column for the given type and value provider. The column is made sortable if the value
   * type implements {@link Comparable} or is a primitive type. No column key or header is set
   * automatically; use the returned {@code EasyColumn} to configure them.
   *
   * @param <V> the column value type
   * @param type the {@code Class} of the column value, used to resolve the {@link ColumnConfiguration}
   * @param getter a value provider that extracts the column value from a bean instance; may return
   *        any subtype of {@code V}
   * @return the {@code EasyColumn} for the added column
   */
  public <V> EasyColumn<T, V> addColumn(Class<V> type, ValueProvider<T, ? extends V> getter) {
    @SuppressWarnings("unchecked")
    ValueProvider<T, V> safeGetter = (ValueProvider<T, V>) getter;
    return createEasyColumn(type, safeGetter);
  }

  @SuppressWarnings("unchecked")
  private BeanPropertyDefinition<T, Object> resolveProperty(String propertyName) {
    return (BeanPropertyDefinition<T, Object>) BeanPropertyDefinition.of(getPropertySet(), beanType, propertyName);
  }

  /**
   * Creates and adds a new column to the wrapped grid for the given bean property, applying the
   * {@link ColumnConfiguration} resolved for the property's value type.
   *
   * <p>
   * If the property has a non-null name, it is used as the column key and as the source for a
   * human-friendly header caption; an exception is thrown if the wrapped grid already has a column
   * with that key. The column is made sortable when the value type implements {@link Comparable} or
   * is a non-{@code void} primitive.
   *
   * <p>
   * Subclasses may override this method to customize how columns are created from bean properties
   * (for example, to apply additional renderers or post-process the resulting {@code EasyColumn}).
   *
   * @param <V> the column value type
   * @param pd the bean property definition describing the column source
   * @return the {@code EasyColumn} for the added column
   * @throws IllegalArgumentException if the wrapped grid already has a column whose key matches the
   *         property name
   */
  protected <V> EasyColumn<T, V> createEasyColumn(BeanPropertyDefinition<T, V> pd) {
    String propertyName = pd.getName();
    if (propertyName != null && getWrappedGrid().getColumnByKey(propertyName) != null) {
      throw new IllegalArgumentException("Multiple columns for the same property: " + propertyName);
    }

    var column = createEasyColumn(pd.getType(), pd.getGetter());

    if (propertyName != null) {
      String caption = SharedUtil.propertyIdToHumanFriendly(propertyName);
      column.setHeader(caption);
      column.setKey(propertyName);
      if (column.getColumn().isSortable()) {
        column.setSortProperty(propertyName);
      }
    }

    return column;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private <V> EasyColumn<T, V> createEasyColumn(Class<V> type, ValueProvider<T, V> getter) {
    ColumnConfiguration<V> config = configuration.resolve(type);
    Grid.Column<T> column = getWrappedGrid().addColumn(EasyColumn.createRenderer(config, getter));
    if (Comparable.class.isAssignableFrom(type) || type.isPrimitive()) {
      column.setComparator((ValueProvider) getter);
    }
    return new EasyColumn<>(config, column, getter, type);
  }


  /**
   * Returns the {@code ColumnConfiguration} for the given value type at the instance level,
   * creating it if it does not yet exist. Modifications to the returned configuration apply to all
   * columns of that type managed by this instance and take precedence over
   * {@link com.flowingcode.vaadin.addons.easygrid.config.GlobalEasyGridConfiguration}.
   *
   * @param <V> the column value type
   * @param type the {@code Class} of the column value type
   * @return the {@code ColumnConfiguration} for the given type
   */
  public <V> ColumnConfiguration<V> typeConfiguration(Class<V> type) {
    return configuration.forType(type);
  }

  @Getter(lazy = true)
  private final RowActionsManager<T> rowActionsManager = new RowActionsManager<>(getWrappedGrid());

}
