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

package org.granite.messaging.amf.io.convert;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.granite.util.ClassUtil;

/**
 * @author Franck WOLFF
 */
public abstract class Converter {

    protected final Converters converters;

    /**
     * Build a new Converter instance.
     *
     * @param converters a {@link Converters} instance (must be not null).
     * @throws NullPointerException (if converters is null).
     */
    public Converter(Converters converters) {
        if (converters == null)
            throw new NullPointerException("converters parameter cannot be null");
        this.converters = converters;
    }

    /**
     * Tells if the supplied object may be converted to the supplied target type by
     * this converter.
     *
     * @param value the object to be converted.
     * @param targetType the target type.
     * @return true if this converter can convert o to the target type, false otherwise.
     */
    public final boolean canConvert(Object value, Type targetType) {
    	if (targetType instanceof TypeVariable<?>)
    		targetType = ClassUtil.getBoundType((TypeVariable<?>)targetType);
    	return internalCanConvert(value, targetType);
    }
    
    protected abstract boolean internalCanConvert(Object value, Type targetType);

    /**
     * Converts the supplied object to the supplied target type.
     *
     * @param value the object to be converted.
     * @param targetType the target type.
     * @return the converted object.
     */
    public final Object convert(Object value, Type targetType) {
    	if (targetType instanceof TypeVariable<?>)
    		targetType = ClassUtil.getBoundType((TypeVariable<?>)targetType);
    	return internalConvert(value, targetType);
    }

    protected abstract Object internalConvert(Object value, Type targetType);
}
