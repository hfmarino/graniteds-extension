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

package org.granite.messaging.amf.io.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Franck WOLFF
 */
public class DefaultClassGetter implements ClassGetter {

    public Class<?> getClass(Object o) {
        if (o == null)
            return null;

        Class<?> clazz = o.getClass();
        if (Proxy.isProxyClass(clazz))
            clazz = clazz.getInterfaces()[0];
        
        if (Enum.class.isAssignableFrom(clazz) && clazz.isAnonymousClass() && Enum.class.isAssignableFrom(clazz.getSuperclass()))
        	clazz = clazz.getSuperclass();

        return clazz;
    }
    
    public boolean isEntity(Object o) {
    	return false;
    }
    
    public boolean isInitialized(Object owner, String propertyName, Object propertyValue) {
        return true;
    }
    
    public void initialize(Object owner, String propertyName, Object propertyValue) {
    }
    
    public List<Object[]> getFieldValues(Object obj, Object dest) {
    	return defaultGetFieldValues(obj, dest);
    }
    
    public static List<Object[]> defaultGetFieldValues(Object obj, Object dest) {
        List<Object[]> fieldValues = new ArrayList<Object[]>();
        
        // Merges field values
        try {
            Class<?> clazz = obj.getClass();
            while (clazz != null) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if ((field.getModifiers() & Modifier.STATIC) != 0 
                        || (field.getModifiers() & Modifier.FINAL) != 0 
                        || (field.getModifiers() & Modifier.VOLATILE) != 0 
                        || (field.getModifiers() & Modifier.NATIVE) != 0 
                        || (field.getModifiers() & Modifier.TRANSIENT) != 0)
                        continue;
                    field.setAccessible(true);
                    Object o = field.get(obj);
                    Object d = field.get(dest);
                    fieldValues.add(new Object[] { field, o, d });
                }
                clazz = clazz.getSuperclass();
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Could not get entity fields ", e);
        }
        
        return fieldValues;
    }
}
