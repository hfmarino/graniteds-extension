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

package org.granite.messaging.webapp;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * @author Venkat DANDA
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class HttpServletRequestParamWrapper extends HttpServletRequestWrapper {
    
    private final Locale locale;
    private final Enumeration<Locale> locales;
    private final Map requestParams;
    private final Map requestAttributes;

    /**
     * @param request
     */
    public HttpServletRequestParamWrapper(HttpServletRequest request) {
        super(request);
        this.locale = request.getLocale();
        this.locales = request.getLocales();
        this.requestParams = new HashMap(request.getParameterMap());
        this.requestAttributes = new HashMap();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
     */
    @Override
    public String getParameter(String name) {
        String retValue = null;
        String[] paramValues = getParameterValues(name);
        if (paramValues != null && paramValues.length > 0) {
            retValue = paramValues[0];
        }
        return retValue;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequestWrapper#getParameterMap()
     */
    @Override
    public Map getParameterMap() {
        return Collections.unmodifiableMap(requestParams);
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequestWrapper#getParameterNames()
     */
    @Override
    public Enumeration getParameterNames() {
        return Collections.enumeration(requestParams.keySet());
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequestWrapper#getParameterValues(java.lang.String)
     */
    @Override
    public String[] getParameterValues(String name) {
        String[] retValues = null;
        String[] tmpValues = (String[]) requestParams.get(name);
        if (tmpValues != null) {
            retValues = new String[tmpValues.length];
            System.arraycopy(tmpValues, 0, retValues, 0, tmpValues.length);
        }
        return retValues;
    }

    /**
     * New method to set the parameter value.
     * @param name
     * @param value
     */
    public void setParameter(String name, String value) {
        String[] param = { value };
        setParameter(name, param);
    }

    /**
     * New method to set the parameter with multiple values.
     * @param name
     * @param values
     */
    public void setParameter(String name, String[] values) {
        requestParams.put(name, values);
    }

	@Override
	public Object getAttribute(String name) {
		return requestAttributes.get(name);
	}

	@Override
	public Enumeration getAttributeNames() {
		return Collections.enumeration(requestAttributes.keySet());
	}

	@Override
	public void removeAttribute(String name) {
		requestAttributes.remove(name);
	}

	@Override
	public void setAttribute(String name, Object o) {
		requestAttributes.put(name, o);
	}
	
	@Override
	public Locale getLocale() {
	    return locale;
	}
	
	@Override
	public Enumeration getLocales() {
	    return locales;
	}
}
