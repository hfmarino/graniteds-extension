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
	import flash.events.EventDispatcher;
	import flash.utils.getQualifiedClassName;
    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.messaging.messages.AsyncMessage;
    import mx.messaging.events.MessageAckEvent;
    import mx.messaging.events.MessageEvent;
    import mx.utils.ObjectUtil;
    import org.granite.reflect.Type;
    import org.granite.gravity.Consumer;
    import org.granite.gravity.Producer;
    import org.granite.util.ClassUtil;
    import org.granite.tide.BaseContext;
	import org.granite.tide.IComponent;
	import org.granite.tide.service.IServiceInitializer;
    

	[Bindable]
    /**
     * 	The EventRouter component can be bound to a JMS topic and send and receive events to the server.<br/>
     *  This component encapsulates a Gravity Consumer and a Producer.<br/>
     * 
     * 	@author William DRAI
     */
	public class EventRouter extends EventDispatcher implements IComponent, IEventInterceptor {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.data.DataObserver");
		
		private var _consumer:Consumer = null;
		private var _producer:Producer = null;
		
		private var _name:String;
		private var _context:BaseContext;
		
		
		public function get meta_name():String {
			return _consumer.destination;
		}
		
		public function meta_init(componentName:String, context:BaseContext):void {
			if (!context.meta_isGlobal())
				throw new Error("Cannot setup EventRouter on conversation context");
			
		    log.debug("init EventRouter {0}", componentName);
			_context = context;
	        _consumer = new Consumer();
	        _producer = new Producer();
	        var serviceInitializer:IServiceInitializer = IServiceInitializer(context.byType(IServiceInitializer));
	        if (serviceInitializer != null) {
	        	serviceInitializer.initialize(_consumer);
	        	serviceInitializer.initialize(_producer);
	        }
	        
	        _consumer.destination = componentName;
	        _consumer.topic = "tideEventTopic";
	        
	        _producer.destination = componentName;
	        _producer.topic = "tideEventTopic";
		}
		
		public function meta_clear():void {
			if (_consumer.subscribed)
				unsubscribe();
		}
		
		
		/**
		 * 	Subscribe the topic
		 */
		public function subscribe():void {
		    _consumer.subscribe();
			_consumer.addEventListener(MessageAckEvent.ACKNOWLEDGE, subscribeHandler);
		    _consumer.addEventListener(MessageEvent.MESSAGE, messageHandler);
		}
		
		public function subscribeHandler(event:Event):void {
			log.info("destination {0} subscribed", meta_name);
			_consumer.removeEventListener(MessageAckEvent.ACKNOWLEDGE, subscribeHandler);
			dispatchEvent(new TideSubscriptionEvent(TideSubscriptionEvent.TOPIC_SUBSCRIBED));
		}

		/**
		 *  Unsubscribe the topic
		 */
		public function unsubscribe():void {
		    _consumer.addEventListener(MessageAckEvent.ACKNOWLEDGE, unsubscribeHandler);
		    _context.meta_tide.checkWaitForLogout();
		    _consumer.unsubscribe();
		}
		
		private function unsubscribeHandler(event:Event):void {
			log.info("destination {0} unsubscribed", meta_name);
		    _consumer.removeEventListener(MessageAckEvent.ACKNOWLEDGE, unsubscribeHandler);
		    _consumer.disconnect();
		    _context.meta_tide.tryLogout();
			dispatchEvent(new TideSubscriptionEvent(TideSubscriptionEvent.TOPIC_UNSUBSCRIBED));
		}


		/**
		 * 	Message handler that dispatches the event received from the JMS topic<br/>
		 *  Could be overriden to provide custom behaviour.
		 * 
		 *  @param event message event from the Consumer
		 */
        protected function messageHandler(messageEvent:MessageEvent):void {
            log.debug("destination {0} message received {1}", meta_name, messageEvent.toString());
            
            var event:AbstractTideEvent = messageEvent.message.body as AbstractTideEvent;
            if (event) {
            	event.fromRemote = true;
            	_context.dispatchEvent(event);
            }
        }
        
        
        public function beforeDispatch(contextEvent:TideContextEvent):void {
        }
        
		/**
		 * 	Event interceptor that sends serialization-enabled events to the JMS topic<br/>
		 *  Could be overriden to provide custom behaviour.
		 * 
		 *  @param event client-side event
		 */
        public function afterDispatch(contextEvent:TideContextEvent):void {
        	if (contextEvent.params == null || contextEvent.params.length == 0 || !(contextEvent.params[0] is AbstractTideEvent))
        		return;
        	
        	var event:AbstractTideEvent = AbstractTideEvent(contextEvent.params[0]);
        	if (event.fromRemote)
        		return;
        	        	
        	var alias:String = Type.forInstance(event).alias;
        	if (alias != null) {
            	log.debug("routing event {0} to destination {1}", event.toString(), meta_name);
            
            	var message:AsyncMessage = new AsyncMessage();
				message.body = ObjectUtil.copy(event);
				_producer.send(message);
			}            
        }
	}
}
