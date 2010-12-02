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

package org.granite.messaging.amf.io.convert.impl;

import java.lang.reflect.Array;
import java.lang.reflect.Type;

import org.granite.messaging.amf.io.convert.Converter;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.io.convert.IllegalConverterArgumentException;

/**
 * @author Franck WOLFF
 */
public class String2CharArray extends Converter {

    public String2CharArray(Converters converters) {
        super(converters);
    }

    @Override
	protected boolean internalCanConvert(Object value, Type targetType) {
        if (targetType instanceof Class<?>) {
            Class<?> targetClass = (Class<?>)targetType;
            return (
                targetClass.isArray()
            ) && (
                targetClass.getComponentType().equals(Character.TYPE) ||
                targetClass.getComponentType().equals(Character.class)
            ) && (
                value == null || value instanceof String
            );
        }
        return false;
    }

    @Override
	protected Object internalConvert(Object value, Type targetType) {
        if (value == null)
            return null;

        Class<?> componentType = ((Class<?>)targetType).getComponentType();

        if (componentType.equals(Character.TYPE)) // char[]
            return ((String)value).toCharArray();

        if (componentType.equals(Character.class)) { // Character[]
            String s = (String)value;
            Object array = Array.newInstance(Character.class, s.length());
            for (int i = 0; i < s.length(); i++)
                Array.set(array, i, Character.valueOf(s.charAt(i)));
            return array;
        }

        throw new IllegalConverterArgumentException(this, value, targetType);
    }
}
