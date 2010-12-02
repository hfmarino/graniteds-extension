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

package org.granite.tide.seam.lazy;

import java.io.Serializable;

import org.hibernate.Session;
import org.jboss.seam.Component;
import org.jboss.seam.Entity;
import org.jboss.seam.framework.PersistenceController;


/**
 * Manager responsible for looking up the EntityHome from the Seam context.
 * @author cingram
 *
 */
public class HibernatePersistenceControllerManager extends PersistenceContextManager {
    
    private String controllerName;  

    public HibernatePersistenceControllerManager(String controllerName) {
        this.controllerName = controllerName;
    }
    
    /**
     * Attach the passed in entity with the EntityManager stored 
     * in the EntityHome object.
     * @param entity
     * @return the attached hibernate object
     */
    @Override
    public Object findEntity(Object entity, String[] fetch) {
        PersistenceController<?> controller = (PersistenceController<?>)Component.getInstance(controllerName);
        Session session = (Session)controller.getPersistenceContext();
        Serializable id = (Serializable)Entity.forClass(entity.getClass()).getIdentifier(entity);
        if (id == null)
            return null;
        return session.get(entity.getClass(), id);
    }

}
