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
package org.granite.config.api.internal;

import org.granite.config.api.Configuration;
/**
 * @author <a href="mailto:gembin@gmail.com">gembin@gmail.com</a>
 * @since 1.1.0
 */
public class ConfigurationImpl implements Configuration {
	
	private String flexCfgFile;
	private String flexCfgPropFile;
	private String graniteCfgFile;
	private String graniteCfgPropFile;

	public void setFlexServicesConfig(String flexCfgFile) {
		this.flexCfgFile = flexCfgFile;
	}

	public void setFlexServicesConfigProperties(String flexCfgPropFile) {
		this.flexCfgPropFile = flexCfgPropFile;
	}

	public void setGraniteConfig(String grainteCfgFile) {
		this.graniteCfgFile = grainteCfgFile;
	}

	public void setGraniteConfigProperties(String graniteCfgPropFile) {
		this.graniteCfgPropFile = graniteCfgPropFile;
	}

	public String getFlexServicesConfig() {
		return flexCfgFile;
	}

	public String getFlexServicesConfigProperties() {
		return flexCfgPropFile;
	}

	public String getGraniteConfigProperties() {
		return graniteCfgPropFile;
	}

	public String getGraniteConfig() {
		return graniteCfgFile;
	}

}
