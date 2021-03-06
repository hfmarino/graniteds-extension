/**
 * Generated by Gas3 v1.1.0 (Granite Data Services) on Sat Jul 26 17:58:20 CEST 2008.
 *
 * WARNING: DO NOT CHANGE THIS FILE. IT MAY BE OVERRIDDEN EACH TIME YOU USE
 * THE GENERATOR. CHANGE INSTEAD THE INHERITED CLASS (Contact.as).
 */

package org.granite.tide.test.data {
	
	import mx.collections.ArrayCollection;
	
	import org.granite.persistence.PersistentSet;
	import org.granite.tide.BaseContext;
	import org.granite.tide.test.Contact;
	import org.granite.tide.test.Person;
	

	[Name("entityConversation", scope="conversation")]
    public class EntityConversation {
    	
    	[In]
    	public var context:BaseContext;
    	
    	[Observer("startConversation")]
    	public function start(person:Person):void {
    		context.person = person;
    	}
    	
    	[Observer("updateConversation")]
    	public function update():void {
    		var person:Person = new Person();
        	person.id = 1;
        	person.version = 0;
        	person.uid = "P1";
        	person.lastName = "Jojo";
        	person.contacts = new PersistentSet();
        	var contact1:Contact = new Contact();
        	contact1.person = person;
        	contact1.id = 1;
        	contact1.version = 0;
        	contact1.uid = "C1";
        	contact1.email = "jojo@jojo.net";
        	person.contacts.addItem(contact1);
    		var contact2:Contact = new Contact();
    		contact2.id = 2;
    		contact2.version = 0;
    		contact2.uid = "C2";
    		contact2.person = person;
    		contact2.email = "toto@toto.net";
        	person.contacts.addItem(contact2);
    		context.meta_mergeExternalData(person);
    	}
    	
    	[Observer("endConversation")]
    	public function end():void {
    		context.meta_end(true);
    	}
    	
    	[Observer("endMergeConversation")]
    	public function endMerge():void {
    		context.person.lastName = "Juju";
    		context.meta_end(true);
    	}
    	
    	[Observer("endMergeConversation2")]
    	public function endMerge2():void {
    		var person:Person = new Person();
        	person.id = 1;
        	person.version = 1;
        	person.uid = "P1";
        	person.lastName = "Juju";
        	person.contacts = new ArrayCollection();
        	context.meta_mergeExternalData(person);
    		context.meta_end(true);
    	}
    }
}
