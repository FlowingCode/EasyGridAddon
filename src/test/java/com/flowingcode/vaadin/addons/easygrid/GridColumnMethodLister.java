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

import com.vaadin.flow.component.grid.Grid;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Prints all public methods declared on {@link Grid.Column}, sorted by name.
 *
 * <p>Each line has the form:
 * <pre>
 *   returnType methodName(paramType1, paramType2, ...)
 * </pre>
 *
 * <p>Run via {@code mvn exec:java -Dexec.mainClass=...GridColumnMethodLister} or directly from an
 * IDE.
 */
public class GridColumnMethodLister {

  public static void main(String[] args) {
    Set<Method> declaredPublic = Arrays.stream(Grid.Column.class.getDeclaredMethods())
        .filter(m -> Modifier.isPublic(m.getModifiers()))
        .collect(Collectors.toSet());

    declaredPublic.stream()
        .sorted(Comparator.comparing(Method::getName)
            .thenComparingInt(Method::getParameterCount))
        .forEach(GridColumnMethodLister::print);
  }

  private static void print(Method m) {
    String params = Arrays.stream(m.getParameterTypes())
        .map(Class::getSimpleName)
        .collect(Collectors.joining(", "));
    System.out.printf("%s :: %s(%s)%n", m.getDeclaringClass().getSimpleName(), m.getName(), params);
  }

}
