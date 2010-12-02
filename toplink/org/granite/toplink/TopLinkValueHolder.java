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

package org.granite.toplink;

import oracle.toplink.essentials.indirection.WeavedAttributeValueHolderInterface;


/**
 * @author William DRAI
 */
public class TopLinkValueHolder implements WeavedAttributeValueHolderInterface {

    private static final long serialVersionUID = 1L;

    private Object value = null;
    
    
    public TopLinkValueHolder() {
    }

    public void setValue(Object value) {
        this.value = value;
    }
    
    public Object getValue() {
        return value;
    }

    public boolean isInstantiated() {
        return false;
    }

    public boolean isCoordinatedWithProperty() {
        return false;
    }

    public boolean isNewlyWeavedValueHolder() {
        return false;
    }

    public void setIsCoordinatedWithProperty(boolean value) {
    }

    public void setIsNewlyWeavedValueHolder(boolean value) {
    }
}
