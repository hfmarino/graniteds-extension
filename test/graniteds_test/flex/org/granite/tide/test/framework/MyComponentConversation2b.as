/**
 * Generated by Gas3 v1.1.0 (Granite Data Services) on Sat Jul 26 17:58:20 CEST 2008.
 *
 * WARNING: DO NOT CHANGE THIS FILE. IT MAY BE OVERRIDDEN EACH TIME YOU USE
 * THE GENERATOR. CHANGE INSTEAD THE INHERITED CLASS (Contact.as).
 */

package org.granite.tide.test.framework {

	[Name("myComponent2b", scope="conversation")]
	[Bindable]
    public class MyComponentConversation2b {
    	
    	public var triggered:Boolean = false;
    	
    	[Observer("renext")]
    	public function renext():void {
    		triggered = true;
    	}
    }
}