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
package net.timewalker.ffmq4.listeners.tcp;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.timewalker.ffmq4.FFMQServerSettings;
import net.timewalker.ffmq4.jmx.JMXAgent;
import net.timewalker.ffmq4.listeners.AbstractClientListener;
import net.timewalker.ffmq4.local.FFMQEngine;
import net.timewalker.ffmq4.utils.Settings;

/**
 * AbstractTcpClientListener
 */
public abstract class AbstractTcpClientListener extends AbstractClientListener
{
	protected static final int DEFAULT_TCP_BACK_LOG = 50;
	
	// Attributes
	protected String listenAddr; 
	protected int listenPort;
	protected int listenerCapacity;
	
	/**
	 * Constructor
	 */
	public AbstractTcpClientListener( FFMQEngine localEngine , 
				                      Settings settings ,
				                      JMXAgent jmxAgent ,
				                      String listenAddr ,
				                      int listenPort )
	{
		super(localEngine,settings,jmxAgent);
		this.listenAddr = listenAddr;
		this.listenPort = listenPort;
		this.listenerCapacity = settings.getIntProperty(FFMQServerSettings.LISTENER_TCP_CAPACITY, 200);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq4.listeners.ClientListener#getCapacity()
	 */
	@Override
	public int getCapacity()
	{
		return listenerCapacity;
	}
	
	protected InetAddress getBindAddress() throws UnknownHostException
	{
		if (listenAddr == null || listenAddr.equalsIgnoreCase("auto"))
			return InetAddress.getLocalHost();
		else
			return InetAddress.getByName(listenAddr);
	}
}
