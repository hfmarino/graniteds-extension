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

package org.granite.gravity.selector;

import java.util.HashMap;

import org.jboss.mq.selectors.Identifier;
import org.jboss.mq.selectors.Operator;
import org.jboss.mq.selectors.SelectorParser;

import flex.messaging.messages.Message;

/**
 * @author William DRAI
 */
public class JBossMQMessageSelector implements MessageSelector {

    private Object selector;
    private HashMap<String, Identifier> identifierMap = new HashMap<String, Identifier>();


    public JBossMQMessageSelector(String selector) {
        try {
            this.selector = SelectorParser.doParse(selector, identifierMap);
        }
        catch (Exception e) {
            throw new RuntimeException("JBossMQ SelectorParser error " + selector, e);
        }
    }

    public synchronized boolean accept(Message message) {
        try {
            for (Identifier identifier : identifierMap.values()) {
                Object value = message.getHeader(identifier.getName());
                identifier.setValue(value);
            }

            Object res = null;

            if (selector instanceof Identifier)
                res = ((Identifier)selector).getValue();
            else if (selector instanceof Operator)
                res = ((Operator)selector).apply();
            else
                res = selector;

            if (res == null)
                return false;

            if (!(res.getClass().equals(Boolean.class)))
               throw new Exception("Bad selector result type: " + res);

            return ((Boolean)res).booleanValue();
        }
        catch (Exception e) {
            throw new RuntimeException("JBossMQ selector accept error " + message, e);
        }
    }

}
