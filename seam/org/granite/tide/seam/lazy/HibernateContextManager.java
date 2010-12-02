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

import org.granite.context.GraniteContext;
import org.granite.messaging.amf.io.util.ClassGetter;
import org.granite.tide.TidePersistenceManager;
import org.hibernate.Session;
import org.jboss.seam.Entity;
import org.jboss.seam.util.Reflections;

/**
 * Manager responsible for the maintaining a reference for the HibernateContext. 
 * @author CIngram
 */
public class HibernateContextManager implements TidePersistenceManager {
	
	private Session session = null;
	
	public HibernateContextManager() {
		
	}
	
	public HibernateContextManager(Session session) {
		this.session = session;
	}
	
	/**
	 * Attach the passed in entity with the HibernateSession.
	 * @param entity
	 * @return the attached entity object
	 */
	public Object attachEntity(Object entity, String[] propertyNames) {
		Object attachedEntity = null;
        ClassGetter getter = GraniteContext.getCurrentInstance().getGraniteConfig().getClassGetter();
        
		try { 
		    attachedEntity = findEntity(entity, propertyNames);
			
			if (propertyNames != null) {
	            for (int i = 0; i < propertyNames.length; i++) {
	                Object initializedObj = Reflections.getGetterMethod(attachedEntity.getClass(), propertyNames[i]).invoke(attachedEntity);
	                
	                getter.initialize(entity, propertyNames[i], initializedObj);
			    }
			}
		} 
		catch(Exception e) {
			throw new RuntimeException("Unable to attach entity and init collection", e);
		}
		disconnectSession();
		
		return attachedEntity;
	}
	
	/**
	 * attaches the entity to the HibernateSession.
	 * @return the attached entity
	 */
	public Object findEntity(Object entity, String[] fetch) {
	    Serializable id = (Serializable)Entity.forClass(entity.getClass()).getIdentifier(entity);
	    if (id == null)
	        return null;
	    return session.get(entity.getClass(), id);
	}
	
    
	/**
	 * disconnects from the Hibernate Session.
	 */
	protected void disconnectSession() {
		session.disconnect();
	}
}
