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

package org.granite.config;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.granite.messaging.amf.io.util.Property;
import org.granite.messaging.amf.io.util.externalizer.Externalizer;
import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedBean;
import org.granite.util.ClassUtil;
import org.granite.util.XMap;

/**
 * @author Franck WOLFF
 */
public class ExternalizerFactory implements ConfigurableFactory<Externalizer> {

    private static final Externalizer NULL_EXTERNALIZER = new Externalizer() {

        private final static String NOT_IMPLEMENTED = "Not implemented (null externalizer)";

        public void configure(XMap properties) {
            throw new RuntimeException(NOT_IMPLEMENTED);
		}
		public List<Property> findOrderedFields(Class<?> clazz) {
            throw new RuntimeException(NOT_IMPLEMENTED);
        }
        public Object newInstance(String type, ObjectInput in)
            throws IOException, ClassNotFoundException, InstantiationException,
                   InvocationTargetException, IllegalAccessException {
            throw new RuntimeException(NOT_IMPLEMENTED);
        }
        public void readExternal(Object o, ObjectInput in)
            throws IOException, ClassNotFoundException, IllegalAccessException {
            throw new RuntimeException(NOT_IMPLEMENTED);
        }
        public void writeExternal(Object o, ObjectOutput out)
            throws IOException, IllegalAccessException {
            throw new RuntimeException(NOT_IMPLEMENTED);
        }
        public int accept(Class<?> clazz) {
            return -1;
        }
    };
    
    private final ConcurrentHashMap<String, Externalizer> externalizersCache = new ConcurrentHashMap<String, Externalizer>();

    public Externalizer getNullInstance() {
        return NULL_EXTERNALIZER;
    }

    public Externalizer getInstance(String type, GraniteConfig config) throws GraniteConfigException {
        return newInstance(type, config);
    }

    public Externalizer getInstanceForBean(
        List<Externalizer> scannedConfigurables,
        Class<?> beanClass,
        GraniteConfig config) throws GraniteConfigException {

    	Externalizer externalizer = NULL_EXTERNALIZER;
    	
        if (!Externalizable.class.isAssignableFrom(beanClass)) {
            ExternalizedBean annotation = beanClass.getAnnotation(ExternalizedBean.class);

            Class<? extends Externalizer> type = null;
            if (annotation != null && annotation.type() != null)
                type = annotation.type();
            else {
                int maxWeight = -1;
                for (Externalizer e : scannedConfigurables) {
                    int weight = e.accept(beanClass);
                    if (weight > maxWeight) {
                        maxWeight = weight;
                        type = e.getClass();
                    }
                }
            }

            if (type != null)
                externalizer = newInstance(type.getName(), config);
        }

        return externalizer;
    }
    
    private Externalizer newInstance(String externalizerType, GraniteConfig config) {
    	Externalizer externalizer = externalizersCache.get(externalizerType);
    	if (externalizer == null) {
    		try {
    			externalizer = ClassUtil.newInstance(externalizerType, Externalizer.class);
    		} catch (Exception e) {
    			throw new GraniteConfigException("Could not instantiate externalizer: " + externalizerType, e);
    		}
    		Externalizer previous = externalizersCache.putIfAbsent(externalizerType, externalizer);
    		if (previous != null)
    			externalizer = previous;
    		else
    			externalizer.configure(config.getExternalizersConfiguration());
    	}
    	return externalizer;
    }
}
