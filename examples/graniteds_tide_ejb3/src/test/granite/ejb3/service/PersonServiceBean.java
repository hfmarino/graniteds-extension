/*
  GRANITE DATA SERVICES
  Copyright (C) 2007-2010 ADEQUATE SYSTEMS SARL

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package test.granite.ejb3.service;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.annotation.security.SecurityDomain;

import test.granite.ejb3.entity.Person;


/**
 * @author Franck WOLFF
 */
@Stateless
@Local(PersonService.class)
@SecurityDomain("other")
public class PersonServiceBean implements PersonService {

    public PersonServiceBean() {
        super();
    }


	@PersistenceContext
    protected EntityManager entityManager;

    
    public Person createPerson(Person person) {
    	entityManager.persist(person);
        return person;
    }

    @RolesAllowed({"admin"})
    public Person modifyPerson(Person person) {
        Person p = (Person)entityManager.merge(person);
        return p;
    }

    @RolesAllowed({"admin"})
    public void deletePerson(Integer personId) {
        Person person = (Person)entityManager.find(Person.class, personId);
        entityManager.remove(person);
    }
}
