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

package org.granite.gravity.jbossweb;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.granite.gravity.AsyncHttpContext;
import org.granite.gravity.Gravity;
import org.granite.gravity.GravityManager;
import org.granite.gravity.tomcat.ByteArrayCometIO;
import org.granite.gravity.tomcat.CometIO;
import org.granite.logging.Logger;
import org.jboss.servlet.http.HttpEvent;

import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public class GravityJBossWebServlet extends AbstractHttpEventServlet {
	
    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.getLogger(GravityJBossWebServlet.class);

    @Override
	public CometIO createCometIO() {
		return new ByteArrayCometIO();
	}

	@Override
    public boolean handleRequest(HttpEvent event, InputStream content) throws IOException, ServletException {

		Gravity gravity = GravityManager.getGravity(getServletContext());

    	HttpServletRequest request = event.getHttpServletRequest();
        HttpServletResponse response = event.getHttpServletResponse();

        try {
            initializeRequest(gravity, request, response);

            Message[] amf3Requests = deserialize(gravity, request, content);

            log.debug(">> [AMF3 REQUESTS] %s", (Object)amf3Requests);

            Message[] amf3Responses = null;
            
            boolean accessed = false;
            for (int i = 0; i < amf3Requests.length; i++) {
                Message amf3Request = amf3Requests[i];

                // Ask gravity to create a specific response (will be null for connect request from tunnel).
                Message amf3Response = gravity.handleMessage(amf3Request);
                String channelId = (String)amf3Request.getClientId();
                
                // Mark current channel (if any) as accessed.
                if (!accessed)
                	accessed = gravity.access(channelId);
                
                // (Re)Connect message from tunnel...
                if (amf3Response == null) {
                    if (amf3Requests.length > 1)
                        throw new IllegalArgumentException("Only one request is allowed on tunnel.");

                    JBossWebChannel channel = (JBossWebChannel)gravity.getChannel(channelId);
                	if (channel == null)
                		throw new NullPointerException("No channel on tunnel connect");
                    
                    // Try to send pending messages if any (using current container thread).
                    if (channel.runReceived(new AsyncHttpContext(request, response, amf3Request)))
                    	return true; // Close http event.
                    
                    // No pending messages, wait for new ones or timeout.
                    setConnectMessage(request, amf3Request);
                    channel.setHttpEvent(event);
                    return false; // Do not close http event.
                }

                if (amf3Responses == null)
                	amf3Responses = new Message[amf3Requests.length];
                amf3Responses[i] = amf3Response;
            }

            log.debug("<< [AMF3 RESPONSES] %s", (Object)amf3Responses);

            serialize(gravity, response, amf3Responses);
        }
        catch (IOException e) {
            log.error(e, "Gravity message error");
            throw e;
        }
        catch (Exception e) {
            log.error(e, "Gravity message error");
            throw new ServletException(e);
        }
        finally {
        	try {
        		if (content != null)
        			content.close();
        	}
        	finally {
        		cleanupRequest(request);
        	}
        }

        return true; // Close http event.
    }

    @Override
	public boolean handleEnd(HttpEvent event) throws IOException, ServletException {
        return true; // Close http event.
	}

	@Override
    public boolean handleError(HttpEvent event) throws IOException {
		if (EventUtil.isErrorButNotTimeout(event))
			log.error("Got an error event: %s", EventUtil.toString(event));

        try {
    		HttpServletRequest request = event.getHttpServletRequest();
        	Message connect = getConnectMessage(request);
        	if (connect != null) { // This should be a timeout.
        		Gravity gravity = GravityManager.getGravity(getServletContext());
		        String channelId = (String)connect.getClientId();
	            JBossWebChannel channel = (JBossWebChannel)gravity.getChannel(channelId);
	            
	            // Cancel channel's execution (timeout or other errors).
	            if (channel != null)
	            	channel.setHttpEvent(null);
        	}
        }
        catch (Exception e) {
        	log.error(e, "Error while processing event: %s", EventUtil.toString(event));
        }
		
		return true; // Close http event.
	}
}
