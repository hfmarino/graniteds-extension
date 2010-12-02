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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import test.granite.ejb3.entity.Person;


/**
 * @author Franck WOLFF
 */
@Stateless
@Local(PeopleService.class)
public class PeopleServiceBean implements PeopleService {

	@PersistenceContext
    protected EntityManager entityManager;
    
    
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    
    public Map<String, Object> find(Person examplePerson, int first, int max, String[] order, boolean[] desc) {
        EntityManager entityManager = getEntityManager();
        Map<String, Object> result = new HashMap<String, Object>(4);
        String from = "from Person e ";
        String where = "where lower(e.lastName) like :lastName ";
        String orderBy = order != null && order.length > 0 ? "order by e." + order[0] + (desc[0] ? " desc" : "") : "";
        String lastName = examplePerson.getLastName() != null ? examplePerson.getLastName() : "";
        Query qc = entityManager.createQuery("select count(e) " + from + where);
        qc.setParameter("lastName", "%" + lastName.toLowerCase() + "%");
        long resultCount = (Long)qc.getSingleResult();
        if (max == 0)
            max = 36;
        Query ql = entityManager.createQuery("select e " + from + where + orderBy);
        ql.setFirstResult(first);
        ql.setMaxResults(max);
        ql.setParameter("lastName", "%" + lastName.toLowerCase() + "%");
        List<?> resultList = ql.getResultList();
        result.put("firstResult", first);
        result.put("maxResults", max);
        result.put("resultCount", resultCount);
        result.put("resultList", resultList);
        return result;
    }
}
