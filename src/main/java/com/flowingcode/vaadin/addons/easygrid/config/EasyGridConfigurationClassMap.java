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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A map from value type to {@link ColumnConfiguration}, building a parent chain by following the
 * class hierarchy so that a configuration for a subtype inherits from its supertype's configuration.
 * Primitive types are mapped to their wrapper counterparts before hierarchy traversal.
 */
@SuppressWarnings("serial")
final class EasyGridConfigurationClassMap implements Serializable {

  private final Map<Class<?>, ColumnConfiguration<?>> map = new HashMap<>();

  @SuppressWarnings("unchecked")
  public synchronized <V> ColumnConfiguration<V> getOrCreate(Class<V> type) {
    return Optional.ofNullable((ColumnConfiguration<V>) map.get(type)).orElseGet(()->{
      var supertype = getSuperclass(type);
      ColumnConfiguration<V> parent =
          supertype != null ? (ColumnConfiguration<V>) getOrCreate(supertype) : null;
      ColumnConfiguration<V> config = new ColumnConfigurationImpl<>(parent);
      map.put(type, config);
      return config;
    });
  }

  private Class<?> getSuperclass(Class<?> type) {
    if (type.isPrimitive()) {
      if (type == boolean.class) {
        return Boolean.class;
      }
      if (type == byte.class) {
        return Byte.class;
      }
      if (type == short.class) {
        return Short.class;
      }
      if (type == int.class) {
        return Integer.class;
      }
      if (type == long.class) {
        return Long.class;
      }
      if (type == float.class) {
        return Float.class;
      }
      if (type == double.class) {
        return Double.class;
      }
      if (type == char.class) {
        return Character.class;
      }
      throw new IllegalArgumentException(type.getName());
    }
    return type.getSuperclass();
  }

  @SuppressWarnings("unchecked")
  public synchronized <V> ColumnConfiguration<V> get(Class<V> type) {
    for (Class<?> c = type; c != null; c = getSuperclass(c)) {
      ColumnConfiguration<?> config = map.get(c);
      if (config != null) {
        return (ColumnConfiguration<V>) config;
      }
    }
    return null;
  }

}
