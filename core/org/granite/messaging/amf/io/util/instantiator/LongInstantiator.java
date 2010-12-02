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

/**
 * @author Franck WOLFF
 */
public class LongInstantiator extends AbstractInstantiator<Long> {

	private static final long serialVersionUID = 1L;

	private static final String U1 = "u1";
	private static final String U0 = "u0";
	
	private static final List<String> orderedFields;
    static {
        List<String> of = new ArrayList<String>(1);
        of.add(U1);
        of.add(U0);
        orderedFields = Collections.unmodifiableList(of);
    }

    public LongInstantiator() {
    }

    @Override
    public List<String> getOrderedFieldNames() {
        return orderedFields;
    }

    @Override
    public Long newInstance() {
        int u1 = (int)((Number)get(U1)).longValue();
        int u0 = (int)((Number)get(U0)).longValue();

        return Long.valueOf(((long)u1 << 32) | (u0 & 0xffffffffL));
    }
}