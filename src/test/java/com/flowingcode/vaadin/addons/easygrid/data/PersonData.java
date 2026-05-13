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
package com.flowingcode.vaadin.addons.easygrid.data;

import com.flowingcode.vaadin.addons.easygrid.model.Address;
import com.flowingcode.vaadin.addons.easygrid.model.Person;
import com.github.javafaker.Faker;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class PersonData {

  private final List<Person> people = new ArrayList<>();

  public List<Person> getPersons() {    
    if (people.isEmpty()) {
      var faker = new Faker();
      for (int i = 0; i <= 10; i++) {
        LocalDate minBirthDate = LocalDate.now().minusYears(99);
        LocalDate maxBirthDate = LocalDate.now().minusYears(1);
        int days = (int) ChronoUnit.DAYS.between(minBirthDate, maxBirthDate);
        LocalDate birthDate = minBirthDate.plusDays(faker.number().numberBetween(0, days));
        int age = Period.between(birthDate, LocalDate.now()).getYears();

        var person = Person.builder()
            .id(i + 1)
            .age(age)
            .birthDate(birthDate)
            .firstName(faker.name().firstName())
            .lastName(faker.name().lastName())
            .address(Address.builder()
                .street(faker.address().streetAddress())
                .number(faker.random().nextInt(10000))
                .city(faker.address().city())
                .postalCode(faker.address().zipCode()).build())
            .phoneNumber(null)
            .appointmentDateTime(faker.random().nextBoolean()
                ? LocalDateTime.now().plusDays(faker.random().nextInt(90))
                : null)
            .subscriber(faker.random().nextBoolean())
            .active(faker.random().nextBoolean())
            .build();
        
        people.add(person);
      }
    }
    return people;
  }
}
