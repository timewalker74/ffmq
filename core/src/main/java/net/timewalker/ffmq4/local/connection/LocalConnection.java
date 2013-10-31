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
package net.timewalker.ffmq3.local.connection;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.common.connection.AbstractConnection;
import net.timewalker.ffmq3.common.destination.DestinationRef;
import net.timewalker.ffmq3.common.destination.DestinationTools;
import net.timewalker.ffmq3.local.FFMQEngine;
import net.timewalker.ffmq3.local.session.LocalSession;
import net.timewalker.ffmq3.security.SecurityContext;
import net.timewalker.ffmq3.utils.id.IntegerID;

/**
 * <p>Local implementation of a JMS {@link Connection}</p>
 */
public class LocalConnection extends AbstractConnection
{
	// Attribute
    protected FFMQEngine engine;
    private SecurityContext securityContext;
    
    /**
     * Constructor
     */
    public LocalConnection( FFMQEngine engine , SecurityContext securityContext , String clientID )
    {
        super(clientID);
        this.engine = engine;
        this.securityContext = securityContext;
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.connection.AbstractConnection#setClientID(java.lang.String)
     */
    @Override
	public final void setClientID(String clientID) throws JMSException
    {
    	externalAccessLock.readLock().lock();
    	try
    	{
	        super.setClientID(clientID);
	        try
	        {
	        	ClientIDRegistry.getInstance().register(clientID);
	        }
	        catch (JMSException e)
	        {
	        	this.clientID = null; // Clear client ID
	        	throw e;
	        }
    	}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.Connection#createSession(boolean, int)
     */
    @Override
	public final Session createSession(boolean transacted, int acknowledgeMode) throws JMSException
    {
    	return createSession(idProvider.createID(), transacted, acknowledgeMode);
    }
    
    public final Session createSession(IntegerID sessionId,boolean transacted, int acknowledgeMode) throws JMSException
    {
        if (!transacted && acknowledgeMode == Session.SESSION_TRANSACTED)
            throw new FFMQException("Acknowledge mode SESSION_TRANSACTED cannot be used for an non-transacted session","INVALID_ACK_MODE");
        
        externalAccessLock.readLock().lock();
    	try
		{
	        checkNotClosed();
	        	
	        LocalSession session = new LocalSession(sessionId,this,engine,transacted,acknowledgeMode);
	        registerSession(session);
	        return session;
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }
    
    /*
     * (non-Javadoc)
     * @see javax.jms.Connection#start()
     */
    @Override
	public final void start() throws JMSException
    {
    	externalAccessLock.readLock().lock();
    	try
		{
	        checkNotClosed();
	        if (started)
	            return;
	        started = true;
	        
	        // Wake up waiting consumers
	        wakeUpConsumers();
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }
    
    /*
     * (non-Javadoc)
     * @see javax.jms.Connection#stop()
     */
    @Override
	public final void stop() throws JMSException
    {
    	externalAccessLock.readLock().lock();
    	try
		{
	        checkNotClosed();
	        if (!started)
	            return;
	        started = false;
	        
	        // Wait for running deliveries to complete ...
	        waitForDeliverySync();
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.common.connection.AbstractConnection#deleteTemporaryQueue(java.lang.String)
     */
    @Override
	public final void deleteTemporaryQueue(String queueName) throws JMSException
    {
        engine.deleteQueue(queueName);
        unregisterTemporaryQueue(queueName);
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.connection.AbstractConnection#deleteTemporaryTopic(java.lang.String)
     */
    @Override
	public final void deleteTemporaryTopic(String topicName) throws JMSException
    {
        engine.deleteTopic(topicName);
        unregisterTemporaryTopic(topicName);
    }
    
    /**
     * Check if the connection has the required credentials to use
     * the given destination
     */
    public final void checkPermission( Destination destination , String action ) throws JMSException
    {
    	if (securityContext == null)
            return; // Security is disabled
    	
    	DestinationRef destinationRef = DestinationTools.asRef(destination);
    	securityContext.checkPermission(destinationRef.getResourceName(), action);        
    }
    
    /**
     * Check if there is a security context enabled for this connection 
     * @return true if security is enabled
     */
    public final boolean isSecurityEnabled()
    {
        return securityContext != null;
    }
    
    /**
     * Check if the connection has the required credentials to use the given resource
     */
    public final void checkPermission( String resource , String action ) throws JMSException
    {
        if (securityContext == null)
            return; // Security is disabled

        securityContext.checkPermission(resource, action);        
    }

    /**
     * Get the connection security context
     * @return the connection security context or null if security is disabled
     */
    public final SecurityContext getSecurityContext()
    {
        return securityContext;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.connection.AbstractConnection#onConnectionClose()
     */
    @Override
	protected void onConnectionClose()
    {
    	super.onConnectionClose();
    	
    	// Unregister client ID
    	if (clientID != null)
            ClientIDRegistry.getInstance().unregister(clientID);
    }
}
