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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.granite.messaging.amf.io.convert.Converters;

/**
 * @author Franck WOLFF
 */
public class MethodProperty extends Property {

    private final Method setter;
    private final Method getter;
    private final Type type;

    public MethodProperty(Converters converters, String name, Method setter, Method getter) {
        super(converters, name);
        this.setter = setter;
        this.getter = getter;
        this.type = getter != null ? getter.getGenericReturnType() : setter.getParameterTypes()[0];
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        if (getter != null) {
            if (getter.isAnnotationPresent(annotationClass))
                return true;
        }
        if (setter != null)
            return setter.isAnnotationPresent(annotationClass);
        return false;
    }

    @Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
    	if (getter != null && getter.isAnnotationPresent(annotationClass))
    		return getter.getAnnotation(annotationClass);
    	if (setter != null)
    		return setter.getAnnotation(annotationClass);
		return null;
	}

	@Override
    public Type getType() {
        return type;
    }

    @Override
    public void setProperty(Object instance, Object value, boolean convert) {
        try {
            Object[] params = new Object[]{convert ? convert(value) : value};
            setter.invoke(instance, params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getProperty(Object instance) {
        try {
            return getter.invoke(instance, new Object[0]);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
