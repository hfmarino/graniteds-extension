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

package org.granite.cdi;

import javax.enterprise.context.Conversation;
import javax.enterprise.inject.spi.Bean;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.servlet.http.HttpSession;

import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.amf.process.AMF3MessageInterceptor;
import org.granite.messaging.service.ServiceException;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.tide.cdi.ConversationState;
import org.granite.tide.cdi.EventState;
import org.granite.tide.cdi.SessionState;
import org.jboss.weld.context.ContextLifecycle;
import org.jboss.weld.context.ConversationContext;
import org.jboss.weld.conversation.ConversationImpl;
import org.jboss.weld.conversation.ConversationManager;
import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.servlet.ConversationBeanStore;

import flex.messaging.messages.Message;


public class CDIInterceptor implements AMF3MessageInterceptor {
	
	private static final Logger log = Logger.getLogger(CDIInterceptor.class);

    private static final String CONVERSATION_ID = "conversationId";
    private static final String IS_LONG_RUNNING_CONVERSATION = "isLongRunningConversation";
    private static final String WAS_LONG_RUNNING_CONVERSATION_CREATED = "wasLongRunningConversationCreated";
    private static final String WAS_LONG_RUNNING_CONVERSATION_ENDED = "wasLongRunningConversationEnded";
    
    
    public static WeldManager lookupBeanManager() {
		HttpGraniteContext context = (HttpGraniteContext)GraniteContext.getCurrentInstance();
		WeldManager manager = (WeldManager)context.getServletContext().getAttribute("javax.enterprise.inject.spi.BeanManager");
		if (manager != null)
			return manager;
		
		InitialContext ic = null;
	    try {
			ic = new InitialContext();
	    	// Standard JNDI binding
	    	return (WeldManager)ic.lookup("java:comp/BeanManager");
	    }
	    catch (NameNotFoundException e) {
	    	if (ic == null)
	    		throw new RuntimeException("No InitialContext");
	    	
	    	// Weld/Tomcat
	    	try {
	    		return (WeldManager)ic.lookup("java:comp/env/BeanManager"); 
	    	}
	    	catch (Exception e1) { 	    	
	    		// JBoss 5/6 (maybe obsolete in Weld 1.0+)
		    	try {
		    		return (WeldManager)ic.lookup("java:app/BeanManager");
		    	}
	    	    catch (Exception e2) {
	    	    	throw new RuntimeException("Could not find Bean Manager", e2);
	    	    }
	    	}
	    }
	    catch (Exception e) {
	    	throw new RuntimeException("Could not find Bean Manager", e);
	    }
    }
    
    
	public void before(Message amfReqMessage) {
		if (log.isTraceEnabled())
			log.trace("Pre processing of request message: %s", amfReqMessage);

		try {
			GraniteContext context = GraniteContext.getCurrentInstance();
			
			if (context instanceof HttpGraniteContext) {
        		// Initialize CDI Context
				HttpSession session = ((HttpGraniteContext)context).getSession();
			    WeldManager beanManager = lookupBeanManager();
			    @SuppressWarnings("unchecked")
			    Bean<ConversationManager> conversationManagerBean = (Bean<ConversationManager>)beanManager.getBeans(ConversationManager.class).iterator().next();
			    ConversationManager conversationManager = (ConversationManager)beanManager.getReference(conversationManagerBean, ConversationManager.class, beanManager.createCreationalContext(conversationManagerBean));
			    
			    String conversationId = (String)amfReqMessage.getHeader(CONVERSATION_ID);
			    conversationManager.beginOrRestoreConversation(conversationId);
			    @SuppressWarnings("unchecked")
			    Bean<Conversation> conversationBean = (Bean<Conversation>)beanManager.getBeans(Conversation.class).iterator().next();
			    Conversation conversation = (Conversation)beanManager.getReference(conversationBean, Conversation.class, beanManager.createCreationalContext(conversationBean)); 
			    
			    String cid = ((ConversationImpl)conversation).getUnderlyingId();
			    ConversationContext conversationContext = lookupConversationContext(beanManager);
			    conversationContext.setBeanStore(new ConversationBeanStore(session, false, cid));
			    conversationContext.setActive(true);
			    
			    @SuppressWarnings("unchecked")
			    Bean<EventState> eventBean = (Bean<EventState>)beanManager.getBeans(EventState.class).iterator().next();
			    EventState eventState = (EventState)beanManager.getReference(eventBean, EventState.class, beanManager.createCreationalContext(eventBean));
			    if (!conversation.isTransient())
			    	eventState.setWasLongRunning(true);
			    
			    if (conversationId != null && conversation.isTransient()) {
			    	log.debug("Starting conversation " + conversationId);
			    	conversation.begin(conversationId);
			    }
				
		        if (Boolean.TRUE.toString().equals(amfReqMessage.getHeader("org.granite.tide.isFirstCall"))) {
		        	@SuppressWarnings("unchecked")
		        	Bean<SessionState> ssBean = (Bean<SessionState>)beanManager.getBeans(SessionState.class).iterator().next();
		        	((SessionState)beanManager.getReference(ssBean, SessionState.class, beanManager.createCreationalContext(ssBean))).setFirstCall(true);
		        }
				
		        if (Boolean.TRUE.toString().equals(amfReqMessage.getHeader("org.granite.tide.isFirstConversationCall")) && !conversation.isTransient()) {
		        	@SuppressWarnings("unchecked")
		        	Bean<ConversationState> csBean = (Bean<ConversationState>)beanManager.getBeans(ConversationState.class).iterator().next();
		        	((ConversationState)beanManager.getReference(csBean, ConversationState.class, beanManager.createCreationalContext(csBean))).setFirstCall(true);
		        }
			}
		}
		catch(Exception e) {
            log.error(e, "Exception while pre processing the request message.");
            throw new ServiceException("Error while pre processing the request message - " + e.getMessage());
		}
	}


	public void after(Message amfReqMessage, Message amfRespMessage) {		
		try {
			if (log.isTraceEnabled())
				log.trace("Post processing of response message: %s", amfReqMessage);

			if (GraniteContext.getCurrentInstance() instanceof HttpGraniteContext) {
			    WeldManager beanManager = lookupBeanManager();
				try {
					// Add conversation management headers to response
					if (amfRespMessage != null) {
					    @SuppressWarnings("unchecked")
					    Bean<Conversation> conversationBean = (Bean<Conversation>)beanManager.getBeans(Conversation.class).iterator().next();
					    Conversation conversation = (Conversation)beanManager.getReference(conversationBean, Conversation.class, beanManager.createCreationalContext(conversationBean));
					    
					    @SuppressWarnings("unchecked")
					    Bean<EventState> eventBean = (Bean<EventState>)beanManager.getBeans(EventState.class).iterator().next();
					    EventState eventState = (EventState)beanManager.getReference(eventBean, EventState.class, beanManager.createCreationalContext(eventBean));
					    if (eventState.wasLongRunning() && !conversation.isTransient())
					    	amfRespMessage.setHeader(WAS_LONG_RUNNING_CONVERSATION_ENDED, true);
						
			            if (eventState.wasCreated() && !conversation.isTransient())
			            	amfRespMessage.setHeader(WAS_LONG_RUNNING_CONVERSATION_CREATED, true);
			            
			            amfRespMessage.setHeader(CONVERSATION_ID, conversation.getId());
						
						amfRespMessage.setHeader(IS_LONG_RUNNING_CONVERSATION, !conversation.isTransient());
					}
				}
				finally {
					// Destroy the CDI context
				    if (((HttpGraniteContext)GraniteContext.getCurrentInstance()).getSession(false) != null) {
					    ConversationContext conversationContext = lookupConversationContext(beanManager);
					    if (conversationContext.isActive()) {
						    
						    @SuppressWarnings("unchecked")
						    Bean<ConversationManager> conversationManagerBean = (Bean<ConversationManager>)beanManager.getBeans(ConversationManager.class).iterator().next();
						    ConversationManager conversationManager = (ConversationManager)beanManager.getReference(conversationManagerBean, ConversationManager.class, beanManager.createCreationalContext(conversationManagerBean));
						    conversationManager.cleanupConversation();
					    }
				    }
				    
			        log.debug("ended request");
				}
			}
		}
		catch (Exception e) {
            log.error(e, "Exception while post processing the response message.");
            throw new ServiceException("Error while post processing the response message - " + e.getMessage());
		}
	}
	
	private ConversationContext lookupConversationContext(WeldManager beanManager) {
	    return beanManager.getServices().get(ContextLifecycle.class).getConversationContext();
	}
}
