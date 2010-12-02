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

package org.granite.tide {

    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TideResultEvent;
    

    /**
     * 	ITideResponder defines an interface for extended remote call responders
     * 	TideResponder is sufficient for most use cases but implementing this allows a more typesafe implementation if needed
     * 	
     * 	@author William DRAI
     */
    public interface ITideResponder {
        
        /**
         * 	Handler for result event
         * 
         * 	@param event result event
         */
        function result(event:TideResultEvent):void;
        
        /**
         * 	Handler for fault event
         * 
         * 	@param event fault event
         */
        function fault(event:TideFaultEvent):void;
    }
}