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
package net.timewalker.ffmq3.local.session;

import javax.jms.Destination;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Topic;

import net.timewalker.ffmq3.FFMQConstants;
import net.timewalker.ffmq3.FFMQCoreSettings;
import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.common.message.AbstractMessage;
import net.timewalker.ffmq3.common.message.MessageSelector;
import net.timewalker.ffmq3.common.message.MessageTools;
import net.timewalker.ffmq3.common.session.AbstractMessageConsumer;
import net.timewalker.ffmq3.local.FFMQEngine;
import net.timewalker.ffmq3.local.TransactionSet;
import net.timewalker.ffmq3.local.connection.LocalConnection;
import net.timewalker.ffmq3.local.destination.LocalQueue;
import net.timewalker.ffmq3.local.destination.LocalTopic;
import net.timewalker.ffmq3.local.destination.notification.NotificationProxy;
import net.timewalker.ffmq3.management.destination.definition.QueueDefinition;
import net.timewalker.ffmq3.management.destination.definition.TopicDefinition;
import net.timewalker.ffmq3.security.Action;
import net.timewalker.ffmq3.security.Resource;
import net.timewalker.ffmq3.utils.ErrorTools;
import net.timewalker.ffmq3.utils.Settings;
import net.timewalker.ffmq3.utils.StringTools;
import net.timewalker.ffmq3.utils.async.AsyncTask;
import net.timewalker.ffmq3.utils.id.IntegerID;
import net.timewalker.ffmq3.utils.id.UUIDProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Implementation of a local JMS {@link MessageConsumer}</p>
 */
public class LocalMessageConsumer extends AbstractMessageConsumer
{
    private static final Log log = LogFactory.getLog(LocalMessageConsumer.class);
    
    // Parent engine
    protected FFMQEngine engine;
    
    // Parsed selector
    protected MessageSelector parsedSelector;
    
    // Runtime
    private String subscriberId;
    private LocalQueue localQueue;
    private LocalTopic localTopic;
    private boolean traceEnabled;
    private boolean receiving;
    private TransactionSet transactionSet;
    
    // Specific to receive mode
    private Object receiveLock = new Object();
    
    // Used for interaction with the remote layer
    private NotificationProxy notificationProxy;
    private int prefetchSize;
    private int prefetchCapacity; 
    private Object prefetchLock = new Object();
    
    // Settings
    private boolean logListenersFailures;
    
    /**
     * Constructor
     */
    public LocalMessageConsumer(FFMQEngine engine,
                                LocalSession session,
                                Destination destination,
                                String messageSelector,
                                boolean noLocal,
                                IntegerID consumerId,
                                String subscriberId) throws JMSException
    {
        super(session,destination,messageSelector,noLocal,consumerId);
        this.engine = engine;
        this.session = session;
        this.parsedSelector = 
            StringTools.isNotEmpty(messageSelector) ?
                new MessageSelector(messageSelector) : null;
        this.traceEnabled = log.isTraceEnabled();
        this.transactionSet = session.getTransactionSet();
        this.notificationProxy = session.getNotificationProxy();
        this.prefetchCapacity = this.prefetchSize = engine.getSetup().getConsumerPrefetchSize();
        this.logListenersFailures = getSettings().getBooleanProperty(FFMQCoreSettings.DELIVERY_LOG_LISTENERS_FAILURES, false);
        this.subscriberId = subscriberId != null ? subscriberId : UUIDProvider.getInstance().getShortUUID();
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.common.session.AbstractMessageConsumer#shouldLogListenersFailures()
     */
    protected final boolean shouldLogListenersFailures()
    {
        return logListenersFailures;
    }

    /**
	 * @return the prefetchSize
	 */
	public final int getPrefetchSize()
	{
		return prefetchSize;
	}
    
    protected final Settings getSettings()
    {
        return engine.getSetup().getSettings();
    }
    
    protected final void initDestination() throws JMSException
    {
        // Security : a consumer destination may only be set at creation time
        //            so we check permissions here once and for all.
        LocalConnection conn = (LocalConnection)session.getConnection();
        if (conn.isSecurityEnabled())
        {
            if (destination instanceof Queue)
            {
                String queueName = ((Queue)destination).getQueueName();
                if (conn.isRegisteredTemporaryQueue(queueName))
                {
                    // OK, temporary destination
                }
                else
                if (queueName.equals(FFMQConstants.ADM_REQUEST_QUEUE))
                {
                    // Only the internal admin thread can consume on this queue
                    if (conn.getSecurityContext() != null)
                        throw new FFMQException("Access denied to administration queue "+queueName,"ACCESS_DENIED");
                }
                else
                if (queueName.equals(FFMQConstants.ADM_REPLY_QUEUE))
                {
                    conn.checkPermission(Resource.SERVER, Action.REMOTE_ADMIN);
                }
                else
                {
                    // Standard queue
                    conn.checkPermission(destination,Action.CONSUME);
                }
            }
            else
            if (destination instanceof Topic)
            {
                String topicName = ((Topic)destination).getTopicName();
                if (conn.isRegisteredTemporaryTopic(topicName))
                {
                    // OK, temporary destination
                }
                else
                {
                    // Standard topic
                    conn.checkPermission(destination,Action.CONSUME);
                }
            }
        }
        
        // Lookup a local destination object from the given reference
        if (destination instanceof Queue)
        {
            Queue queueRef = (Queue)destination;
            this.localQueue = engine.getLocalQueue(queueRef.getQueueName());
            
            // Check temporary destinations scope (JMS Spec 4.4.3 p2)
            session.checkTemporaryDestinationScope(localQueue);
            
            this.localQueue.registerConsumer(this);
        }
        else
        if (destination instanceof Topic)
        {
            Topic topicRef = (Topic)destination;
            this.localTopic = engine.getLocalTopic(topicRef.getTopicName());
            
            // Check temporary destinations scope (JMS Spec 4.4.3 p2)
            session.checkTemporaryDestinationScope(localTopic);
            
            // Deploy a local queue for this consumer
            TopicDefinition topicDef = this.localTopic.getDefinition();
            QueueDefinition tempDef = topicDef.createQueueDefinition(topicRef.getTopicName(), subscriberId, !isDurable());            
            if (engine.localQueueExists(tempDef.getName()))
                this.localQueue = engine.getLocalQueue(tempDef.getName());
            else
                this.localQueue = engine.createQueue(tempDef);
            
            // Register on both the queue and topic
            this.localQueue.registerConsumer(this);
            this.localTopic.registerConsumer(this);
        }
        else
            throw new InvalidDestinationException("Unsupported destination : "+destination);
    }
    
    private void unregister()
    {
    	if (localTopic != null)
            localTopic.unregisterConsumer(this);
        if (localQueue != null)
        {
            localQueue.unregisterConsumer(this);

            try
            {
                // Drop volatile topic subscriber queue
                if ((destination instanceof Topic) && !isDurable())
                {
                	localQueue.close();
                	((LocalSession)session).deleteQueue(localQueue.getName());
                }
            }
            catch (JMSException e)
            {
            	ErrorTools.log(e, log);
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.jms.MessageConsumer#setMessageListener(javax.jms.MessageListener)
     */
    public final void setMessageListener(MessageListener messageListener) throws JMSException
    {
        super.setMessageListener(messageListener);
        
        // If the connection was already started, wake up the new listener in case there were messages
        // waiting in the destination
        if (messageListener != null && connection.isStarted())
        	engine.getDeliveryAsyncTaskManager().execute(wakeUpAsyncTask);
    }
    
    /**
     * Test if the consumer is durable
     */
    public boolean isDurable()
    {
        return false;
    }
    
    /**
     * Get the parsed message selector for this consumer
     * @return the parsedSelector
     */
    public final MessageSelector getParsedSelector()
    {
        return parsedSelector;
    }
    
    /**
     * Get the parsed message selector for this consumer to be used
     * when receiving a message from the local queue
     * @return the parsedSelector
     */
    public final MessageSelector getReceiveSelector()
    {
        return localTopic == null ? parsedSelector : null;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.session.AbstractMessageConsumer#onConsumerClose()
     */
    protected final void onConsumerClose()
    {
    	super.onConsumerClose();
    	
    	// Unregister the consumer from the associated destination
    	unregister();
    	
    	try
    	{
    		engine.getDeliveryAsyncTaskManager().cancelTask(wakeUpAsyncTask);
    	}
    	catch (JMSException e)
    	{
    		ErrorTools.log(e, log);
    	}
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.session.AbstractMessageConsumer#onConsumerClosed()
     */
    protected final void onConsumerClosed()
    {
    	// Wake up blocked listeners
    	synchronized (receiveLock) 
		{
    		receiveLock.notifyAll();
		}
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.session.AbstractMessageConsumer#receiveFromDestination(long, boolean)
     */
    public final AbstractMessage receiveFromDestination(long timeout, boolean duplicateRequired) throws JMSException
    {
    	synchronized (receiveLock)
		{
	        if (closed)
	            return null; // [JMS SPEC]
	        
	        if (receiving)
	        	throw new FFMQException("Consumer should not be accessed by more than one thread","ILLEGAL_USAGE");
	        
	        receiving = true;
	        try
	        {
		        MessageSelector selector = getReceiveSelector();
		        
		        // No-wait simplified case
		        if (timeout == 0)
		        {
		        	if (!connection.isStarted())
		        		return null;
		        	
		        	AbstractMessage message = localQueue.get((LocalSession)session,
						            						 transactionSet,
					                                         selector);        	
		            if (message == null)
		            	return null;
		            
		        	if (traceEnabled)
		                log.trace(session+" [GET] in "+localQueue+" - "+message);
		
		        	if (duplicateRequired)
		        		message = MessageTools.duplicate(message);
		            message.markAsReadOnly();
		            
		            return message;
		        }
			
		        // Wait loop
		        long now = System.currentTimeMillis();
		        long startTime = now;
		        
		        // Passive wait
		        while (!closed && (timeout < 0 || (now - startTime < timeout)))
		        {
		        	// Don't do anything if connection is not started
		    		if (connection.isStarted())
		    		{
		                // Try obtaining something from target queue
		    			AbstractMessage message = localQueue.get((LocalSession)session,
		    					                                 transactionSet,
		    					                                 selector);
			            if (message != null)
			            {
			                if (traceEnabled)
			                    log.trace(session+" [GET] in "+localQueue+" - "+message);
			                
			                if (duplicateRequired)
				        		message = MessageTools.duplicate(message);
				            message.markAsReadOnly();
				            
				            return message;
			            }
		    		}
		            
		            if (traceEnabled)
		                log.trace("Entering passive wait on "+localQueue+" (timeout="+timeout+")");
		            
		            try
		            {
		                if (timeout <= 0)
		                	receiveLock.wait();
		                else
		                	receiveLock.wait(timeout - (now - startTime));
		            }
		            catch (InterruptedException e)
		            {
		                return null;
		            }
		
		            now = System.currentTimeMillis();
		        }
		        
		        return null;
	        }
	        finally
	        {
	        	receiving = false;
	        }
		}
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.common.session.AbstractMessageConsumer#wakeUp()
     */
    public final void wakeUp() throws JMSException
    {
    	// Check that consumer is not closed
		if (closed)
            return;

        // Check that the connection is properly started
        if (!connection.isStarted())
            return;
	
	    propagateNotification();
    }
    
    public final void prefetchMore() throws JMSException
    {
    	synchronized (prefetchLock)
    	{
    		this.prefetchCapacity = prefetchSize; // Reset capacity to maximum
    	}
    	
    	wakeUp();
    }
    
    public final void restorePrefetchCapacity( int amount ) throws JMSException
    {
    	synchronized (prefetchLock)
    	{
    		this.prefetchCapacity += amount;
    	}
    	wakeUp();
    }
    
    private void propagateNotification() throws JMSException
    {
    	// If a notification proxy is registered, that mean we are 
    	// actually serving a remote consumer
    	if (notificationProxy != null)
        {
    	    int count = 0;
    		synchronized (prefetchLock)
			{
    			if (prefetchCapacity < prefetchSize)
    				return; // Consumer is busy, we can give up immediately because it get back to us later anyway
    			
    		    // Push up to 'prefetchCapacity' messages to the remote consumer
	    		while (prefetchCapacity > 0)
	    		{    			
	    			AbstractMessage message = receiveFromDestination(0, false);
	    			if (message != null)
	    			{
	    			    count++;
	    				prefetchCapacity--;
	    				notificationProxy.addNotification(id,message);
	    			}
	    			else
	    			    break;
	    		}
    		}
    		
    		// Flush
    		if (count > 0)
    		    notificationProxy.flush();
        }
        else
        {
        	// Is there a local consumer to wake up ?
	        if (messageListener != null)
	        {
	        	// Dispatch to listener in another thread to avoid deadlocks
	        	engine.getDeliveryAsyncTaskManager().execute(wakeUpAsyncTask);
	        }
	        else
	        {
	        	synchronized (receiveLock)
				{
		        	// Wake up a random thread blocked in a receive() call
		        	if (receiving)
		        		receiveLock.notify();
				}
	        }
        }
    }
    
    /*
     * (non-Javadoc)
     * @see javax.jms.TopicSubscriber#getNoLocal()
     */
    public final boolean getNoLocal()
    {
        return noLocal;
    }
    
    /**
     * Get the local queue associated to this consumer
     */
    public final LocalQueue getLocalQueue()
    {
        return localQueue;
    }
    
    /**
	 * @return the subscriberId
	 */
	public final String getSubscriberId()
	{
		return subscriberId;
	}
	
	//--------------------------------------------------------------------
	
	private final WakeUpAsyncTask wakeUpAsyncTask = new WakeUpAsyncTask();
	
	private final class WakeUpAsyncTask implements AsyncTask
	{
		/**
		 * Constructor
		 */
		public WakeUpAsyncTask()
		{
			super();
		}
		
		/* (non-Javadoc)
	     * @see net.timewalker.ffmq3.utils.async.AsyncTask#isMergeable()
	     */
	    public final boolean isMergeable()
	    {
	    	return true;
	    }
	    
		/* (non-Javadoc)
		 * @see net.timewalker.ffmq3.utils.async.AsyncTask#execute()
		 */
		public final void execute()
		{
			wakeUpMessageListener();
		}
	}
}
