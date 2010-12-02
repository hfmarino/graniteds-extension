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

package org.granite.hibernate;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.granite.messaging.amf.io.util.instantiator.AbstractInstantiator;
import org.hibernate.proxy.HibernateProxy;

/**
 * @author Franck WOLFF
 */
public class HibernateProxyInstantiator extends AbstractInstantiator<HibernateProxy> {

    private static final long serialVersionUID = 1L;

    private final ConcurrentHashMap<String, ProxyFactory> proxyFactories;
    private final String detachedState;
    private Serializable id;

    public HibernateProxyInstantiator(ConcurrentHashMap<String, ProxyFactory> proxyFactories, String detachedState) {
        this.proxyFactories = proxyFactories;
        this.detachedState = detachedState;
    }

    public void readId(ObjectInput in) throws IOException, ClassNotFoundException {
        this.id = (Serializable)in.readObject();
    }

    protected Serializable getId() {
        return id;
    }

    @Override
    public List<String> getOrderedFieldNames() {
        return Collections.emptyList();
    }

    @Override
    public HibernateProxy newInstance() throws IOException, ClassNotFoundException, InstantiationException {
        final String[] tokens = detachedState.split("\\Q:\\E", -1);

        ProxyFactory factory = proxyFactories.get(tokens[0]);
        if (factory == null) {
            factory = newProxyFactory(tokens[0]);
            ProxyFactory previousFactory = proxyFactories.putIfAbsent(tokens[0], factory);
            if (previousFactory != null)
                factory = previousFactory;
        }

        return factory.getProxyInstance(tokens[1], tokens[2], id);
    }
    
    protected ProxyFactory newProxyFactory(String initializerClassName) {
    	return new ProxyFactory(initializerClassName);
    }
}
