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

package org.granite.tide.collections {
    
    import flash.events.Event;
    import flash.events.IEventDispatcher;
    
    import mx.binding.utils.BindingUtils;
    import mx.collections.ArrayCollection;
    import mx.collections.CursorBookmark;
    import mx.collections.IList;
    import mx.collections.IViewCursor;
    import mx.collections.ListCollectionView;
    import mx.collections.Sort;
    import mx.collections.SortField;
    import mx.collections.errors.ItemPendingError;
    import mx.core.IPropertyChangeNotifier;
    import mx.core.IUID;
    import mx.core.mx_internal;
    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;
    import mx.events.PropertyChangeEvent;
    import mx.events.PropertyChangeEventKind;
    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.rpc.IResponder;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;
    import mx.utils.ArrayUtil;
    import mx.utils.ObjectUtil;
    
	import org.granite.util.ClassUtil;
	import org.granite.tide.BaseContext;
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TideResultEvent;
    
    use namespace mx_internal;
	
	
	[Bindable]
	/**
	 * 	Base implementation of a paged collection that supports ItemPendingError.<br/>
	 *  The collection holds two complete pages of data.<br/>
	 * 	<br/>
	 * 	Subclassed have to implement a getResult method that will retrieve a range of elements.
	 */
	public class PagedCollection extends ListCollectionView {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.collections.PagedCollection");
        
        
        public static const COLLECTION_PAGE_CHANGE:String = "collectionPageChange";

        public static const RESULT:String = "result";
        public static const FAULT:String = "fault";
        
		
		/**
		 * 	@private
		 */
		protected var _componentName:String = null;
		protected var _context:BaseContext = null;
        protected var _initializing:Boolean = false;
        private var _initSent:Boolean = false;
        
		/**
		 * 	@private
		 */
		protected var _first:int;			// Current first index of local data
		/**
		 * 	@private
		 */
        protected var _last:int;			// Current last index of local data
		/**
		 * 	@private
		 */
        protected var _max:int;           // Page size
		/**
		 * 	@private
		 */
        protected var _count:int;         // Result count
        private var _list:IList;
		/**
		 * 	@private
		 */
		protected var _fullRefresh:Boolean = false;
		/**
		 * 	@private
		 */
		protected var _filterRefresh:Boolean = false;
		private var _ipes:Array;		// Array of ItemPendingErrors
		
		// GDS-523
		public var uidProperty:String = "uid";
		
		// GDS-712
		public var multipleSort:Boolean = true;
		
		
		public function PagedCollection() {
		    log.debug("create collection");
			super();
			this.sort = new Sort();
			this.sort.compareFunction = nullCompare;
			this.sort.fields = new Array();
			_ipes = null;
			_first = 0;
			_last = 0;
			_count = 0;
			_list = null;
			_initializing = true;
			addEventListener("sortChanged", sortChangedHandler);
		}
		
		
		public function meta_init(componentName:String, context:BaseContext):void {
			_componentName = componentName;
			_context = context;
		}
		
		
		/**
		 *	Get total number of elements
		 *  
		 *  @return collection total length
		 */
		public override function get length():int {
		    if (_initializing) {
		    	if (!_initSent) {
		    		log.debug("initial find");
			    	find(0, _max);
			    	_initSent = true;
			    }
		        return 0;     // Must be initialized to >0 to enable ItemPendingError at first getItemAt
		    }
		    else if (localIndex == null)
		        return 0;
			return _count;
		}
		
		/**
		 *  Set the page size. The collection will store in memory twice this page size, and each server call
		 *  will return at most the page size.
		 * 
		 *  @param max maximum number of requested elements
		 */
		public function set maxResults(max:int):void {
			_max = max;
		}
		
		
		private var _elementName:String;
		
		public function set elementClass(elementClass:Class):void {
			if (_elementName != null)
	        	_context.removeEventListener("org.granite.tide.data.refresh." + _elementName, refreshHandler);
			
			_elementName = elementClass != null ? ClassUtil.getUnqualifiedClassName(elementClass) : null;
			
			if (_elementName != null)
	        	_context.addEventListener("org.granite.tide.data.refresh." + _elementName, refreshHandler, false, 0, true);
		}
		
		
		
		/**
		 * 	Clear collection content
		 */
		public function clear():void {
			handleResult({ resultList: new ArrayCollection(), resultCount: 0, firstResult: 0, maxResults: _max });
			_initializing = true;
			_initSent = false;
		}
    	
    	
    	// GDS-524
		public var beforeFindCall:Function = null;
		
		
		/**
		 *	Abstract method: trigger a results query for the current filter
		 *	@param first	: index of first required result
		 *  @param last     : index of last required result
		 */
		protected function find(first:int, last:int):void {
			log.debug("find from {0} to {1}", first, last);
			
			if (beforeFindCall != null)
				beforeFindCall(first, last);
		}
		
		
		/**
		 *	Force refresh of collection when filter/sort have been changed
		 * 
		 *  @return always false
		 */
		public function fullRefresh():Boolean {
		    _fullRefresh = true;
		    return refresh();
		}
		
		/**
		 *	Refresh collection with new filter/sort parameters
		 * 
		 *  @return always false
		 */
		public override function refresh():Boolean {
			// _ipes = null;
			
			if (_fullRefresh) {
				log.debug("full refresh");
			
			    // Force complete refresh after changing sorting or filtering
			    if (localIndex != null) {
			        for (var i:int = 0; i < localIndex.length; i++)
			            stopTrackUpdates(localIndex[i]);
			    }
    			localIndex = null;
    			_fullRefresh = false;
    			if (_filterRefresh) {
    			    _first = 0;
    			    _last = _first+_max;
    			    _filterRefresh = false;
    			}
            }
            else
				log.debug("refresh");			
            
			find(_first, _last);
			return false;
		}
		
		
		private function refreshHandler(event:Event):void {
			fullRefresh();
		}
		
		
		/**
		 * 	Internal handler for sort events
		 * 
		 * 	@param event sort event
		 */
		private function sortChangedHandler(event:Event):void {
		    _fullRefresh = true;
		}
		
		
		/**
		 *  Build a result object from the result event
		 *  
		 *  @param event the result event
		 *  @param first first index requested
		 *  @param max max elements requested
		 *   
		 *  @return an object containing data from the collection
		 *      resultList   : the retrieved data
		 *      resultCount  : the total count of elements (non paged)
		 *      firstResult  : the index of the first retrieved element
		 *      maxResults   : the maximum count of retrieved elements 
		 */
		protected function getResult(event:TideResultEvent, first:int, max:int):Object {
		    throw new Error("Abstract method, must be implemented by subclasses");
		}
		
		/**
		 * 	@private
		 *  Initialize collection after first find
		 *   
		 *  @param the result event of the first find
		 */
		protected function initialize(event:TideResultEvent):void {
		}
		
		/**
		 * 	@private
		 *	Event handler for results query
		 * 
		 *  @param event the result event
		 *  @param first first requested index
		 *  @param max max elements requested
		 */
		protected function findResult(event:TideResultEvent, first:int, max:int):void {
		    var result:Object = getResult(event, first, max);
		    
		    handleResult(result, event);
		}
		
		/**
		 * 	@private
		 *	Event handler for results query
		 * 
		 *  @param result the result object
		 *  @param event the result event
		 */
		protected function handleResult(result:Object, event:TideResultEvent = null):void {
			_list = result.resultList as IList;
			
			if (this.sort != null)
				this.sort.compareFunction = nullCompare;	// Avoid automatic sorting
			
			var expectedFirst:int = 0;
			var expectedLast:int = 0;
			
			if (_initializing && event) {
				if (_max == 0 && result.hasOwnProperty("maxResults"))
			    	_max = result.maxResults;
			    initialize(event);
			}
			
			var nextFirst:int = result.firstResult;
			var nextLast:int = nextFirst + result.maxResults;
			log.debug("handle result {0} - {1}", nextFirst, nextLast);
			
			if (!_initializing) {
			    expectedFirst = nextFirst;
			    expectedLast = nextLast;
			}
			var page:int = nextFirst / _max;
			// log.debug("findResult page {0} ({1} - {2})", page, nextFirst, nextLast);
			
			var newCount:int = result.resultCount;
			if (newCount != _count) {
			    var pce:PropertyChangeEvent = PropertyChangeEvent.createUpdateEvent(this, "length", _count, newCount); 
				_count = newCount;
				dispatchEvent(pce);
			}
		
		    _initializing = false;
			
			var i:int;
            var obj:Object;
            var dispatchRefresh:Boolean = (localIndex == null);
		    
		    var entityName:String;
		    var entityNames:Array = null;
		    if (localIndex != null) {
		    	entityNames = new Array();
		        for (i = 0; i < localIndex.length; i++) {
					entityName = ClassUtil.getUnqualifiedClassName(localIndex[i]);
					if (entityName != _elementName && entityNames.indexOf(entityName) < 0)
						entityNames.push(entityName);

		            stopTrackUpdates(localIndex[i]);
		        }
		        for each (entityName in entityNames)
		        	_context.removeEventListener("org.granite.tide.data.refresh." + entityName, refreshHandler);
		    }
			localIndex = _list.toArray();
		    if (localIndex != null) {
		    	entityNames = new Array();
		        for (i = 0; i < localIndex.length; i++) {
					entityName = ClassUtil.getUnqualifiedClassName(localIndex[i]);
					if (entityName != _elementName && entityNames.indexOf(entityName) < 0)
						entityNames.push(entityName);
						
		            startTrackUpdates(localIndex[i]);
		        }
		        for each (entityName in entityNames)
		        	_context.addEventListener("org.granite.tide.data.refresh." + entityName, refreshHandler, false, 0, true);
		    }
		    
			// Must be before collection event dispatch because it can trigger a new getItemAt
			_first = nextFirst;
			_last = nextLast;
		    
		    if (_ipes != null) {
		        var nextIpes:Array = new Array();
		        
    		    while (_ipes.length > 0) {
    		        // Must pop the ipe before calling result
    		        var a0:Array = _ipes.pop() as Array;
    		        log.debug("call IPE responders received {0} - {1} expected {2} - {3}", int(a0[1]), int(a0[2]), expectedFirst, expectedLast);
    		        if (int(a0[1]) == expectedFirst && int(a0[2]) == expectedLast) {
    		            var ipe:ItemPendingError = a0[0] as ItemPendingError;
    		            if (ipe.responders != null) {
    		            	var saveThrowIpe:Boolean = _throwIpe;
    		            	_throwIpe = false;
            				// Trigger responders for current pending item
            				for (var j:int = 0; j < ipe.responders.length; j++)
            					ipe.responders[j].result(event);
            				_throwIpe = saveThrowIpe;
    		            }
    		        }
    		        else
    		            nextIpes.push(a0);
    		    }
    		    
    		    _ipes = nextIpes;
    		}
			
//		    _ipes = null;
		    
		    if (event != null)
		    	dispatchEvent(new CollectionEvent(COLLECTION_PAGE_CHANGE, false, false, RESULT, -1, -1, [ event ]));
		    
		    if (dispatchRefresh) {
		        _tempSort = new NullSort();
	        	saveThrowIpe = _throwIpe;
	            _throwIpe = false;
		        super.refresh();
		        _throwIpe = saveThrowIpe;
		        _tempSort = null;
		    }
		    
		    _maxGetAfterHandle = -1;
		    _firstGetNext = -1;
		}
		
		
//		public override function dispatchEvent(event:Event):Boolean {
//			if (_tempSort is NullSort && event is CollectionEvent && CollectionEvent(event).kind == CollectionEventKind.REFRESH)
//				CollectionEvent(event).kind = CollectionEventKind.RESET;
//			return super.dispatchEvent(event);
//		} 
    	
		
		private var _tempSort:NullSort = null;
    	
    	// All this ugly hack because ListCollectionView.revision is private
        mx_internal override function getBookmarkIndex(bookmark:CursorBookmark):int {
        	var saveThrowIpe:Boolean = _throwIpe;
            _throwIpe = false;
            var index:int = super.getBookmarkIndex(bookmark);
            _throwIpe = saveThrowIpe;
            return index;
        }
 
 
        [Bindable("listChanged")]
    	public override function get list():IList {
    	    return _list;
    	}
		
        [Bindable("sortChanged")]
		public override function get sort():Sort {
		    if (_tempSort && !_tempSort.sorted)
		        return _tempSort;
		    return super.sort;
		}
		
		
		/**
		 *  @private
		 *	Event handler for results fault
		 *  
		 *  @param event the fault event
		 *  @param first first requested index
		 *  @param max max elements requested
		 */
		protected function findFault(event:TideFaultEvent, first:int, max:int):void {
			handleFault(event);
		}
		
		/**
		 * 	@private
		 *	Event handler for results query fault
		 * 
		 *  @param event the fault event
		 */
		protected function handleFault(event:TideFaultEvent):void {
			log.debug("findFault: {0} => {1} ({2})", event.target, event.fault);
			
			var nextIpes:Array = new Array();
			if (_ipes != null) {
				while (_ipes.length > 0) {
					var a:Array = _ipes.pop() as Array;
					var ipe:ItemPendingError = ItemPendingError(a[0]);
					
					if ((_max == 0 && int(a[1]) == 0 && int(a[2]) == 0) || (int(a[1]) == _first && int(a[2]) == _last)) {
						if (ipe.responders != null) {
				        	var saveThrowIpe:Boolean = _throwIpe;
				            _throwIpe = false;
							for (var j:int = 0; j < ipe.responders.length; j++)
								ipe.responders[j].fault(event);
    		            	_throwIpe = saveThrowIpe;
						}
					}
					else
						nextIpes.push(a);
				}
				_ipes = nextIpes;
			}
		    
	    	dispatchEvent(new CollectionEvent(COLLECTION_PAGE_CHANGE, false, false, FAULT, -1, -1, [ event ]));
		    
		    _maxGetAfterHandle = -1;
		    _firstGetNext = -1;
		}
		
		
		private var _throwIpe:Boolean = true;		
		private var _maxGetAfterHandle:int = -1;
		private var _firstGetNext:int = -1;
		
		
		/**
		 * 	Override of getItemAt with ItemPendingError management
		 * 
		 *	@param index index of requested item
		 *	@param prefetch not used
		 *  @return object at specified index
		 */
		public override function getItemAt(index:int, prefetch:int = 0):Object {
			if (index < 0)
				return null;
		
			var ipe:ItemPendingError;
			
			var a:Array;
			var i:int;
			if (_max == 0 || _initializing) {
				if (!_initSent) {
					log.debug("initial find");
				    find(0, _max);
				    _initSent = true;
				}
			    return null;
			}
			else {
				if (_firstGetNext == -1) {
					if (_maxGetAfterHandle == -1)
						_maxGetAfterHandle = index;
					else if (index > _maxGetAfterHandle)
						_maxGetAfterHandle = index;
						
					if (index < _maxGetAfterHandle)
						_firstGetNext = index;
				}
				else if (index > _maxGetAfterHandle && _firstGetNext < _maxGetAfterHandle)
					_firstGetNext = index;
			
    			if (localIndex && _ipes != null) {
    				// Check if requested page is already pending, and rethrow existing error
    				// Always rethrow when data is after (workaround for probable bug of Flex DataGrid)
    				for (i = 0; i < _ipes.length; i++) {
    					a = _ipes[i] as Array;
    					ipe = ItemPendingError(a[0]);
    					if (index >= int(a[1]) && index < int(a[2]) && int(a[2]) > _last && ipe.responders == null && index != _firstGetNext) {
    					    log.debug("forced rethrow of existing IPE for index {0} ({1} - {2})", index, int(a[1]), int(a[2]));
						    // log.debug("stacktrace {0}", ipe.getStackTrace());
    					    if (ListCollectionView.mx_internal::VERSION.substring(0, 1) != "2")
    						    throw ipe;
    					}
    				}
    			}
        	    
    			if (localIndex && index >= _first && index < _last) {	// Local data available for index
    			    var j:int = index-_first;
    			    // log.debug("getItemAt index {0} (current {1} to {2})", index, _first, _last);
    				return localIndex[j];
    			}
    			
    			if (!_throwIpe)
    			    return null;
    			
    			if (_ipes != null) {
    				// Check if requested page is already pending, and rethrow existing error
    				for (i = 0; i < _ipes.length; i++) {
    					a = _ipes[i] as Array;
    					ipe = ItemPendingError(a[0]);
    					if (index >= int(a[1]) && index < int(a[2])) {
    					    log.debug("rethrow existing IPE for index {0} ({1} - {2})", index, int(a[1]), int(a[2]));
    						throw ipe;
    					}
    				}
    			}
        	    
			    var page:int = index / _max;
			    
    			// Trigger a results query for requested page
    			var nfi:int = 0;
    			var nla:int = 0;
    			var idx:int = page * _max;
    			if (index >= _last && index < _last + _max) {
    				nfi = _first;
    				nla = _last + _max;
    				if (nla > nfi + 2*_max)
    				    nfi = nla - 2*_max;
    				if (nfi < 0)
    				    nfi = 0;
    				if (nla > _count)
    				    nla = _count;
    			}
    			else if (index < _first && index >= _first - _max) {
    				nfi = _first - _max;
    				if (nfi < 0)
    					nfi = 0;
    				nla = _last;
    				if (nla > nfi + 2*_max)
    				    nla = nfi + 2*_max;
    				if (nla > _count)
    				    nla = _count;
    			}
    			else {
    				nfi = index - _max;
    				nla = nfi + 2 * _max;
    				if (nfi < 0)
    					nfi = 0;
    				if (nla > _count)
    				    nla = _count;
    			}
				log.debug("request find for index " + index);
    			find(nfi, nla);
            }
			
			// Throw ItemPendingError for requested index
			// log.debug("ItemPendingError for index " + index + " triggered " + nfi + " to " + nla);
			ipe = new ItemPendingError("Items pending from " + nfi + " to " + nla + " for index " + index);
			if (_ipes == null)
				_ipes = new Array();
			_ipes.push([ipe, nfi, nla]);
		    log.debug("throw IPE for index {0} ({1} - {2})", index, nfi, nla);
		    // log.debug("stacktrace {0}", ipe.getStackTrace());
			throw ipe;
		}
		
		
		/**
		 * 	Override of getItemIndex
		 * 
		 * 	@param obj searched object
		 *  @return index of object in collection
		 */
		public override function getItemIndex(obj:Object):int {
			if (obj == null)
				return -1;
			
            // log.debug("getItemIndex {0}", obj);
			if (localIndex) {
				var idx:int;
				if (obj is IUID) {
					// Local data available: lookup by id
					for (idx = 0; idx < localIndex.length; idx++) {
						if (IUID(obj).uid == IUID(localIndex[idx]).uid)
							return _first + idx;
					}
				}
				else if (obj.hasOwnProperty(uidProperty)) {
					// GDS-523
					for (idx = 0; idx < localIndex.length; idx++) {
						if (obj[uidProperty] == localIndex[idx][uidProperty])
							return _first + idx;
					}
				}
			}
			
			return -1;
		}
		
		
		private function nullCompare(o1:Object, o2:Object, fields:Array = null):int {
			if (o1 is IUID && o2 is IUID) {
				if (IUID(o1) == IUID(o2))
					return 0;
				return 1;
			}
			// GDS-523
			if (o1 != null && o2 != null && o1.hasOwnProperty(uidProperty) && o2.hasOwnProperty(uidProperty)) {
				if (o1[uidProperty] == o2[uidProperty])
					return 0;
				return 1;
			}
			return 0;
		}
		
		
        protected function startTrackUpdates(item:Object):void {
            if (item && item is IEventDispatcher)
                IEventDispatcher(item).addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, itemUpdateHandler, false, 0, true);
        }
        
        protected function stopTrackUpdates(item:Object):void {
            if (item && item is IEventDispatcher)
                IEventDispatcher(item).removeEventListener(PropertyChangeEvent.PROPERTY_CHANGE, itemUpdateHandler);    
	    }
	    
        protected function itemUpdateHandler(event:PropertyChangeEvent):void {
    		if (hasEventListener(CollectionEvent.COLLECTION_CHANGE)) {
		        var ce:CollectionEvent = new CollectionEvent(CollectionEvent.COLLECTION_CHANGE);
		        ce.kind = CollectionEventKind.UPDATE;
		        ce.items.push(event);
		        ce.location = -1;
		        dispatchEvent(ce);
		    }
        }
	}
}


import mx.collections.Sort;

class NullSort extends Sort {
    
    private var _sorted:Boolean = false;
    
    public function get sorted():Boolean {
        return _sorted;
    }
    
    public override function sort(array:Array):void {
        _sorted = true;
    }
}
