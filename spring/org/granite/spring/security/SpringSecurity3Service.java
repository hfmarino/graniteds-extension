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

package org.granite.spring.security;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.service.security.AbstractSecurityContext;
import org.granite.messaging.service.security.AbstractSecurityService;
import org.granite.messaging.service.security.SecurityServiceException;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.context.support.WebApplicationContextUtils;


/**
 * @author Bouiaw
 */
public class SpringSecurity3Service extends AbstractSecurityService {
        
	private static final Logger log = Logger.getLogger(SpringSecurity3Service.class);
	
    private static final String FILTER_APPLIED = "__spring_security_scpf_applied";
	
	private AuthenticationManager authenticationManager = null;
	private SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
	private AbstractSpringSecurity3Interceptor securityInterceptor = null;
	private String authenticationManagerBeanName = null;
	private Method getRequest = null;
	private Method getResponse = null;
    
	
	public SpringSecurity3Service() {
		log.debug("Starting Spring 3 Security Service!");
		try {
	    	getRequest = HttpRequestResponseHolder.class.getDeclaredMethod("getRequest");
	    	getRequest.setAccessible(true);
	    	getResponse = HttpRequestResponseHolder.class.getDeclaredMethod("getResponse");
	    	getResponse.setAccessible(true);
		}
		catch (Exception e) {
			throw new RuntimeException("Could not get methods from HttpRequestResponseHolder", e);
		}
    }
	
	
	public void setAuthenticationManager(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}
	
	public void setSecurityContextRepository(SecurityContextRepository securityContextRepository) {
		this.securityContextRepository = securityContextRepository;
	}
	
	public void setSecurityInterceptor(AbstractSpringSecurity3Interceptor securityInterceptor) {
		this.securityInterceptor = securityInterceptor;
	}

    public void configure(Map<String, String> params) {
        log.debug("Configuring with parameters %s: ", params);
        if (params.containsKey("authentication-manager-bean-name"))
        	authenticationManagerBeanName = params.get("authentication-manager-bean-name");
    }
    
    public void login(Object credentials) {
        List<String> decodedCredentials = Arrays.asList(decodeBase64Credentials(credentials));

        HttpGraniteContext graniteContext = (HttpGraniteContext)GraniteContext.getCurrentInstance();
        HttpServletRequest httpRequest = graniteContext.getRequest();

        String user = decodedCredentials.get(0);
        String password = decodedCredentials.get(1);
        Authentication auth = new UsernamePasswordAuthenticationToken(user, password);

        ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(
            httpRequest.getSession().getServletContext()
        );
        if (ctx != null) {
        	lookupAuthenticationManager(ctx, authenticationManagerBeanName);
            
            try {
                Authentication authentication = authenticationManager.authenticate(auth);
                
                HttpRequestResponseHolder holder = new HttpRequestResponseHolder(graniteContext.getRequest(), graniteContext.getResponse());
    	        SecurityContext securityContext = securityContextRepository.loadContext(holder);
    	        securityContext.setAuthentication(authentication);
    	        SecurityContextHolder.setContext(securityContext);
	            try {
	            	securityContextRepository.saveContext(securityContext, (HttpServletRequest)getRequest.invoke(holder), (HttpServletResponse)getResponse.invoke(holder));
	            }
	            catch (Exception e) {
	            	log.error(e, "Could not save context after authentication");
	            }
            } 
            catch (BadCredentialsException e) {
                throw SecurityServiceException.newInvalidCredentialsException(e.getMessage());
            }
        }

        log.debug("Logged In!");
    }
    
    public void lookupAuthenticationManager(ApplicationContext ctx, String authenticationManagerBeanName) throws SecurityServiceException {
    	if (this.authenticationManager != null)
    		return;
    	
    	@SuppressWarnings("unchecked")
    	Map<String, AuthenticationManager> authManagers = BeanFactoryUtils.beansOfTypeIncludingAncestors(ctx, AuthenticationManager.class);
    	
        if (authenticationManagerBeanName != null) {
        	this.authenticationManager = authManagers.get(authenticationManagerBeanName);
        	if (authenticationManager == null) {
        		log.error("AuthenticationManager bean not found " + authenticationManagerBeanName);
        		throw SecurityServiceException.newInvalidCredentialsException("Authentication failed");
        	}
        	return;
        }
        else if (authManagers.size() > 1) {
        	log.error("More than one AuthenticationManager beans found, specify which one to use in Spring config <graniteds:security-service authentication-manager='myAuthManager'/> or in granite-config.xml <security type='org.granite.spring.security.SpringSecurity3Service'><param name='authentication-manager-bean-name' value='myAuthManager'/></security>");
    		throw SecurityServiceException.newInvalidCredentialsException("Authentication failed");
        }
        
    	this.authenticationManager = authManagers.values().iterator().next();
    }

    
    public Object authorize(AbstractSecurityContext context) throws Exception {
        log.debug("Authorize: %s", context);
        log.debug("Is %s secured? %b", context.getDestination().getId(), context.getDestination().isSecured());

        startAuthorization(context);
        
        HttpGraniteContext graniteContext = (HttpGraniteContext)GraniteContext.getCurrentInstance();
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        HttpRequestResponseHolder holder = null;
        
        if (graniteContext.getRequest().getAttribute(FILTER_APPLIED) == null) {
	        holder = new HttpRequestResponseHolder(graniteContext.getRequest(), graniteContext.getResponse());
	        SecurityContext contextBeforeChainExecution = securityContextRepository.loadContext(holder);
		    SecurityContextHolder.setContext(contextBeforeChainExecution);
		    if (isAuthenticated(authentication))
		    	contextBeforeChainExecution.setAuthentication(authentication);
		    else
		    	authentication = contextBeforeChainExecution.getAuthentication();
        }
        
        if (context.getDestination().isSecured()) {
            if (!isAuthenticated(authentication) || authentication instanceof AnonymousAuthenticationToken) {
                log.debug("Is not authenticated!");
                throw SecurityServiceException.newNotLoggedInException("User not logged in");
            }
            if (!userCanAccessService(context, authentication)) { 
                log.debug("Access denied for: %s", authentication.getName());
                throw SecurityServiceException.newAccessDeniedException("User not in required role");
            }
        }

        try {
        	Object returnedObject = securityInterceptor != null 
        		? securityInterceptor.invoke(context)
        		: endAuthorization(context);
            
            return returnedObject;
        }
        catch (AccessDeniedException e) {
        	throw SecurityServiceException.newAccessDeniedException(e.getMessage());
        }
        catch (InvocationTargetException e) {
            handleAuthorizationExceptions(e);
            throw e;
        }
        finally {
            if (graniteContext.getRequest().getAttribute(FILTER_APPLIED) == null) {
	            SecurityContext contextAfterChainExecution = SecurityContextHolder.getContext();
	            SecurityContextHolder.clearContext();
	            try {
	            	securityContextRepository.saveContext(contextAfterChainExecution, (HttpServletRequest)getRequest.invoke(holder), (HttpServletResponse)getResponse.invoke(holder));
	            }
	            catch (Exception e) {
	            	log.error(e, "Could not extract wrapped context from holder");
	            }
            }
        }
    }

    public void logout() {
    	HttpGraniteContext context = (HttpGraniteContext)GraniteContext.getCurrentInstance();
    	HttpSession session = context.getSession(false);
    	if (session != null && securityContextRepository.containsContext(context.getRequest()))    		
    		session.invalidate();
        
    	SecurityContextHolder.clearContext();
    }

    protected boolean isUserInRole(Authentication authentication, String role) {
        for (GrantedAuthority ga : authentication.getAuthorities()) {
            if (ga.getAuthority().matches(role))
                return true;
        }
        return false;
    }

    protected boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }

    protected boolean userCanAccessService(AbstractSecurityContext context, Authentication authentication) {
        log.debug("Is authenticated as: %s", authentication.getName());

        for (String role : context.getDestination().getRoles()) {
            if (isUserInRole(authentication, role)) {
                log.debug("Allowed access to %s in role %s", authentication.getName(), role);
                return true;
            }
            log.debug("Access denied for %s not in role %s", authentication.getName(), role);
        }
        return false;
    }

    protected void handleAuthorizationExceptions(InvocationTargetException e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            // Don't create a dependency to javax.ejb in SecurityService...
            if (t instanceof SecurityException ||
                t instanceof AccessDeniedException ||
                "javax.ejb.EJBAccessException".equals(t.getClass().getName()))
                throw SecurityServiceException.newAccessDeniedException(t.getMessage());
        }
    }

}
