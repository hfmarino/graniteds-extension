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

package org.granite.gravity.config;

import org.granite.config.flex.Adapter;
import org.granite.config.flex.Destination;
import org.granite.util.XMap;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Scope;


@Scope(ScopeType.APPLICATION)
public class AbstractActiveMQTopicDestination extends AbstractJmsTopicDestination {

    ///////////////////////////////////////////////////////////////////////////
    // Instance fields.
   
    private String brokerUrl = null;
    private boolean createBroker = true;
    private boolean waitForStart = false;
    private boolean durable = false;
    private String fileStoreRoot = null;

	
	public String getBrokerUrl() {
		return brokerUrl;
	}

	public void setBrokerUrl(String brokerUrl) {
		this.brokerUrl = brokerUrl;
	}

	public boolean isCreateBroker() {
		return createBroker;
	}

	public void setCreateBroker(boolean createBroker) {
		this.createBroker = createBroker;
	}

	public boolean isWaitForStart() {
		return waitForStart;
	}

	public void setWaitForStart(boolean waitForStart) {
		this.waitForStart = waitForStart;
	}

	public boolean isDurable() {
		return durable;
	}

	public void setDurable(boolean durable) {
		this.durable = durable;
	}

	public String getFileStoreRoot() {
		return fileStoreRoot;
	}

	public void setFileStoreRoot(String fileStoreRoot) {
		this.fileStoreRoot = fileStoreRoot;
	}

	
	@Override
	protected Adapter buildAdapter() {
		return new Adapter("activemq-adapter", "org.granite.gravity.adapters.ActiveMQServiceAdapter", new XMap());
	}
	
	@Override
	protected Destination buildDestination(Adapter adapter) {
		Destination destination = super.buildDestination(adapter);
		destination.getProperties().put("server", null);
    	destination.getProperties().put("server/broker-url", brokerUrl);
    	destination.getProperties().put("server/create-broker", String.valueOf(createBroker));
    	if (createBroker) {
    		destination.getProperties().put("server/wait-for-start", String.valueOf(waitForStart));
    		destination.getProperties().put("server/durable", String.valueOf(durable));
    		if (durable)
    			destination.getProperties().put("server/file-store-root", fileStoreRoot);
    	}
    	return destination;
	}
}
