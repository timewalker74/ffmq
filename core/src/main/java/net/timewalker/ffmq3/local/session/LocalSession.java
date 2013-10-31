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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.jms.Destination;
import javax.jms.IllegalStateException;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import net.timewalker.ffmq3.FFMQConstants;
import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.common.destination.TemporaryQueueRef;
import net.timewalker.ffmq3.common.destination.TemporaryTopicRef;
import net.timewalker.ffmq3.common.message.AbstractMessage;
import net.timewalker.ffmq3.common.session.AbstractSession;
import net.timewalker.ffmq3.local.FFMQEngine;
import net.timewalker.ffmq3.local.MessageLock;
import net.timewalker.ffmq3.local.MessageLockSet;
import net.timewalker.ffmq3.local.TransactionItem;
import net.timewalker.ffmq3.local.TransactionSet;
import net.timewalker.ffmq3.local.connection.LocalConnection;
import net.timewalker.ffmq3.local.destination.AbstractLocalDestination;
import net.timewalker.ffmq3.local.destination.LocalQueue;
import net.timewalker.ffmq3.local.destination.notification.NotificationProxy;
import net.timewalker.ffmq3.security.Action;
import net.timewalker.ffmq3.security.Resource;
import net.timewalker.ffmq3.utils.Committable;
import net.timewalker.ffmq3.utils.ErrorTools;
import net.timewalker.ffmq3.utils.StringTools;
import net.timewalker.ffmq3.utils.concurrent.SynchronizationBarrier;
import net.timewalker.ffmq3.utils.id.IntegerID;
import net.timewalker.ffmq3.utils.id.UUIDProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Implementation of a local JMS {@link Session}</p>
 */
public class LocalSession extends AbstractSession
{
    private static final Log log = LogFactory.getLog(LocalSession.class);

    // Attributes
    protected FFMQEngine engine;
    
    // Runtime
    private List pendingPuts = new Vector();
    private TransactionSet transactionSet = new TransactionSet();
    private boolean debugEnabled = log.isDebugEnabled();
    
    // For internal use by the remote layer
    protected NotificationProxy notificationProxy;
    
    // Message stats
    private long consumedCount;
    private long producedCount;
    
    /**
     * Constructor
     */
    public LocalSession( IntegerID id , LocalConnection connection , FFMQEngine engine , boolean transacted , int acknowlegdeMode )
    {
        super(id,connection,transacted,acknowlegdeMode);
        this.engine = engine;
    }
    
    /**
	 * @param notificationProxy the notificationProxy to set
	 */
	public final void setNotificationProxy(NotificationProxy notificationProxy)
	{
		this.notificationProxy = notificationProxy;
	}
    
	/**
	 * @return the notificationProxy
	 */
	public final NotificationProxy getNotificationProxy()
	{
		return notificationProxy;
	}
    
    /**
     * Called from producers when sending a message
     * @param message message to dispatch
     * @throws JMSException
     */
    public final void dispatch( AbstractMessage message ) throws JMSException
    {
        // Security
        LocalConnection conn = (LocalConnection)getConnection();
        if (conn.isSecurityEnabled())
        {
            Destination destination = message.getJMSDestination();
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
                    conn.checkPermission(Resource.SERVER, Action.REMOTE_ADMIN);
                }
                else
                if (queueName.equals(FFMQConstants.ADM_REPLY_QUEUE))
                {
                    // Only the internal admin thread can produce on this queue
                    if (conn.getSecurityContext() != null)
                        throw new FFMQException("Access denied to administration queue "+queueName,"ACCESS_DENIED");
                }
                else
                {
                    // Standard queue
                    conn.checkPermission(destination,Action.PRODUCE);
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
                    conn.checkPermission(destination,Action.PRODUCE);   
                }
            }
            else
                throw new InvalidDestinationException("Unsupported destination : "+destination);
        }
        
        if (debugEnabled)
            log.debug(this+" [PUT] in "+message.getJMSDestination()+" - "+message);
        
        externalAccessLock.readLock().lock();
    	try
        {
            checkNotClosed();
            
            pendingPuts.add(message);
            
        	if (!transacted)
        		commitUpdates(false, null, true); // FIXME Async commit ?
        }
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }
    	
    /*
     * (non-Javadoc)
     * @see javax.jms.Session#commit()
     */
    public final void commit() throws JMSException
    {
    	commit(true,null);
    }
    
    /**
     * Commit pending put/get operations in this session
     * @param commitGets
     * @param deliveredMessageIDs
     * @throws JMSException
     */
    public final void commit( boolean commitGets , List deliveredMessageIDs ) throws JMSException
    {
    	if (!transacted)
            throw new IllegalStateException("Session is not transacted"); // [JMS SPEC]
    	
    	externalAccessLock.readLock().lock();
    	try
		{
	    	checkNotClosed();   
	        commitUpdates(commitGets,deliveredMessageIDs,true);
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }
    
    /* (non-Javadoc)
     * @see javax.jms.Session#rollback()
     */
    public final void rollback() throws JMSException
    {
    	rollback(true, null);
    }

    /**
     * Rollback pending put/get operations in this session
     * @param rollbackGets
     * @param deliveredMessageIDs
     * @throws JMSException
     */
    public final void rollback( boolean rollbackGets, List deliveredMessageIDs ) throws JMSException
    {
    	if (!transacted)
            throw new IllegalStateException("Session is not transacted"); // [JMS SPEC]
    	
    	externalAccessLock.readLock().lock();
    	try
		{
	    	checkNotClosed();
	        rollbackUpdates(true,rollbackGets, deliveredMessageIDs);
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }

    /**
     * Rollback undelivered get operations in this session
     * @param undeliveredMessageIDs
     * @throws JMSException
     */
    public final void rollbackUndelivered( List undeliveredMessageIDs ) throws JMSException
    {
    	externalAccessLock.readLock().lock();
    	try
		{
	    	checkNotClosed();
	        rollbackUpdates(false,true, undeliveredMessageIDs);
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }

	private AbstractLocalDestination getLocalDestination( AbstractMessage message ) throws JMSException
    {
		Destination destination = message.getJMSDestination();
		
		if (destination instanceof Queue)
		{
			Queue queueRef = (Queue)destination;
			return engine.getLocalQueue(queueRef.getQueueName());
		}
		else
		if (destination instanceof Topic)
		{
			Topic topicRef = (Topic)destination;
			return engine.getLocalTopic(topicRef.getTopicName());
		}
		else
			throw new InvalidDestinationException("Unsupported destination : "+destination);
    }
    
    private List computeLocalTargetDestinations( List pendingPuts , List queuesWithGet ) throws JMSException
    {
    	int initialSize = Math.max((pendingPuts != null ? pendingPuts.size() : 0)+
    			                   (queuesWithGet != null ? queuesWithGet.size() : 0),16);
    	List targetCommitables = new ArrayList(initialSize);
    	
    	if (queuesWithGet != null)
    		targetCommitables.addAll(queuesWithGet);
    	
    	if (pendingPuts != null)
    	{
    		for (int i = 0 ; i < pendingPuts.size() ; i++)
    		{
    			AbstractMessage msg = (AbstractMessage)pendingPuts.get(i);
    			AbstractLocalDestination destination = getLocalDestination(msg);
    			if (!targetCommitables.contains(destination))
                	targetCommitables.add(destination);
    		}
    	}
        
    	// Sort list (important to avoid deadlocks when locking destinations for update)
    	Collections.sort(targetCommitables, DESTINATION_COMPARATOR);
    	
        return targetCommitables; 
    }
    
    private void commitUpdates( boolean commitGets , List deliveredMessageIDs , boolean commitPuts ) throws JMSException
    {
    	SynchronizationBarrier commitBarrier = null;
    	List queuesWithGet = null;
    	MessageLockSet locks = null;
    	JMSException putFailure = null;
    	Set committables = new HashSet();
    	
    	// 1 - Build a list of queues updated in get operations
    	if (commitGets && transactionSet.size() > 0)
    	{
    		if (deliveredMessageIDs != null)
    			queuesWithGet = transactionSet.updatedQueues(deliveredMessageIDs);
    		else
				queuesWithGet = transactionSet.updatedQueues();
    	}
    	
    	// 2 - Build a list of all target destinations
    	List targetDestinations = computeLocalTargetDestinations(commitPuts ? pendingPuts : null,queuesWithGet);
    	
    	// 3 - Lock target destinations
    	for (int i = 0; i < targetDestinations.size(); i++)
		{
    		Committable committable = (Committable)targetDestinations.get(i);
    		committable.openTransaction();
		}
    	try
    	{
	    	if (commitPuts)
	    	{
	    		// 4 - Try sending all pending queue messages first (because this may fail if a queue is full)
	    		synchronized (pendingPuts)
	    		{
	    		    if (!pendingPuts.isEmpty())
	    		    {
	    		    	int pendingSize = pendingPuts.size();
	    		    	locks = new MessageLockSet(pendingSize);
	    		    	
	        			if (debugEnabled)
	                		log.debug(this+" - COMMIT [PUT] "+pendingPuts.size()+" message(s)");
	            		
	    				// Put messages in locked state. They will be unlocked after proper commit.
	        			try
	        			{
		    		    	for (int i = 0; i < pendingPuts.size(); i++) 
		    		    	{
		    		    		AbstractMessage message = (AbstractMessage)pendingPuts.get(i);
		    		    		AbstractLocalDestination targetDestination = getLocalDestination(message);
		    		    		if (targetDestination.putLocked(message, this, locks))
		    		    			committables.add(targetDestination);
		    				}
		    		    	
		    		    	// All messages successfully pushed
	        				pendingPuts.clear();
	        			}
	        			catch (JMSException e)
	        			{
	        				if (transacted)
	        				{
		        				// Oops, something went wrong, we need to rollback what we have done yet
		        				for (int i = 0; i < locks.size(); i++)
								{
									MessageLock item = locks.get(i);
									item.getDestination().removeLocked(item);
								}
		        				
		        				// Store failure (will be re-thrown later after transaction commit, see below)
		        				putFailure = e;
	        				}
	        				else
	        					ErrorTools.log(e, log);
	        			}	        			
	        			
	        			producedCount += pendingSize;
	    		    }
	    		}
	    	}
	
	    	// 5 - Commit pending get messages, i.e. delete them from destinations
	    	if (queuesWithGet != null && putFailure == null)
	    	{    	
	    		TransactionItem[] pendingGets;
	    		if (deliveredMessageIDs != null)
	    		{
	    			// Commit only delivered messages
	    			if (debugEnabled)
		        		log.debug(this+" - COMMIT [GET] "+deliveredMessageIDs.size()+" message(s)");
	    			pendingGets = transactionSet.clear(deliveredMessageIDs);
	    		}
	    		else
	    		{
	    			// Commit the whole transaction set
	    			if (debugEnabled)
		        		log.debug(this+" - COMMIT [GET] "+transactionSet.size()+" message(s)");
	    			pendingGets = transactionSet.clear();
	    		}
	    		
	            for (int i = 0; i < queuesWithGet.size(); i++)
	            {
	                LocalQueue localQueue = (LocalQueue)queuesWithGet.get(i);
	                if (localQueue.remove(this,pendingGets))
	                	committables.add(localQueue);
	                consumedCount++;
	            }
	    	}
	    	
	    	// 6 - Commit destinations
	    	if (committables.size() > 0)
	    	{
	    		commitBarrier = new SynchronizationBarrier();
	    		
	    		Iterator commitables = committables.iterator();
	    		while (commitables.hasNext())
	    		{
	    			Committable commitable = (Committable)commitables.next();
	    			commitable.commitChanges(commitBarrier);
	    		}
	    	}
    	}
    	finally
    	{
    		// 7 - Release locks
    		for (int i = 0; i < targetDestinations.size(); i++)
    		{
        		Committable committable = (Committable)targetDestinations.get(i);
        		committable.closeTransaction();
    		}
    	}
    	
    	// 8 - If something went wrong during put operations, stop here
    	if (putFailure != null)
    		throw putFailure;
    	
    	// 9 - Wait for commit barrier if necessary
    	if (commitBarrier != null)
    	{	
    		try
    		{
    			commitBarrier.waitFor();
    		}
    		catch (InterruptedException e)
    		{
    			throw new JMSException("Commit barrier was interrupted");
    		}
    	}
    	
    	// 10 - Unlock and deliver messages
    	if (locks != null)
    	{
			for (int i = 0; i < locks.size(); i++)
			{
				MessageLock item = locks.get(i);
				item.getDestination().unlockAndDeliver(item);
			}
    	}
    }
    
    private void rollbackUpdates( boolean rollbackPuts , boolean rollbackGets, List deliveredMessageIDs ) throws JMSException
    {
    	// Clear pending put messages
    	if (rollbackPuts && transacted)
    	{
    		if (!pendingPuts.isEmpty())
    		{
	    		if (debugEnabled)
	        		log.debug(this+" - ROLLBACK [PUT] "+pendingPuts.size()+" message(s)");
	    		
	    		pendingPuts.clear();
    		}
    	}
    	
    	// Rollback pending get messages
    	if (rollbackGets && transactionSet.size() > 0)
    	{
    		SynchronizationBarrier commitBarrier = null;
    		Set committables = new HashSet();
    		
    		// 1 - Check for pending get operations
    		TransactionItem[] pendingGets;
			if (deliveredMessageIDs != null)
			{
				// Rollback only delivered messages
				if (debugEnabled)
	        		log.debug(this+" - ROLLBACK [GET] "+deliveredMessageIDs.size()+" message(s)");
				pendingGets = transactionSet.clear(deliveredMessageIDs);
			}
			else
			{
				// Rollback the whole transaction set
				if (debugEnabled)
	        		log.debug(this+" - ROLLBACK [GET] "+transactionSet.size()+" message(s)");
				pendingGets = transactionSet.clear();
			}
			List queuesWithGet = computeUpdatedQueues(pendingGets);
			MessageLockSet locks = new MessageLockSet(pendingGets.length);
			
			// 2 - Compute target destinations lists
			List targetDestinations = computeLocalTargetDestinations(null,queuesWithGet);
			
			// 3 - Lock target destinations
			for (int i = 0; i < targetDestinations.size(); i++)
			{
	    		Committable committable = (Committable)targetDestinations.get(i);
	    		committable.openTransaction();
			}
	    	try
	    	{
	    		// 4 - Redeliver locked messages to queues
	    		for (int i = 0; i < queuesWithGet.size(); i++)
				{
					LocalQueue localQueue = (LocalQueue)queuesWithGet.get(i);
					if (localQueue.redeliverLocked(this,pendingGets,locks))
						committables.add(localQueue);
				}
	    		
	    		// 5 - Commit destinations
		    	if (committables.size() > 0)
		    	{
		    		commitBarrier = new SynchronizationBarrier();
		    		
		    		Iterator commitables = committables.iterator();
		    		while (commitables.hasNext())
		    		{
		    			Committable commitable = (Committable)commitables.next();
		    			commitable.commitChanges(commitBarrier);
		    		}
		    	}
	    	}
	    	finally
	    	{
	    		// 6 - Release locks
	    		for (int i = 0; i < targetDestinations.size(); i++)
	    		{
	        		Committable committable = (Committable)targetDestinations.get(i);
	        		committable.closeTransaction();
	    		}
	    	}
    		
	    	// 7 - Wait for commit barrier if necessary
	    	if (commitBarrier != null)
	    	{	
	    		try
	    		{
	    			commitBarrier.waitFor();
	    		}
	    		catch (InterruptedException e)
	    		{
	    			throw new JMSException("Commit barrier was interrupted");
	    		}
	    	}
	    	
	    	// 8 - Unlock and re-deliver messages if necessary
			for (int i = 0; i < locks.size(); i++)
			{
				MessageLock item = locks.get(i);
				item.getDestination().unlockAndDeliver(item);
			}
    	}
    }
    
    private List computeUpdatedQueues( TransactionItem[] pendingGets )
    {
        List updatedQueues = new ArrayList(Math.max(pendingGets.length,16));
        for (int i = 0 ; i < pendingGets.length ; i++)
        {
            LocalQueue localQueue = pendingGets[i].getDestination();
            if (!updatedQueues.contains(localQueue))
                updatedQueues.add(localQueue);
        }
        return updatedQueues;
    }
    
    private boolean hasPendingUpdates()
    {
    	return transactionSet.size() > 0 || pendingPuts.size() > 0;
    }
    
    /**
	 * @return the transactionSet
	 */
	protected final TransactionSet getTransactionSet()
	{
		return transactionSet;
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.common.session.AbstractSession#onSessionClose()
	 */
	protected void onSessionClose()
	{
    	// Rollback updates
	    try
	    {
        	if (hasPendingUpdates())
                rollbackUpdates(true,true, null);
	    }
        catch (JMSException e)
        {
            ErrorTools.log(e, log);
        }
    	
    	super.onSessionClose();
    }
    
    /* (non-Javadoc)
     * @see javax.jms.Session#createBrowser(javax.jms.Queue, java.lang.String)
     */
    public QueueBrowser createBrowser(Queue queueRef, String messageSelector) throws JMSException
    {
    	return createBrowser(idProvider.createID(), queueRef, messageSelector);
    }
    
    public QueueBrowser createBrowser(IntegerID browserId,Queue queueRef, String messageSelector) throws JMSException
    {
    	externalAccessLock.readLock().lock();
    	try
		{
	        checkNotClosed();
	        LocalQueue localQueue = engine.getLocalQueue(queueRef.getQueueName());
	
	        // Check temporary destinations scope (JMS Spec 4.4.3 p2)
	        checkTemporaryDestinationScope(localQueue);
	        
	        LocalQueueBrowser browser = new LocalQueueBrowser(this,localQueue,messageSelector,browserId);
	        registerBrowser(browser);
	        return browser;
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }

    /* (non-Javadoc)
     * @see javax.jms.Session#createConsumer(javax.jms.Destination, java.lang.String, boolean)
     */
    public MessageConsumer createConsumer(Destination destination, String messageSelector, boolean noLocal) throws JMSException
    {
    	return createConsumer(idProvider.createID(), destination, messageSelector, noLocal);
    }

    /**
     * Create a consumer with the given id
     */
    public MessageConsumer createConsumer(IntegerID consumerId,Destination destination, String messageSelector, boolean noLocal) throws JMSException
    {
    	externalAccessLock.readLock().lock();
    	try
		{
	        checkNotClosed();
	        LocalMessageConsumer consumer = new LocalMessageConsumer(engine,this,destination,messageSelector,noLocal,consumerId,null);
	        registerConsumer(consumer);
	        consumer.initDestination();
	        return consumer;
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }

    /* (non-Javadoc)
     * @see javax.jms.Session#createDurableSubscriber(javax.jms.Topic, java.lang.String, java.lang.String, boolean)
     */
    public TopicSubscriber createDurableSubscriber(Topic topic, String subscriptionName, String messageSelector, boolean noLocal) throws JMSException
    {
    	return createDurableSubscriber(idProvider.createID(), topic, subscriptionName, messageSelector, noLocal);
    }
    
    public TopicSubscriber createDurableSubscriber(IntegerID consumerId, Topic topic, String subscriptionName, String messageSelector, boolean noLocal) throws JMSException
    {
    	if (StringTools.isEmpty(subscriptionName))
            throw new FFMQException("Empty subscription name","INVALID_SUBSCRIPTION_NAME");
    	
    	externalAccessLock.readLock().lock();
    	try
		{
	        checkNotClosed();
	        
	        // Get the client ID
	        String clientID = connection.getClientID();
	        
	        // Create the consumer
	        String subscriberId = clientID+"-"+subscriptionName;
	        LocalDurableTopicSubscriber subscriber = new LocalDurableTopicSubscriber(engine,this,topic,messageSelector,noLocal,consumerId,subscriberId);
	        registerConsumer(subscriber);
	        subscriber.initDestination();
	        
	        // Register the subscription
	        engine.subscribe(clientID, subscriptionName);
	        
	        return subscriber;
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }

    /* (non-Javadoc)
     * @see javax.jms.Session#createProducer(javax.jms.Destination)
     */
    public MessageProducer createProducer(Destination destination) throws JMSException
    {
    	externalAccessLock.readLock().lock();
    	try
		{
	        checkNotClosed();
	        LocalMessageProducer producer = new LocalMessageProducer(this,destination,idProvider.createID());
	        registerProducer(producer);
	        return producer;
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }

    /* (non-Javadoc)
     * @see javax.jms.Session#recover()
     */
    public final void recover() throws JMSException
    {
    	recover(null);
    }
    
    /**
     * @see #rollback(boolean, List)
     */
    public final void recover( List deliveredMessageIDs ) throws JMSException
    {
    	externalAccessLock.readLock().lock();
    	try
		{
	    	checkNotClosed();
	        if (transacted)
	            throw new IllegalStateException("Session is transacted"); // [JMS SPEC]
	
	        rollbackUpdates(true,true, deliveredMessageIDs);
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }

    /* (non-Javadoc)
     * @see javax.jms.Session#unsubscribe(java.lang.String)
     */
    public void unsubscribe(String subscriptionName) throws JMSException
    {
    	externalAccessLock.readLock().lock();
    	try
		{
	        checkNotClosed();
	        if (StringTools.isEmpty(subscriptionName))
	            throw new FFMQException("Empty subscription name","INVALID_SUBSCRIPTION_NAME");
	
	        // Remove remaining subscriptions on all topics
	        engine.unsubscribe(connection.getClientID(), subscriptionName);
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }
    
    /* (non-Javadoc)
     * @see javax.jms.Session#createTemporaryQueue()
     */
    public TemporaryQueue createTemporaryQueue() throws JMSException
    { 	
    	externalAccessLock.readLock().lock();
    	try
		{
	        checkNotClosed();
	        String queueName = "TEMP-QUEUE-"+UUIDProvider.getInstance().getShortUUID();
	        engine.createTemporaryQueue(queueName);
	        connection.registerTemporaryQueue(queueName);
	        
	        return new TemporaryQueueRef(connection,queueName);
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }
    
    /* (non-Javadoc)
     * @see javax.jms.Session#createTemporaryTopic()
     */
    public TemporaryTopic createTemporaryTopic() throws JMSException
    {
    	externalAccessLock.readLock().lock();
    	try
		{
	        checkNotClosed();
	        String topicName = "TEMP-TOPIC-"+UUIDProvider.getInstance().getShortUUID();
	        engine.createTemporaryTopic(topicName);
	        connection.registerTemporaryTopic(topicName);
	        
	        return new TemporaryTopicRef(connection,topicName);
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.common.session.AbstractSession#acknowledge()
     */
    public final void acknowledge() throws JMSException
    {      
    	acknowledge(null);
    }
    
    /**
     * @see #commit(boolean,List)
     */
    public final void acknowledge( List deliveredMessageIDs ) throws JMSException
    {      
        if (transacted)
            throw new IllegalStateException("Session is transacted"); // [JMS SPEC]
        
        externalAccessLock.readLock().lock();
        try
		{
	        checkNotClosed();
	        commitUpdates(true,deliveredMessageIDs,false);
		}
        finally
        {
        	externalAccessLock.readLock().unlock();
        }
    }
    
    /**
     * Delete a queue
     * @param queueName
     * @throws JMSException
     */
    protected final void deleteQueue( String queueName ) throws JMSException
    {
    	transactionSet.removeUpdatesForQueue(queueName);
        engine.deleteQueue(queueName);
    }
    
    /**
     * Get the number of messages actually produced by this session
	 * @return the number of messages actually produced by this session
	 */
	public final long getProducedCount()
	{
		return producedCount;
	}
    
	/**
     * Get the number of messages actually consumed by this session
	 * @return the number of messages actually consumed by this session
	 */
	public final long getConsumedCount()
	{
		return consumedCount;
	}
	
	/*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        sb.append("(consumed=");
        sb.append(consumedCount);
        sb.append(",produced=");
        sb.append(producedCount);
        sb.append(")");
        
        return sb.toString();
    }
    
    //----------------------------------------------------------------------
    
    private static final DestinationComparator DESTINATION_COMPARATOR = new DestinationComparator();
    
    private static final class DestinationComparator implements Comparator
    {
    	/**
		 * Constructor
		 */
		public DestinationComparator()
		{
			super();
		}
    	
    	/* (non-Javadoc)
    	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
    	 */
    	public int compare(Object obj1, Object obj2)
    	{
    		AbstractLocalDestination dest1 = (AbstractLocalDestination)obj1;
    		AbstractLocalDestination dest2 = (AbstractLocalDestination)obj2;
    		
    		int delta = dest1.getName().compareTo(dest2.getName());
    		if (delta != 0)
    			return delta;
    		
    		return dest1.getClass().getName().compareTo(dest2.getClass().getName());
    	}
    }
}
