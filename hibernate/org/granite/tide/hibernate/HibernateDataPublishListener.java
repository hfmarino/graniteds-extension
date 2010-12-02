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

package org.granite.tide.hibernate;

import org.granite.tide.data.DataContext;
import org.granite.tide.data.DataContext.EntityUpdateType;
import org.hibernate.HibernateException;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;


public class HibernateDataPublishListener implements PostInsertEventListener, PostDeleteEventListener, PostUpdateEventListener {

    private static final long serialVersionUID = 1L;
    

	public void onPostInsert(PostInsertEvent event) {
    	DataContext.addUpdate(EntityUpdateType.PERSIST, event.getEntity());
	}
	
	public void onPostDelete(PostDeleteEvent event) {
    	DataContext.addUpdate(EntityUpdateType.REMOVE, event.getEntity());
    }

	public void onPostUpdate(PostUpdateEvent event) throws HibernateException {
		DataContext.addUpdate(EntityUpdateType.UPDATE, event.getEntity());
	}
}
