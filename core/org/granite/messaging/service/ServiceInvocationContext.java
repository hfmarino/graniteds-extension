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

package org.granite.messaging.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.granite.config.flex.Destination;
import org.granite.messaging.service.security.AbstractSecurityContext;

import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public class ServiceInvocationContext extends AbstractSecurityContext {

    private final Object bean;
    private final Method method;
    private Object[] parameters;

    public ServiceInvocationContext(
        Message message,
        Destination destination,
        Object bean,
        Method method,
        Object[] parameters) {

        super(message, destination);
        this.bean = bean;
        this.method = method;
        this.parameters = parameters;
    }

    public Object getBean() {
        return bean;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getParameters() {
        return parameters;
    }
    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    @Override
    public Object invoke() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        return method.invoke(bean, parameters);
    }

    @Override
    public String toString() {
        return getClass().getName() + '{' +
            "\n  message=" + getMessage() +
            "\n  destination=" + getDestination() +
            "\n  bean=" + bean +
            "\n  method=" + method +
            "\n  parameters=" + Arrays.toString(parameters) +
        '}';
    }
}
