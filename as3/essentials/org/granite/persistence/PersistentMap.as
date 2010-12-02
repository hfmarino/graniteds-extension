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

package org.granite.persistence {

    import flash.utils.IDataInput;
    import flash.utils.IDataOutput;
    
    import mx.core.IUID;
    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;
    import mx.events.PropertyChangeEvent;
    
    import org.granite.collections.BasicMap;
    import org.granite.collections.IPersistentCollection;
    import org.granite.collections.UIDArraySet;


    [RemoteClass(alias="org.granite.messaging.persistence.ExternalizablePersistentMap")]
    /**
     * @author Francesco FARAONE
     */
    public class PersistentMap extends BasicMap implements IPersistentCollection {

        private var _initializing:Boolean = false;
        private var _initialized:Boolean = false;
        private var _metadata:String = null;
        private var _dirty:Boolean = false;

        public function PersistentMap(initialized:Boolean = true) {
            super(new UIDArraySet());
            _initialized = initialized;
            if (_initialized)
	            addEventListener(CollectionEvent.COLLECTION_CHANGE, dirtyCheckHandler);
        }


        final public function isInitialized():Boolean {
            return _initialized;
        }

        final public function initializing():void {
            clear();
            _initializing = true;
            _dirty = false;
            removeEventListener(CollectionEvent.COLLECTION_CHANGE, dirtyCheckHandler);
        }

        final public function initialize():void {
            _initializing = false;
            _initialized = true;
            _dirty = false;
            addEventListener(CollectionEvent.COLLECTION_CHANGE, dirtyCheckHandler);
        }

        final public function uninitialize():void {
            clear();
            _initialized = false;
            _dirty = false;
            removeEventListener(CollectionEvent.COLLECTION_CHANGE, dirtyCheckHandler);
        }
        
        public function clone():PersistentMap {
        	var map:PersistentMap = new PersistentMap(_initialized);
        	map._metadata = _metadata;
        	map._dirty = _dirty;
        	if (_initialized) {
        		for each (var key:Object in keySet)
        			map[key] = this[key];
        	}
        	return map;
        }
        
        private function dirtyCheckHandler(event:CollectionEvent):void {
            if (!_initialized)
                return;
            if (event.kind == CollectionEventKind.ADD)
                _dirty = true;
            else if (event.kind == CollectionEventKind.REMOVE)
                _dirty = true;
            else if (event.kind == CollectionEventKind.UPDATE) {
                for (var i:uint = 0; i < event.items.length; i++) {
                    if (event.items[i] is PropertyChangeEvent) {
                        var pce:PropertyChangeEvent = event.items[i] as PropertyChangeEvent;
                        if ((pce.newValue is IUID || pce.oldValue is IUID) && (pce.newValue as IUID) != (pce.oldValue as IUID)) {
                            _dirty = true;
                            break;
                        }
                    }
                }
            }
        }


        override public function readExternal(input:IDataInput):void {
            _initialized = input.readObject() as Boolean;
            _metadata = input.readObject() as String;
            if (_initialized) {
                _dirty = input.readObject() as Boolean;
                super.readExternal(input);
            }
        }

        override public function writeExternal(output:IDataOutput):void {
            output.writeObject(_initialized);
            output.writeObject(_metadata);
            if (_initialized) {
                output.writeObject(_dirty);
                super.writeExternal(output);
            }
        }
    }
}