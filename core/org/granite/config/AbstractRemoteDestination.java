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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.granite.config.flex.Channel;
import org.granite.config.flex.Destination;
import org.granite.config.flex.EndPoint;
import org.granite.config.flex.Service;
import org.granite.config.flex.ServicesConfig;
import org.granite.logging.Logger;
import org.granite.util.XMap;


public class AbstractRemoteDestination {
	
    private static final Logger log = Logger.getLogger(AbstractRemoteDestination.class);


    ///////////////////////////////////////////////////////////////////////////
    // Instance fields.
   
    private String id = null;
    private String source = null;
    private List<String> roles = null;
    
    
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

    public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
	public List<String> getRoles() {
		return roles;
	}
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	
    protected void init(AbstractFrameworkGraniteConfig graniteConfig) {
    	ServicesConfig servicesConfig = graniteConfig.getServicesConfig();
    	initServices(servicesConfig);
    }
    
    public void initServices(ServicesConfig servicesConfig) {
    	Channel channel = servicesConfig.findChannelById("graniteamf");
    	if (channel == null) {
    		channel = new Channel("graniteamf", "mx.messaging.channels.AMFChannel",
    				new EndPoint("http://{server.name}:{server.port}/{context.root}/graniteamf/amf", "flex.messaging.endpoints.AMFEndpoint"),
    				new XMap());
    		servicesConfig.addChannel(channel);
    	}
    	
    	List<Service> services = servicesConfig.findServicesByMessageType("flex.messaging.messages.RemotingMessage");
    	Service service = null;
    	if (services == null || services.isEmpty()) {
    		service = new Service("granite-service", "flex.messaging.services.RemotingService", "flex.messaging.messages.RemotingMessage", 
    				null, null, new HashMap<String, Destination>());
    		servicesConfig.addService(service);
    	}
    	else
    		service = services.get(0);
    	
    	service.getDestinations().put(source, buildDestination());
    	
    	log.info("Registered remote destination %s", source);
    }
	
	protected Destination buildDestination() {
    	List<String> channelIds = new ArrayList<String>();
    	channelIds.add("graniteamf");
    	Destination destination = new Destination(source, channelIds, new XMap(), roles, null, null);
    	return destination;
	}
}
