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

package org.granite.tide.rpc {

    import mx.rpc.IResponder;
    import mx.rpc.AbstractOperation;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.ITideResponder;
    

    /**
     * @author William DRAI
     */
    [ExcludeClass]
    public class ComponentResponder implements IResponder {
        
        private var _sourceContext:BaseContext;
        private var _sourceModulePrefix:String;
        private var _componentName:String;
        private var _operation:String;
        private var _clearOp:AbstractOperation;
        private var _resultHandler:Function;
        private var _faultHandler:Function;
        private var _tideResponder:ITideResponder;
        private var _info:Object;
        
        
        public function ComponentResponder(sourceContext:BaseContext, resultHandler:Function, faultHandler:Function, 
            componentName:String = null, operation:String = null, clearOp:AbstractOperation = null, tideResponder:ITideResponder = null, info:Object = null):void {
            _sourceContext = sourceContext;
            _sourceModulePrefix = _sourceContext.meta_tide.currentModulePrefix;
            _componentName = componentName;
            _operation = operation;
            _clearOp = clearOp;
            _resultHandler = resultHandler;
            _faultHandler = faultHandler;
            _tideResponder = tideResponder;
            _info = info;   
        }
        
        
	    public function result(data:Object):void {
	        if (_info != null)
		        _resultHandler(_sourceContext, _sourceModulePrefix, data, _info, _componentName, _operation, _tideResponder);
		    else
		        _resultHandler(_sourceContext, _sourceModulePrefix, data, _componentName, _operation, _tideResponder);
		    
		    if (_clearOp != null)
		        _clearOp.clearResult();
	    }
	    
	    public function fault(info:Object):void {
	        if (_info != null)
		        _faultHandler(_sourceContext, _sourceModulePrefix, info, _info, _componentName, _operation, _tideResponder);
	        else
		        _faultHandler(_sourceContext, _sourceModulePrefix, info, _componentName, _operation, _tideResponder);
		    
		    if (_clearOp != null)
		        _clearOp.clearResult();
	    }
    }
}
