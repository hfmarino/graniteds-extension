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

package org.granite.validation.constraints {

	import flash.utils.Dictionary;
	
	import org.granite.math.BigDecimal;
	import org.granite.reflect.Annotation;
	import org.granite.validation.BaseConstraint;
	import org.granite.validation.ValidatorFactory;
	import org.granite.validation.helper.ConstraintHelper;
	import org.granite.validation.helper.ParameterDefinition;
	
	/**
	 * The ActionsScript3 implementation of the
	 * <code>javax.validation.constraints.DecimalMax</code> annotation
	 * validator.<p />
	 * 
	 * The annotated element must be a number whose value must be lower or
	 * equal to the specified maximum.<p />
	 * 
	 * Accepted arguments are:
	 * <ul>
	 * <li>message (optional): used to create the error message (override the
	 * 		default message).</li>
	 * <li>groups (optional): a comma separated list of fully qualified interface names that
	 * 		specifies the processing groups with which the constraint declaration is associated.</li>
	 * <li>payload (optional): a comma separated list of <code>String</code> that specifies the
	 * 		payloads with which the constraint declaration is associated (unlike the Java
	 * 		implementation, payloads are arbitrary <code>String</code>s and does not represent
	 * 		necessarily existing class names).</li>
	 * <li>value (<b>required</b>): the maximum accepted value.</li>
	 * </ul>
	 * 
	 * Supported types are:
	 * <ul>
	 * <li><code>org.granite.math.BigDecimal</code></li>
	 * <li><code>org.granite.math.BigInteger</code></li>
	 * <li><code>org.granite.math.Long</code></li>
	 * <li><code>Number</code> (with possible rounding issues)</li>
	 * <li><code>int</code></li>
	 * <li><code>uint</code></li>
	 * <li><code>String</code></li>
	 * </ul>
	 * 
	 * <code>null</code> elements are considered valid.<p />
	 * 
	 * Example:<p />
	 * <listing>
	 * [DecimalMax(message="{my.custom.message}", groups="path.to.MyGroup1, path.to.MyGroup2", value="123.5")]
	 * public function get property():BigDecimal {
	 *     ...
	 * }</listing>
	 * 
	 * General notes on escaping:
	 * <ul>
	 * <li>Double quotes: all double quotes (<code>"</code>) in argument values <b>must</b> be escaped
	 * 		by using the XML entity (<code>&amp;quot;</code>).</li>
	 * <li>Ampersand: all ampersands (<code>&amp;</code>) in argument values should be escaped by
	 * 		using the XML entity (<code>&amp;amp;</code>).</li>
	 * <li>Less and greater than: all "less than" (<code>&lt;</code>) characters in argument values
	 * 		<b>must</b> be escaped by using the XML entity (<code>&amp;lt;</code>). All "greater than"
	 * 		(<code>&gt;</code>) characters in argument values should be escaped by using the XML entity
	 * 		(<code>&amp;gt;</code>)</li>
	 * <li>White spaces: since all argument values are trimed, you may use the pseudo XML entity
	 * 		(<code>&amp;space;</code>) in order to keep leading or trailing white spaces in literals.</li>
	 * <li>Comma: arguments specified as comma separated String lists (such as <code>groups</code>)
	 * 		may use the pseudo XML entity (<code>&amp;comma;</code>) in order to keep comma characters
	 * 		in literals.</li>
	 * </ul>
	 *
	 * @author Franck WOLFF
	 */
	public class DecimalMax extends BaseConstraint {
		
		///////////////////////////////////////////////////////////////////////
		// Static constants.

		/**
		 * The default message key of this constraint. 
		 */
		public static const DEFAULT_MESSAGE:String = "{javax.validation.constraints.DecimalMax.message}";

		private static const ACCEPTED_TYPES:Array = NUMBER_STRING_TYPES;

		private static const VALUE_PARAMETER:ParameterDefinition = new ParameterDefinition(
			"value",
			BigDecimal,
			null,
			false
		);
		
		///////////////////////////////////////////////////////////////////////
		// Instance fields.

		private var _value:BigDecimal;
		
		///////////////////////////////////////////////////////////////////////
		// Properties.
		
		/**
		 * The maximum value as specified in the annotation arguments.
		 */
		public function get value():BigDecimal {
			return _value;
		}

		///////////////////////////////////////////////////////////////////////
		// IConstraint implementation.

		/**
		 * @inheritDoc
		 */
		override public function initialize(annotation:Annotation, factory:ValidatorFactory):void {
			var params:Dictionary = internalInitialize(factory, annotation, DEFAULT_MESSAGE, [VALUE_PARAMETER]);
			_value = params[VALUE_PARAMETER.name];
		}
		
		/**
		 * @inheritDoc
		 */
		override public function validate(value:*):String {
			if (Null.isNull(value) || value === Number.NEGATIVE_INFINITY)
				return null;
			
			if (value === Number.POSITIVE_INFINITY)
				return message;
			
			ConstraintHelper.checkValueType(this, value, ACCEPTED_TYPES);

			var number:BigDecimal = null;

			if (value is BigDecimal)
				number = (value as BigDecimal);
			else {
				try {
					number = new BigDecimal(value);
				}
				catch (e:Error) {
				}
			}

			if (number == null || number.compareTo(_value) > 0)
				return message;

			return null;
		}
	}
}