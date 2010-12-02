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

package org.granite.tide.validators;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author William DRAI
 */
public class InvalidValue implements Externalizable {

	private final Object rootBean;
    private final Object bean;
    private final Class<?> beanClass;
    private final String path;
    private final Object value;
    private final String message;

    
    public InvalidValue(Object bean, String path, Object value, String message) {
        if (bean == null || path == null)
            throw new NullPointerException("bean and path parameters cannot be null");
        this.rootBean = bean;
        this.bean = bean;
        this.beanClass = bean.getClass();
        this.path = path;
        this.value = value;
        this.message = message;
    }
    
    public InvalidValue(Object rootBean, Object bean, String path, Object value, String message) {
        if (bean == null || path == null)
            throw new NullPointerException("bean and path parameters cannot be null");
        this.rootBean = rootBean;
        this.bean = bean;
        this.beanClass = bean.getClass();
        this.path = path;
        this.value = value;
        this.message = message;
    }

    public InvalidValue(Class<?> beanClass, String path, Object value, String message) {
        if (beanClass == null || path == null)
            throw new NullPointerException("beanClass and path parameters cannot be null");
        this.rootBean = null;
        this.bean = null;
        this.beanClass = beanClass;
        this.path = path;
        this.value = value;
        this.message = message;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        // write only bean...
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(rootBean);
        out.writeObject(bean);
        out.writeObject(beanClass.getName());
        out.writeObject(path);
        out.writeObject(value);
        out.writeObject(message);
    }
}
