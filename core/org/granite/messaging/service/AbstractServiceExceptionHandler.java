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

import flex.messaging.messages.Message;

/**
 * @author Marcelo SCHROEDER
 */
public abstract class AbstractServiceExceptionHandler implements ServiceExceptionHandler {

	private static final long serialVersionUID = 1L;
	
	private final boolean logException;

	public AbstractServiceExceptionHandler(boolean logException) {
		this.logException = logException;
	}

	protected boolean getLogException() {
		return logException;
	}
	
	protected ServiceException getServiceException(ServiceInvocationContext context, Throwable e) {
    	String method = (context.getMethod() != null ? context.getMethod().toString() : "null");
        return getServiceException(context.getMessage(), context.getDestination(), method, e);
    }

    protected ServiceException getServiceException(Message request, Destination destination, String method, Throwable e) {

        if (e instanceof ServiceException)
            return (ServiceException)e;

        String detail = "\n" +
            "- destination: " + (destination != null ? destination.getId() : "") + "\n" +
            "- method: " + method + "\n" +
            "- exception: " + e.toString() + "\n";

        return new ServiceException(getClass().getSimpleName() + ".Call.Failed", e.getMessage(), detail, e);
    }
}
