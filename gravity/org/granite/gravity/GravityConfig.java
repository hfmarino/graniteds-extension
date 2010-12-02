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

package org.granite.gravity;

import javax.servlet.ServletContext;

import org.granite.config.GraniteConfig;
import org.granite.config.GraniteConfigReloadListener;
import org.granite.util.XMap;

/**
 * @author Franck WOLFF
 */
public class GravityConfig implements GraniteConfigReloadListener {

	public static final String DEFAULT_GRAVITY_FACTORY = DefaultGravityFactory.class.getName();
	public static final long DEFAULT_CHANNEL_IDLE_TIMEOUT_MILLIS = 30 * 60000L;
	public static final long DEFAULT_LONG_POLLING_TIMEOUT_MILLIS = 20000L;
	public static final boolean DEFAULT_RETRY_ON_ERROR = true;
	public static final int DEFAULT_MAX_MESSAGES_QUEUED_PER_CHANNEL = Integer.MAX_VALUE;
	public static final long DEFAULT_RECONNECT_INTERVAL_MILLIS = 30000L;
	public static final int DEFAULT_RECONNECT_MAX_ATTEMPTS = 60;
	public static final int DEFAULT_CORE_POOL_SIZE = 5;
	public static final int DEFAULT_MAXIMUM_POOL_SIZE = 20;
	public static final long DEFAULT_KEEP_ALIVE_TIME_MILLIS = 10000L;
	public static final int DEFAULT_QUEUE_CAPACITY = Integer.MAX_VALUE;
	
    // General Gravity configuration.
	private String gravityFactory = DEFAULT_GRAVITY_FACTORY;

	// Channel configuration.
	private long channelIdleTimeoutMillis = DEFAULT_CHANNEL_IDLE_TIMEOUT_MILLIS;
    private long longPollingTimeoutMillis = DEFAULT_LONG_POLLING_TIMEOUT_MILLIS;
	private boolean retryOnError = DEFAULT_RETRY_ON_ERROR;
	private int maxMessagesQueuedPerChannel = DEFAULT_MAX_MESSAGES_QUEUED_PER_CHANNEL;

    // Client advices.
    private long reconnectIntervalMillis = DEFAULT_RECONNECT_INTERVAL_MILLIS;
    private int reconnectMaxAttempts = DEFAULT_RECONNECT_MAX_ATTEMPTS;
    
    // Free configuration options.
    private XMap extra = null;

    // Thread pool configuration.
    private int corePoolSize = DEFAULT_CORE_POOL_SIZE;
    private int maximumPoolSize = DEFAULT_MAXIMUM_POOL_SIZE;
    private long keepAliveTimeMillis = DEFAULT_KEEP_ALIVE_TIME_MILLIS;
    private int queueCapacity = DEFAULT_QUEUE_CAPACITY;
    
    // Container specific ChannelFactory
    private final ChannelFactory channelFactory;
	
	public GravityConfig(GraniteConfig graniteConfig, ChannelFactory channelFactory) {
		
		parseConfig(graniteConfig.getGravityConfig());
		
		this.channelFactory = channelFactory;
	}
	
	private void parseConfig(XMap config) {
		if (config != null) {
			gravityFactory = config.get("@factory", String.class, DEFAULT_GRAVITY_FACTORY);

			// Channel configuration.
			channelIdleTimeoutMillis = config.get("@channel-idle-timeout-millis", Long.TYPE, DEFAULT_CHANNEL_IDLE_TIMEOUT_MILLIS);
			longPollingTimeoutMillis = config.get("@long-polling-timeout-millis", Long.TYPE, DEFAULT_LONG_POLLING_TIMEOUT_MILLIS);
			retryOnError = config.get("@retry-on-error", Boolean.TYPE, DEFAULT_RETRY_ON_ERROR);
			maxMessagesQueuedPerChannel = config.get("@max-messages-queued-per-channel", Integer.TYPE, DEFAULT_MAX_MESSAGES_QUEUED_PER_CHANNEL);

			// Advices sent to clients.
			reconnectIntervalMillis = config.get("@reconnect-interval-millis", Long.TYPE, DEFAULT_RECONNECT_INTERVAL_MILLIS);
			reconnectMaxAttempts = config.get("@reconnect-max-attempts", Integer.TYPE, DEFAULT_RECONNECT_MAX_ATTEMPTS);
			
			// Free configuration options.
			extra = config.getOne("configuration");
			
			// Thread pool configuration.
			corePoolSize = config.get("thread-pool/@core-pool-size", Integer.TYPE, DEFAULT_CORE_POOL_SIZE);
			maximumPoolSize = config.get("thread-pool/@maximum-pool-size", Integer.TYPE, DEFAULT_MAXIMUM_POOL_SIZE);
			keepAliveTimeMillis = config.get("thread-pool/@keep-alive-time-millis", Long.TYPE, DEFAULT_KEEP_ALIVE_TIME_MILLIS);
			queueCapacity = config.get("thread-pool/@queue-capacity", Integer.TYPE, DEFAULT_QUEUE_CAPACITY);
		}
	}

	public void onReload(ServletContext context, GraniteConfig config) {
		parseConfig(config.getGravityConfig());
		GravityManager.reconfigure(context, this);
	}

	public String getGravityFactory() {
		return gravityFactory;
	}

	public long getChannelIdleTimeoutMillis() {
		return channelIdleTimeoutMillis;
	}
	public void setChannelIdleTimeoutMillis(long channelIdleTimeoutMillis) {
		this.channelIdleTimeoutMillis = channelIdleTimeoutMillis;
	}

	public long getLongPollingTimeoutMillis() {
		return longPollingTimeoutMillis;
	}
	public void setLongPollingTimeoutMillis(long longPollingTimeoutMillis) {
		this.longPollingTimeoutMillis = longPollingTimeoutMillis;
	}

	public boolean isRetryOnError() {
		return retryOnError;
	}
	public void setRetryOnError(boolean retryOnError) {
		this.retryOnError = retryOnError;
	}

	public int getMaxMessagesQueuedPerChannel() {
		return maxMessagesQueuedPerChannel;
	}
	public void setMaxMessagesQueuedPerChannel(int maxMessagesQueuedPerChannel) {
		this.maxMessagesQueuedPerChannel = maxMessagesQueuedPerChannel;
	}

	public long getReconnectIntervalMillis() {
		return reconnectIntervalMillis;
	}
	public void setReconnectIntervalMillis(long reconnectIntervalMillis) {
		this.reconnectIntervalMillis = reconnectIntervalMillis;
	}

	public int getReconnectMaxAttempts() {
		return reconnectMaxAttempts;
	}
	public void setReconnectMaxAttempts(int reconnectMaxAttempts) {
		this.reconnectMaxAttempts = reconnectMaxAttempts;
	}

	public XMap getExtra() {
		return (extra != null ? extra : XMap.EMPTY_XMAP);
	}

	public int getCorePoolSize() {
		return corePoolSize;
	}
	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public int getMaximumPoolSize() {
		return maximumPoolSize;
	}
	public void setMaximumPoolSize(int maximumPoolSize) {
		this.maximumPoolSize = maximumPoolSize;
	}

	public long getKeepAliveTimeMillis() {
		return keepAliveTimeMillis;
	}
	public void setKeepAliveTimeMillis(long keepAliveTimeMillis) {
		this.keepAliveTimeMillis = keepAliveTimeMillis;
	}

	public int getQueueCapacity() {
		return queueCapacity;
	}
	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}

	public ChannelFactory getChannelFactory() {
		return channelFactory;
	}
}
