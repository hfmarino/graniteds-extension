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

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Collection;

import org.granite.messaging.amf.io.convert.Converter;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.io.convert.IllegalConverterArgumentException;
import org.granite.util.ClassUtil;
import org.granite.util.CollectionUtil;

/**
 * @author Franck WOLFF
 */
public class Collection2Collection extends Converter {

    public Collection2Collection(Converters converters) {
        super(converters);
    }

    @Override
	protected boolean internalCanConvert(Object value, Type targetType) {

        Type targetComponentType = CollectionUtil.getComponentType(targetType);
        if (targetComponentType == null)
            return false; // not a collection.

        if (value == null)
            return true;

        if (!(value instanceof Collection<?>))
            return false;

        if (targetComponentType.equals(Object.class) || targetComponentType instanceof WildcardType)
            return true;

        Converter itemConverter = null;
        for (Object item : (Collection<?>)value) {

            if (itemConverter == null)
                itemConverter = converters.getConverter(item, targetComponentType);
            else if (!itemConverter.canConvert(item, targetComponentType))
                itemConverter = converters.getConverter(item, targetComponentType);

            if (itemConverter == null)
                return false;
        }

        return true;
    }

    @Override
	protected Object internalConvert(Object value, Type targetType) {

        if (value == null)
            return null;

        if (value instanceof Collection<?>) {
            Collection<?> c = (Collection<?>)value;

            Type targetComponentType = CollectionUtil.getComponentType(targetType);
            if (targetComponentType != null) {

                Class<?> targetClass = ClassUtil.classOfType(targetType);
                if (targetClass.isInstance(value) &&
                    (targetComponentType.equals(Object.class) || targetComponentType instanceof WildcardType))
                    return value;

                Collection<Object> targetInstance = null;
                try {
                    targetInstance = CollectionUtil.newCollection(targetClass, c.size());
                } catch (Exception e) {
                    throw new IllegalConverterArgumentException(this, value, targetType, e);
                }

                Converter itemConverter = null;
                for (Object item : c) {

                    if (itemConverter == null)
                        itemConverter = converters.getConverter(item, targetComponentType);
                    else if (!itemConverter.canConvert(item, targetComponentType))
                        itemConverter = converters.getConverter(item, targetComponentType);

                    if (itemConverter == null)
                        throw new IllegalConverterArgumentException(this, value, targetType);

                    targetInstance.add(itemConverter.convert(item, targetComponentType));
                }

                return targetInstance;
            }
        }

        throw new IllegalConverterArgumentException(this, value, targetType);
    }
}
