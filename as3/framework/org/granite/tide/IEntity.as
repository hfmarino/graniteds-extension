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

package org.granite.tide {
	
	import mx.core.IPropertyChangeNotifier;
	
	/**
	 * 	Interface for Tide managed entities
	 *  In general it will be implemented by entities generated by gas3.
	 *  
	 *  Otherwise it can also be implemented by simple ActionScript entities with :
	 *   
	 * 
	 * 	@author William DRAI
	 */
	public interface IEntity extends IPropertyChangeNotifier {
		
//		/**
//		 *	Merge with an modified entity instance coming from another context of from the server
//		 * 
//		 * 	@param em entity manager context
//		 *  @param entity external entity
//		 */ 
//        function meta_merge(em:IEntityManager, entity:*):void;		
//		
//		/**
//		 *	Defines the EntityManager context for this entity
//		 * 
//		 * 	@param em entity manager context
//		 */ 
//		function meta_setEntityManager(em:IEntityManager):void;
//		
//		/**
//		 *	Return the EntityManager context for this entity
//		 * 
//		 * 	@return the entity manager
//		 */ 
//		function meta_getEntityManager():IEntityManager;
	}
}
