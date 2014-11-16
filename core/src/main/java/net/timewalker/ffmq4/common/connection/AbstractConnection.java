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
package net.timewalker.ffmq4.common.connection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.IllegalStateException;
import javax.jms.InvalidClientIDException;
import javax.jms.JMSException;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;

import net.timewalker.ffmq4.FFMQException;
import net.timewalker.ffmq4.common.session.AbstractSession;
import net.timewalker.ffmq4.utils.ErrorTools;
import net.timewalker.ffmq4.utils.StringTools;
import net.timewalker.ffmq4.utils.id.IntegerID;
import net.timewalker.ffmq4.utils.id.IntegerIDProvider;
import net.timewalker.ffmq4.utils.id.UUIDProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Base implementation for a JMS connection</p>
 */
public abstract class AbstractConnection implements Connection
{
    private static final Log log = LogFactory.getLog(AbstractConnection.class);
    
    // ID and client ID handling
    protected String id = UUIDProvider.getInstance().getShortUUID();
    protected String clientID;
    private static ConnectionMetaData metaData = new ConnectionMetaDataImpl();
    
    // Runtime
    protected boolean started;
    protected boolean closed;
    private ExceptionListener exceptionListener;
    private Set<String> temporaryQueues = new HashSet<>();
    private Set<String> temporaryTopics = new HashSet<>();
    protected IntegerIDProvider idProvider = new IntegerIDProvider();
    protected ReadWriteLock externalAccessLock = new ReentrantReadWriteLock();
    private Object exceptionListenerLock = new Object();
    
    // Children
    private Map<IntegerID,AbstractSession> sessions = new Hashtable<>();
    
    /**
     * Constructor
     */
    public AbstractConnection( String clientID )
    {
        this.clientID = clientID;
    }
    
    /**
     * Get the connection id
     * @return the id
     */
    public String getId()
    {
        return id;
    }

    /* (non-Javadoc)
     * @see javax.jms.Connection#getClientID()
     */
    @Override
	public String getClientID() throws JMSException
    {
    	externalAccessLock.readLock().lock();
    	try
		{
	        if (clientID == null)
	            throw new InvalidClientIDException("Client ID not set");
	        return clientID;
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }

    /* (non-Javadoc)
     * @see javax.jms.Connection#getMetaData()
     */
    @Override
	public ConnectionMetaData getMetaData()
    {
        return metaData;
    }

    /* (non-Javadoc)
     * @see javax.jms.Connection#setClientID(java.lang.String)
     */
    @Override
	public void setClientID(String clientID) throws JMSException
    {
    	externalAccessLock.readLock().lock();
    	try
		{
    		checkNotClosed();
	        if (StringTools.isEmpty(clientID))
	            throw new InvalidClientIDException("Empty client ID");
	        if (this.clientID != null)
	            throw new IllegalStateException("Client ID is already set"); // [JMS SPEC]
	        this.clientID = clientID;
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }

    /* (non-Javadoc)
     * @see javax.jms.Connection#getExceptionListener()
     */
    @Override
	public ExceptionListener getExceptionListener()
    {
    	synchronized (exceptionListenerLock)
    	{
    		return exceptionListener;
    	}
    }

    /* (non-Javadoc)
     * @see javax.jms.Connection#setExceptionListener(javax.jms.ExceptionListener)
     */
    @Override
	public void setExceptionListener(ExceptionListener listener) throws JMSException
    {
    	synchronized (exceptionListenerLock)
    	{
    		checkNotClosed();
    		this.exceptionListener = listener;
    	}
    }
    
    /**
     * Triggered when a JMSException is internally catched
     */
    public final void exceptionOccured( JMSException exception )
    {
        try
        {
        	synchronized (exceptionListenerLock)
        	{
        		if (exceptionListener != null)
        			exceptionListener.onException(exception);
        	}
        }
        catch (Exception e)
        {
            log.error("Exception listener failed",e);
        }
    }
    
    /**
     * Register a temporary queue name
     */
    public final void registerTemporaryQueue( String queueName )
    {
        synchronized (temporaryQueues)
        {
            temporaryQueues.add(queueName);    
        }
    }
    
    /**
     * Unregister a temporary queue name
     */
    public final void unregisterTemporaryQueue( String queueName )
    {
        synchronized (temporaryQueues)
        {
        	temporaryQueues.remove(queueName);    
        }
    }
    
    /**
     * Check if a temporary queue was registered with this connection
     */
    public final boolean isRegisteredTemporaryQueue( String queueName )
    {
        synchronized (temporaryQueues)
        {
            return temporaryQueues.contains(queueName);    
        }
    }
    
    /**
     * Register a temporary topic name
     */
    public final void registerTemporaryTopic( String topicName )
    {
    	synchronized (temporaryTopics)
		{
    		temporaryTopics.add(topicName);
		}
    }
    
    /**
     * Unregister a temporary topic name
     */
    public final void unregisterTemporaryTopic( String topicName )
    {
        synchronized (temporaryTopics)
        {
            temporaryTopics.remove(topicName);    
        }
    }
    
    /**
     * Check if a temporary topic was registered with this connection
     */
    public final boolean isRegisteredTemporaryTopic( String topicName )
    {
        synchronized (temporaryTopics)
        {
            return temporaryTopics.contains(topicName);    
        }
    }
    
    /**
     * Drop all registered temporary queues
     */
    private void dropTemporaryQueues()
    {
        synchronized (temporaryQueues)
		{
	        Iterator<String> remainingQueues = temporaryQueues.iterator();
	        while (remainingQueues.hasNext())
	        {
	            String queueName = remainingQueues.next();
	            try
	            {
	                deleteTemporaryQueue(queueName);
	            }
	            catch (JMSException e)
	            {
	            	ErrorTools.log(e, log);
	            }
	        }
		}
    }
    
    /**
     * Delete a temporary queue
     */
    public abstract void deleteTemporaryQueue( String queueName ) throws JMSException;
    
    /**
     * Delete a temporary topic
     */
    public abstract void deleteTemporaryTopic( String topicName ) throws JMSException;

    /* (non-Javadoc)
     * @see javax.jms.Connection#close()
     */
    @Override
	public final void close()
    {
    	externalAccessLock.writeLock().lock();
    	try
		{
	        if (closed)
	            return;
	        closed = true;
	        onConnectionClose();
		}
    	finally
    	{
    		externalAccessLock.writeLock().unlock();
    	}
    	onConnectionClosed();
    }
    
    protected void onConnectionClose()
    {
    	// Close remaining sessions
        closeRemainingSessions();

        // Drop temporary queues
        dropTemporaryQueues();
    }
    
    protected void onConnectionClosed()
    {
        // Nothing
    }
    
    /**
     * Close remaining sessions
     */
    private void closeRemainingSessions()
    {
        if (sessions == null)
            return;
        
        List<AbstractSession> sessionsToClose = new ArrayList<>(sessions.size());
        synchronized (sessions)
        {
            sessionsToClose.addAll(sessions.values());
        }
        for (int n = 0 ; n < sessionsToClose.size() ; n++)
        {
            Session session = sessionsToClose.get(n);
            log.debug("Auto-closing unclosed session : "+session);
            try
            {
                session.close();
            }
            catch (JMSException e)
            {
                ErrorTools.log(e, log);
            }
        }
    }

    /**
     * Wake up all children consumers
     * (Used by LocalConnection only)
     */
    protected final void wakeUpLocalConsumers()
    {
        try
        {
        	List<AbstractSession> sessionsSnapshot = new ArrayList<>(sessions.size());
            synchronized (sessions)
            {
            	sessionsSnapshot.addAll(sessions.values());
            }
            for(int n=0;n<sessionsSnapshot.size();n++)
            {
                AbstractSession session = sessionsSnapshot.get(n);
                session.wakeUpConsumers();
            }
        }
        catch (JMSException e)
        {
        	ErrorTools.log(e, log);
        }
    }
    
    /**
     * Wait for sessions to finish the current deliveridispatching
     */
    protected final void waitForDeliverySync()
    {
    	List<AbstractSession> sessionsSnapshot = new ArrayList<>(sessions.size());
        synchronized (sessions)
        {
        	sessionsSnapshot.addAll(sessions.values());
        }
        for(int n=0;n<sessionsSnapshot.size();n++)
        {
            AbstractSession session = sessionsSnapshot.get(n);
            session.waitForDeliverySync();
        }
    }
    
    /**
     * Lookup a registered session
     */
    public final AbstractSession lookupRegisteredSession( IntegerID sessionId )
    {
        return sessions.get(sessionId);
    }
    
    /**
     * Register a session
     */
    protected final void registerSession( AbstractSession sessionToAdd )
    {
    	if (sessions.put(sessionToAdd.getId(),sessionToAdd) != null)
    		throw new IllegalArgumentException("Session "+sessionToAdd.getId()+" already exists");
    }
    
    /**
     * Unregister a session
     */
    public final void unregisterSession( AbstractSession sessionToRemove )
    {
        if (sessions.remove(sessionToRemove.getId()) == null)
            log.warn("Unknown session : "+sessionToRemove);
    }
    
    /**
     * Check that the connection is not closed
     */
    protected final void checkNotClosed() throws JMSException
    {
		if (closed)
			throw new FFMQException("Connection is closed","CONNECTION_CLOSED");
    }
    
    /*
     * (non-Javadoc)
     * @see javax.jms.Connection#createConnectionConsumer(javax.jms.Destination, java.lang.String, javax.jms.ServerSessionPool, int)
     */
    @Override
	public ConnectionConsumer createConnectionConsumer(Destination destination, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException
    {
        throw new FFMQException("Unsupported feature","UNSUPPORTED_FEATURE");
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.Connection#createDurableConnectionConsumer(javax.jms.Topic, java.lang.String, java.lang.String, javax.jms.ServerSessionPool, int)
     */
    @Override
	public ConnectionConsumer createDurableConnectionConsumer(Topic topic, String subscriptionName, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException
    {
        throw new FFMQException("Unsupported feature","UNSUPPORTED_FEATURE");
    }

    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    @Override
	protected void finalize() throws Throwable
    {
        if (externalAccessLock != null && !closed)
        {
            log.warn("Connection was not properly closed, closing it now.");
            try
            {
                close();
            }
            catch (Throwable e)
            {
                log.error("Could not auto-close connection",e);
            }
        }
    }
    
    /**
     * Check if the connection is started
     * NOT SYNCHRONIZED TO AVOID DEADLOCKS
     */
    public boolean isStarted()
    {
   		return started && !closed;
    }
    
    /**
     * Get the number of active sessions for this connection
     * @return the number of active sessions for this connection
     */
    public int getSessionsCount()
    {
    	return sessions.size();
    }
    
    /**
     * Get the number of active producers for this connection
     * @return the number of active producers for this connection
     */
    public int getConsumersCount()
    {
    	synchronized (sessions)
		{
    		if (sessions.isEmpty())
    			return 0;
    
    		int total = 0;
    		Iterator<AbstractSession>sessionsIterator = sessions.values().iterator();
			while (sessionsIterator.hasNext())
			{
				AbstractSession session = sessionsIterator.next();
				total += session.getConsumersCount();
			}
			return total;
		}
    }
    
    /**
     * Get the number of active producers for this connection
     * @return the number of active producers for this connection
     */
    public int getProducersCount()
    {
    	synchronized (sessions)
		{
    		if (sessions.isEmpty())
    			return 0;
    
    		int total = 0;
    		Iterator<AbstractSession> sessionsIterator = sessions.values().iterator();
			while (sessionsIterator.hasNext())
			{
				AbstractSession session = sessionsIterator.next();
				total += session.getProducersCount();
			}
			return total;
		}
    }
    
    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Connection[#");
        sb.append(id);
        sb.append("](started=");
        sb.append(started);
        sb.append(")");
        
        return sb.toString();
    }
    
    /**
	 * Get a description of entities held by this object
	 */
	public void getEntitiesDescription( StringBuilder sb )
	{
		sb.append(toString());
		sb.append("{");
		synchronized (sessions)
		{
			if (!sessions.isEmpty())
			{
				int pos = 0;
				Iterator<AbstractSession> sessionsIterator = sessions.values().iterator();
				while (sessionsIterator.hasNext())
				{
					AbstractSession session = sessionsIterator.next();
					if (pos++ > 0)
						sb.append(",");
					session.getEntitiesDescription(sb);
				}
			}
		}
		sb.append("}");
	}
}
