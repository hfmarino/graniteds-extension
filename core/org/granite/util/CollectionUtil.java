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

package org.granite.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Franck WOLFF
 */
public class CollectionUtil {

    public static Type getComponentType(Type collectionType) {

        Class<?> collectionClass = ClassUtil.classOfType(collectionType);
        if (collectionClass == null || !Collection.class.isAssignableFrom(collectionClass))
            return null;

        if (collectionType instanceof ParameterizedType) {
            Type[] componentTypes = ((ParameterizedType)collectionType).getActualTypeArguments();
            if (componentTypes != null && componentTypes.length == 1)
                return componentTypes[0];
        }

        return Object.class;
    }

    @SuppressWarnings("unchecked")
    public static Collection<Object> newCollection(Class<?> targetClass, int length)
        throws InstantiationException, IllegalAccessException  {

        if (targetClass.isInterface()) {

            if (Set.class.isAssignableFrom(targetClass)) {
                if (SortedSet.class.isAssignableFrom(targetClass))
                    return new TreeSet<Object>();
                return new HashSet<Object>(length);
            }

            if (targetClass.isAssignableFrom(ArrayList.class))
                return new ArrayList<Object>(length);

            throw new IllegalArgumentException("Unsupported collection interface: " + targetClass);
        }

        return (Collection<Object>)targetClass.newInstance();
    }
}
