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

import java.io.File;

import javax.jms.JMSException;

import net.timewalker.ffmq4.FFMQException;
import net.timewalker.ffmq4.FFMQSecurityException;
import net.timewalker.ffmq4.FFMQServerSettings;
import net.timewalker.ffmq4.utils.Settings;
import net.timewalker.ffmq4.utils.xml.XMLDescriptorReader;

/**
 * XMLSecurityConnector
 */
public final class XMLSecurityConnector implements SecurityConnector
{
    public static final String DEFAULT_SECURITY_FILE = "../conf/security.xml";
    
    private XMLSecurityDescriptor descriptor;
    
    /**
     * Constructor
     */
    public XMLSecurityConnector( Settings settings ) throws JMSException
    {
        String securityDescriptorFilePath = settings.getStringProperty(FFMQServerSettings.SECURITY_CONNECTOR_XML_SECURITY, DEFAULT_SECURITY_FILE);
        File securityDescriptorFile = new File(securityDescriptorFilePath);
        if (!securityDescriptorFile.canRead())
            throw new FFMQException("Cannot access security descriptor file : "+securityDescriptorFile.getAbsolutePath(),"FS_ERROR");
        
        this.descriptor = (XMLSecurityDescriptor)new XMLDescriptorReader().read(securityDescriptorFile, XMLSecurityDescriptorHandler.class);
        this.descriptor.check();
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.security.SecurityConnector#getContext(java.lang.String, java.lang.String)
     */
	@Override
	public SecurityContext getContext(String userName, String password) throws FFMQSecurityException 
	{
		if (userName == null)
			throw new FFMQSecurityException("User name not specified","INVALID_SECURITY_DESCRIPTOR");
		
		// Check user and password
		User user = descriptor.getUser(userName);
        if (user == null || !user.getPassword().equals(password))
            throw new FFMQSecurityException("Invalid user/password","INVALID_SECURITY_DESCRIPTOR");
        
        return user;
	}
}
