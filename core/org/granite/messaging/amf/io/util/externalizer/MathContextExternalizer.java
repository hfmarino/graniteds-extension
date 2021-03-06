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

package org.granite.messaging.amf.io.util.externalizer;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.math.MathContext;

import org.granite.messaging.amf.io.util.instantiator.MathContextInstantiator;

/**
 * @author Franck WOLFF
 */
public class MathContextExternalizer extends DefaultExternalizer {

	public MathContextExternalizer() {
	}

	@Override
	public int accept(Class<?> clazz) {
		return (clazz == MathContext.class ? 1 : -1);
	}

	@Override
	public Object newInstance(String type, ObjectInput in) throws IOException, ClassNotFoundException,
			InstantiationException, InvocationTargetException, IllegalAccessException {
		return new MathContextInstantiator();
	}

	@Override
	public void writeExternal(Object o, ObjectOutput out) throws IOException, IllegalAccessException {
		MathContext mc = (MathContext)o;
		out.writeObject(mc.getPrecision());
		out.writeObject(mc.getRoundingMode());
	}
}
