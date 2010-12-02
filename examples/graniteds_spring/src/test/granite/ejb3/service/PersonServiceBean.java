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

import java.util.List;
import java.util.Set;

import org.granite.messaging.service.annotations.RemoteDestination;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import test.granite.ejb3.entity.Contact;
import test.granite.ejb3.entity.Country;
import test.granite.ejb3.entity.Person;


@Service("personService")
@RemoteDestination(id="personService", source="personService", securityRoles={"ROLE_ADMIN", "ROLE_USER"})
public class PersonServiceBean extends AbstractEntityService implements PersonService {

    private static final long serialVersionUID = 1L;


    @Transactional(readOnly=true)
    public List<Person> findAllPersons() {
        return findAll(Person.class);
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly=true)
    public List<Person> findAllPersons(String name) {
        return entityManager.createQuery(
            "select distinct p from Person p " +
            "where upper(p.firstName) like upper('%" + name + "%') or upper(p.lastName) like upper('%" + name + "%')"
        ).getResultList();
    }

    @Transactional(readOnly=true)
    public List<Country> findAllCountries() {
        return findAll(Country.class);
    }

    @Transactional
    public Person createPerson(Person person) {
        return this.merge(person);
    }

    @Transactional
    public Person modifyPerson(Person person) {
        return this.merge(person);
    }

    @Transactional
    public void deletePerson(final Person person) {
        Person foundPerson = entityManager.find(Person.class, person.getId());
        entityManager.remove(foundPerson);
    }

    @Transactional
    public void deleteContact(final Contact contact) {
        Contact foundContact = entityManager.find(Contact.class, contact.getId());

        foundContact.getPerson().getContacts().remove(foundContact);
        if (foundContact.equals(foundContact.getPerson().getMainContact())) {
            Set<Contact> contacts = foundContact.getPerson().getContacts();
            foundContact.getPerson().setMainContact(contacts.isEmpty() ? null : contacts.iterator().next());
        }
        entityManager.remove(foundContact);
    }
}
