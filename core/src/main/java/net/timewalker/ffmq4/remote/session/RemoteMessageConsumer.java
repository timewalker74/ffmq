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
package net.timewalker.ffmq4.remote.session;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageListener;

import net.timewalker.ffmq4.FFMQCoreSettings;
import net.timewalker.ffmq4.client.ClientEnvironment;
import net.timewalker.ffmq4.common.message.AbstractMessage;
import net.timewalker.ffmq4.common.session.AbstractMessageConsumer;
import net.timewalker.ffmq4.storage.message.MessageSerializationLevel;
import net.timewalker.ffmq4.transport.PacketTransportEndpoint;
import net.timewalker.ffmq4.transport.packet.query.CloseConsumerQuery;
import net.timewalker.ffmq4.transport.packet.query.CreateConsumerQuery;
import net.timewalker.ffmq4.transport.packet.query.PrefetchQuery;
import net.timewalker.ffmq4.utils.ErrorTools;
import net.timewalker.ffmq4.utils.Settings;
import net.timewalker.ffmq4.utils.async.AsyncTask;
import net.timewalker.ffmq4.utils.async.AsyncTaskManager;
import net.timewalker.ffmq4.utils.id.IntegerID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * RemoteMessageConsumer
 */
public class RemoteMessageConsumer extends AbstractMessageConsumer
{
    private static final Log log = LogFactory.getLog(RemoteMessageConsumer.class);
    
    // Parent remote connection
    protected PacketTransportEndpoint transportEndpoint;
    
    // Runtime
    private boolean traceEnabled;
    private LinkedList<AbstractMessage> prefetchQueue = new LinkedList<>();
    private Semaphore prefetchSemaphore = new Semaphore(0);
    protected boolean donePrefetching = false;
    private AsyncTaskManager asyncTaskManager;
    
    // Settings
    private boolean logListenersFailures;
    
    /**
     * Constructor
     */
    public RemoteMessageConsumer(IntegerID consumerId,
    		                     RemoteSession session, 
                                 Destination destination, 
                                 String messageSelector, 
                                 boolean noLocal) throws JMSException
    {
        super(session,destination,messageSelector,noLocal,consumerId);
        this.transportEndpoint = session.getTransportEndpoint();
        this.asyncTaskManager = ClientEnvironment.getAsyncTaskManager();
        this.traceEnabled = log.isTraceEnabled();
        this.logListenersFailures = getSettings().getBooleanProperty(FFMQCoreSettings.DELIVERY_LOG_LISTENERS_FAILURES, false);
        log.debug("New remote consumer ID is "+consumerId);
    }
        
    /**
     * Initialize the remote endpoint for this consumer
     */
    protected void remoteInit() throws JMSException
    {
        CreateConsumerQuery query = new CreateConsumerQuery();
        query.setConsumerId(id);
        query.setSessionId(session.getId());
        query.setDestination(destination);
        query.setMessageSelector(messageSelector);
        query.setNoLocal(noLocal);
        transportEndpoint.blockingRequest(query);        
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.common.session.AbstractMessageConsumer#shouldLogListenersFailures()
     */
    @Override
	protected final boolean shouldLogListenersFailures()
    {
        return logListenersFailures;
    }
    
    private final Settings getSettings()
    {
        return ClientEnvironment.getSettings();
    }
    
    /* (non-Javadoc)
     * @see javax.jms.MessageConsumer#setMessageListener(javax.jms.MessageListener)
     */
    @Override
	public final void setMessageListener(MessageListener messageListener) throws JMSException
    {
        super.setMessageListener(messageListener);
        
        // If the connection was already started, wake up the new listener in case there were messages
        // waiting in the destination
        if (messageListener != null && session.getConnection().isStarted())
        	wakeUpMessageListener();
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.common.session.AbstractMessageConsumer#onConsumerClose()
     */
    @Override
	protected final void onConsumerClose()
    {
    	super.onConsumerClose();

    	// Unlock waiting threads
    	prefetchSemaphore.release();
    	
    	try
    	{
        	CloseConsumerQuery query = new CloseConsumerQuery();
    		query.setSessionId(session.getId());
    		query.setConsumerId(id);
    		
    		// Append message IDs to rollback
    		synchronized (prefetchQueue)
            {
    		    while (!prefetchQueue.isEmpty())
    		    {
    		    	AbstractMessage msg = prefetchQueue.removeFirst();
    		        query.addUndeliveredMessageID(msg.getJMSMessageID());
    		    }
            }
    		
    		transportEndpoint.blockingRequest(query);
    	}
        catch (JMSException e)
        {
            ErrorTools.log(e, log);
        }
    }
    
    public final boolean addToPrefetchQueue( AbstractMessage prefetchedMessage , boolean donePrefetching )
    {
    	externalAccessLock.readLock().lock();
    	try
        {
            if (closed)
                return false; // Consumer is closed, refuse prefetched messages
            
        	synchronized (prefetchQueue)
    		{
    			if (traceEnabled)
            		log.trace("#"+id+" [PREFETCHED] from "+destination+" - "+prefetchedMessage);
    			
        		prefetchQueue.add(prefetchedMessage);
        		this.donePrefetching = donePrefetching;
    		}
        }
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
        prefetchSemaphore.release();
        
        // Wake up listener asynchronously
		try
		{
			if (messageListener != null)
				asyncTaskManager.execute(wakeUpTask);
		}
		catch (JMSException e)
		{
			ErrorTools.log(e, log);
		}
		
		return true;
    }
    
    private AbstractMessage getFromPrefetchQueue( long timeout )
    {
    	boolean shouldPrefetchMore = false;
    	synchronized (prefetchQueue)
        {
    		if (donePrefetching && prefetchQueue.isEmpty())
    		{
    			shouldPrefetchMore = true;
    			donePrefetching = false;
    		}
        }
    	
    	// Ask for more messages if necessary (asynchronous)
    	if (shouldPrefetchMore)
    	{
	    	try
	    	{
		    	prefetchFromDestination();
	    	}
	    	catch (JMSException e)
	    	{
	    		log.error("Cannot prefetch more messages from remote server",e);
	    	}
    	}
    	
    	// Wait for a message to be available
    	try
    	{
    		if (timeout >= 0)
    		{
		    	if (!prefetchSemaphore.tryAcquire(timeout,TimeUnit.MILLISECONDS))
					return null;
    		}
    		else
    			prefetchSemaphore.acquire();
    	}
    	catch (InterruptedException e)
    	{
    		return null; // Abort
    	}
    		
    	// Get the message from the queue
    	AbstractMessage message;
    	externalAccessLock.readLock().lock();
    	try
        {
    		if (closed)
    			return null; // [JMS SPEC]
    		
	    	synchronized (prefetchQueue)
	        {
	    		// Consistency check
	        	if (prefetchQueue.isEmpty())
	        		throw new IllegalStateException("Prefetch queue is empty");
	        	
	        	message = prefetchQueue.removeFirst();
	        }
        }
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}

    	((RemoteSession)session).notifyDeliveredMessage(message.getJMSMessageID());
    	
     	if (traceEnabled)
            log.trace("#"+id+" [GET PREFETCHED] in "+destination+" - "+message);

        // Make sure the message is fully deserialized and marked as read-only
     	message.ensureDeserializationLevel(MessageSerializationLevel.FULL);
		message.markAsReadOnly();
		
        return message;
    }
    
    /**
     * Prefetch messages from destination
     * @throws JMSException
     */
    private void prefetchFromDestination() throws JMSException
    {
        // Lazy test, do not synchronize here but on response (see addToPrefetchQueue())
    	if (closed)
            return;

    	if (traceEnabled)
    		log.trace("#"+id+" Prefetching more from destination "+destination);
    	
    	// Ask for more
        PrefetchQuery query = new PrefetchQuery();
        query.setSessionId(session.getId());
        query.setConsumerId(id);
        transportEndpoint.nonBlockingRequest(query);
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.common.session.AbstractMessageConsumer#receiveFromDestination(long, boolean)
     */
    @Override
	protected final AbstractMessage receiveFromDestination(long timeout, boolean duplicateRequired) throws JMSException
    {
        if (closed)
            return null; // [JMS SPEC]

        // Get something from preftech queue
        return getFromPrefetchQueue(timeout);
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.common.session.AbstractMessageConsumer#wakeUp()
     */
    @Override
	protected final void wakeUp()
    {
    	// Note : Only called asynchronously by a wake up task after a notification packet has arrived
    	
    	// Check that the consumer is not closed
		if (closed)
			return;
    		
        // Check that the connection is started
        if (!session.getConnection().isStarted())
            return;
	        
        if (messageListener != null)
        {
        	wakeUpMessageListener();
        }
        else
        	throw new IllegalStateException("Unexpected message availability notification");
    }
    
    //----------------------------------------------------------------------------------------
    
    private final WakeUpTask wakeUpTask = new WakeUpTask();
    
    private final class WakeUpTask implements AsyncTask
    {
    	/**
		 * Constructor
		 */
		public WakeUpTask()
		{
			super();
		}
    	
    	/* (non-Javadoc)
         * @see net.timewalker.ffmq4.utils.async.AsyncTask#isMergeable()
         */
        @Override
		public final boolean isMergeable()
        {
        	return true;
        }
        
        /* (non-Javadoc)
         * @see net.timewalker.ffmq4.utils.async.AsyncTask#execute()
         */
        @Override
		public final void execute()
        {
        	wakeUp();
        }
    }
}
