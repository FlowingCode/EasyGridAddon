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
package com.flowingcode.vaadin.addons.easygrid.test;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

/**
 * Generic base for parameterized delegation tests.
 *
 * <p>Verifies that for each method supplied by the subclass's {@code @Parameters}, the target
 * class (a) exposes the method, (b) returns {@code this} (chainability), and (c) produces the
 * expected side-effect on the underlying delegate. If the target class does not expose the method
 * at all, the test fails with a descriptive message.
 *
 * <p>Concrete subclasses must supply:
 * <ul>
 * <li>a {@code @Parameters} static method that calls
 * {@link #parameters(Class) parameters(delegateClass)};</li>
 * <li>{@link #filter(Method)}: selects which methods are exercised;</li>
 * <li>implementations of {@link #getTargetClass()}, {@link #getDelegateClass()},
 * {@link #createDelegate()}, {@link #createTarget(Object)},
 * {@link #buildArg(Class, int)}, and {@link #assertDelegated(Object, String, Object[])}.</li>
 * </ul>
 */
@RunWith(Parameterized.class)
public abstract class DelegationTest {

  protected final Method method;

  protected DelegationTest(Method method) {
    this.method = method;
    method.setAccessible(true);
  }

  /** Returns the class under test, derived from the return type of {@link #createTarget}. */
  protected final Class<?> getTargetClass() {
    for (Class<?> c = getClass(); c != DelegationTest.class; c = c.getSuperclass()) {
      try {
        return c.getDeclaredMethod("createTarget", Object.class).getReturnType();
      } catch (NoSuchMethodException ignored) {
      }
    }
    return Object.class;
  }

  /**
   * Builds the {@code @Parameters} collection from all public methods of {@code delegateClass}.
   *
   * <p>Each element is {@code {methodName, method}}. Methods excluded by {@link #filter} are
   * skipped at runtime rather than at parameterization time.
   *
   * @param delegateClass the delegate class whose methods are enumerated
   * @return parameter rows for {@link Parameterized}
   */
  protected static Collection<Object[]> parameters(Class<?> delegateClass,
      Predicate<Method> filter) {
    return Arrays.stream(delegateClass.getMethods())
        .filter(m -> Modifier.isPublic(m.getModifiers()))
        .filter(m -> !Modifier.isStatic(m.getModifiers()))
        .filter(m -> m.getDeclaringClass() != Object.class)
        .filter(filter)
        .sorted(Comparator.comparing(Method::getName).thenComparingInt(Method::getParameterCount))
        .map(m -> new Object[] {signatureOf(m), m})
        .collect(Collectors.toList());
  }

  private static Object signatureOf(Method m) {
    return m.getName() + Stream.of(m.getParameterTypes()).map(Class::getSimpleName)
        .collect(Collectors.joining(",", "(", ")"));
  }

  /**
   * Creates a fresh delegate instance for each test run. Returning {@code null} skips target
   * construction and only verifies that the target class exposes the method signature.
   */
  protected abstract Object createDelegate() throws ReflectiveOperationException;

  /**
   * Creates a fresh target instance wrapping {@code delegate}. Only called when
   * {@link #createDelegate()} returns a non-null value.
   */
  protected abstract Object createTarget(Object delegate) throws ReflectiveOperationException;

  /**
   * Returns a test argument for a parameter of the given type at position {@code index}.
   *
   * @throws UnsupportedOperationException if no factory is registered for {@code paramType}
   */
  private final Object buildArg(Class<?> type) {
    if (type == String.class) {
      return "value";
    } else if (type.isEnum()) {
      return type.getEnumConstants()[0];
    } else if (type == boolean.class) {
      return Boolean.TRUE;
    } else if (type == int.class) {
      return Integer.valueOf(0);
    } else if (type.isArray()) {
      return Array.newInstance(type.getComponentType(), 0);
    } else {
      return Mockito.mock(type);
    }
  }

  /**
   * Asserts that invoking {@code methodName(args)} on the target has produced the expected
   * side-effect on {@code delegate}.
   */
  protected abstract void assertDelegated(Object delegate, String methodName, Object[] args)
      throws ReflectiveOperationException;

  @Test
  public final void testMethod() throws Throwable {
    Object delegate = createDelegate();
    
    Method targetMethod;
    try {
      targetMethod = getTargetClass().getMethod(method.getName(), method.getParameterTypes());
    } catch (NoSuchMethodException e) {
      fail(getTargetClass().getSimpleName() + " does not expose " + signatureOf(method));
      return;
    }

    verifyMethod(targetMethod);

    if (delegate != null) {
      Object target = createTarget(delegate);
      if (target == null) {
        fail("createTarget() must not return null when createDelegate() returns non-null");
        return;
      }

      try {
        Object[] args = buildArgs(method.getParameterTypes());
        targetMethod.setAccessible(true);
        Object result = targetMethod.invoke(target, args);
        if (isChainable(targetMethod)) {
          verifyResult(target, result, method.getName());
        }
        assertDelegated(delegate, method.getName(), args);
      } catch (InvocationTargetException e) {
        throw e.getCause();
      }
    }
  }

  protected void verifyMethod(Method targetMethod) {
    // Do nothing
  }

  /**
   * Returns {@code true} if the method under test is expected to return the target instance
   */
  protected boolean isChainable(Method targetMethod) {
    return !method.getName().startsWith("get");
  }

  protected void verifyResult(Object target, Object result, String methodName) {
    assertSame(methodName + " must return target", target, result);
  }

  private Object[] buildArgs(Class<?>[] types) {
    Object[] args = new Object[types.length];
    for (int i = 0; i < types.length; i++) {
      args[i] = buildArg(types[i]);
    }
    return args;
  }

}
