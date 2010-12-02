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

package org.granite.messaging.service.tide;

import java.util.Set;
import java.util.regex.Pattern;


/**
 * @author Franck WOLFF
 */
public class TideComponentNameMatcher implements TideComponentMatcher {
    
    private final boolean disabled;
    private final Pattern pattern;
    
    public TideComponentNameMatcher(String name, boolean disabled) {
        this.pattern = Pattern.compile(name);
        this.disabled = disabled;
    }
    
    public boolean matches(String name, Set<Class<?>> classes, Object instance, boolean disabled) {
        return disabled == this.disabled && pattern.matcher(name).matches();
    }
    
    @Override
    public String toString() {
    	return "Name matcher: " + pattern.pattern() + (disabled ? " (disabled)" : "");
    }
    
    public static void main(String[] args) {
        TideComponentNameMatcher matcher = new TideComponentNameMatcher(".*Service", false);
        System.out.println(matcher.matches("helloService", null, null, false));
    }
}
