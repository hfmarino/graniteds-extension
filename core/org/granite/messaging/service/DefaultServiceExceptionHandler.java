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

import org.granite.config.flex.Destination;
import org.granite.logging.Logger;
import org.granite.messaging.service.security.SecurityServiceException;

import flex.messaging.messages.Message;

/**
 * @author Marcelo SCHROEDER
 */
public class DefaultServiceExceptionHandler extends AbstractServiceExceptionHandler {

    private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(DefaultServiceExceptionHandler.class);

	public DefaultServiceExceptionHandler() {
		this(true);
	}
	
	public DefaultServiceExceptionHandler(boolean logException) {
		super(logException);
	}
	
    public ServiceException handleNoSuchMethodException(Message request,
            Destination destination, Object invokee, String method,
            Object[] args, NoSuchMethodException e) {

    	if (getLogException())
    		log.error(e, "Could not process remoting message: %s", request);

    	return getServiceException(request, destination, method, e);
    }

    public ServiceException handleInvocationException(ServiceInvocationContext context, Throwable e) {
        
    	if (getLogException()) {
	    	if (e instanceof SecurityServiceException)
	            log.debug(e, "Could not process remoting message: %s", context.getMessage());
	        else
	            log.error(e, "Could not process remoting message: %s", context.getMessage());
    	}

        return getServiceException(context, e);
    }
}
