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
import java.util.Locale;
import java.util.regex.Pattern;

import org.granite.messaging.amf.io.convert.Converter;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.io.convert.IllegalConverterArgumentException;
import org.granite.messaging.amf.io.convert.Reverter;

/**
 * @author Franck WOLFF
 */
public class String2Locale extends Converter implements Reverter {

	private static final Pattern SPLITTER = Pattern.compile(Pattern.quote("_"));
	
    public String2Locale(Converters converters) {
        super(converters);
    }

    @Override
	protected boolean internalCanConvert(Object value, Type targetType) {
        return targetType.equals(Locale.class) && (value == null || value instanceof String);
    }

    @Override
	protected Object internalConvert(Object value, Type targetType) {
        if (value == null)
            return null;
        String[] tokens = SPLITTER.split((String)value, 3);
        if (tokens.length == 1)
        	return new Locale(tokens[0]);
        if (tokens.length == 2)
        	return new Locale(tokens[0], tokens[1]);
        if (tokens.length == 3)
        	return new Locale(tokens[0], tokens[1], tokens[2]);
        throw new IllegalConverterArgumentException(this, value, targetType, "Illegal Locale: " + value);
    }

	public boolean canRevert(Object value) {
		return (value == null) || (value instanceof Locale);
	}

	public Object revert(Object value) {
		if (value != null)
			value = value.toString();
		return value;
	}
}
