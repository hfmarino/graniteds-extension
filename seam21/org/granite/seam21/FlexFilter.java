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

package org.granite.seam21;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.Channel;
import org.granite.config.flex.Destination;
import org.granite.config.flex.EndPoint;
import org.granite.config.flex.Factory;
import org.granite.config.flex.Service;
import org.granite.config.flex.ServicesConfig;
import org.granite.logging.Logger;
import org.granite.messaging.amf.process.AMF3MessageInterceptor;
import org.granite.messaging.service.ExceptionConverter;
import org.granite.messaging.service.tide.TideComponentAnnotatedWithMatcher;
import org.granite.messaging.service.tide.TideComponentInstanceOfMatcher;
import org.granite.messaging.service.tide.TideComponentNameMatcher;
import org.granite.messaging.service.tide.TideComponentTypeMatcher;
import org.granite.messaging.webapp.AMFEndpoint;
import org.granite.util.XMap;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.web.AbstractFilter;


@Scope(ScopeType.APPLICATION)
@Name("org.granite.seam.flexFilter")
@Startup
@Install(precedence=Install.BUILT_IN, value=false, classDependencies={"org.granite.seam21.Seam21GraniteConfig"})
@BypassInterceptors
@org.jboss.seam.annotations.web.Filter
public class FlexFilter extends AbstractFilter {
	
    private static final Logger log = Logger.getLogger(FlexFilter.class);
	
    private FilterConfig config = null;
    private GraniteConfig graniteConfig = null;
    private ServicesConfig servicesConfig = null;
    
    private List<String> tideRoles = null;
    private List<String> tideAnnotations = null;
    private List<String> tideInterfaces = null;
    private List<String> tideNames = null;
    private List<String> tideTypes = null;
    private List<Class<? extends ExceptionConverter>> exceptionConverters = null;
    private AMF3MessageInterceptor amf3MessageInterceptor = null;
    private boolean tide = false;
    
    
    public FlexFilter() {
    	super();
    	setUrlPattern("/graniteamf/*");
    }
    
    
    @Override
    public void init(FilterConfig config) {
    	this.config = config;
    }
	
    
	@Create
	public void seamInit() {
		Seam21GraniteConfig seam21GraniteConfig = (Seam21GraniteConfig)Component.getInstance(Seam21GraniteConfig.class, true);
		
        this.graniteConfig = seam21GraniteConfig.getGraniteConfig();
        if (tideAnnotations != null) {
        	for (String ta : tideAnnotations) {
        		try {
        			this.graniteConfig.getTideComponentMatchers().add(new TideComponentAnnotatedWithMatcher(ta, false));
        			log.debug("Enabled components annotated with %s for Tide remoting", ta);
        		}
        		catch (Exception e) {
        			log.error(e, "Could not add tide-component annotation %s", ta);
        		}
        	}
        }
        if (tideInterfaces != null) {
        	for (String ti : tideInterfaces) {
        		try {
        			this.graniteConfig.getTideComponentMatchers().add(new TideComponentInstanceOfMatcher(ti, false));
        			log.debug("Enabled components extending %s for Tide remoting", ti);
        		}
        		catch (Exception e) {
        			log.error(e, "Could not add tide-component interface %s", ti);
        		}
        	}
        }
        if (tideNames != null) {
        	for (String tn : tideNames) {
        		try {
        			this.graniteConfig.getTideComponentMatchers().add(new TideComponentNameMatcher(tn, false));
        			log.debug("Enabled components named like %s for Tide remoting", tn);
        		}
        		catch (Exception e) {
        			log.error(e, "Could not add tide-component name %s", tn);
        		}
        	}
        }
        if (tideTypes != null) {
        	for (String tt : tideTypes) {
        		try {
        			this.graniteConfig.getTideComponentMatchers().add(new TideComponentTypeMatcher(tt, false));
        			log.debug("Enabled components with type %s for Tide remoting", tt);
        		}
        		catch (Exception e) {
        			log.error(e, "Could not add tide-component type %s", tt);
        		}
        	}
        }
        if (exceptionConverters != null) {
        	for (Class<? extends ExceptionConverter> ec : exceptionConverters) {
        		this.graniteConfig.registerExceptionConverter(ec);
    			log.debug("Registered exception converter %s", ec);
        	}
        }
        if (amf3MessageInterceptor != null)
        	this.graniteConfig.setAmf3MessageInterceptor(amf3MessageInterceptor);
        
        servicesConfig = seam21GraniteConfig.getServicesConfig();
        
        Channel channel = servicesConfig.findChannelById("graniteamf");
        if (channel == null) {
        	channel = new Channel("graniteamf", "mx.messaging.channels.AMFChannel", 
        		new EndPoint("http://{server.name}:{server.port}/{context.root}/graniteamf/amf", "flex.messaging.endpoints.AMFEndpoint"), 
        		new XMap());
        	servicesConfig.addChannel(channel);
        }
        
        if (tide) {
        	Factory factory = servicesConfig.findFactoryById("tide-seam-factory");
        	if (factory == null) {
        		factory = new Factory("tide-seam-factory", "org.granite.tide.seam.SeamServiceFactory", new XMap());
        		servicesConfig.addFactory(factory);
        	}
        	
        	Service service = servicesConfig.findServiceById("granite-service");
        	if (service == null) {
        		service = new Service("granite-service", "flex.messaging.services.RemotingService", 
        			"flex.messaging.messages.RemotingMessage", null, null, new HashMap<String, Destination>());
        	}
        	Destination destination = servicesConfig.findDestinationById("flex.messaging.messages.RemotingMessage", "seam");
        	if (destination == null) {
        		List<String> channelIds = new ArrayList<String>();
        		channelIds.add("graniteamf");
        		destination = new Destination("seam", channelIds, new XMap(), tideRoles, null, null);
        		destination.getProperties().put("factory", factory.getId());
        		destination.getProperties().put("validator-name", "tideValidator");
        		service.getDestinations().put(destination.getId(), destination);
        		servicesConfig.addService(service);
        	}
        	
        	log.info("Registered Tide/Seam service factory and destination");
        }
        else {
        	Factory factory = new Factory("seam-factory", "org.granite.seam.SeamServiceFactory", new XMap());
        	servicesConfig.addFactory(factory);
        	
        	Service service = new Service("granite-service", "flex.messaging.services.RemotingService", 
        		"flex.messaging.messages.RemotingMessage", null, null, new HashMap<String, Destination>());
        	servicesConfig.addService(service);
            
            servicesConfig.scan(null);
        	
        	log.info("Registered Seam service factory");
        }
	}
	
	public void setTideRoles(List<String> tideRoles) {
		this.tideRoles = tideRoles;
	}
	
	public void setTideAnnotations(List<String> tideAnnotations) {
		this.tideAnnotations = tideAnnotations;
	}
	
	public void setTideInterfaces(List<String> tideInterfaces) {
		this.tideInterfaces = tideInterfaces;
	}
	
	public void setTideNames(List<String> tideNames) {
		this.tideNames = tideNames;
	}
	
	public void setTideTypes(List<String> tideTypes) {
		this.tideTypes = tideTypes;
	}
	
	public void setExceptionConverters(List<Class<? extends ExceptionConverter>> exceptionConverters) {
		this.exceptionConverters = exceptionConverters;
	}
	
	public void setAmf3MessageInterceptor(AMF3MessageInterceptor amf3MessageInterceptor) {
		this.amf3MessageInterceptor = amf3MessageInterceptor;
	}
	
	public void setTide(boolean tide) {
		this.tide = tide;
	}
	
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
     	throws IOException, ServletException {
		if (isMappedToCurrentRequestPath(request)) {         
			AMFEndpoint.service(graniteConfig, servicesConfig, config.getServletContext(), 
					(HttpServletRequest)request, (HttpServletResponse)response);
		}
		else { 
			chain.doFilter(request, response);
		}
	}
}