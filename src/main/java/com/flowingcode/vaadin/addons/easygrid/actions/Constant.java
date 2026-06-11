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
package com.flowingcode.vaadin.addons.easygrid.actions;

import com.vaadin.flow.function.ValueProvider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("serial")
final class Constant<T, V> implements ValueProvider<T, V> {

  @Getter
  private final V value;

  public static <T, V> Constant<T, V> of(V value) {
    return new Constant<>(value);
  }

  public static <T, V> Constant<T, V> ofNullable(V value) {
    return value == null ? null : of(value);
  }

  @Override
  public V apply(T source) {
    return value;
  }

  @Override
  public String toString() {
    return "CONSTANT["+value+"]";
  }
  
}
