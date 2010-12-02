/*
  GRANITE DATA SERVICES
  Copyright (C) 2007 ADEQUATE SYSTEMS SARL

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

package test.granite.ejb3.service;

import org.granite.tide.annotations.TideEnabled;
import org.granite.tide.data.DataEnabled;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.framework.EntityQuery;
import test.granite.ejb3.entity.Person;

import static org.granite.tide.data.DataEnabled.PublishMode.ON_SUCCESS;


@Restrict( "#{identity.loggedIn}" )
@TideEnabled
@DataEnabled( topic = "addressBookTopic", params = AddressBookParams.class, publish = ON_SUCCESS )
public class PeopleQuery extends EntityQuery<Person>
{

    private static final long serialVersionUID = 1L;

    @Override
    protected String getCountEjbql()
    {
        return super.getCountEjbql().replace( "count(*)", "count(p)" );
    }
}
