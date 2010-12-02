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

package org.granite.tide.cdi;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.event.Reception;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;


/**
 * TideEvents override to intercept CDI events handling
 * 
 * @author William DRAI
 */
public class TideEvents {

    private static final long serialVersionUID = -5395975397632138270L;
    
    
    @Inject
    private BeanManager manager;
    
    
    public void processEvent(@Observes(notifyObserver=Reception.ALWAYS) @Any Object event) {
    	try {
    		@SuppressWarnings("unchecked")
    		Bean<CDIServiceContext> scBean = (Bean<CDIServiceContext>)manager.getBeans(CDIServiceContext.class).iterator().next();
    		CDIServiceContext serviceContext = (CDIServiceContext)manager.getReference(scBean, CDIServiceContext.class, manager.createCreationalContext(scBean));
	    	if (serviceContext != null)
	    		serviceContext.processEvent(event);
    	}
    	catch (ContextNotActiveException e) {
    		// Ignore event, no session context
    	}
    }
}
