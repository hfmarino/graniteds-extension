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

package org.granite.tide.data {
    
    import flash.display.DisplayObject;
    import flash.events.Event;
    import flash.events.EventDispatcher;
    import flash.events.IEventDispatcher;
    import flash.utils.Dictionary;
    import flash.utils.ByteArray;
    import flash.utils.flash_proxy;
    import flash.utils.getQualifiedClassName;
    import flash.utils.getQualifiedSuperclassName;
    
    import mx.binding.utils.BindingUtils;
    import mx.binding.utils.ChangeWatcher;
    import mx.collections.ArrayCollection;
    import mx.collections.ArrayList;
    import mx.collections.ICollectionView;
    import mx.collections.IList;
    import mx.collections.ItemResponder;
    import mx.collections.ListCollectionView;
    import mx.controls.Alert;
    import mx.core.Application;
    import mx.core.IUID;
    import mx.core.IUIComponent;
    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;
    import mx.events.FlexEvent;
    import mx.events.PropertyChangeEvent;
    import mx.events.PropertyChangeEventKind;
    import mx.events.ValidationResultEvent;
    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.messaging.events.ChannelFaultEvent;
    import mx.messaging.messages.ErrorMessage;
    import mx.rpc.remoting.mxml.RemoteObject;
    import mx.rpc.AbstractOperation;
    import mx.rpc.AsyncToken;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;
    import mx.utils.ObjectProxy;
    import mx.utils.ObjectUtil;
    import mx.utils.UIDUtil;
    import mx.utils.object_proxy;
    import mx.validators.ValidationResult;
    import mx.data.IManaged;
    import mx.data.utils.Managed;
    
    import org.granite.collections.BasicMap;
    import org.granite.collections.IMap;
    import org.granite.collections.IPersistentCollection;
    import org.granite.collections.UIDWeakSet;
    import org.granite.events.SecurityEvent;
    import org.granite.meta;
    import org.granite.util.Enum;
    import org.granite.math.BigNumber;
	import org.granite.reflect.Type;
    import org.granite.tide.BaseContext;
    import org.granite.tide.IContextManager;
    import org.granite.tide.Tide;
    import org.granite.tide.IEntity;
    import org.granite.tide.IEntityManager;
    import org.granite.tide.IExpression;
    import org.granite.tide.EntityDescriptor;
    import org.granite.tide.IPropertyHolder;
    import org.granite.tide.collections.PersistentCollection;
    import org.granite.tide.collections.PersistentMap;
    import org.granite.tide.events.IConversationEvent;
    import org.granite.tide.events.TideEvent;
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.data.events.TideDataConflictsEvent;


    use namespace flash_proxy;
    use namespace object_proxy;
    use namespace meta;


	/**
	 * 	PersistenceContext is the base implementation of the entity container context
	 * 
     * 	@author William DRAI
	 */
    public class EntityManager {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.data.EntityManager");
    
    	private var _context:BaseContext;
    	private var _contextManager:IContextManager;
        
        private var _externalData:Boolean = false;
        private var _sourceContext:BaseContext = null;
        private var _entityCache:Dictionary = null;
        private var _mergeConflicts:Conflicts = null;
        private var _uninitializeAllowed:Boolean = true;
        private var _versionChangeCache:Dictionary = null;
        private var _entitiesByUID:UIDWeakSet = new UIDWeakSet();
        private var _entityReferences:Dictionary = new Dictionary(true);
        private var _dirtyCheckContext:DirtyCheckContext = null;
        private var _collectionListeners:Dictionary = new Dictionary(true);
        public var resolvingConflict:Boolean = false;
        


        public function EntityManager(context:BaseContext, contextManager:IContextManager) {
            super();
            _context = context;
            _contextManager = contextManager;
            _dirtyCheckContext = new DirtyCheckContext(_context);
        }
        
        
        /**
         * 	@private
         *  Clear the current context
         *  Destroys all components/context variables
         * 
         *  @param force force complete destruction of context (all event listeners...), used for testing
         */ 
        public function clear():void {
            for each (var e:Object in _entitiesByUID.data) {
            	if (e is IEntity)
            		Managed.setEntityManager(IEntity(e), null);
            }
        	_entitiesByUID = new UIDWeakSet();
        	_entityReferences = new Dictionary(true);
            _dirtyCheckContext.clear();
            
            for (var obj:Object in _collectionListeners) {
            	switch (_collectionListeners[obj]) {
            	case "entityCollection":
            		IEventDispatcher(obj).removeEventListener(CollectionEvent.COLLECTION_CHANGE, entityCollectionChangeHandler);
            		break;
            	case "collection":
                    IEventDispatcher(obj).removeEventListener(CollectionEvent.COLLECTION_CHANGE, _context.meta_collectionChangeHandler);
                    break;
            	case "entityMap":
            		IEventDispatcher(obj).removeEventListener(CollectionEvent.COLLECTION_CHANGE, entityMapChangeHandler);
            		break;
            	case "map":
                    IEventDispatcher(obj).removeEventListener(CollectionEvent.COLLECTION_CHANGE, _context.meta_mapChangeHandler);
                    break;
                }
            }
            _collectionListeners = new Dictionary(true);
        }
                
        /**
         * 	@private
         *  Clears entity cache
         */ 
        public function clearCache():void {
            _entityCache = null;
            _mergeConflicts = null;
            resolvingConflict = false;
            _versionChangeCache = null;
            _uninitializeAllowed = true;
        }
        
        /**
         *	@private 	
         *  'threadlocal' indicating that incoming data does not come from the current session 
         *  
		 * 	@param externalData external data
         */ 
        public function set externalData(externalData:Boolean):void {
            _externalData = externalData;
        }
        
        /**
         *	@private 	
         *  'threadlocal' indicating that incoming data comes from another context 
         *  
		 * 	@param sourceContext source context of incoming data
         */ 
        public function set sourceContext(sourceContext:BaseContext):void {
            _sourceContext = sourceContext;
        }
        
        /**
         *	@private 	
         *  'threadlocal' indicating that incoming data comes from another context 
         *  
		 * 	@return source context of incoming data
         */ 
        public function get sourceContext():BaseContext {
            return _sourceContext;
        }
        
        
        /**
         * 	@private
         *	Allow uninitialize of persistent collections
         *
         *  @param allowed allow uninitialize of collections
         */
        public function set uninitializeAllowed(allowed:Boolean):void {
        	_uninitializeAllowed = allowed;
        }
        
        
        /**
         *	Entity manager is dirty when any entity/collection/map has been modified
         *
         *  @return is dirty
         */
        public function get dirty():Boolean {
            return _dirtyCheckContext.dirty;
        }
        
        
        /**
         *	List of conflicts detected during last merge operation
         * 
         *  @return conflicts list 
         */
        public function get mergeConflicts():Conflicts {
        	return _mergeConflicts;
        }
		
		
		/**
		 *  @private
		 *  Attach an entity to this context
		 * 
		 *  @param entity an entity
		 *  @param putInCache put entity in cache
		 */
		public function attachEntity(entity:IEntity, putInCache:Boolean = true):void {
			var em:IEntityManager = Managed.getEntityManager(entity);
			if (em != null && em !== _context && !Object(em).meta_finished) {
			 	throw new Error("The entity instance " + BaseContext.toString(entity) 
			 		+ " cannot be attached to two contexts (current: " + (Object(em).meta_isGlobal() ? "global" : Object(em).contextId) 
			 		+ ", new: " + (_context.meta_isGlobal() ? "global" : _context.contextId) + ")");
			}
			
			Managed.setEntityManager(entity, _context);
			if (putInCache)
				_entitiesByUID.put(entity);			
		}
        
        /**
         *	@private
         *  Internal implementation of object attach
         * 
         *  @param object object
         *  @param cache internal cache to avoid graph loops
         */ 
        public function attach(object:Object, cache:Dictionary):void {
            if (ObjectUtil.isSimple(object))
            	return;
            
            if (cache[object] != null)
                return;
            cache[object] = object;
            
            if (object is IEntity)
                attachEntity(IEntity(object));
            
            var cinfo:Object = ObjectUtil.getClassInfo(object, null, { includeTransient: false });
            for each (var p:String in cinfo.properties) {
                var val:Object = object[p];
                
                if (!ObjectUtil.isSimple(val)) {
                	attach(val, cache);
                }
                else if (val is IList && !(val is IPersistentCollection && !IPersistentCollection(val).isInitialized())) {
                    var coll:IList = IList(val);
                    for each (var o:Object in coll)
                    	attach(o, cache);
                }
                else if (val is IMap && !(val is IPersistentCollection && !IPersistentCollection(val).isInitialized())) {
                    var map:IMap = IMap(val);
                    for each (var key:Object in map.keySet) {
                        var value:Object = map.get(key);
                        attach(key, cache);
                        attach(value, cache);
                    }
                }
            }
        }
        
        
        /** 
         *  @private 
         * 	Retrives an entity in the cache from its uid
         *   
         *  @param obj an entity
         *  @param nullIfAbsent return null if entity not cached in context
         */
        public function getCachedObject(object:Object, nullIfAbsent:Boolean = false):Object {
        	if (object is IEntity) {
        		var entity:Object = _entitiesByUID.get(getQualifiedClassName(object) + ":" + IUID(object).uid);
        		if (entity)
        			return entity;
        		if (nullIfAbsent)
        			return null;
        	}
        	return object;
        }
        
        /** 
         *  @private 
         * 	Retrives the owner entity of the provided object (collection/map/entity)
         *   
         *  @param obj an entity
         */
        public function getOwnerEntity(object:Object):Object {
            var refs:Array = _entityReferences[object];
            if (!refs)
                return null;
            
            for (var i:int = 0; i < refs.length; i++) {
                if (refs[i] is String)
                    return _entitiesByUID.get(refs[i] as String);
            }
            return null;
        }
        
        
        /**
         *  @private
         *  Retrieves context expression path for the specified entity (internal implementation)
         *   
         *  @param obj an entity
         *  @param recurse should recurse until 'real' context path, otherwise object reference can be returned
         *  @param cache graph visitor cache
         *  @return the path from the entity context (or null is no path found)
         */
        public function getReference(obj:Object, recurse:Boolean, cache:Dictionary = null):IExpression {
        	if (cache) {
	            if (cache[obj] != null)    // We are in a graph loop, no reference can be found from this path
	                return null;
	            cache[obj] = obj;
	        }
	        else if (recurse)
	        	throw new Error("Cache must be provided to get reference recursively");
            
            var refs:Array = _entityReferences[obj];
            if (!refs)
                return null;
            
            for (var i:int = 0; i < refs.length; i++) {
            	// Return first context expression reference that is remote enabled
                if (refs[i] is IExpression && _context.meta_tide.getComponentRemoteSync(IExpression(refs[i]).componentName) != Tide.SYNC_NONE)
                    return refs[i] as IExpression;
            }
            
            if (recurse) {
            	var ref:Object;
                for (i = 0; i < refs.length; i++) {
                    if (refs[i] is String) {
                        ref = _entitiesByUID.get(refs[i] as String);
                        if (ref != null) {
                            ref = getReference(ref, recurse, cache);
                            if (ref != null)
                                return IExpression(ref);
                        }
                    }
                    else if (!(refs[i] is IExpression)) {
                    	ref = refs[i];
                    	if (ref != null) {
                    		ref = getReference(ref, recurse, cache);
                            if (ref != null)
                                return IExpression(ref);
                    	} 
                    }
                }
            }
            return null;
        }
        
        
        /**
         *  @private
         *  Init references array for an object
         *   
         *  @param obj an entity
         */
        private function initRefs(obj:Object):Array {
            var refs:Array = _entityReferences[obj];
            if (!refs) {
                refs = new Array();
                _entityReferences[obj] = refs;
            }
            return refs;
        }
        
        /**
         *  @private 
         *  Registers a reference to the provided object with either a parent or res
         * 
         *  @param obj an entity
         *  @param parent the parent entity
         *  @param res the context expression
         */ 
        public function addReference(obj:Object, parent:Object, res:IExpression = null):void {
            if (obj is IEntity)
                attachEntity(IEntity(obj));
            
            var refs:Array = _entityReferences[obj] as Array;
            if (!(obj is IPersistentCollection) && res != null) {
                refs = initRefs(obj);
                var found:Boolean = false;
                for (var i:int = 0; i < refs.length; i++) {
                    if (!(refs[i] is IExpression))
                        continue; 
                    var r:IExpression = refs[i] as IExpression;
                    if (r.componentName == res.componentName && r.expression == res.expression) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    refs.push(res);
            }
            if (parent is IUID) {
                var ref:String = getQualifiedClassName(parent) + ":" + parent.uid;
                if (refs == null || refs.indexOf(ref) < 0) {
                    refs = initRefs(obj);
                    refs.push(ref);
                }
            }
	       	else if (parent) {
	       		if (refs == null || refs.indexOf(parent) < 0) {
	       			refs = initRefs(obj);
	       			refs.push(parent);
	       		}
	       	}
        }
        
        /**
         *	@private  
         *  Removes a reference on the provided object
         *
         *  @param obj an entity
         *  @param parent the parent entity to dereference
         *  @param res expression to remove
         */ 
        public function removeReference(obj:Object, parent:IUID = null, res:IExpression = null):void {
            var refs:Array = _entityReferences[obj];
            if (!refs)
                return;
            var idx:int = -1;
            if (parent)
                idx = refs.indexOf(getQualifiedClassName(parent) + ":" + parent.uid);
            else if (res) {
                for (var i:int = 0; i < refs.length; i++) {
                    if (refs[i] is IExpression && IExpression(refs[i]).path == res.path) {
                        idx = i;
                        break;
                    }
                }
            }
            if (idx >= 0)
                refs.splice(idx, 1);
            
            if (refs.length == 0)
            	delete _entityReferences[obj];
            
            var elt:Object = null;
            if (obj is IList || obj is Array) {
            	for each (elt in obj)
            		removeReference(elt, parent);
            }
            else if (obj is IMap) {
            	for (elt in obj) {
            		var val:Object = obj.get(elt);
            		removeReference(val, parent);
            		removeReference(elt, parent);
            	}
            }
        }
        

		private var _mergeUpdate:Boolean = false;
        
        /**
         *  Merge an object coming from the server in the context
         *
         *  @param obj external object
         *  @param previous previously existing object in the context (null if no existing object)
         *  @param expr current path from the context
         *  @param parent parent object for collections
         *  @param propertyName property name of the current object in the parent object
		 *  @param setter setter function to update the private property
		 *  @param forceUpdate force update of property (used for externalized properties)
         *
         *  @return merged object (should === previous when previous not null)
         */
        public function mergeExternal(obj:Object, previous:Object = null, expr:IExpression = null, 
									  parent:Object = null, propertyName:String = null, setter:Function = null, forceUpdate:Boolean = false):Object {
            if (_entityCache == null) {
                _entityCache = new Dictionary();
                _mergeUpdate = true;
            }
            var saveMergeUpdate:Boolean = _mergeUpdate;
            
			var addRef:Boolean = false;
            var fromCache:Boolean = false;
            var prev:Object = _entityCache[obj];
            var next:Object = obj;
            if (prev) {
                next = prev;
                fromCache = true;
            }
            else {
                // Clear change tracking
            	removeTrackingListeners(previous, parent); 
                
				if (obj == null) {
					next = null;
				}
				else if (((obj is IPersistentCollection && !IPersistentCollection(obj).isInitialized()) 
                	|| (obj is IPersistentCollection && !(previous is IPersistentCollection))) && parent is IEntity && propertyName) {
                    next = mergePersistentCollection(IPersistentCollection(obj), previous, null, IEntity(parent), propertyName);
                    addRef = true;
                }
                else if (obj is IList) {
                    next = mergeCollection(IList(obj), previous, parent == null ? expr : null, parent, propertyName);
                    addRef = true;
                }
                else if (obj is Array) {
                    next = mergeArray(obj as Array, previous, parent == null ? expr : null, parent, propertyName);
                    addRef = true;
                }
                else if (obj is IMap) {
                    next = mergeMap(IMap(obj), previous, parent == null ? expr : null, parent, propertyName);
                    addRef = true;
                }
                else if (obj is Enum) {
                	next = Enum.normalize(obj as Enum);
                }
                else if (!ObjectUtil.isSimple(obj) && !(obj is BigNumber) && !(obj is XML) && !(obj is ByteArray)) {
                    next = mergeEntity(obj, previous, expr, parent);
                	addRef = true;
                }
            }
			
            if (next && !fromCache && addRef
                && (expr != null || (prev == null && parent != null))) {
                // Store reference from current object to its parent entity or root component expression
                // If it comes from the cache, we are probably in a circular graph 
                addReference(next, parent, expr);
            }
            
            _mergeUpdate = saveMergeUpdate;
            
            if ((_mergeUpdate || forceUpdate) && setter != null && parent != null && propertyName != null && parent is IManaged) {
            	if (!resolvingConflict || propertyName != _context.meta_tide.getEntityDescriptor(IEntity(parent)).versionPropertyName) {
	                setter(next);
	                Managed.setProperty(IManaged(parent), propertyName, previous, next);
	            }
            }
			
			if ((_mergeUpdate || forceUpdate) && !fromCache && obj is IEntity) {
				// @TODO Try to improve performance here by not iterating on child contexts where unnecessary  
				// && _context.meta_isGlobal()) {
				
				// Propagate to existing conversation contexts where the entity is present
				_contextManager.forEachChildContext(_context, function(ctx:BaseContext, entity:IEntity):void {
					if (ctx === _sourceContext)
						return;
					if (ctx.meta_getCachedObject(entity, true) != null)
						ctx.meta_mergeFromContext(_context, entity, _externalData);
				}, obj);
			}
            
            return next;
        }


        /**
         *  @private 
         *  Merge an entity coming from the server in the context
         *
         *  @param obj external entity
         *  @param previous previously existing object in the context (null if no existing object)
         *  @param expr current path from the context
         *  @param parent parent object for collections
         * 
         *  @return merged entity (=== previous when previous not null)
         */ 
        private function mergeEntity(obj:Object, previous:Object, expr:IExpression = null, parent:Object = null):Object {
        	if (obj != null || previous != null)
            	log.debug("mergeEntity: {0} previous {1}{2}", BaseContext.toString(obj), BaseContext.toString(previous), obj === previous ? " (same)" : "");
        	
            var dest:Object = obj;
            var p:Object = null;
            if (obj is IUID) {
                p = _entitiesByUID.get(getQualifiedClassName(obj) + ":" + IUID(obj).uid);
                if (p) {
                    previous = p;
                    dest = previous;
                }
            }
            if (dest !== previous && previous && (objectEquals(previous, obj)
				|| (parent != null && !(previous is IUID)))) 	// GDS-649 Case of embedded objects 
                dest = previous;
            
            if (dest === obj && p == null && obj != null && _sourceContext != null) {
            	dest = Type.forInstance(obj).constructor.newInstance();
            	if (obj is IUID)
            		dest.uid = obj.uid;
            }

			try {
	        	if (obj is IEntity && !obj.meta::isInitialized() && objectEquals(previous, obj)) {
	                var desc:EntityDescriptor = _context.meta_tide.getEntityDescriptor(IEntity(obj));
	        		// Don't overwrite existing entity with an uninitialized proxy when optimistic locking is defined
	        		if (desc.versionPropertyName != null) {
	        			log.debug("ignored received uninitialized proxy");
	        			_dirtyCheckContext.markNotDirty(previous);
		    			return previous;
		    		}
	        	}
	            
	            if (dest is IEntity && !dest.meta::isInitialized())
	            	log.debug("initialize lazy entity: {0}", BaseContext.toString(dest));
	        }
	        catch (e:ReferenceError) {
	        	// Entity class does not implement meta:isInitialized, consider as true
	        }
            
            if (dest != null && dest is IEntity && dest === obj) {
            	log.debug("received entity {0} used as destination (ctx: {1})", BaseContext.toString(obj), 
            		_context.meta_isGlobal() ? "global" : _context.contextId);
            }
            
            var fromCache:Boolean = (p && dest === p); 
            
            if (!fromCache && dest is IUID)
                _entitiesByUID.put(IUID(dest));            
            
        	_entityCache[obj] = dest;
            if (_versionChangeCache == null)
                _versionChangeCache = new Dictionary();
            
            if (dest is IEntity) {
                desc = _context.meta_tide.getEntityDescriptor(IEntity(dest));
                
                // Associate entity with the current context
                attachEntity(IEntity(dest), false);
				
				var ignore:Boolean = false;
                if (previous && dest === previous) {
                    // Check version for optimistic locking
                    if (desc.versionPropertyName != null && !resolvingConflict) {
                        var newVersion:Number = obj[desc.versionPropertyName] as Number;
                        var oldVersion:Number = dest[desc.versionPropertyName] as Number;
                    	if (newVersion < oldVersion || (isNaN(newVersion) && !isNaN(oldVersion))) {
                    		log.warn("ignored merge of older version of {0} (current: {1}, received: {2})", 
                    			BaseContext.toString(dest), oldVersion, newVersion);
                        	ignore = true;
                        }
                    	else if (newVersion > oldVersion || (!isNaN(newVersion) && isNaN(oldVersion))) {
                    		_versionChangeCache[dest] = true;
                    		
                    		if (_externalData && _dirtyCheckContext.isEntityChanged(IEntity(dest))) {
                    			// Conflict between externally received data and local modifications
                    			log.error("conflict with external data detected on {0} (current: {1}, received: {2})",
                    				BaseContext.toString(dest), oldVersion, newVersion);
                    				
                    			if (_mergeConflicts == null)
                    				_mergeConflicts = new Conflicts(_context, this);
                				_mergeConflicts.addConflict(dest as IEntity, obj as IEntity);
                    			
                    			ignore = true;
                    		}
                    		else
                    			_mergeUpdate = true;
                    	}
                    	else {
                    		// Data has been changed locally and not persisted, don't overwrite when version number is unchanged
                    		if (_dirtyCheckContext.isEntityChanged(IEntity(dest)))
                    			_mergeUpdate = false;
                    		else
                    			_mergeUpdate = true;
                    	}
                    }
                    else if (!resolvingConflict)
                    	_versionChangeCache[dest] = true;
                }
                else
                	_versionChangeCache[dest] = true;
                
                if (!ignore) {
                	if (desc.mergeGDS21)
                		dest.meta::merge(_context, obj);
                	else if (desc.mergeGDS20)
                		dest.meta_merge(_context, obj);
                	else
                		EntityManager.defaultMerge(_context, obj, dest, _mergeUpdate, expr);
                }
            }
            else
                EntityManager.defaultMerge(_context, obj, dest, _mergeUpdate, expr);
            
            if (previous && obj !== previous && previous is IUID && _dirtyCheckContext.isSaved(previous)) {
                var pce:PropertyChangeEvent = new PropertyChangeEvent(PropertyChangeEvent.PROPERTY_CHANGE, 
                    false, false, PropertyChangeEventKind.UPDATE, null, previous, previous);
                previous.dispatchEvent(pce);
            }

			if (dest != null && _versionChangeCache[dest] != null && !resolvingConflict)
				_dirtyCheckContext.markNotDirty(dest);
			
			if (dest != null)
				log.debug("mergeEntity result: {0}", BaseContext.toString(dest));
            
            return dest;
        }
        

        /**
         *  @private 
         *  Merge a collection coming from the server in the context
         *
         *  @param coll external collection
         *  @param previous previously existing collection in the context (can be null if no existing collection)
         *  @param expr current path from the context
         *  @param parent owner object for collections
         *  @param propertyName property name in owner object
         * 
         *  @return merged collection (=== previous when previous not null)
         */ 
        private function mergeCollection(coll:IList, previous:Object, expr:IExpression, parent:Object = null, propertyName:String = null):IList {
            log.debug("mergeCollection: {0} previous {1}", BaseContext.toString(coll), BaseContext.toString(previous));

            if (previous && previous is IPersistentCollection && !IPersistentCollection(previous).isInitialized()) {
                log.debug("initialize lazy collection {0}", BaseContext.toString(previous));
                _entityCache[coll] = previous;
                
                IPersistentCollection(previous).initializing();
                
                for (var i:int = 0; i < coll.length; i++) {
                    var obj:Object = coll.getItemAt(i);

                    obj = mergeExternal(obj, null, null, propertyName != null ? parent : null);
                    previous.addItem(obj);
                }
                
                IPersistentCollection(previous).initialize();
    
                // Keep notified of collection updates to notify the server at next remote call
            	addTrackingListeners(previous, parent);

                return IList(previous);
            }

			var tracking:Boolean = false;
			
			var nextList:IList = null;            
            var list:IList = null;
            if (previous && previous is IList)
                list = IList(previous);
            else if (_sourceContext != null)
            	list = ObjectUtil.copy(coll) as IList;
            else
                list = coll;
                            
            _entityCache[coll] = list;
            
            // Restore collection sort/filter state
            var prevColl:IList = list !== coll ? list : null;
            if (prevColl is ICollectionView && coll is ICollectionView) {
                ICollectionView(coll).sort = ICollectionView(prevColl).sort;
                ICollectionView(coll).filterFunction = ICollectionView(prevColl).filterFunction;
                ICollectionView(coll).refresh();
            }
            
            if (prevColl && _mergeUpdate) {
            	// Enable tracking before modifying collection when resolving a conflict
            	// so the dirty checking can save changes
	            if (resolvingConflict) {
	            	addTrackingListeners(prevColl, parent);
	            	tracking = true;
	            }
	            
                for (i = 0; i < prevColl.length; i++) {
                    obj = prevColl.getItemAt(i);
                    found = false;
                    for (j = 0; j < coll.length; j++) {
                        var next:Object = coll.getItemAt(j);
                        if (objectEquals(next, obj)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        prevColl.removeItemAt(i);
                        i--;
                    }
                }
            }
            for (i = 0; i < coll.length; i++) {
                obj = coll.getItemAt(i);
                if (prevColl) {
                    var found:Boolean = false;
                    for (var j:int = i; j < prevColl.length; j++) {
                        var prev:Object = prevColl.getItemAt(j);
                        if (i < prevColl.length && objectEquals(prev, obj)) {
                            obj = mergeExternal(obj, prev, propertyName != null ? expr : null, propertyName != null ? parent : null);
                            
                            if (j != i) {
                            	prevColl.removeItemAt(j);
                            	if (i < prevColl.length)
                            		prevColl.addItemAt(obj, i);
                            	else
                            		prevColl.addItem(obj);
                            	if (i > j)
                            		j--;
                            }
                            else if (obj !== prev)
                                prevColl.setItemAt(obj, i);
                            
                            found = true;
                        }
                    }
                    if (!found) {
                        obj = mergeExternal(obj, null, propertyName != null ? expr : null, propertyName != null ? parent : null);
                        
                        if (_mergeUpdate) {
	                        if (i < prevColl.length)
	                        	prevColl.addItemAt(obj, i);
	                        else
	                        	prevColl.addItem(obj);
	                    }
                    }
                }
                else {
                	prev = obj;
                    obj = mergeExternal(obj, null, propertyName != null ? expr : null, propertyName != null ? parent : null);
                    if (obj !== prev)
                		coll.setItemAt(obj, i);
                }
            }
            if (prevColl && _mergeUpdate) {
            	if (!resolvingConflict)
					_dirtyCheckContext.markNotDirty(previous, parent as IEntity);
                
                nextList = prevColl;
            }
            else
            	nextList = coll;
            
            // Wrap persistent collections
            if (parent is IEntity && propertyName != null && nextList is IPersistentCollection && !(nextList is PersistentCollection)) {
                log.debug("create initialized persistent collection from {0}", BaseContext.toString(nextList));
                
                nextList = new PersistentCollection(IEntity(parent), propertyName, IPersistentCollection(nextList));
            }
            else
            	log.debug("mergeCollection result: {0}", BaseContext.toString(nextList));
            
            _entityCache[coll] = nextList;
            
            if (!tracking)
            	addTrackingListeners(nextList, parent);

            return nextList;
        }
        
        /**
         *  @private 
         *  Merge an array coming from the server in the context
         *
         *  @param array external collection
         *  @param previous previously existing array in the context (can be null if no existing array)
         *  @param expr current path from the context
         *  @param parent owner objects
         *  @param propertyName property name in owner object
         * 
         *  @return merged array
         */ 
        private function mergeArray(array:Array, previous:Object, expr:IExpression, parent:Object = null, propertyName:String = null):Array {
            log.debug("mergeArray: {0} previous {1}", BaseContext.toString(array), BaseContext.toString(previous));
            
            var prevArray:Array = previous is Array ? previous as Array : new Array();
            if (prevArray.length > 0 && prevArray !== array)
                prevArray.splice(0, prevArray.length);
            _entityCache[array] = prevArray;
            
            for (var i:int = 0; i < array.length; i++) {
                var obj:Object = array[i];
                obj = mergeExternal(obj, null, propertyName != null ? expr : null, propertyName != null ? parent : null);
                
                if (_mergeUpdate) {
	                if (prevArray !== array)
	                	prevArray.push(obj);
	               	else
	               		prevArray[i] = obj;
	            }
            }
            
            log.debug("mergeArray result: {0}", BaseContext.toString(prevArray));
            
            return prevArray;
        }

        /**
         *  @private 
         *  Merge a map coming from the server in the context
         *
         *  @param map external map
         *  @param previous previously existing map in the context (null if no existing map)
         *  @param expr current path from the context
         *  @param parent owner object for the map if applicable
         * 
         *  @return merged map (=== previous when previous not null)
         */ 
        private function mergeMap(map:IMap, previous:Object, expr:IExpression, parent:Object = null, propertyName:String = null):IMap {
            log.debug("mergeMap: {0} previous {1}", BaseContext.toString(map), BaseContext.toString(previous));

            var value:Object;
            var key:Object;
            
            if (previous && previous is IPersistentCollection && !IPersistentCollection(previous).isInitialized()) {
                log.debug("initialize lazy map {0}", BaseContext.toString(previous));
                _entityCache[map] = previous;
                
                IPersistentCollection(previous).initializing();
                
                for each (key in map.keySet) {
                    value = map.get(key);
                    key = mergeExternal(key, null, null, propertyName != null ? parent: null);
                    value = mergeExternal(value, null, null, parent);
                    previous.put(key, value);
                }
                
                IPersistentCollection(previous).initialize();
    
                // Keep notified of collection updates to notify the server at next remote call
            	addTrackingListeners(previous, parent);

                return IMap(previous);
            }
            
			var tracking:Boolean = false;
			
            var nextMap:IMap = null;
            var m:IMap = null;
            if (previous && previous is IMap)
                m = IMap(previous);
            else if (_sourceContext != null)
            	m = ObjectUtil.copy(map) as IMap;
            else
                m = map;
            _entityCache[map] = m;
            
            var prevMap:IMap = m !== map ? m : null;
            
            if (prevMap) {
	            if (resolvingConflict) {
	            	addTrackingListeners(prevMap, parent);
	            	tracking = true;
	            }
	            
                if (map !== prevMap) {
                    for each (key in map.keySet) {
                        value = map.get(key);
                        var newKey:Object = mergeExternal(key, null, null, parent);
                        value = mergeExternal(value, null, null, parent);
                        if (_mergeUpdate || prevMap.containsKey(newKey))
                        	prevMap.put(newKey, value);
                    }
                    
                    if (_mergeUpdate) {
                    	for each (key in prevMap.keySet) {
                    		var found:Boolean = false;
                    		for each (var k:Object in map.keySet) {
                    			if (objectEquals(k, key)) {
                    				found = true;
                    				break;
                    			}
                    		}
                    		if (!found)
                    			prevMap.remove(key);
                    	}
                    }
                }
                
                if (_mergeUpdate && !resolvingConflict)
					_dirtyCheckContext.markNotDirty(previous, parent as IEntity);
                
                nextMap = prevMap;
            }
            else {
	            var addedToMap:Array = new Array();
	            for each (key in map.keySet) {
	                value = mergeExternal(map.get(key), null, null, parent);
	                key = mergeExternal(key, null, null, parent);
	                addedToMap.push([ key, value ]);
	            }
	            map.clear();
	            for each (var obj:Object in addedToMap)
	                map.put(obj[0], obj[1]);
	                
	            nextMap = map;
	        }
                
            if (parent is IEntity && propertyName != null && nextMap is IPersistentCollection && !(nextMap is PersistentMap)) {
                log.debug("create initialized persistent map from {0}", BaseContext.toString(nextMap));
                nextMap = new PersistentMap(IEntity(parent), propertyName, IPersistentCollection(nextMap));
            }
            else
            	log.debug("mergeMap result: {0}", BaseContext.toString(nextMap));
            
            if (!tracking)
            	addTrackingListeners(nextMap, parent);
            
            return nextMap;
        } 


        /**
         *  @private 
         *  Wraps a persistent collection to manage lazy initialization
         *
         *  @param coll the collection to wrap
         *  @param previous the previous existing collection
         *  @param expr the path expression from the context
         *  @param parent the owner object
         *  @param propertyName owner property
         * 
         *  @return the wrapped persistent collection
         */ 
        protected function mergePersistentCollection(coll:IPersistentCollection, previous:Object, expr:IExpression, parent:IEntity, propertyName:String):Object {
            var oldVersion:Number;
            var newVersion:Number;
            var uninitialize:Boolean = true;
            var desc:EntityDescriptor = null;
            
            if (previous is PersistentCollection) {
	            _entityCache[coll] = previous;
                if (PersistentCollection(previous).isInitialized()) {
                	if (_uninitializeAllowed && _versionChangeCache[PersistentCollection(previous).entity] != null) {
	                    log.debug("uninitialize lazy collection {0}", BaseContext.toString(previous));
	                    PersistentCollection(previous).uninitialize();
	                }
	                else
	                	log.debug("keep initialized collection {0}", BaseContext.toString(previous));
                }
            	addTrackingListeners(previous, parent);
                return previous;
            }
            else if (previous is PersistentMap) {
	            _entityCache[coll] = previous;
                if (PersistentMap(previous).isInitialized()) {
                	if (_uninitializeAllowed && _versionChangeCache[PersistentMap(previous).entity] != null) {
	                    log.debug("uninitialize lazy map {0}", BaseContext.toString(previous));
	                    PersistentMap(previous).uninitialize();
	                }
	                else
	                	log.debug("keep initialized map {0}", BaseContext.toString(previous));
                }
            	addTrackingListeners(previous, parent);
                return previous;
            }
            
            if (coll is IMap) {
            	var pmap:PersistentMap = new PersistentMap(parent, propertyName, 
            		(coll is PersistentMap ? duplicatePersistentMap(PersistentMap(coll).object) : IPersistentCollection(coll)));
	            _entityCache[coll] = pmap;
            	if (pmap.isInitialized()) {
	                for each (var key:Object in pmap.keySet) {
	                    var value:Object = pmap.remove(key);
	                    key = mergeExternal(key, null, null, parent);
	                    value = mergeExternal(value, null, null, parent);
	                    pmap.put(key, value);
	                }
            		addTrackingListeners(pmap, parent);
	            }
            	return pmap;
            }
            
            var pcoll:PersistentCollection = new PersistentCollection(parent, propertyName, 
            	(coll is PersistentCollection ? duplicatePersistentCollection(PersistentCollection(coll).object) : IPersistentCollection(coll)));
            _entityCache[coll] = pcoll;
            if (pcoll.isInitialized()) {
	            for (var i:int = 0; i < pcoll.length; i++) {
					var obj:Object = mergeExternal(pcoll.getItemAt(i), null, null, parent);
					if (obj !== pcoll.getItemAt(i)) 
						pcoll.setItemAt(obj, i);
	            }
            	addTrackingListeners(pcoll, parent);
	        }
            return pcoll;
        }
        
        private function duplicatePersistentCollection(coll:Object):IPersistentCollection {
        	if (coll is IPersistentCollection)
        		return coll.clone() as IPersistentCollection;
        	
        	throw new Error("Not a persistent collection " + BaseContext.toString(coll)); 
        }
        
        private function duplicatePersistentMap(map:Object):IPersistentCollection {
        	if (map is IPersistentCollection)
        		return map.clone() as IPersistentCollection;
        	
        	throw new Error("Not a persistent map " + BaseContext.toString(map)); 
        }
        
        
        /**
         *  Merge conversation context variables in global context 
         *  Only applicable to conversation contexts 
         */
        public function mergeInContext(context:BaseContext):void {
        	var cache:Dictionary = new Dictionary();
            for each (var obj:Object in _entitiesByUID.data) {
            	// Reset local dirty state, only server state can safely be merged in global context
            	if (obj is IEntity)
            		resetEntity(IEntity(obj), cache);
            	context.meta_mergeFromContext(_context, obj);
            }
        }
        
        
        /**
         *  Dispatch an event when last merge generated conflicts 
         */
        public function handleMergeConflicts():void {
        	if (_mergeConflicts != null && !_mergeConflicts.empty)
        		_context.dispatchEvent(new TideDataConflictsEvent(_context, _mergeConflicts));
        }
        
        public function resolveMergeConflicts():void {
        	if (_mergeConflicts != null && _mergeConflicts.allResolved)
        		_mergeConflicts = null;
        }
        
        
        /**
         *  Default implementation of entity merge for simple ActionScript beans with public properties
         *  Can be used to implement Tide managed entities with simple objects
         *
         *  @param em the context
         *  @param obj source object
         *  @param dest destination object
         *  @param expr current path of the entity in the context (mostly for internal use)
         */ 
        public static function defaultMerge(em:IEntityManager, obj:Object, dest:Object, mergeUpdate:Boolean = true, expr:IExpression = null):void {
            var cinfo:Object = ObjectUtil.getClassInfo(obj, null, { includeTransient: false });
			var rw:Array = new Array();
            for each (var p:String in cinfo.properties) {
                var o:Object = obj[p];
				var d:Object = dest[p];
                o = em.meta_mergeExternal(o, d, expr, dest, p);
                if (o !== d && mergeUpdate)
                	dest[p] = o;
				rw.push(p);
            }
			cinfo = ObjectUtil.getClassInfo(obj, rw, { includeReadOnly: true });
			for each (p in cinfo.properties)
				em.meta_mergeExternal(obj[p], dest[p], expr, dest, p);
        }

    
        /**
         *  Equality for objects, using uid property when possible
         *
         *  @param obj1 object
         *  @param obj2 object
         * 
         *  @return true when objects are instances of the same entity
         */ 
        public function objectEquals(obj1:Object, obj2:Object):Boolean {
            if ((obj1 is IPropertyHolder && obj2 is IEntity) || (obj1 is IEntity && obj2 is IPropertyHolder))
                return false;

            if (obj1 is IUID && obj2 is IUID && getQualifiedClassName(obj1) == getQualifiedClassName(obj2)) {
            	try {
	            	if (obj1 is IEntity && (!obj1.meta::isInitialized() || !obj2.meta::isInitialized())) {
	            		// Compare with identifier for uninitialized entities
	            		var edesc:EntityDescriptor = _context.meta_tide.getEntityDescriptor(IEntity(obj1));
	            		if (edesc.idPropertyName != null)
	            			return obj1[edesc.idPropertyName] == obj2[edesc.idPropertyName];
	            	}
	            }
	            catch (e:ReferenceError) {
	            	// Entity class does not implement meta::isInitialized, consider as true
	            }
                return IUID(obj1).uid == IUID(obj2).uid;
            }
            
            if (obj1 is Enum && obj2 is Enum && obj1.equals(obj2))
            	return true; 
            
            if (obj1 is BigNumber && obj2 is BigNumber && obj1.equals(obj2))
            	return true; 

            return obj1 === obj2;
        }
        
        
        /**
         *  Check if entity property has been changed since last remote call
         *
         *  @param entity entity to check
         *  @param propertyName property to check
         *  @param value current value to compare with saved value
         *   
         *  @return true is value has been changed
         */ 
        public function isEntityPropertyChanged(entity:IEntity, propertyName:String, value:Object):Boolean {
        	return _dirtyCheckContext.isEntityPropertyChanged(entity, propertyName, value);
        }
        
        
        /**
         *  Check if entity has changed since last save point
         *
         *  @param entity entity to restore
         *  @param propName property name
         *  @param value
         *   
         *  @return entity is dirty
         */ 
        public function isEntityChanged(entity:IEntity, propName:String = null, value:* = null):Boolean {
            return _dirtyCheckContext.isEntityChanged(entity, propName, value);
        }
        
        
        /**
         *  Discard changes of entity from last version received from the server
         *
         *  @param entity entity to restore
         */ 
        public function resetEntity(entity:IEntity, cache:Dictionary):void {
        	_dirtyCheckContext.resetEntity(entity, cache);
        }


        /**
         *  @private 
         *  Interceptor for managed entity setters
         *
         *  @param entity entity to intercept
         *  @param propName property name
         *  @param oldValue old value
         *  @param newValue new value
         */ 
        public function setEntityProperty(entity:IEntity, propName:String, oldValue:*, newValue:*):void {
        	if (newValue !== oldValue) {
        		if (oldValue != null) {
        			removeReference(oldValue, entity);
        			removeTrackingListeners(oldValue, entity);
        		}
        		
            	if (newValue is IUID || newValue is IList || newValue is IMap || newValue is Array) {
            		addReference(newValue, entity);
            		addTrackingListeners(newValue, entity);
            	}
            }
            
            _dirtyCheckContext.setEntityProperty(entity, propName, oldValue, newValue);
        }


        /**
         *  @private 
         *  Interceptor for managed entity getters
         *
         *  @param entity entity to intercept
         *  @param propName property name
         *  @param value value
         * 
         *  @return value
         */ 
        public function getEntityProperty(entity:IEntity, propName:String, value:*):* {
            if (propName == "meta_dirty")
                return _dirtyCheckContext.isEntityChanged(entity);
            
            return value;
        }


        /**
         *	@private 
         *  Remove tracking events
         *
         *  @param previous previously existing object in the context (null if no existing object)
         *  @param parent parent object for collections
         */
        private function addTrackingListeners(previous:Object, parent:Object):void {
        	if (_collectionListeners == null)
        		return;
        	
            if (previous != null && previous is ListCollectionView) {
                if (parent != null) {
                    ListCollectionView(previous).addEventListener(CollectionEvent.COLLECTION_CHANGE, entityCollectionChangeHandler, false, 0, true);
                    _collectionListeners[previous] = "entityCollection";
                }
                else {
                    ListCollectionView(previous).addEventListener(CollectionEvent.COLLECTION_CHANGE, _context.meta_collectionChangeHandler, false, 0, true);
                    _collectionListeners[previous] = "collection";
                }
            }
            else if (previous != null && previous is IMap) {
                if (parent != null) {
                    IMap(previous).addEventListener(CollectionEvent.COLLECTION_CHANGE, entityMapChangeHandler, false, 0, true);
                    _collectionListeners[previous] = "entityMap";
                }
                else {
                    IMap(previous).addEventListener(CollectionEvent.COLLECTION_CHANGE, _context.meta_mapChangeHandler, false, 0, true);
                    _collectionListeners[previous] = "map";
                }
            }
        }

        /**
         *	@private 
         *  Remove tracking events
         *
         *  @param previous previously existing object in the context (null if no existing object)
         *  @param parent parent object for collections
         */
        private function removeTrackingListeners(previous:Object, parent:Object):void {
        	if (_collectionListeners == null || previous == null || previous is XMLList)
        		return;
        	
            if (previous is ListCollectionView) {
                if (parent != null)
                    ListCollectionView(previous).removeEventListener(CollectionEvent.COLLECTION_CHANGE, entityCollectionChangeHandler);
                else
                    ListCollectionView(previous).removeEventListener(CollectionEvent.COLLECTION_CHANGE, _context.meta_collectionChangeHandler);
            }
            else if (previous is IMap) {
                if (parent != null)
                    IMap(previous).removeEventListener(CollectionEvent.COLLECTION_CHANGE, entityMapChangeHandler);
                else
                    IMap(previous).removeEventListener(CollectionEvent.COLLECTION_CHANGE, _context.meta_mapChangeHandler);
            }
            
            delete _collectionListeners[previous];
        }


        /**
         *  @private 
         *  Collection event handler to save changes on managed collections
         *
         *  @param event collection event
         */ 
        private function entityCollectionChangeHandler(event:CollectionEvent):void {
        	if (_sourceContext === _context || _context.meta_finished)
        		return;
        	
            var i:int = 0;
            
            if (event.kind == CollectionEventKind.ADD && event.items && event.items.length > 0) {
            	var parent:Object = getOwnerEntity(event.target);
                for (i = 0; i < event.items.length; i++) {
                    if (event.items[i] is IEntity) {
                    	if (parent)
                    		addReference(IEntity(event.items[i]), parent);
                    	else
                        	attachEntity(IEntity(event.items[i]));
                    }
                }
            }
            
            if (event.kind != CollectionEventKind.ADD && event.kind != CollectionEventKind.REMOVE 
				&& event.kind != CollectionEventKind.RESET && event.kind != CollectionEventKind.REPLACE)
                return;
            
            log.debug("collection changed: {0} {1}", event.kind, BaseContext.toString(event.target));
            
            _dirtyCheckContext.entityCollectionChangeHandler(event);
            
            _context.meta_entityCollectionChangeHandler(event);
        }
        
        
        /**
         *  @private 
         *  Collection event handler to save changes on managed maps
         *
         *  @param event map event
         */ 
        private function entityMapChangeHandler(event:CollectionEvent):void {
        	if (_sourceContext === _context || _context.meta_finished)
        		return;
        	
            var i:int = 0;
            
            if (event.kind == CollectionEventKind.ADD && event.items && event.items.length > 0) {
            	var parent:Object = getOwnerEntity(event.target);
                for (i = 0; i < event.items.length; i++) {
                    if (event.items[i] is IEntity) {
                    	if (parent)
                    		addReference(IEntity(event.items[i]), parent);
                    	else
                        	attachEntity(IEntity(event.items[i]));
                    }
                    else if (event.items[i] is Array) {
                        var obj:Array = event.items[i] as Array;
	                    if (obj[0] is IEntity) {
	                    	if (parent)
	                    		addReference(IEntity(obj[0]), parent);
	                    	else
	                        	attachEntity(IEntity(obj[0]));
	                    }
	                    if (obj[1] is IEntity) {
	                    	if (parent)
	                    		addReference(IEntity(obj[1]), parent);
	                    	else
	                        	attachEntity(IEntity(obj[1]));
	                    }
                    }
                }
            }
            
            if (event.kind != CollectionEventKind.ADD && event.kind != CollectionEventKind.REMOVE)
                return;
            
            log.debug("map changed: {0} {1}", event.kind, BaseContext.toString(event.target));
            
            _dirtyCheckContext.entityMapChangeHandler(event);
            
            _context.meta_entityMapChangeHandler(event);
        }
    }
}
