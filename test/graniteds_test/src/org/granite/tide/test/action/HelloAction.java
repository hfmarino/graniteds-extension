package org.granite.tide.test.action;

import org.granite.tide.test.entity.Person;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;


@Name("hello")
@Scope(ScopeType.EVENT)
public class HelloAction {
    
    @In
    private Person person;
    
    public String hello() {
        return "Hello " + person.getLastName();
    }
}
