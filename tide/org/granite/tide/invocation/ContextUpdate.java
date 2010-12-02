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


/**
 * @author William DRAI
 */
public class ContextUpdate implements Serializable {

    private static final long serialVersionUID = 1L;
    
    
    private String componentName;
    private String expression;
    private Object value;
    private int scope;
    private boolean restrict;
    
    
    public ContextUpdate() {
    }
    
    public ContextUpdate(String componentName, String expression, Object value, int scope, boolean restrict) {
        this.componentName = componentName;
        this.expression = expression;
        this.value = value;
        this.scope = scope;
        this.restrict = restrict;
    }

    public String getComponentName() {
        return componentName;
    }
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }
    
    public String getExpression() {
        return expression;
    }
    public void setExpression(String expression) {
        this.expression = expression;
    }

    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }
    
    public int getScope() {
        return scope;
    }
    public void setScope(int scope) {
        this.scope = scope;
    }
    
    public boolean getRestrict() {
        return restrict;
    }
    public void setRestrict(boolean restrict) {
        this.restrict = restrict;
    }

    
    @Override
    public String toString() {
        if (expression == null)
            return componentName + (scope == 1 ? " (SESSION)" : (scope == 2 ? " (CONVERSATION)" : "")) + (restrict ? " (Restricted)" :"");
        
        return componentName + "." + expression + (scope == 1 ? " (SESSION)" : (scope == 2 ? " (CONVERSATION)" : "")) + (restrict ? " (Restricted)" :"");
    }
}
