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
package net.timewalker.ffmq4.jndi;

import java.net.URI;
import java.util.Hashtable;

import javax.jms.JMSException;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.naming.Context;

import net.timewalker.ffmq4.FFMQException;
import net.timewalker.ffmq4.local.FFMQEngine;
import net.timewalker.ffmq4.remote.connection.RemoteTopicConnection;
import net.timewalker.ffmq4.transport.PacketTransportType;

/**
 * <p>Implementation of a JMS {@link TopicConnectionFactory}</p>
 */
public final class FFMQTopicConnectionFactory extends FFMQConnectionFactory implements TopicConnectionFactory
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor
	 */
	public FFMQTopicConnectionFactory()
	{
		super();
	}
	
    /**
     * Constructor
     */
    public FFMQTopicConnectionFactory(Hashtable<String,Object> environment)
    {
        super(environment);
    }

    /* (non-Javadoc)
     * @see javax.jms.TopicConnectionFactory#createTopicConnection()
     */
    @Override
	public TopicConnection createTopicConnection() throws JMSException
    {
    	String username = getStringProperty(Context.SECURITY_PRINCIPAL,null); 
    	String password = getStringProperty(Context.SECURITY_CREDENTIALS,null);
    	
        return createTopicConnection(username,password);
    }

    /* (non-Javadoc)
     * @see javax.jms.TopicConnectionFactory#createTopicConnection(java.lang.String, java.lang.String)
     */
    @Override
	public TopicConnection createTopicConnection(String userName, String password) throws JMSException
    {
        URI providerURL = getProviderURI();     
        
        String scheme = providerURL.getScheme();
        if (scheme.equals(PacketTransportType.VM))
        {
            String engineName = providerURL.getHost();
            return FFMQEngine.getDeployedInstance(engineName).openTopicConnection(userName, password, clientID);
        }
        else 
    	if (scheme.equals(PacketTransportType.TCP) ||
            scheme.equals(PacketTransportType.TCPS) ||
            scheme.equals(PacketTransportType.TCPNIO))
    	{
            return new RemoteTopicConnection(providerURL, userName, password, clientID);
    	}
        else
            throw new FFMQException("Unknown transport protocol : " + scheme,"INVALID_TRANSPORT_PROTOCOL");
    }
}
