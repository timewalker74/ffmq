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
package net.timewalker.ffmq4.management.peer;

import javax.jms.JMSException;

import net.timewalker.ffmq4.management.InvalidDescriptorException;
import net.timewalker.ffmq4.utils.Checkable;

/**
 * <p>Implementation of a JMS peer descriptor.</p>
 */
public final class PeerDescriptor implements Checkable
{
    // Attributes
	private String jdniInitialContextFactoryName;
	private String jndiConnectionFactoryName;
	private String providerURL;
	private String userName;
	private String password;
	
	public String getJdniInitialContextFactoryName()
	{
		return jdniInitialContextFactoryName;
	}

	public void setJdniInitialContextFactoryName(String jdniInitialContextFactoryName)
	{
		this.jdniInitialContextFactoryName = jdniInitialContextFactoryName;
	}

	public String getJndiConnectionFactoryName()
	{
		return jndiConnectionFactoryName;
	}

	public void setJndiConnectionFactoryName(String jndiConnectionFactoryName)
	{
		this.jndiConnectionFactoryName = jndiConnectionFactoryName;
	}

	public String getProviderURL()
	{
		return providerURL;
	}

	public void setProviderURL(String providerURL)
	{
		this.providerURL = providerURL;
	}

	public String getUserName()
	{
		return userName;
	}

	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	/* (non-Javadoc)
	 * @see net.timewalker.ffmq4.utils.Checkable#check()
	 */
	@Override
	public void check() throws JMSException
	{
		if (jdniInitialContextFactoryName == null)
			throw new InvalidDescriptorException("Missing peer property : 'jdniInitialContextFactoryName'");
		if (jndiConnectionFactoryName == null)
			throw new InvalidDescriptorException("Missing peer property : 'jndiConnectionFactoryName'");
		if (providerURL == null)
			throw new InvalidDescriptorException("Missing peer property : 'providerURL'");
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
        
        sb.append("jdniInitialContextFactoryName=");
        sb.append(jdniInitialContextFactoryName);
        sb.append(" jndiConnectionFactoryName=");
        sb.append(jndiConnectionFactoryName);
        sb.append(" providerURL=");
        sb.append(providerURL);
        sb.append(" userName=");
        sb.append(userName);
        sb.append(" password=");
        sb.append(password);
        
        return sb.toString();
	}
}
