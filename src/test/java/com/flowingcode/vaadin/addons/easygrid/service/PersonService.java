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
package com.flowingcode.vaadin.addons.easygrid.service;

import com.flowingcode.vaadin.addons.easygrid.data.PersonData;
import com.flowingcode.vaadin.addons.easygrid.model.Person;
import java.util.List;

public class PersonService {

  private final PersonData personData = new PersonData();

  public List<Person> fetch(int offset, int limit) {
    return personData.getPersons().stream().skip(offset).limit(limit).toList();
  }

  public int count() {
    return personData.getPersons().size();
  }

  public List<Person> fetchAll() {
    return personData.getPersons();
  }

}
