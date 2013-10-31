/*
 * This file is part of FFMQ.
 *
 * FFMQ is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * FFMQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FFMQ; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package net.timewalker.ffmq3.security;

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;

import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.management.InvalidDescriptorException;
import net.timewalker.ffmq3.security.Privilege;
import net.timewalker.ffmq3.security.SecurityContext;
import net.timewalker.ffmq3.utils.Checkable;
import net.timewalker.ffmq3.utils.StringTools;

/**
 * UserImpl
 */
public class User implements SecurityContext, Checkable
{
    private String name;
    private String password;
    private List privileges = new ArrayList();
    
    /**
     * Constructor
     */
    public User()
    {
        super();
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password)
    {
        this.password = password;
    }
    
    /**
	 * Add a privilege to this context
	 * @param privilege the privilege to add
	 */
	public void addPrivilege( Privilege privilege )
	{
		privileges.add(privilege);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.security.SecurityContext#checkPermission(java.lang.String, java.lang.String)
	 */
	public void checkPermission(String resourceName, String action) throws JMSException 
	{
		for (int i = 0; i < privileges.size(); i++)
		{
			Privilege privilege = (Privilege)privileges.get(i);
			if (privilege.matches(resourceName, action))
				return;
		}
		throw new FFMQException("Access denied to resource '"+resourceName+"' for action '"+action+"'","ACCESS_DENIED");
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.utils.Checkable#check()
	 */
	public void check() throws JMSException
    {
	    if (StringTools.isEmpty(name))
	        throw new InvalidDescriptorException("Missing user name in security descriptor");
	    if (password == null)
	        throw new InvalidDescriptorException("Missing password definition for user "+name);
    }
}
