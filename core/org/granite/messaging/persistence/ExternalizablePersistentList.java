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

package org.granite.messaging.persistence;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.granite.config.GraniteConfig;
import org.granite.context.GraniteContext;
import org.granite.messaging.amf.io.convert.Converters;

/**
 * @author Franck WOLFF
 */
public class ExternalizablePersistentList extends AbstractExternalizablePersistentCollection {

    private static final long serialVersionUID = 1L;

    public ExternalizablePersistentList() {
    }

	public ExternalizablePersistentList(List<?> content, boolean initialized, boolean dirty) {
		super(null, initialized, dirty);
		setContentFromList(content);
	}

	public ExternalizablePersistentList(Object[] content, boolean initialized, boolean dirty) {
		super(content, initialized, dirty);
	}
	
	public List<?> getContentAsList(Type target) {
		List<Object> list = null;
		if (content != null) {
			list = new ArrayList<Object>(content.length);
			
            GraniteConfig config = GraniteContext.getCurrentInstance().getGraniteConfig();
            Converters converters = config.getConverters();
			Type[] typeArguments = null;
			if (target instanceof ParameterizedType)
				typeArguments = ((ParameterizedType)target).getActualTypeArguments();
			
	        for (Object o : content) {
	        	if (typeArguments != null)
	        		list.add(converters.convert(o, typeArguments[0]));
	        	else
	        		list.add(o);
	        }
		}
        return list;
	}
	
	public void setContentFromList(List<?> list) {
		content = (list != null ? list.toArray() : null);
	}
}
