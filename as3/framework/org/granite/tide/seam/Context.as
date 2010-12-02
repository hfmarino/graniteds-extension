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

package org.granite.tide.seam { 

    import flash.events.Event;
    import flash.events.IEventDispatcher;
    import flash.utils.Dictionary;
    import flash.utils.flash_proxy;
    import flash.utils.getQualifiedClassName;
    
    import mx.collections.ArrayCollection;
    import mx.collections.ArrayList;
    import mx.collections.IList;
    import mx.collections.ItemResponder;
    import mx.collections.ListCollectionView;
    import mx.collections.Sort;
    import mx.controls.Alert;
    import mx.core.IUID;
    import mx.core.UIComponent;
    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;
    import mx.events.FlexEvent;
    import mx.events.PropertyChangeEvent;
    import mx.events.PropertyChangeEventKind;
    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.messaging.events.ChannelFaultEvent;
    import mx.messaging.messages.ErrorMessage;
    import mx.rpc.AbstractOperation;
    import mx.rpc.AsyncToken;
    import mx.rpc.remoting.mxml.RemoteObject;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;
    import mx.utils.ObjectProxy;
    import mx.utils.ObjectUtil;
    import mx.utils.object_proxy;
    
    import org.granite.collections.IPersistentCollection;
    import org.granite.events.SecurityEvent;
    import org.granite.meta;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Component;
    import org.granite.tide.IComponent;
    import org.granite.tide.IEntity;
    import org.granite.tide.IEntityManager;
    import org.granite.tide.IExpression;
    import org.granite.tide.IIdentity;
    import org.granite.tide.IInvocationCall;
    import org.granite.tide.IInvocationResult;
    import org.granite.tide.IPropertyHolder;
    import org.granite.tide.Tide;
    import org.granite.tide.TideMessage;
    import org.granite.tide.TideResponder;
    import org.granite.tide.impl.ComponentProperty;
    import org.granite.tide.invocation.InvocationCall;
    import org.granite.tide.invocation.InvocationResult;
    import org.granite.tide.invocation.ContextUpdate;
    import org.granite.tide.invocation.ContextResult;
    import org.granite.tide.invocation.ContextEvent;
    import org.granite.tide.invocation.ContextEventListener;
    import org.granite.tide.collections.PersistentCollection;
    import org.granite.tide.events.TideContextEvent;
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.seam.security.Identity;
    import org.granite.tide.seam.framework.StatusMessages;


    use namespace flash_proxy;
    use namespace object_proxy;
    use namespace meta;


    [Bindable]
    /**
     * 	The Context class is the implementation of a Tide context for Seam
     * 
     * 	@author William DRAI
     */ 
    public dynamic class Context extends BaseContext {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.seam.Context");

        private var _updates:ArrayCollection = new ArrayCollection();
        private var _pendingUpdates:ArrayCollection = new ArrayCollection();
        [ArrayElementType("org.granite.tide.invocation.ContextResult")]
        private var _results:Array = new Array();
        private var _lastResults:ArrayCollection = new ArrayCollection();



        public function Context(tide:Tide, parentContext:BaseContext = null) {
            super(tide, parentContext);
        }

		
		/**
		 * 	Current conversationId of the context
		 */
        public function get conversationId(): String {
            return contextId;
        }

		/**
		 * 	Updates the current conversationId of the context
		 * 
		 * 	@param conversationId the new conversationId
		 */
        public function set conversationId(conversationId:String): void {
            meta_setContextId(conversationId);
        }
        
        
		/**
		 * 	@private
		 * 	@return current list of updates that will be sent to the server
		 */
        public function get meta_updates():IList {
            return _updates;
        }
        
		/**
		 * 	@private
		 * 	@return current list of results that will be requested from the server
		 */
        public function get meta_results():Array {
            return _results;
        }
        
        
        /**
		 * 	@private
         *	Reinitialize the context
         * 
         *  @param force force complete destruction of context
         */
        public override function meta_clear(force:Boolean = false):void {
            _updates.removeAll();
            _pendingUpdates.removeAll();
            _results = new Array();
            _lastResults.removeAll();
            
            super.meta_clear(force);
            
            _updates.removeAll();
            _pendingUpdates.removeAll();
            _results = new Array();
            _lastResults.removeAll();
        }
        
        /**
		 * 	@private
         *  Add update to current context 
         *  Note: always move the update in last position in case the value can depend on previous updates
         * 
         *  @param componentName name of the component/context variable
         *  @param expr EL expression to evaluate
         *  @param value value to send to server
		 *  @param typed component name represents a typed component instance  
         */
        public override function meta_addUpdate(componentName:String, expr:String, value:*, typed:Boolean = false):void {
            if (!meta_tracking)
                return;

            if (_tide.getComponentRemoteSync(componentName) != Tide.SYNC_BIDIRECTIONAL)
                return;
            
            var val:Object = value;
            if (val is IPropertyHolder)
                val = IPropertyHolder(val).object;

            var found:Boolean = false;
            for (var i:int = 0; i < _updates.length; i++) {
                var u:ContextUpdate = _updates.getItemAt(i) as ContextUpdate;
                if (u.componentName == componentName && u.expression == expr) {
                    u.value = val;
                    if (i < _updates.length-1) {
                        found = false;
                        _updates.removeItemAt(i);    // Remove here to add it in last position
                        i--;
                    }
                    else
                        found = true;
                }
                else if (u.componentName == componentName && u.expression != null && (expr == null || u.expression.indexOf(expr + ".") == 0)) {
                    _updates.removeItemAt(i);
                    i--;
                }
                else if (u.componentName == componentName && expr != null && (u.expression == null || expr.indexOf(u.expression + ".") == 0))
                    found = true;
            }

            if (!found) {
                log.debug("add new update {0}.{1}", componentName, expr);
                _updates.addItem(new ContextUpdate(componentName, expr, val, _tide.getComponentScope(componentName)));
            }
        }

        /**
		 * 	@private
         *  Add result evaluator in current context
         * 
         *  @param componentName name of the component/context variable
         *  @param expr EL expression to evaluate
         * 
         *  @return true if the result was not already present in the current context 
         */
        public override function meta_addResult(componentName:String, expr:String):Boolean {
            if (!meta_tracking)
                return false;
                
            if (_tide.getComponentRemoteSync(componentName) == Tide.SYNC_NONE)
                return false;            
            
            // Check in existing results
            for each (var o:Object in _results) {
                var r:ContextResult = o as ContextResult;
                if (r.componentName == componentName && r.expression == expr)
                    return false;
            }
            
            // Check in last received results
            var e:String = componentName + (expr != null ? "." + expr : "");
            if (_lastResults.getItemIndex(e) >= 0)
                return false;
            
            log.debug("addResult {0}.{1}", componentName, expr);
            _results.push(new ContextResult(componentName, expr));
            return true;
        }
                
        
        /**
         * 	@private
         * 
         * 	Reset current call context and returns saved context
         * 
         *  @return saved call context
         */
        public override function meta_saveAndResetCallContext():Object {
        	var savedCallContext:Object = { 
        		updates: new ArrayCollection(_updates.toArray()), 
        		results: _results.concat(), 
        		pendingUpdates: new ArrayCollection(_pendingUpdates.toArray()),
        		lastResults: new ArrayCollection(_lastResults.toArray()) 
        	};
        	
            _updates.removeAll();
            _pendingUpdates.removeAll();
            _results = new Array();
            _lastResults.removeAll();
            
            return savedCallContext;
        }
        
        /**
         *	@private
         * 
         * 	Restore call context
         * 
         *  @param callContext object containing the current call context
         */ 
        public override function meta_restoreCallContext(callContext:Object):void {
        	_updates = callContext.updates as ArrayCollection;
        	_results = callContext.results as Array;
        	_pendingUpdates = callContext.pendingUpdates as ArrayCollection;
        	_lastResults = callContext.lastResults as ArrayCollection;
		}         	


        /**
		 * 	@private
         *  Logs the current call
         */ 
        private function meta_traceCall():void {
            log.debug("updates: {0}", toString(_updates));
            log.debug("results: {0}", toString(_results));
        }
        
        /**
		 * 	@private
         *  Calls a remote component
         * 	
         *  @param component the target component
         *  @op name of the called metho
         *  @arg method parameters
         * 
         *  @return the operation token
         */
        public override function meta_callComponent(component:IComponent, op:String, args:Array, withContext:Boolean = true):AsyncToken {
            log.debug("callComponent {0}.{1}", component.meta_name, op);
            meta_traceCall();
            
            var token:AsyncToken = super.meta_callComponent(component, op, args, withContext);

            if (withContext) {
                _pendingUpdates = new ArrayCollection(_updates.toArray());
                _updates.removeAll();
            }
            
            return token;
        }

        
        /**
		 * 	@private
         *  Calls the server login
         * 
         *  @param identity identity component
         *  @param username user name
         *  @param password user password
         *  @resultHandler result callback
         *  @faultHandler fault callback
         * 
         *  @return the operation token
         */
        public override function meta_login(identity:IIdentity, username:String, password:String, resultHandler:Function = null, faultHandler:Function = null):AsyncToken {
            meta_traceCall();
            
            // Keep only updates for identity component
            for (var i:int = 0; i < _updates.length; i++) {
                var u:ContextUpdate = _updates.getItemAt(i) as ContextUpdate;
                if (u.componentName != identity.meta_name) {
                    _updates.removeItemAt(i);
                    i--;
                }
            }
            
            var token:AsyncToken = super.meta_login(identity, username, password, resultHandler, faultHandler);
            
            _updates.removeAll();

            return token;
        }
        
        /**
		 * 	@private
         *  Calls the server login
         * 
         *  @param identity identity component
         *  @param username user name
         *  @param password user password
         *  @resultHandler result callback
         *  @faultHandler fault callback
         * 
         *  @return the operation token
         */
        public override function meta_isLoggedIn(identity:IIdentity, resultHandler:Function = null, faultHandler:Function = null):AsyncToken {
            log.debug("isLoggedIn");
            meta_traceCall();
            
            // Keep only updates for identity component
            for (var i:int = 0; i < _updates.length; i++) {
                var u:ContextUpdate = _updates.getItemAt(i) as ContextUpdate;
                if (u.componentName != identity.meta_name) {
                    _updates.removeItemAt(i);
                    i--;
                }
            }
            
            var token:AsyncToken = super.meta_isLoggedIn(identity, resultHandler, faultHandler);
            
            _updates.removeAll();

            return token;
        }


        /**
		 * 	@private
         *  Constructs a call object and prepares the remote operation
         * 
         *  @param operation current operation
         *  @param withContext send the current updates and results along with the operation
         * 
         *  @return the call object
         */
        public override function meta_prepareCall(operation:AbstractOperation, withContext:Boolean = true):IInvocationCall {
		    SeamOperation(operation).conversationId = conversationId;
    	    SeamOperation(operation).conversationPropagation = (contextId != null && !meta_isContextIdFromServer ? "join" : null);
    		SeamOperation(operation).firstCall = (withContext && _tide.firstCall);
    		SeamOperation(operation).firstConvCall = (withContext && meta_firstCall);
		    
		    var call:InvocationCall = null;
		    if (withContext) {
		        call = new InvocationCall(_tide.newListeners, _updates, _results);
		        _tide.newListeners.removeAll();
		    }
		    else
		        call = new InvocationCall();
		    return call;
        }
        

        /**
		 * 	@private
         *  Manages a remote call result
         * 
         *  @param componentName name of the target component
         *  @param operation name of the called operation
         *  @param ires invocation result object
         *  @param result real result object (may be included in the invocationResult)
         */
        public override function meta_result(componentName:String, operation:String, ires:IInvocationResult, result:Object, mergeWith:Object = null):void {
            meta_preResult();
            
            _pendingUpdates.removeAll();
            _lastResults.removeAll();
            
            var mergeExternal:Boolean = true;
            if (ires != null) {
                var invocationResult:InvocationResult = InvocationResult(ires);
                mergeExternal = invocationResult.merge;
				
				if (invocationResult.updates)
					meta_handleUpdates(null, invocationResult.updates);
                
				// Handle scope changes
				if (_tide.isComponentInEvent(componentName) && invocationResult.scope == Tide.SCOPE_SESSION && !meta_isGlobal()) {
					var instance:Object = this[componentName];
					this[componentName] = null;
					_tide.setComponentScope(componentName, Tide.SCOPE_SESSION);
					this[componentName] = instance;
				}
				
                _tide.setComponentScope(componentName, invocationResult.scope);
                _tide.setComponentRestrict(componentName, invocationResult.restrict ? Tide.RESTRICT_YES : Tide.RESTRICT_NO);
                
                meta_tracking = false;
                
                var resultMap:IList = invocationResult.results;
                if (resultMap) {
                    log.debug("result conversationId {0}", conversationId);
                    
                    // Order the results by container, i.e. 'person.contacts' has to be evaluated after 'person'
                    var rmap:ListCollectionView = new ListCollectionView(resultMap);
                    rmap.sort = new Sort();
                    rmap.sort.compareFunction = meta_compareResults;
                    rmap.refresh(); 
        
                    for (var k:int = 0; k < rmap.length; k++) {
                        var r:ContextUpdate = rmap.getItemAt(k) as ContextUpdate;
                        var val:Object = r.value;
        
                        log.debug("update expression {0}: {1}", r, BaseContext.toString(val));
                        _lastResults.addItem(r.componentName + (r.expression != null ? "." + r.expression : ""));
        
                        var compName:String = r.componentName;
                        if (val != null) {
                            if (_tide.getComponentRestrict(compName) == Tide.RESTRICT_UNKNOWN)
                                _tide.setComponentRestrict(compName, r.restrict ? Tide.RESTRICT_YES : Tide.RESTRICT_NO);
							
							if (_tide.getComponentScope(compName) == Tide.SCOPE_UNKNOWN)
	                            _tide.setComponentScope(compName, r.scope);
                        }
                        _tide.setComponentGlobal(compName, true);
                        
                        var obj:Object = meta_getInstanceNoProxy(compName);
                        var p:Array = r.expression != null ? r.expression.split(".") : [];
                        if (p.length > 1) {
                            for (var i:int = 0; i < p.length-1; i++)
                                obj = obj[p[i] as String];
                        }
                        else if (p.length == 0)
                            _tide.setComponentRemoteSync(compName, Tide.SYNC_BIDIRECTIONAL);
        
                        var previous:Object = null;
                        var propName:String = null;
                        if (p.length > 0) {
                            propName = p[p.length-1] as String
                                   
                            if (obj is IPropertyHolder)
                                previous = IPropertyHolder(obj).object[propName];
                            else if (obj != null)
                                previous = obj[propName];
                        }
                        else
                            previous = obj;
                        
                        // Don't merge with temporary properties
                        if (previous is ComponentProperty || previous is Component)
                            previous = null;
                        
                        var res:ContextResult = new ContextResult(r.componentName, r.expression);
        
                        if (!meta_isGlobal() && r.scope == Tide.SCOPE_SESSION)
                            val = _tide.getContext().meta_mergeExternal(val, previous, res);
                        else
                            val = meta_mergeExternal(val, previous, res);
    
                        if (propName != null) {
                            if (obj is IPropertyHolder) {
                                var evt:ResultEvent = new ResultEvent(ResultEvent.RESULT, false, true, val);
                                IPropertyHolder(obj).meta_propertyResultHandler(propName, evt);
                            }
                            else if (obj != null)
                                obj[propName] = val;
                        }
                        else
                            this[r.componentName] = val;
                    }
                }
            }
            
            // Merges final result object
            if (result) {
            	if (mergeExternal)
                	result = meta_mergeExternal(result, mergeWith);
                else
                	log.debug("skipped merge of remote result");
	            if (ires != null)
	                InvocationResult(ires).result = result;
	        }
            
            meta_tracking = true;
            
            this['statusMessages'].setFromServer(invocationResult);
            
            if (ires != null) {
                // Remove all received results from current results list
                var newResults:Array = new Array();
                for each (var cr:ContextResult in _results) {
                    var found:Boolean = false;
                    for each (var u:ContextUpdate in rmap) {
                        if (cr.matches(u.componentName, u.expression)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        newResults.push(cr);
                }
                _results = newResults;
				
            	// Dispatch received data update events
				if (InvocationResult(ires).updates)
					meta_handleUpdateEvents(InvocationResult(ires).updates);
                
                // Dispatch received context events
                var events:IList = invocationResult.events;
                for (var ie:uint = 0; ie < events.length; ie++) {
                    var event:ContextEvent = events.getItemAt(ie) as ContextEvent;
                    meta_internalRaiseEvent(event.eventType, event.params);
                }
            }
            
            super.meta_result(componentName, operation, ires, result, mergeWith);
            log.debug("result merged into local context");
        }


        /**
		 * 	@private
         *  Manages a remote call fault
         * 
         *  @param componentName name of the target component
         *  @param operation name of the called operation
         *  @param emsg error message
         */
        public override function meta_fault(componentName:String, operation:String, emsg:ErrorMessage):void {
//            for each (var update:ContextUpdate in _pendingUpdates)
//                _updates.addItem(update);
            _pendingUpdates.removeAll();
            _lastResults.removeAll();

            super.meta_fault(componentName, operation, emsg);

            if (emsg != null && emsg.extendedData)
            	this['statusMessages'].setFromServer(emsg.extendedData);
        }
        
        /**
		 * 	@private
         *  Clears entity cache
         */ 
        public override function meta_clearCache():void {
            super.meta_clearCache();            
            // Discard last results
            _lastResults.removeAll();
        }
        
        /**
		 * 	@private
         *  Comparator for expression evaluation ordering
         * 
         *  @param r1 expression 1
         *  @param r2 expression 2
         *  @param fields unused
         * 
         *  @return comparison value
         */
        private function meta_compareResults(r1:ContextUpdate, r2:ContextUpdate, fields:Array = null):int {
            if (r1.componentName != r2.componentName)
                return r1.componentName < r2.componentName ? -1 : 1;
            
            if (r1.expression == null)
                return r2.expression == null ? 0 : -1;
            
            if (r2.expression == null)
                return 1;
            
            if (r1.expression == r2.expression)
                return 0;
            
            if (r1.expression.indexOf(r2.expression) == 0)
                return 1;
            if (r2.expression.indexOf(r1.expression) == 0)
                return -1;
            
            return r1.expression < r2.expression ? -1 : 0; 
        }
        
        
        /**
		 * 	@private
         *  Implements managed entity functionality
         *  All entities delegate setters to this context method
         * 
         *  @param entity managed entity
         *  @param propName property name
         *  @param oldValue previous value
         *  @param newValue new value
         */ 
        public override function meta_setEntityProperty(entity:IEntity, propName:String, oldValue:*, newValue:*):void {
            super.meta_setEntityProperty(entity, propName, oldValue, newValue);

            meta_addUpdates(entity);
        }
        

        /**
		 * 	@private
         *  Implements managed entity functionality
         *  All entities delegate getters to this context method
         * 
         *  @param entity managed entity
         *  @param propName property name
         *  @param value previous value
         * 
         *  @return new value
         */ 
        public override function meta_getEntityProperty(entity:IEntity, propName:String, value:*):* {
            if (value is IEntity || value is IList || value is IPersistentCollection) {
                var ref:IExpression = meta_getReference(entity, false);
                if (ref)
                    meta_addResult(ref.componentName, ref.expression ? ref.expression + "." + propName : propName);
            }
            
            return super.meta_getEntityProperty(entity, propName, value);
        }
        
            
        /**
		 * 	@private
         *  Handles updates on managed collections
         * 
         *  @param event collection event
         */ 
        public override function meta_collectionChangeHandler(event:CollectionEvent):void {
            super.meta_collectionChangeHandler(event);
            
            if (event.target is IComponent)
            	return;

            if (event.kind == CollectionEventKind.ADD || event.kind == CollectionEventKind.REMOVE
				|| event.kind == CollectionEventKind.REPLACE || event.kind == CollectionEventKind.RESET)
                meta_addUpdates(event.target);
        }
            
        /**
		 * 	@private
         *  Handles updates on managed collections
         * 
         *  @param event collection event
         */ 
        public override function meta_entityCollectionChangeHandler(event:CollectionEvent):void {
            super.meta_entityCollectionChangeHandler(event);

            if (event.items && event.items.length > 0 && event.items[0] is IEntity)
                meta_addUpdates(event.target);
            else if (event.kind == CollectionEventKind.UPDATE && event.items && event.items.length > 0) {
                var pce:PropertyChangeEvent = event.items[0];
                if (pce.source is IEntity)
                    meta_addUpdates(event.target);
            }
        }
            
        /**
		 * 	@private
         *  Handles updates on managed maps
         * 
         *  @param event collection event
         */ 
        public override function meta_mapChangeHandler(event:CollectionEvent):void {
            super.meta_mapChangeHandler(event);

            if (event.kind == CollectionEventKind.ADD || event.kind == CollectionEventKind.REMOVE || event.kind == CollectionEventKind.RESET)
                meta_addUpdates(event.target);
        }
            
        /**
		 * 	@private
         *  Handles updates on managed collections
         * 
         *  @param event collection event
         */ 
        public override function meta_entityMapChangeHandler(event:CollectionEvent):void {
            super.meta_entityMapChangeHandler(event);

            if (event.items && event.items.length > 0 && event.items[0] is Array && event.items[0][1] is IEntity)
                meta_addUpdates(event.target);
            else if (event.kind == CollectionEventKind.UPDATE && event.items && event.items.length > 0) {
                var pce:PropertyChangeEvent = event.items[0];
                if (pce.source is IEntity)
                    meta_addUpdates(event.target);
            }
        }


        /**
		 * 	@private
         *  Recursively add updates to referencing objects of provided object
         *
         *  @param entity object
         */ 
        private function meta_addUpdates(entity:Object):void {
            if (!meta_tracking || meta_finished)
                return;
            
            var ref:IExpression = meta_getReference(entity);
            if (ref)
                meta_addUpdate(ref.componentName, ref.expression, meta_evaluate(ref));
        }
        
        /**
		 * 	@private
         *  Recursively add results for referencing objects of provided object
         *
         *  @param entity object
         * 
         *  @return a result has been added
         */ 
        private function meta_addResults(entity:Object):Boolean {
            if (!meta_tracking || meta_finished)
                return false;
            
            var ref:IExpression = meta_getReference(entity);
            if (ref)
                return meta_addResult(ref.componentName, ref.expression);
            return false;
        }
    }
}
