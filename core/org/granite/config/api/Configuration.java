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
package org.granite.config.api;

/**
 * API use to override the default GraniteDS configuration.
 * @author <a href="mailto:gembin@gmail.com">gembin@gmail.com</a>
 * @since 1.1.0
 */
public interface Configuration {
	/**
	 * set the granite-config.xml path
	 * @param cfgFile
	 */
	public void setGraniteConfig(String cfgFile);
	
	public String getGraniteConfig();
	
	/**
	 * set the  granite-config.properties path
	 * @param granitecfgPropFile
	 */
	public void setGraniteConfigProperties(String granitecfgPropFile);
	
	public String getGraniteConfigProperties();
	
	/**
	 * set the services-config.xml path
	 * @param flexCfgFile
	 */
	public void setFlexServicesConfig(String flexCfgFile);
	
	public String getFlexServicesConfig();
	
	/**
	 *  set the services-config.properties path
	 * @param flexCfgPropFile
	 */
	public void setFlexServicesConfigProperties(String flexCfgPropFile);
	
	public String getFlexServicesConfigProperties();

}

