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
package com.flowingcode.vaadin.addons.easygrid.it;

import com.flowingcode.vaadin.addons.easygrid.actions.EasyRowAction;
import com.flowingcode.vaadin.testbench.rpc.RmiRemote;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.function.SerializablePredicate;

public interface RmiEasyRowAction<T> extends RmiRemote {

  RmiEasyRowAction<T> visibleWhen(SerializablePredicate<T> predicate);

  RmiEasyRowAction<T> enabledWhen(SerializablePredicate<T> predicate);

  RmiEasyRowAction<T> withConfirmation(String title, String message);

  RmiEasyRowAction<T> addThemeVariants(ButtonVariant variant);

  void remove();

  static <T> RmiEasyRowAction<T> of(EasyRowAction<T> action) {
    return new RmiEasyRowAction<T>() {
      @Override
      public RmiEasyRowAction<T> visibleWhen(SerializablePredicate<T> predicate) {
        action.visibleWhen(predicate);
        return this;
      }

      @Override
      public RmiEasyRowAction<T> enabledWhen(SerializablePredicate<T> predicate) {
        action.enabledWhen(predicate);
        return this;
      }

      @Override
      public RmiEasyRowAction<T> withConfirmation(String title, String message) {
        action.withConfirmation(title, message);
        return this;
      }

      @Override
      public RmiEasyRowAction<T> addThemeVariants(ButtonVariant variant) {
        action.addThemeVariants(variant);
        return this;
      }

      @Override
      public void remove() {
        action.remove();
      }
    };
  }

}
