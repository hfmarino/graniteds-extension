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

package org.granite.reflect.visitor.handlers {

	import flash.utils.getQualifiedClassName;
	
	import org.granite.reflect.DynamicProperty;
	import org.granite.reflect.visitor.IHandler;
	import org.granite.reflect.visitor.Visitable;

	/**
	 * Handles any <code>Object</code> instance without inheritance
	 * (eg: <code>new Object()</code> or <code>{key: 'value'}</code>).
	 * 
	 * @author Franck WOLFF
	 */
	public class ObjectHandler implements IHandler {
		
		public function canHandle(visitable:Visitable):Boolean {
			return getQualifiedClassName(visitable.value) === 'Object';
		}
		
		public function handle(visitable:Visitable, filter:Function):Array {
			var visitables:Array = new Array();

			for each (var name:String in (visitable.value as Object)) {
				var element:DynamicProperty = new DynamicProperty(visitable.value, name);
				var child:Visitable = new Visitable(visitable, element, element.value);
				if (filter(child))
					visitables.push(child);
			}
			
			return visitables;
		}
	}
}