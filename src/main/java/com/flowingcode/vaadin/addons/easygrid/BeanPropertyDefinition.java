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

import com.vaadin.flow.data.binder.PropertyDefinition;
import com.vaadin.flow.data.binder.PropertySet;
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.internal.BeanUtil;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * A {@code PropertyDefinition} that additionally exposes the introspected
 * {@link PropertyDescriptor} for the underlying bean property.
 *
 * @param <T> the bean type
 * @param <V> the property value type
 */
@SuppressWarnings("serial")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanPropertyDefinition<T, V> implements PropertyDefinition<T, V> {

    private final PropertyDefinition<T, V> pd;
    private final SerializableSupplier<PropertyDescriptor> descriptor;

    /**
     * Returns a {@code BeanPropertyDefinition} for the given property, combining the
     * {@code PropertySet} entry with the {@link PropertyDescriptor} obtained via introspection.
     *
     * @param <T> the bean type
     * @param propertySet the property set to look up the property in
     * @param beanType the bean class, used for introspection
     * @param propertyName the name of the property
     * @return the resolved {@code BeanPropertyDefinition}
     * @throws IllegalArgumentException if the property cannot be resolved
     */
    public static <T> BeanPropertyDefinition<T, ?> of(PropertySet<T> propertySet,
        Class<?> beanType, String propertyName) {
      PropertyDefinition<T, ?> property;
      try {
        property = propertySet.getProperty(propertyName).get();
      } catch (NoSuchElementException | IllegalArgumentException e) {
        throw new IllegalArgumentException(
            "Can't resolve property name '" + propertyName + "' from '" + propertySet + "'", e);
      }
      return new BeanPropertyDefinition<>(property,
          () -> getPropertyDescriptor(beanType, propertyName));
    }

    private static PropertyDescriptor getPropertyDescriptor(Class<?> beanType,
        String propertyName) {
      try {
        return BeanUtil.getPropertyDescriptor(beanType, propertyName);
      } catch (IntrospectionException e) {
        throw new RuntimeReflectiveOperationException(e);
      }
    }

    /**
     * Returns the {@code PropertyDescriptor} for the underlying bean property, or {@code null} if
     * no descriptor supplier was provided.
     *
     * @return the {@code PropertyDescriptor}, or {@code null} if no descriptor supplier was provided
     */
    public PropertyDescriptor getDescriptor() {
        return Optional.ofNullable(descriptor).map(Supplier::get).orElse(null);
    }
    
    @Override
    public ValueProvider<T, V> getGetter() {
        return pd.getGetter();
    }
    
    @Override
    public Optional<Setter<T, V>> getSetter() {
        return pd.getSetter();
    }
    
    @Override
    public Class<V> getType() {
        return pd.getType();
    }
    
    @Override
    public Class<?> getPropertyHolderType() {
        return pd.getPropertyHolderType();
    }
    
    @Override
    public String getName() {
        return pd.getName();
    }
    
    @Override
    public String getTopLevelName() {
        return pd.getTopLevelName();
    }
    
    @Override
    public String getCaption() {
        return pd.getCaption();
    }
    
    @Override
    public PropertySet<T> getPropertySet() {
        return pd.getPropertySet();
    }
    
    @Override
    public PropertyDefinition<T, ?> getParent() {
        return pd.getParent();
    }
    
    @Override
    public boolean isSubProperty() {
        return pd.isSubProperty();
    }

    @Override
    public boolean isGenericType() {
        return pd.isGenericType();
    }

}
