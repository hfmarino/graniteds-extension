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

package org.granite.tide.cdi {

    import mx.rpc.remoting.mxml.Operation;
    import mx.core.mx_internal;
    use namespace mx_internal;
    import mx.rpc.AsyncToken;
    import mx.messaging.messages.IMessage;
    import mx.rpc.remoting.mxml.RemoteObject;
    import mx.rpc.events.ResultEvent;
    import org.granite.tide.Tide;
    import org.granite.tide.rpc.TideOperation;


    [ExcludeClass]
    /**
     * Class that passes the conversationId in the header. The conversation id is retrieved from
     * the passed in RemoteObject.
     *
     * @author Cameron Ingram, Venkat Danda
     */
    public class CdiOperation extends TideOperation {

        private var _conversationId:String;
        private var _isFirstCall:Boolean;
        private var _isFirstConvCall:Boolean;

        
        public function CdiOperation(tide:Tide, svc:RemoteObject = null, name:String = null):void {
            super(tide, svc, name);
        }

        public function set conversationId(cid:String):void {
            _conversationId = cid;
        }

        public function set firstCall(isFirstCall:Boolean):void {
            _isFirstCall = isFirstCall;
        }

        public function set firstConvCall(isFirstCall:Boolean):void {
            _isFirstConvCall = isFirstCall;
        }
        
        /*
          Overriden invoke so that the conversation id can get passed with every invokation.  For some reason
          RemoteObject will only pass the conversation id once, after that every invokation will not set the header.
          Seems like a bug to me...
        */
        mx_internal override function invoke(msg:IMessage, token:AsyncToken = null):AsyncToken {

            msg.headers[Tide.CONVERSATION_TAG] = _conversationId;
			if (_isFirstCall)
            	msg.headers[Tide.IS_FIRST_CALL_TAG] = "true";
            if (_isFirstConvCall)
            	msg.headers[Tide.IS_FIRST_CONVERSATION_CALL_TAG] = "true";

            return super.invoke(msg, token);
        }
    }
}