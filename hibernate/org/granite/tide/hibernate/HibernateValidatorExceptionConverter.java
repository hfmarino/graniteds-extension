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

package org.granite.tide.hibernate;

import java.util.Map;

import org.granite.messaging.service.ExceptionConverter;
import org.granite.messaging.service.ServiceException;
import org.hibernate.validator.InvalidStateException;
import org.hibernate.validator.InvalidValue;


public class HibernateValidatorExceptionConverter implements ExceptionConverter {
    
    public static final String VALIDATION_FAILED = "Validation.Failed";
    
    private Class<InvalidStateException> INVALID_STATE_EXCEPTION = InvalidStateException.class;
    

    public boolean accepts(Throwable t, Throwable finalException) {
        return t.getClass().equals(INVALID_STATE_EXCEPTION);
    }

    public ServiceException convert(Throwable t, String detail, Map<String, Object> extendedData) {
        InvalidValue[] invalidValues = ((InvalidStateException)t).getInvalidValues();
        extendedData.put("invalidValues", HibernateValidator.convertInvalidValues(invalidValues));
        
        ServiceException se = new ServiceException(VALIDATION_FAILED, t.getMessage(), detail, t);
        se.getExtendedData().putAll(extendedData);
        return se;
    }

}
