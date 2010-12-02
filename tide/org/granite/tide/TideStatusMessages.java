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

package org.granite.tide;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author William DRAI
 */
public class TideStatusMessages {
    
	private static final List<TideMessage> EMPTY_MESSAGES = Collections.emptyList();
	private static final Map<String, List<TideMessage>> EMPTY_KEYED_MESSAGES = Collections.emptyMap();
    public static final TideStatusMessages EMPTY_STATUS_MESSAGES = new TideStatusMessages(EMPTY_MESSAGES, EMPTY_KEYED_MESSAGES);
    
	private List<TideMessage> messages;
	private Map<String, List<TideMessage>> keyedMessages;
	
    
    public TideStatusMessages(List<TideMessage> messages) {
        this.messages = messages;
        this.keyedMessages = EMPTY_KEYED_MESSAGES;
    }
    
    public TideStatusMessages(List<TideMessage> messages, Map<String, List<TideMessage>> keyedMessages) {
        this.messages = messages;
        this.keyedMessages = keyedMessages;
    }
    
    public List<TideMessage> getMessages() {
    	return messages;
    }
    
    public Map<String, List<TideMessage>> getKeyedMessages() {
    	return keyedMessages;
    }
}

