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

package org.granite.events {

    import flash.events.Event;


    /**
     *	Event dispatched on security errors.
     *  
     * 	@author Franck WOLFF
     */
    public class SecurityEvent extends Event {

        public static const ALL:String = "all";

        public static const INVALID_CREDENTIALS:String = "invalidCredentials";
        public static const NOT_LOGGED_IN:String = "notLoggedIn";
        public static const SESSION_EXPIRED:String = "sessionExpired";
        public static const ACCESS_DENIED:String = "accessDenied";

        public var message:String;

        public function SecurityEvent(type:String,
                                      message:String = null,
                                      bubbles:Boolean = false,
                                      cancelable:Boolean = false) {
            super(type, bubbles, cancelable);
            this.message = message;
        }

        override public function clone():Event {
            return new SecurityEvent(type, message, bubbles, cancelable);
        }
    }
}