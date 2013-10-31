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
package net.timewalker.ffmq3.jndi;

import java.net.URI;
import java.util.Hashtable;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.naming.Context;

import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.local.FFMQEngine;
import net.timewalker.ffmq3.remote.connection.RemoteQueueConnection;
import net.timewalker.ffmq3.transport.PacketTransportType;

/**
 * <p>Implementation of a JMS {@link QueueConnectionFactory}</p>
 */
public final class FFMQQueueConnectionFactory extends FFMQConnectionFactory implements QueueConnectionFactory
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor
	 */
	public FFMQQueueConnectionFactory()
	{
		super();
	}
	
    /**
     * Constructor
     */
    public FFMQQueueConnectionFactory(Hashtable<String,Object> environment)
    {
        super(environment);
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.QueueConnectionFactory#createQueueConnection()
     */
    @Override
	public QueueConnection createQueueConnection() throws JMSException
    {
    	String username = getStringProperty(Context.SECURITY_PRINCIPAL,null); 
    	String password = getStringProperty(Context.SECURITY_CREDENTIALS,null);
    	
        return createQueueConnection(username,password);
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.QueueConnectionFactory#createQueueConnection(java.lang.String, java.lang.String)
     */
    @Override
	public QueueConnection createQueueConnection(String userName, String password) throws JMSException
    {
        URI providerURL = getProviderURI();
        
        String scheme = providerURL.getScheme();
        if (scheme.equals(PacketTransportType.VM))
        {
            String engineName = providerURL.getHost();
            return FFMQEngine.getDeployedInstance(engineName).openQueueConnection(userName, password, clientID);
        }
        else 
        if (scheme.equals(PacketTransportType.TCP) ||
            scheme.equals(PacketTransportType.TCPS) ||
            scheme.equals(PacketTransportType.TCPNIO))
        {
            return new RemoteQueueConnection(providerURL, userName, password, clientID);
        }
        else
            throw new FFMQException("Unknown transport protocol : " + scheme,"INVALID_TRANSPORT_PROTOCOL");
    }
}
