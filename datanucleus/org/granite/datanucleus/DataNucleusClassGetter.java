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

package org.granite.datanucleus;

import java.util.Map;

import javax.jdo.spi.Detachable;
import javax.jdo.spi.PersistenceCapable;

import org.granite.logging.Logger;
import org.granite.messaging.amf.io.util.DefaultClassGetter;

/**
 * @author William DRAI
 */
public class DataNucleusClassGetter extends DefaultClassGetter {

    public final static Logger log = Logger.getLogger(DataNucleusClassGetter.class);
    

    @Override
    public Class<?> getClass(Object o) {
    	// Nothing special here: DataNucleus uses bytecode enhancement
        return super.getClass(o);
    }
    
    @Override
    public boolean isEntity(Object o) {
    	return o instanceof PersistenceCapable;    
    }
    
    
    @Override
    public boolean isInitialized(Object owner, String propertyName, Object propertyValue) {
    	if (propertyValue != null || !(owner instanceof PersistenceCapable))
    		return true;
    	Map<String, Boolean> loadedState = DataNucleusExternalizer.getLoadedState((Detachable)owner, getClass(owner));
        return loadedState.containsKey(propertyName) && loadedState.get(propertyName);
    }
    
    @Override
    public void initialize(Object owner, String propertyName, Object propertyValue) {
    	if (propertyValue != null)
    		propertyValue.toString();
    }
}
