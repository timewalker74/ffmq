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
package net.timewalker.ffmq4.security;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.jms.JMSException;

import net.timewalker.ffmq4.utils.descriptor.AbstractXMLBasedDescriptor;

/**
 * XMLSecurityDescriptor
 */
public final class XMLSecurityDescriptor extends AbstractXMLBasedDescriptor
{
    private Map<String,User> userMap = new Hashtable<>();
    
    public User getUser( String userName )
    {
        return userMap.get(userName);
    }
    
    public void addUser( User user )
    {
        userMap.put(user.getName(), user);
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.utils.Checkable#check()
     */
    @Override
	public void check() throws JMSException
    {
        Iterator<User> users = userMap.values().iterator();
        while (users.hasNext())
        {
            User user = users.next();
            user.check();
        }
    }
}
