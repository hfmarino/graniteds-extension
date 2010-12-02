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

package org.granite.messaging.service.security;

import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.util.Map;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.granite.context.GraniteContext;
import org.granite.messaging.webapp.HttpGraniteContext;

import weblogic.servlet.security.ServletAuthentication;

/**
 * @author Franck WOLFF
 */
public class WebLogicSecurityService extends AbstractSecurityService {

    public WebLogicSecurityService() {
    }

    public void configure(Map<String, String> params) {
    }

    public void login(Object credentials) throws SecurityServiceException {
        String[] decoded = decodeBase64Credentials(credentials);

        HttpGraniteContext context = (HttpGraniteContext)GraniteContext.getCurrentInstance();
        HttpServletRequest httpRequest = context.getRequest();
        HttpServletResponse httpResponse = context.getResponse();

        int result = ServletAuthentication.FAILED_AUTHENTICATION;
        try {
        	result = ServletAuthentication.login(decoded[0], decoded[1], httpRequest, httpResponse);
        }
        catch (LoginException e) {
        }
        if (result != ServletAuthentication.AUTHENTICATED)
        	throw SecurityServiceException.newInvalidCredentialsException("Wrong username or password");
        
        // Make sure we have a valid HTTP session.
        httpRequest.getSession(true);
    }

    public Object authorize(AbstractSecurityContext context) throws Exception {

        startAuthorization(context);

        if (context.getDestination().isSecured()) {
            HttpGraniteContext graniteContext = (HttpGraniteContext)GraniteContext.getCurrentInstance();
            HttpServletRequest httpRequest = graniteContext.getRequest();

            Principal principal = httpRequest.getUserPrincipal();
            if (principal == null) {
                if (httpRequest.getRequestedSessionId() != null) {
                    HttpSession httpSession = httpRequest.getSession(false);
                    if (httpSession == null || httpRequest.getRequestedSessionId().equals(httpSession.getId()))
                        throw SecurityServiceException.newSessionExpiredException("Session expired");
                }
                throw SecurityServiceException.newNotLoggedInException("User not logged in");
            }
            
            boolean accessDenied = true;
            for (String role : context.getDestination().getRoles()) {
                if (httpRequest.isUserInRole(role)) {
                    accessDenied = false;
                    break;
                }
            }
            if (accessDenied)
                throw SecurityServiceException.newAccessDeniedException("User not in required role");
        }

        try {
            return endAuthorization(context);
        } catch (InvocationTargetException e) {
            for (Throwable t = e; t != null; t = t.getCause()) {
                // Don't create a dependency to javax.ejb in SecurityService...
                if (t instanceof SecurityException ||
                    "javax.ejb.EJBAccessException".equals(t.getClass().getName()))
                    throw SecurityServiceException.newAccessDeniedException(t.getMessage());
            }
            throw e;
        }
    }

    public void logout() throws SecurityServiceException {
        HttpGraniteContext graniteContext = (HttpGraniteContext)GraniteContext.getCurrentInstance();
        HttpServletRequest httpRequest = graniteContext.getRequest();
        
        // Make sure we invalidate current HTTP session.
        if (httpRequest.getSession(false) != null)
        	httpRequest.getSession().invalidate();

        ServletAuthentication.logout(httpRequest);
    }
}
