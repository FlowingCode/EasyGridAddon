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
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridSelectionModel;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.grid.dataview.GridDataView;
import com.vaadin.flow.component.grid.dataview.GridLazyDataView;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.InMemoryDataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.Registration;
import java.util.Set;

/**
 * Defines the subset of {@link com.vaadin.flow.component.grid.Grid} methods that
 * {@link EasyGridComposite} re-exposes by delegating to the wrapped grid instance. Used as the
 * type argument for Lombok {@code @Delegate} on the {@link EasyGridComposite.IEasyGridDelegate} inner class.
 *
 * @param <T> the grid bean type
 */
public sealed interface IEasyGridComposite<T> permits EasyGridComposite.IEasyGridDelegate {

  /**
   * Adds the given data generator. If the generator was already added, does nothing.
   *
   * @param generator the data generator to add
   * @return a registration that can be used to remove the data generator
   */
  Registration addDataGenerator(DataGenerator<T> generator);

  /**
   * Adds an item click listener to this component.
   *
   * @param listener the listener to add, not {@code null}
   * @return a handle that can be used for removing the listener
   */
  Registration addItemClickListener(ComponentEventListener<ItemClickEvent<T>> listener);

  /**
   * Adds a selection listener to the current selection model.
   * <p>
   * This is a shorthand for {@code grid.getSelectionModel().addSelectionListener()}. To get more
   * detailed selection events, use {@link Grid#getSelectionModel()} and either
   * {@link com.vaadin.flow.component.grid.GridSingleSelectionModel#addSingleSelectionListener(com.vaadin.flow.data.selection.SingleSelectionListener)}
   * or
   * {@link com.vaadin.flow.component.grid.GridMultiSelectionModel#addMultiSelectionListener(com.vaadin.flow.data.selection.MultiSelectionListener)}
   * depending on the used selection mode.
   *
   * @param listener the listener to add
   * @return a registration handle to remove the listener
   * @throws UnsupportedOperationException if selection has been disabled with
   *         {@link SelectionMode#NONE}
   */
  Registration addSelectionListener(SelectionListener<Grid<T>, T> listener);

  /**
   * Adds a new column that shows components.
   * <p>
   * This is a shorthand for {@link Grid#addColumn(com.vaadin.flow.data.renderer.Renderer)} with a
   * {@link com.vaadin.flow.data.renderer.ComponentRenderer}.
   * <p>
   * <em>NOTE:</em> Using {@code ComponentRenderer} is not as efficient as the built in renderers or
   * using {@link com.vaadin.flow.data.renderer.LitRenderer}.
   * <p>
   * Every added column sends data to the client side regardless of its visibility state. Don't add
   * a new column at all or use {@link Grid#removeColumn(Grid.Column)} to avoid sending extra data.
   *
   * @param componentProvider a value provider that will return a component for the given item
   * @param <V> the component type
   * @return the new column
   */
  <V extends Component> Grid.Column<T> addComponentColumn(ValueProvider<T, V> componentProvider);

  /**
   * Gets the generic data view for the grid. This data view should only be used when
   * {@link #getListDataView()} or {@link #getLazyDataView()} is not applicable for the underlying
   * data provider.
   *
   * @return the generic {@link com.vaadin.flow.data.provider.DataView} implementation for grid
   * @see #getListDataView()
   * @see #getLazyDataView()
   */
  GridDataView<T> getGenericDataView();

  /**
   * Gets the lazy data view for the grid. This data view should only be used when the items are
   * provided lazily from the backend with:
   * <ul>
   * <li>{@link Grid#setItems(com.vaadin.flow.data.provider.CallbackDataProvider.FetchCallback)}</li>
   * <li>{@link Grid#setItems(com.vaadin.flow.data.provider.CallbackDataProvider.FetchCallback, com.vaadin.flow.data.provider.CallbackDataProvider.CountCallback)}</li>
   * <li>{@link #setItems(BackEndDataProvider)}</li>
   * </ul>
   * If the items are not fetched lazily an exception is thrown. When the items are in-memory, use
   * {@link #getListDataView()} instead.
   *
   * @return the lazy data view that provides access to the data bound to the grid
   */
  GridLazyDataView<T> getLazyDataView();

  /**
   * Gets the list data view for the grid. This data view should only be used when the items are
   * in-memory set with:
   * <ul>
   * <li>{@link Grid#setItems(java.util.Collection)}</li>
   * <li>{@link Grid#setItems(Object[])}</li>
   * <li>{@link #setItems(ListDataProvider)}</li>
   * </ul>
   * If the items are not in-memory an exception is thrown. When the items are fetched lazily, use
   * {@link #getLazyDataView()} instead.
   *
   * @return the list data view that provides access to the items in the grid
   */
  GridListDataView<T> getListDataView();

  /**
   * This method is a shorthand that delegates to the currently set selection model.
   *
   * @see GridSelectionModel
   *
   * @return a set with the selected items, never {@code null}
   */
  Set<T> getSelectedItems();

  /**
   * Supply items with a {@code BackEndDataProvider} that lazy loads items from a backend. Note that
   * component will query the data provider for the item count. In case that is not desired for
   * performance reasons, use
   * {@link Grid#setItems(com.vaadin.flow.data.provider.CallbackDataProvider.FetchCallback)}
   * instead.
   * <p>
   * The returned data view object can be used for further configuration, or later on fetched with
   * {@link #getLazyDataView()}. For using in-memory data, like {@link java.util.Collection}, use
   * {@link com.vaadin.flow.data.provider.HasListDataView#setItems(java.util.Collection)} instead.
   *
   * @param dataProvider {@code BackEndDataProvider} instance
   * @return {@link com.vaadin.flow.data.provider.LazyDataView} instance for further configuration
   */
  GridLazyDataView<T> setItems(BackEndDataProvider<T, Void> dataProvider);

  /**
   * Set a generic data provider for the component to use and returns the base {@link com.vaadin.flow.data.provider.DataView}
   * that provides API to get information on the items.
   * <p>
   * This method should be used only when the data provider type is not either
   * {@link ListDataProvider} or {@link BackEndDataProvider}.
   *
   * @param dataProvider {@code DataProvider} instance to use, not {@code null}
   * @return {@code DataView} providing information on the data
   */
  GridDataView<T> setItems(DataProvider<T, Void> dataProvider);

  /**
   * Sets a {@code ListDataProvider} for the component to use and returns a {@link com.vaadin.flow.data.provider.ListDataView}
   * that provides information and allows operations on the items.
   *
   * @param dataProvider {@code ListDataProvider} providing items to the component
   * @return {@code ListDataView} providing access to the items
   */
  GridListDataView<T> setItems(ListDataProvider<T> dataProvider);

  /**
   * Sets an in-memory data provider for the component to use.
   * <p>
   * Note! Using a {@link ListDataProvider} instead of an {@code InMemoryDataProvider} is
   * recommended to get access to {@link com.vaadin.flow.data.provider.ListDataView} API by using
   * {@link #setItems(ListDataProvider)}.
   *
   * @param dataProvider {@code InMemoryDataProvider} to use, not {@code null}
   * @return {@link com.vaadin.flow.data.provider.DataView} providing information on the data
   */
  GridDataView<T> setItems(InMemoryDataProvider<T> dataProvider);

  /**
   * Sets the grid's selection mode.
   * <p>
   * To use your custom selection model, you can use
   * {@link Grid#setSelectionModel(GridSelectionModel, SelectionMode)}, see existing selection model
   * implementations for example.
   *
   * @param selectionMode the selection mode to switch to, not {@code null}
   * @return the used selection model
   *
   * @see SelectionMode
   * @see GridSelectionModel
   */
  GridSelectionModel<T> setSelectionMode(SelectionMode selectionMode);

  /**
   * Sets the defined columns as sortable, based on the given property names.
   * <p>
   * This is a shortcut for setting all columns not sortable and then calling
   * {@link Grid.Column#setSortable(boolean)} for each of the columns defined by the given
   * propertyNames.
   * <p>
   * You can set sortable columns for nested properties with dot notation, eg.
   * {@code "property.nestedProperty"}
   * <p>
   * <strong>Note:</strong> This method can only be used for a {@link Grid} created from a bean type
   * with {@link Grid#Grid(Class)}.
   *
   * @param propertyNames the property names used to reference the columns
   *
   * @throws IllegalArgumentException if any of the propertyNames refers to a non-existing column
   */
  void setSortableColumns(String... propertyNames);

}
