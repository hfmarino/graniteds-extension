/*
  GRANITE DATA SERVICES
  Copyright (C) 2007 ADEQUATE SYSTEMS SARL

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

package test.granite.cdi.service;


import org.granite.config.servlet3.FlexFilter;
import org.granite.gravity.config.AbstractMessagingDestination;
import org.granite.gravity.config.servlet3.MessagingDestination;
import org.granite.tide.cdi.CDIServiceFactory;
import org.granite.tide.cdi.Identity;


@FlexFilter(
		tide=true,
		type="cdi",
		factoryClass=CDIServiceFactory.class,
		tideInterfaces={Identity.class}
)
public class GraniteConfig {

	@MessagingDestination(noLocal=true, sessionSelector=true)
	AbstractMessagingDestination addressBookTopic;

}
