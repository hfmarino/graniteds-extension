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
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Franck WOLFF
 */
public class MapUtil {

    public static Type[] getComponentTypes(Type mapType) {
        final Class<?> mapClass = ClassUtil.classOfType(mapType);

        if (!Map.class.isAssignableFrom(mapClass))
            return null;

        Type[] componentTypes = new Type[] {
            WildcardType.class,
            WildcardType.class
        };

        if (mapType instanceof ParameterizedType) {
            Type[] argTypes = ((ParameterizedType)mapType).getActualTypeArguments();
            if (argTypes != null) {
                if (argTypes.length > 0)
                    componentTypes[0] = argTypes[0];
                if (argTypes.length > 1)
                    componentTypes[1] = argTypes[1];
            }
        }

        return componentTypes;
    }

    @SuppressWarnings("unchecked")
    public static Map<Object, Object> newMap(Class<?> targetClass, int length)
        throws InstantiationException, IllegalAccessException  {

        if (targetClass.isInterface()) {

            if (SortedMap.class.isAssignableFrom(targetClass))
                return new TreeMap<Object, Object>();

            if (targetClass.isAssignableFrom(HashMap.class))
                return new HashMap<Object, Object>(length);

            throw new IllegalArgumentException("Unsupported collection interface: " + targetClass);
        }

        return (Map<Object, Object>)targetClass.newInstance();
    }
}
