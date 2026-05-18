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
package com.flowingcode.vaadin.addons.easygrid.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import lombok.Getter;

@Getter
public class NumberSample {

  private final BigDecimal bigDecimal;
  private final BigInteger bigInteger;
  private final Integer integer;
  private final int intValue;
  private final Long longValue;
  private final double doubleValue;

  // Overflow is intentional: demo data exercises all numeric types including truncated values.
  public NumberSample(BigDecimal value) {
    this.bigDecimal = value;
    this.bigInteger = value.toBigInteger();
    this.integer = bigInteger.intValue();
    this.intValue = integer;
    this.longValue = bigInteger.longValue();
    this.doubleValue = value.doubleValue();
  }

}
