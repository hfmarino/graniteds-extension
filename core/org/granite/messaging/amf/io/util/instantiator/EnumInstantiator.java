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

package org.granite.messaging.amf.io.util.instantiator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.granite.messaging.service.ServiceException;
import org.granite.util.ClassUtil;

/**
 * @author Igor SAZHNEV
 */
public class EnumInstantiator extends AbstractInstantiator<Enum<?>> {

    private static final long serialVersionUID = -6116814787518316453L;

    private final String type;

    private static final List<String> orderedFields;
    static {
        List<String> of = new ArrayList<String>(1);
        of.add("value");
        orderedFields = Collections.unmodifiableList(of);
    }

    public EnumInstantiator(String type) {
        this.type = type;
    }

    @Override
    public List<String> getOrderedFieldNames() {
        return orderedFields;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Enum<?> newInstance() {
        Enum<?> enumInstance = null;

        String enumValue = null;
        try {
            Class<? extends Enum> enumClass = (Class<? extends Enum>)ClassUtil.forName(type);
            enumValue = (String)get("value");
            if (enumValue == null) {
                Object[] enumConstants = enumClass.getEnumConstants();
                if (enumConstants == null || enumConstants.length == 0)
                    throw new ServiceException("Invalid Enum type: " + type);
                enumValue = ((Enum<?>)enumConstants[0]).name();
            }
            enumInstance = Enum.valueOf(enumClass, enumValue);
        } catch (ClassNotFoundException e) {
            throw new ServiceException("Could not find Enum type for: " + type, e);
        } catch (IllegalArgumentException e) {
            throw new ServiceException("Could not find Enum constant for: " + type + '.' + enumValue, e);
        }

        return enumInstance;
    }
}