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

/**
 * @author Franck WOLFF
 */
public class NoConverterFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Object value;
    private final Type targetType;

    public NoConverterFoundException(Object value, Type targetType) {
        this(value, targetType, buildDefaultMessage(value, targetType), null);
    }

    public NoConverterFoundException(Object value, Type targetType, String message) {
        this(value, targetType, message, null);
    }

    public NoConverterFoundException(Object value, Type targetType, Throwable cause) {
        this(value, targetType, buildDefaultMessage(value, targetType), cause);
    }

    public NoConverterFoundException(Object value, Type targetType, String message, Throwable cause) {
        super(message, cause);
        this.value = value;
        this.targetType = targetType;
    }

    public Object getValue() {
        return value;
    }

    public Type getTargetType() {
        return targetType;
    }

    private static String buildDefaultMessage(Object value, Type targetType) {
        try {
            return "Cannot convert: " + value + " to: " + targetType;
        } catch (Exception e) {
            return "No converter found. Additionally, an error occured when trying to build default error message: " + e.toString();
        }
    }
}
