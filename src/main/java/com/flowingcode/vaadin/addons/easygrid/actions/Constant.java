package com.flowingcode.vaadin.addons.easygrid.actions;

import com.vaadin.flow.function.ValueProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("serial")
final class Constant<T, V> implements ValueProvider<T, V> {

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

}
