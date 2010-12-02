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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.granite.messaging.amf.io.util.Property;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractInstantiator<T> extends HashMap<String, Object> {

	private static final long serialVersionUID = 1L;

	private final List<Referer> referers = new ArrayList<Referer>();

    protected AbstractInstantiator() {
    }

    public abstract T newInstance() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException;
    public abstract List<String> getOrderedFieldNames();

    public void addReferer(Object referer, Property property) {
        referers.add(new Referer(referer, property));
    }

    public T resolve() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        T object = newInstance();
        if (object == null)
        	throw new InstantiationException("Instantiator newInstance() method cannot return null");
        for (Referer referer : referers)
            referer.property.setProperty(referer.object, object);
        return object;
    }

    static class Referer {
        final Object object;
        final Property property;

        Referer(Object object, Property property) {
            this.object = object;
            this.property = property;
        }
    }
}
