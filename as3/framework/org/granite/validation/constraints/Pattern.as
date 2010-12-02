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
	
	import org.granite.reflect.Annotation;
	import org.granite.validation.BaseConstraint;
	import org.granite.validation.ValidatorFactory;
	import org.granite.validation.helper.ConstraintHelper;
	import org.granite.validation.helper.ParameterDefinition;
	
	/**
	 * The ActionsScript3 implementation of the
	 * <code>javax.validation.constraints.Pattern</code> annotation
	 * validator.<p />
	 * 
	 * The annotated String must match the supplied regular expression.<p />
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
	 * <li>regexp (<b>required</b>): the regular expression to match.</li>
	 * <li>flags (optional): a comma separated flag list considered when resolving the
	 * 		regular expression.</li>
	 * </ul>
	 * 
	 * Supported types are:
	 * <ul>
	 * <li><code>String</code></li>
	 * </ul>
	 * 
	 * <code>null</code> elements are considered valid.<p />
	 * 
	 * Example:<p />
	 * <listing>
	 * [Pattern(message="{my.custom.message}", groups="path.to.MyGroup1, path.to.MyGroup2", regexp="[a-z]+", flags="CASE_INSENSITIVE, DOTALL")]
	 * public function get property():String {
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
	public class Pattern extends BaseConstraint {
		
		///////////////////////////////////////////////////////////////////////
		// Static constants.

		/**
		 * The default message key of this constraint. 
		 */
		public static const DEFAULT_MESSAGE:String = "{javax.validation.constraints.Pattern.message}";

		// Supported java.util.regex.Pattern flags.
		
		/**
		 * Enables case-insensitive matching (see
		 * <code>java.util.regex.Pattern#CASE_INSENSITIVE</code>).
		 */
		public static const CASE_INSENSITIVE:String = "CASE_INSENSITIVE";

		/**
		 * Enables dotall mode (see
		 * <code>java.util.regex.Pattern#DOTALL</code>).
		 */
		public static const DOTALL:String = "DOTALL";

		/**
		 * Enables multiline mode (see
		 * <code>java.util.regex.Pattern#MULTILINE</code>).
		 */
		public static const MULTILINE:String = "MULTILINE";
		
		private static const ACCEPTED_TYPES:Array = STRING_TYPES;

		private static const REGEXP_PARAMETER:ParameterDefinition = new ParameterDefinition(
			"regexp",
			String,
			null,
			false
		);
		
		private static const FLAGS_PARAMETER:ParameterDefinition = new ParameterDefinition(
			"flags",
			String,
			[],
			true,
			true
		);

		///////////////////////////////////////////////////////////////////////
		// Instance fields.

		private var _regexp:String;
		private var _flags:Array;

		///////////////////////////////////////////////////////////////////////
		// Properties.
		
		/**
		 * The regular expression to match.
		 */
		public function get regexp():String {
			return _regexp;
		}

		/**
		 * The array of flags considered when resolving the regular expression.
		 * 
		 * @see #CASE_INSENSITIVE
		 * @see #DOTALL
		 * @see #MULTILINE
		 */
		public function get flags():Array {
			return _flags;
		}

		///////////////////////////////////////////////////////////////////////
		// IConstraint implementation.

		/**
		 * @inheritDoc
		 */
		override public function initialize(annotation:Annotation, factory:ValidatorFactory):void {
			var params:Dictionary = internalInitialize(factory, annotation, DEFAULT_MESSAGE, [REGEXP_PARAMETER, FLAGS_PARAMETER]);
			_regexp = params[REGEXP_PARAMETER.name];
			_flags = params[FLAGS_PARAMETER.name];
		}
		
		/**
		 * @inheritDoc
		 */
		override public function validate(value:*):String {
			if (Null.isNull(value))
				return null;

			ConstraintHelper.checkValueType(this, value, ACCEPTED_TYPES);
			
			var flags:String = "";
			for each (var flag:String in _flags) {
				switch (flag) {
					case CASE_INSENSITIVE:
						flags += "i";
						break;
					case DOTALL:
						flags += "s";
						break;
					case MULTILINE:
						flags += "m";
						break;
					default:
						// ignored...
						break;
				}
			}
			
			var re:RegExp = new RegExp(_regexp, flags);

			if (!re.test(value as String))
				return message;
			
			return null;
		}
	}
}