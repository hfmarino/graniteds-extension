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

package org.granite.tide.invocation;

import java.io.Serializable;
import java.util.Arrays;


/**
 * @author William DRAI
 */
public class ContextEvent implements Serializable {

    private static final long serialVersionUID = 1L;
    
    
    private String eventType;
    private Object[] params;
    
    
    public ContextEvent() {
    }
    
    public ContextEvent(String eventType, Object[] params) {
        this.eventType = eventType;
        this.params = params;
    }

    public String getEventType() {
        return eventType;
    }
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public Object[] getParams() {
        return params;
    }
    public void setParams(Object[] params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return eventType;
    }

    
    @Override
    public int hashCode() {
        return eventType.hashCode() + 31*Arrays.hashCode(params);
    }
    
    @Override
    public boolean equals(Object object) {
        if (object == null || !object.getClass().equals(ContextEvent.class))
            return false;
        
        ContextEvent event = (ContextEvent)object;
        if (!event.getEventType().equals(eventType))
            return false;
        
        return Arrays.equals(event.getParams(), params);
    }
}
