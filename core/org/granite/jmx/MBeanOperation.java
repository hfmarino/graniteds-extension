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

package org.granite.jmx;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Inherited
@Retention(value=RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
/**
 * The MBeanOperation annotation may be placed on any method that
 * will act as an OpenMBeanOperation.
 * 
 * @author Franck WOLFF
 */
public @interface MBeanOperation {

	/**
	 * The description that will be shown in a JMX console for this MBean operation.
	 * 
	 * @return the description that will be shown in a JMX console for
	 * 		this MBean operation.
	 */
	String description();
	
	/**
	 * The impact of this MBean operation.
	 * 
	 * @return the impact of this MBean operation.
	 */
	Impact impact() default Impact.UNKNOWN;
	
	/**
	 * Operation impact. See {@link javax.management.MBeanOperationInfo}.
	 * 
	 * @author Franck WOLFF
	 */
	public static enum Impact {
		ACTION,
		ACTION_INFO,
		INFO,
		UNKNOWN
	}
}
