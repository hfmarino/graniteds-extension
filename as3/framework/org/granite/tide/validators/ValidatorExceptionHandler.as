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

package org.granite.tide.validators {

    import flash.utils.flash_proxy;
	import flash.utils.Dictionary;
    
    import mx.collections.IList;
    import mx.collections.errors.ItemPendingError;
    import mx.messaging.messages.ErrorMessage;
    import mx.rpc.events.ResultEvent;
    import mx.utils.ObjectProxy;
    import mx.utils.ObjectUtil;
    import mx.utils.object_proxy;
    
    import org.granite.collections.IPersistentCollection;
    import org.granite.meta;
	import org.granite.validation.ConstraintViolationEvent;
	import org.granite.validation.ValidationEvent;
    import org.granite.tide.BaseContext;
    import org.granite.tide.IComponent;
    import org.granite.tide.IEntity;
    import org.granite.tide.IEntityManager;
    import org.granite.tide.IExceptionHandler;
    import org.granite.tide.IPropertyHolder;

    use namespace flash_proxy;
    use namespace object_proxy;
    

    /**
     * 	Validation exception handler that accepts Validation.Failed faults (server-side Tide converts Hibernate validator errors to this fault code)
     * 	and dispatches them on the context
     * 
     * 	@author William DRAI
     */
    public class ValidatorExceptionHandler implements IExceptionHandler {
        
        public static const VALIDATION_FAILED:String = "Validation.Failed"; 
        
        
        public function accepts(emsg:ErrorMessage):Boolean {
            return emsg.faultCode == VALIDATION_FAILED;
        }

        public function handle(context:BaseContext, emsg:ErrorMessage):void {
			var invalidValues:Array = emsg.extendedData.invalidValues as Array;
			if (invalidValues) {
				var bean:Object, rootBean:Object;
				var violationsMap:Dictionary = new Dictionary();
				for each (var iv:InvalidValue in invalidValues) {
					rootBean = context.meta_getCachedObject(iv.rootBean);
					bean = iv.bean ? context.meta_getCachedObject(iv.bean) : null;
					
					var violations:Array = violationsMap[rootBean];
					if (violations == null) {
						violations = [];
						violationsMap[rootBean] = violations;
					}
					var violation:ServerConstraintViolation = new ServerConstraintViolation(iv, rootBean, bean);
					violations.push(violation);
				}
				
				for (rootBean in violationsMap) {
					rootBean.dispatchEvent(new ValidationEvent(ValidationEvent.START_VALIDATION));
					
					violations = violationsMap[bean];
					for each (violation in violations)
						rootBean.dispatchEvent(new ConstraintViolationEvent(violation));
					
					rootBean.dispatchEvent(new ValidationEvent(ValidationEvent.END_VALIDATION));
				}
			}
        }
    }
}
