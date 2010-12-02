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

package org.granite.tide.data;

import org.granite.logging.Logger;
import org.granite.context.GraniteContext;
import org.granite.messaging.amf.io.util.ClassGetter;
import org.granite.tide.TidePersistenceManager;
import org.granite.tide.TideTransactionManager;
import org.granite.util.Reflections;


/**
 * Responsible for attaching a entity with the entity mangager
 * @author cingram
 *
 */
public abstract class AbstractTidePersistenceManager implements TidePersistenceManager {
	
	private static final Logger log = Logger.getLogger(AbstractTidePersistenceManager.class);
	
	protected TideTransactionManager tm;

	
	public AbstractTidePersistenceManager(TideTransactionManager tm) {
		this.tm = tm;
    	if (this.tm == null)
    		throw new RuntimeException("transaction manager is null");
	}
	
	
	public Object attachEntity(Object entity, String[] propertyNames) {
		return attachEntity(this, entity, propertyNames);
	}
	
	/**
	 * Attach the passed in entity with the EntityManager.
	 * @param entity
	 * @return the attached entity object
	 */
	public Object attachEntity(TidePersistenceManager pm, Object entity, String[] propertyNames) {
		Object attachedEntity = null;
        ClassGetter getter = GraniteContext.getCurrentInstance().getGraniteConfig().getClassGetter();
        
		Object tx = tm.begin(pm instanceof TideTransactionPersistenceManager ? (TideTransactionPersistenceManager)pm : null);
		if (tx == null)
		    throw new RuntimeException("Could not initiate transaction for lazy initialization");
		
		try {
            //the get is called to give the children a chance to override and
            //use the implemented method
			if (propertyNames != null)
				attachedEntity = findEntity(entity, propertyNames);
			else
				attachedEntity = entity;
            
            if (attachedEntity != null && propertyNames != null) {
                for (int i = 0; i < propertyNames.length; i++) {
                    Object initializedObj = Reflections.getGetterMethod(attachedEntity.getClass(), propertyNames[i]).invoke(attachedEntity);

                    //This is here to make sure the list is forced to return a value while operating inside of a 
                    //session. Forcing the  initialization of object.
                    if (getter != null)
                        getter.initialize(entity, propertyNames[i], initializedObj);
                }
            }
		    
            tm.commit(tx);
	    }
	    catch (Exception e) {
	    	String propertyName = propertyNames != null && propertyNames.length > 0 ? propertyNames[0] : "";
	    	log.error(e, "Error during lazy-initialization of collection: %s", propertyName);
	        tm.rollback(tx);
	    }
        
        return attachedEntity;
	} 
	
    /**
     * Finds the entity with the persistence context.
     * @return the entity with the persistence context.
     */
	public abstract Object findEntity(Object entity, String[] fetch);
	
}
