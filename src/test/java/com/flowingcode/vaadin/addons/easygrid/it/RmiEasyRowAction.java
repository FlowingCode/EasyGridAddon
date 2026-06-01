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
