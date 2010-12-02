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

package org.granite.jmx;

import javax.management.ObjectName;
import javax.servlet.ServletContext;

import org.granite.config.ServletGraniteConfig;
import org.granite.config.flex.ServletServicesConfig;
import org.granite.logging.Logger;

/**
 * An utility class that register/unregister GraniteDS MBeans.
 * 
 * @author Franck WOLFF
 */
public class GraniteMBeanInitializer {
	
	private static final Logger log = Logger.getLogger(GraniteMBeanInitializer.class);

	public static void registerMBeans(ServletContext context, ServletGraniteConfig gConfig, ServletServicesConfig sConfig) {
        try {
            ObjectName name = new ObjectName("org.granite:type=GraniteConfig,context=" + context.getServletContextName());
	        log.info("Registering MBean: %s", name);
            OpenMBean mBean = OpenMBean.createMBean(gConfig);
        	MBeanServerLocator.getInstance().register(mBean, name);
        }
        catch (Exception e) {
        	log.error(e, "Could not register GraniteConfig MBean for context: %s", context.getServletContextName());
        }
	}	

	public static void unregisterMBeans(ServletContext context) {
        try {
            ObjectName name = new ObjectName("org.granite:type=GraniteConfig,context=" + context.getServletContextName());
	        log.info("Unregistering MBean: %s", name);
        	MBeanServerLocator.getInstance().unregister(name);
        }
        catch (Exception e) {
        	log.error(e, "Could not unregister GraniteConfig MBean for context: %s", context.getServletContextName());
        }
	}	
}
