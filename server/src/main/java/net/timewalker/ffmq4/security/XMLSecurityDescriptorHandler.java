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

import net.timewalker.ffmq4.security.Privilege;
import net.timewalker.ffmq4.utils.descriptor.AbstractDescriptor;
import net.timewalker.ffmq4.utils.xml.AbstractXMLDescriptorHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * XMLSecurityDescriptorHandler
 */
public final class XMLSecurityDescriptorHandler extends AbstractXMLDescriptorHandler
{    
    private XMLSecurityDescriptor descriptor = new XMLSecurityDescriptor();
    private User currentUser;
    private Privilege currentPrivilege;
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.utils.xml.DescriptorHandler#getDescriptor()
     */
    @Override
	public AbstractDescriptor getDescriptor()
    {
        return descriptor;
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.utils.xml.DescriptorHandler#before(java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
	protected void before(String name, String currentPath, Attributes attributes) throws SAXException
    {
        if (currentPath.equals("security/users/user"))
        {
            currentUser = new User();
            currentUser.setName(getRequired(attributes, "name"));
            currentUser.setPassword(getRequired(attributes, "password"));
        }
        if (currentPath.equals("security/users/user/privilege"))
        {
            currentPrivilege = new Privilege();
            currentPrivilege.setResourcePattern(getRequired(attributes, "resource"));
            currentPrivilege.setActions(getRequired(attributes, "actions"));
        }
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.utils.xml.DescriptorHandler#onNode(java.lang.String, java.lang.String)
     */
    @Override
	protected void onNode(String name, String currentPath) throws SAXException
    {
        if (currentPath.equals("security") ||
            currentPath.equals("security/users"))
        {
            // Nothing to do
        }
        else if (currentPath.equals("security/users/user"))
        {
            descriptor.addUser(currentUser);
            currentUser = null;
        }
        else if (currentPath.equals("security/users/user/privilege"))
        {
            currentUser.addPrivilege(currentPrivilege);
            currentPrivilege = null;
        }
        else
            throw new SAXException("Unexpected node : " + name + " (" + currentPath + ")");
    }
}