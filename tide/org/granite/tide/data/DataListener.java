/*
  GRANITE DATA SERVICES
  Copyright (C) 2007 ADEQUATE SYSTEMS SARL

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

package org.granite.tide.data;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;


public class DataListener {

    private static final long serialVersionUID = 1L;
    
    
    @PostLoad
    public void onPostLoad(Object entity) {
    	DataMergeContext.addLoadedEntity(entity);
    }
    
    @PostPersist
    public void onPostPersist(Object entity) {
    	DataMergeContext.addLoadedEntity(entity);
    }
    
    @PostUpdate
    public void onPostUpdate(Object entity) {
    	DataMergeContext.addLoadedEntity(entity);
    }
}
