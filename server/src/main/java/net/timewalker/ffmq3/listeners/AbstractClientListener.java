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
package net.timewalker.ffmq3.listeners;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import net.timewalker.ffmq3.jmx.JMXAgent;
import net.timewalker.ffmq3.local.FFMQEngine;
import net.timewalker.ffmq3.utils.Settings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * AbstractClientListener
 */
public abstract class AbstractClientListener implements ClientListener
{
	private static final Log log = LogFactory.getLog(AbstractClientListener.class);
	
	// Attributes
	protected FFMQEngine localEngine;
	protected Settings settings;
	protected JMXAgent jmxAgent;
	
	// Runtime
	private List clientList = new Vector();
	protected int acceptedClients;
	private int droppedClients;
	private int maxActiveClients;
	protected boolean started;
	
	/**
	 * Constructor
	 */
	public AbstractClientListener( FFMQEngine localEngine , Settings settings , JMXAgent jmxAgent )
	{
		this.localEngine = localEngine;
		this.settings = settings;
		this.jmxAgent = jmxAgent;
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.listeners.ClientListener#getEngineName()
	 */
	public String getEngineName()
	{
		return localEngine.getName();
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.listeners.ClientListener#isStarted()
	 */
	public synchronized boolean isStarted()
	{
		return started;
	}
	
	protected void registerClient( ClientProcessor processor )
    {
		synchronized (clientList)
		{
	        clientList.add(processor);
	        acceptedClients++;
	        if (clientList.size() > maxActiveClients)
	        	maxActiveClients = clientList.size();
	        
	        // Register for JMX
	        if (jmxAgent != null)
	        {
	        	try
	        	{
	        		jmxAgent.register(createProcessorName(processor), processor);
	        	}
	        	catch (Exception e)
	        	{
	        		log.error("Could not register client in JMX agent",e);
	        	}
	        }
		}
    }
    
    protected void unregisterClient( ClientProcessor processor )
    {
    	synchronized (clientList)
		{
    		clientList.remove(processor);
    		droppedClients++;
    		
    		// Unregister from JMX
    		if (jmxAgent != null)
    		{
	        	try
	        	{
	        		jmxAgent.unregister(createProcessorName(processor));
	        	}
	        	catch (Exception e)
	        	{
	        		log.error("Could not unregister client from JMX agent",e);
	        	}
	        }	
		}
    }
	
    private ObjectName createProcessorName( ClientProcessor processor ) throws MalformedObjectNameException
    {
    	return new ObjectName(JMXAgent.JMX_DOMAIN+":type=Listeners,listener="+getName()+",children=clients,id="+processor.getClientID());
    }
    
    protected void closeRemainingClients()
    {
	    List clientsToStop = new ArrayList();
	    synchronized (clientList)
		{
	    	clientsToStop.addAll(clientList);
		}
		Iterator clients = clientsToStop.iterator();
		while (clients.hasNext())
		{
			ClientProcessor clientProcessor = (ClientProcessor)clients.next();
			clientProcessor.stop();
		}
		clientList.clear();
    }
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.listeners.ListenerMBean#getActiveClients()
	 */
	public int getActiveClients()
	{
		return clientList.size();
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.listeners.ListenerMBean#getAcceptedTotal()
	 */
	public int getAcceptedTotal()
	{
		return acceptedClients;
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.listeners.ListenerMBean#getDroppedTotal()
	 */
	public int getDroppedTotal()
	{
		return droppedClients;
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.listeners.ListenerMBean#getMaxActiveClients()
	 */
	public int getMaxActiveClients()
	{
		return maxActiveClients;
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.listeners.ListenerMBean#resetStatistics()
	 */
	public void resetStats()
	{
		acceptedClients = 0;
		droppedClients = 0;
		maxActiveClients = 0;
	}
}
