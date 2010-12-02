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

package org.granite.util {

	import flash.utils.Dictionary;

	/**
	 * Static utility methods for <code>Dictionary</code> manipulation.
	 * 
	 * @author Franck WOLFF 
	 */
	public class DictionaryUtil {

		public static function getValues(dictionary:Dictionary):Array {
			var values:Array = new Array(), value:*;
			for each (value in dictionary)
				values.push(value);
			return values;
		}

		public static function getKeys(dictionary:Dictionary):Array {
			var keys:Array = new Array(), key:*;
			for (key in dictionary)
				keys.push(key);
			return keys;
		}

		public static function getLength(dictionary:Dictionary):uint {
			var length:uint = 0, key:*;
			for (key in dictionary)
				length++;
			return length;
		}

		public static function getEntries(dictionary:Dictionary):Array {
			var entries:Array = new Array(), key:*;
			for (key in dictionary)
				entries.push([key, dictionary[key]]);
			return entries;
		}
	}
}
