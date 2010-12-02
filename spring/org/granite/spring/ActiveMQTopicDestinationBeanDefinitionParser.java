/*
  GRANITE DATA SERVICES
  Copyright (C) 2007-2009 ADEQUATE SYSTEMS SARL

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

package org.granite.spring;

import java.util.List;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.Conventions;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author William Drai
 */
public class ActiveMQTopicDestinationBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    @SuppressWarnings("unchecked")
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
    	builder.setLazyInit(false);
        
        mapOptionalAttributes(element, parserContext, builder, "id", "name", "connection-factory", "jndi-name", 
        		"acknowledge-mode", "transacted-sessions", "no-local", "session-selector", "broker-url", "create-broker", 
        		"wait-for-start", "durable", "file-store-root");
        
        Object sourceElement = parserContext.extractSource(element);
        ManagedList roles = new ManagedList();
        roles.setSource(sourceElement);
        List<Element> rolesElements = DomUtils.getChildElementsByTagName(element, "roles");
        for (Element rolesElement : rolesElements) {
            List<Element> valueElements = DomUtils.getChildElementsByTagName(rolesElement, "role");
            for (Element valueElement : valueElements)
            	roles.add(valueElement.getTextContent());
        }
        if (!roles.isEmpty())
        	builder.addPropertyValue("roles", roles);        
    }

    @Override
    protected String getBeanClassName(Element element) {
        return "org.granite.spring.ActiveMQTopicDestination";
    }

    static void mapOptionalAttributes(Element element, ParserContext parserContext, BeanDefinitionBuilder builder, String... attrs) {
        for (String attr : attrs) {
            String value = element.getAttribute(attr);
            if (StringUtils.hasText(value)) {
                String propertyName = Conventions.attributeNameToPropertyName(attr);
                if (validateProperty(element, parserContext, propertyName, attr)) {
                    builder.addPropertyValue(propertyName, value);
                }
            }
        }
    }

    private static boolean validateProperty(Element element, ParserContext parserContext, String propertyName, String attr) {
        if (!StringUtils.hasText(propertyName)) {
            parserContext.getReaderContext().error(
                "Illegal property name trying to convert from attribute '" + attr + "' : cannot be null or empty.",
                parserContext.extractSource(element));
            return false;
        }
        return true;
    }
}
