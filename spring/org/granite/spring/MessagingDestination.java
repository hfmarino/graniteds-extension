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

package org.granite.spring;

import org.granite.gravity.config.AbstractMessagingDestination;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class MessagingDestination extends AbstractMessagingDestination implements InitializingBean, ApplicationContextAware {

    private ApplicationContext context = null;
    
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}

	
    public void afterPropertiesSet() {
		SpringGraniteConfig springGraniteConfig = (SpringGraniteConfig)context.getBeansOfType(SpringGraniteConfig.class).values().iterator().next();
		
		init(springGraniteConfig);
    }
}
