package org.granite.tide.test;

import org.granite.tide.invocation.InvocationResult;
import org.granite.tide.test.home.Entity1;
import org.granite.tide.test.home.Entity2;
import org.junit.Assert;
import org.junit.Test;


public class TideHomeTest extends TideTestCase {
	
	@Test
    public void testHomeCallGDS566() {
    	Entity1 entity1 = new Entity1();
    	entity1.setId(1200L);
    	entity1.setSomeObject("$$Proxy$$test");
    	Entity2 entity2 = new Entity2();
    	entity2.setId(1201L);
    	
        InvocationResult result = invokeComponent("baseHome", "update", 
    		new Object[] {}, 
    		new Object[] { 
				new Object[] { "baseHome", "id", entity1.getId() }, 
    			new Object[] { "baseHome", "instance", entity1 } 
    		}, 
    		new String[] { "baseHome.instance" }, 
    		null);
        
        Assert.assertEquals("Entity1 id", 1200L, (long)((Entity1)result.getResults().get(0).getValue()).getId());
        Assert.assertEquals("Entity1 obj", "$$Proxy$$test", ((Entity1)result.getResults().get(0).getValue()).getSomeObject());
        
        result = invokeComponent("baseHome", "update", 
    		new Object[] {}, 
    		new Object[] { 
				new Object[] { "baseHome", "id", entity2.getId() }, 
    			new Object[] { "baseHome", "instance", entity2 } 
    		}, 
    		new String[] { "baseHome.instance", "baseHome.instance.someObject" }, 
    		null);
        
        Assert.assertEquals("Entity2 id", 1201L, (long)((Entity2)result.getResults().get(0).getValue()).getId());
        
        result = invokeComponent("baseHome", "update", 
    		new Object[] {}, 
    		new Object[] { 
				new Object[] { "baseHome", "id", entity1.getId() }, 
    			new Object[] { "baseHome", "instance", entity1 } 
    		}, 
    		new String[] { "baseHome.instance", "baseHome.instance.someObject" }, 
    		null);
        
        Assert.assertEquals("Entity1 id", 1200L, (long)((Entity1)result.getResults().get(0).getValue()).getId());
        Assert.assertEquals("Entity1 obj", "test", ((Entity1)result.getResults().get(0).getValue()).getSomeObject());
    }
}
