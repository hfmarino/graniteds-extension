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

package org.granite.tide.impl {

	import flash.utils.Dictionary;
	
	import org.granite.reflect.Annotation;
	import org.granite.reflect.Type;

    /**
     * 	Cache used with DescribeTypeCahe for component descriptions
     *  
     * 	@author William DRAI
     */
    [ExcludeClass]
    public class ComponentInfo {
    	
		private static const _cache:Dictionary = new Dictionary(true);
		
    	private var _name:String;
    	private var _create:String;
    	private var _module:String;
    	private var _scope:String;
    	private var _restrict:String;
    	private var _events:Array;
    	
    	
		public static function forType(type:Type):ComponentInfo {
			var info:ComponentInfo = _cache[type];
			if (info == null) {
				info = new ComponentInfo(type, new Restrictor());
				_cache[type] = info;
			}
			return info;
		}
		
    	function ComponentInfo(type:Type, restrictor:*):void {
			
			if (!(restrictor is Restrictor))
				throw new Error("Illegal call to constructor");
			
			var name:Annotation = type.getAnnotation('Name');
			if (name) {
				_name = name.getArgValue('', '');
				_create = name.getArgValue('create', '');
				_module = name.getArgValue('module', '');
				_scope = name.getArgValue('scope', '');
				_restrict = name.getArgValue('restrict', '');
				
			}
			
			for each (var managedEvent:Annotation in type.getAnnotations(false, 'ManagedEvent')) {
				if (_events == null)
					_events = new Array();
	        	_events.push(managedEvent.getArgValue('name', ''));
			}
    	}
    	
    	public function get name():String {
    		return _name;
    	}
    	
    	public function get create():String {
    		return _create;
    	}
    	
    	public function get module():String {
    		return _module;
    	}
    	
    	public function get scope():String {
    		return _scope;
    	}
    	
    	public function get restrict():String {
    		return _restrict;
    	}
    	
    	public function get events():Array {
    		return _events;
    	}
    }
}
class Restrictor {
}