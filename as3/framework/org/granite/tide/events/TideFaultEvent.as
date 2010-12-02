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

package org.granite.tide.events {

    import flash.events.Event;
    import mx.rpc.AsyncToken;
    import mx.rpc.Fault;

    import org.granite.tide.BaseContext;
    

    /**
     * 	Event that is provided to Tide fault handlers and that holds the fault object.
     * 
     * 	@author William DRAI
     */
    public class TideFaultEvent extends TideEvent {
        
        public static const FAULT:String = "fault";

        public var asyncToken:AsyncToken;
        public var fault:Fault;

        public function TideFaultEvent(type:String,
                                      context:BaseContext,
                                      bubbles:Boolean = false,
                                      cancelable:Boolean = false,
                                      asyncToken:AsyncToken = null,
                                      fault:Fault = null) {
            super(type, context, bubbles, cancelable);
            this.asyncToken = asyncToken;
            this.fault = fault;
        }

        override public function clone():Event {
            return new TideFaultEvent(type, context, bubbles, cancelable, asyncToken, fault);
        }
    }
}
