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

package org.granite.messaging.service.tide;

import java.util.Set;

import org.granite.config.GraniteConfigException;
import org.granite.util.ClassUtil;


/**
 * @author Franck WOLFF
 */
public class TideComponentInstanceOfMatcher implements TideComponentMatcher {
    
    private final boolean disabled;
    private final Class<?> parentClass;
    
    public TideComponentInstanceOfMatcher(String type, boolean disabled) {
        try {
            parentClass = ClassUtil.forName(type);
            this.disabled = disabled;
        } catch (Exception e) {
            throw new GraniteConfigException("Could not instantiate instanceof matcher parent class: " + type, e);
        }
    }
    
    public boolean matches(String name, Set<Class<?>> classes, Object instance, boolean disabled) {
    	for (Class<?> clazz : classes) {
    		if (disabled == this.disabled && parentClass.isAssignableFrom(clazz))
    			return true;
    	}
    	return false;
    }
    
    @Override
    public String toString() {
    	return "Instanceof matcher: " + parentClass.getName() + (disabled ? " (disabled)" : "");
    }
}
