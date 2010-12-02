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

package org.granite.messaging.amf.io;

import java.io.IOException;

/**
 * Required for JDK 1.4 port: the IOException(Throwable cause) constructor
 * isn't available before JDK 5.
 * 
 * @author Franck WOLFF
 */
public class AMF3SerializationException extends IOException {

	private static final long serialVersionUID = 1L;

	public AMF3SerializationException() {
	}

	public AMF3SerializationException(String message) {
		super(message);
	}

	public AMF3SerializationException(Throwable cause) {
		super();
		initCause(cause);
	}

	public AMF3SerializationException(String message, Throwable cause) {
		super(message);
		initCause(cause);
	}
}
