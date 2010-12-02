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
    
    import org.granite.tide.BaseContext;


    /**
     * 	Event that is provided to Tide result handlers and that holds the result object.
     * 
     * 	@author William DRAI
     */
    public class TideResultEvent extends TideEvent {
        
        public static const RESULT:String = "result";
        
        
        public var asyncToken:AsyncToken;
        public var result:Object;

        public function TideResultEvent(type:String,
                                      context:BaseContext,
                                      bubbles:Boolean = false,
                                      cancelable:Boolean = false,
                                      asyncToken:AsyncToken = null,
                                      result:Object = null) {
            super(type, context, bubbles, cancelable);
            this.asyncToken = asyncToken;
            this.result = result;
        }

        override public function clone():Event {
            return new TideResultEvent(type, context, bubbles, cancelable, asyncToken, result);
        }
    }
}
