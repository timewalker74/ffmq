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
package net.timewalker.ffmq4.local.destination;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Queue;

import net.timewalker.ffmq4.FFMQException;
import net.timewalker.ffmq4.common.message.AbstractMessage;
import net.timewalker.ffmq4.common.message.MessageSelector;
import net.timewalker.ffmq4.local.FFMQEngine;
import net.timewalker.ffmq4.local.MessageLock;
import net.timewalker.ffmq4.local.MessageLockSet;
import net.timewalker.ffmq4.local.TransactionItem;
import net.timewalker.ffmq4.local.TransactionSet;
import net.timewalker.ffmq4.local.session.LocalMessageConsumer;
import net.timewalker.ffmq4.local.session.LocalQueueBrowserCursor;
import net.timewalker.ffmq4.local.session.LocalSession;
import net.timewalker.ffmq4.management.destination.definition.QueueDefinition;
import net.timewalker.ffmq4.storage.data.DataStoreFullException;
import net.timewalker.ffmq4.storage.message.MessageSerializationLevel;
import net.timewalker.ffmq4.storage.message.MessageStore;
import net.timewalker.ffmq4.storage.message.impl.BlockFileMessageStore;
import net.timewalker.ffmq4.storage.message.impl.InMemoryMessageStore;
import net.timewalker.ffmq4.utils.ErrorTools;
import net.timewalker.ffmq4.utils.async.AsyncTask;
import net.timewalker.ffmq4.utils.concurrent.BlockingBoundedFIFO;
import net.timewalker.ffmq4.utils.concurrent.CopyOnWriteList;
import net.timewalker.ffmq4.utils.concurrent.SynchronizationBarrier;
import net.timewalker.ffmq4.utils.concurrent.WaitTimeoutException;
import net.timewalker.ffmq4.utils.watchdog.ActiveObject;
import net.timewalker.ffmq4.utils.watchdog.ActivityWatchdog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Implementation for a local JMS {@link Queue}</p>
 */
public final class LocalQueue extends AbstractLocalDestination implements Queue, LocalQueueMBean, ActiveObject
{   
    private static final Log log = LogFactory.getLog(LocalQueue.class);

    // Scheduler thread handling delayed redeliveries
    private static final Timer redeliveryTimer = new Timer(true); 
    
    // Definition
    private FFMQEngine engine;
    private QueueDefinition queueDef;
    
    // Message stores
    private MessageStore volatileStore;
    private MessageStore persistentStore;
    private Object storeLock = new Object();
   
    // Statistics
    private AtomicLong sentToQueueCount = new AtomicLong();
    private AtomicLong receivedFromQueueCount = new AtomicLong();
    private AtomicLong acknowledgedGetCount = new AtomicLong();
    private AtomicLong rollbackedGetCount = new AtomicLong();
    private AtomicLong expiredCount = new AtomicLong();
    
    // Settings
    private long inactivityTimeout;
    private long redeliveryDelay;
    private boolean traceEnabled = log.isTraceEnabled();
    
    // Runtime
    private boolean pendingChanges;
    private long lastActivity;
    private volatile int consumerOffset = 0; // Used for a round-robin-like consumer wake-up
    private BlockingBoundedFIFO<AbstractMessage> notificationQueue;
    
    /**
     * Constructor
     */
    public LocalQueue( FFMQEngine engine , QueueDefinition queueDef ) throws JMSException
    {
        super(queueDef);
        this.engine = engine;
        this.queueDef = queueDef;
        
        // Create notification FIFO
        int notificationQueueMaxSize =
        	Math.max(engine.getSetup().getNotificationAsyncTaskManagerThreadPoolMaxSize()+1,
        			 engine.getSetup().getInternalNotificationQueueMaxSize());
        this.notificationQueue = new BlockingBoundedFIFO<>(notificationQueueMaxSize,5*1000); /* 5s timeout */
        
        // Init volatile store
        if (queueDef.getMaxNonPersistentMessages() > 0)
        {
            this.volatileStore = new InMemoryMessageStore(queueDef);
            this.volatileStore.init();
        }
        
        // Init persistent store
        if (queueDef.hasPersistentStore())
        {
            this.persistentStore = new BlockFileMessageStore(queueDef,engine.getDiskIOAsyncTaskManager());
            this.persistentStore.init();
        }
        
        this.inactivityTimeout = engine.getSetup().getWatchdogConsumerInactivityTimeout()*1000L;
        this.redeliveryDelay = engine.getSetup().getRedeliveryDelay();
        this.lastActivity = System.currentTimeMillis();
        ActivityWatchdog.getInstance().register(this);
    }
    
    /**
     * Get the queue definition
     */
    public QueueDefinition getDefinition()
    {
        return queueDef;
    }
    
    /*
     * (non-Javadoc)
     * @see javax.jms.Queue#getQueueName()
     */
    @Override
	public String getQueueName()
    {
        return getName();
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.local.destination.AbstractLocalDestination#putLocked(net.timewalker.ffmq4.common.message.AbstractMessage, net.timewalker.ffmq4.local.session.LocalSession, net.timewalker.ffmq4.local.MessageLockSet)
     */
    @Override
    public boolean putLocked(AbstractMessage message, LocalSession session, MessageLockSet locks) throws JMSException
    {
    	checkNotClosed();
    	checkTransactionLock();
    	
    	// Consistency check
    	if (!message.isInternalCopy())
    		throw new FFMQException("Message instance is not an FFMQ internal copy !","CONSISTENCY_ERROR");
    	
    	// Dispatch message to the adequate store
    	MessageStore targetStore;
    	if (message.getJMSDeliveryMode() == DeliveryMode.NON_PERSISTENT)
    	{
    	    // Use volatile store if possible, otherwise fallback to persistent store
    	    targetStore = volatileStore != null ? volatileStore : persistentStore;
    	}
    	else
    	    targetStore = persistentStore;
    	    
    	if (targetStore == null)
    	    throw new FFMQException("Queue does not support this delivery mode : "+
    	                            (message.getJMSDeliveryMode() == DeliveryMode.NON_PERSISTENT ? 
    	                            "DeliveryMode.NON_PERSISTENT" : "DeliveryMode.PERSISTENT"),
    	                            "INVALID_DELIVERY_MODE");
    	
    	int newHandle;
        synchronized (storeLock)
        {
            newHandle = targetStore.store(message);
            if (newHandle == -1)
            {
            	// No space left for this message in the target store
            	if (targetStore == volatileStore && persistentStore != null && queueDef.isOverflowToPersistent())
            	{
            		// Fallback to persistent store if possible
            		targetStore = persistentStore;
            		newHandle = targetStore.store(message);
            	}
 
            	// Cannot store the message anywhere
            	if (newHandle == -1)
            		throw new DataStoreFullException("Cannot store message : queue is full : "+getName());
            }
            
            targetStore.lock(newHandle);
        	locks.add(newHandle, targetStore.getDeliveryMode(), this, message);
        }
        
        if (message.getJMSDeliveryMode() == DeliveryMode.PERSISTENT && requiresTransactionalUpdate())
        {
        	pendingChanges = true;
        	return true;
        }
        else
        	return false;
    }
    
    /**
     * Unlock a message.
     * Listeners are automatically notified of the new message availability.
     */
    public void unlockAndDeliver( MessageLock lockRef ) throws JMSException
    {
    	MessageStore targetStore;
    	if (lockRef.getDeliveryMode() == DeliveryMode.NON_PERSISTENT)
    	    targetStore = volatileStore;
    	else
    	    targetStore = persistentStore;
    	
    	int handle = lockRef.getHandle();
    	AbstractMessage message = lockRef.getMessage();
    	synchronized (storeLock)
        {
    		targetStore.unlock(handle);
        }
    	sentToQueueCount.incrementAndGet();
    	
    	sendAvailabilityNotification(message);
    }
    
    /**
     * Remove a locked message from this queue. The message is deleted from the underlying store.
     */
    public void removeLocked( MessageLock lockRef ) throws JMSException
    {
    	checkTransactionLock();
    	
    	MessageStore targetStore;
    	if (lockRef.getDeliveryMode() == DeliveryMode.NON_PERSISTENT)
    	    targetStore = volatileStore;
    	else
    	{
    	    targetStore = persistentStore;
    	    if (requiresTransactionalUpdate())
    	    	pendingChanges = true;
    	}
    	
    	synchronized (storeLock)
        {
    		targetStore.delete(lockRef.getHandle());
        }
    }
    
    /**
     * Commit get operations on this queue (messages are removed)
     * @return true if a store commit is required to ensure data safety
     */
    public boolean remove( LocalSession localSession , TransactionItem[] items ) throws JMSException
    {
    	checkNotClosed();
    	checkTransactionLock();
    	
    	int volatileCommitted = 0;
    	int persistentCommitted = 0;
    	
        synchronized (storeLock)
        {
    		for (int n = 0 ; n < items.length ; n++)
            {
    			TransactionItem transactionItem = items[n];
            	if (transactionItem.getDestination() != this)
            		continue;
            	
            	if (traceEnabled)
                	log.trace(localSession+" COMMIT "+transactionItem.getMessageId());
            	
            	// Delete message from store
            	if (transactionItem.getDeliveryMode() == DeliveryMode.PERSISTENT)
            	{
            		persistentStore.delete(transactionItem.getHandle());
            		persistentCommitted++;
            	}
            	else
            	{
            		volatileStore.delete(transactionItem.getHandle());
            		volatileCommitted++;
            	}
            }
        }
        acknowledgedGetCount.addAndGet(volatileCommitted + persistentCommitted);
        
        if (persistentCommitted > 0 && requiresTransactionalUpdate())
        {
        	pendingChanges = true;
        	return true;
        }
        else
        	return false;
    }
    
    /**
     * Rollback get operations on this queue (messages are unlocked)..
     * Consumers are notified of rollbacked messages availability
     * @return true if a commit is required to ensure data safety
     */
    public boolean redeliverLocked( TransactionItem[] items , MessageLockSet locks ) throws JMSException
    {
    	checkNotClosed();
    	checkTransactionLock();
    	
    	int volatileRollbacked = 0;
    	int persistentRollbacked = 0;

        synchronized (storeLock)
        {
            for (int n = 0 ; n < items.length ; n++)
            {
            	TransactionItem transactionItem = items[n];
            	if (transactionItem.getDestination() != this)
            		continue; // Not for us
            	
            	MessageStore store = transactionItem.getDeliveryMode() == DeliveryMode.PERSISTENT ? persistentStore : volatileStore;
            	int handle = transactionItem.getHandle();
            	
            	// Retrieve message content
           		AbstractMessage msg = store.retrieve(handle);
           		
           		// Update redelivered flag both in memory and message store
           		msg.setJMSRedelivered(true);
           		handle = store.replace(handle, msg);
           		
            	if (redeliveryDelay > 0)
            	{
            	    // Keep the message locked so it cannot be re-consumed immediately            	    
            		// and schedule message unlock after redeliveryDelay milliseconds
            		redeliveryTimer.schedule(new RedeliveryTask(msg,store,handle), 
            				                 redeliveryDelay);
            	}
            	else
            	{
            		// Store lock for later release
            		locks.add(handle, store.getDeliveryMode(), this, msg);
            	}

                if (transactionItem.getDeliveryMode() == DeliveryMode.PERSISTENT)
                	persistentRollbacked++;
                else
                	volatileRollbacked++;
            }
        }
        rollbackedGetCount.addAndGet(volatileRollbacked + persistentRollbacked);

        if (persistentRollbacked > 0 && requiresTransactionalUpdate())
        {
        	pendingChanges = true;
        	return true;
        }
        else
        	return false;
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.utils.Committable#commitChanges(net.timewalker.ffmq4.utils.concurrent.SynchronizationBarrier)
     */
    @Override
	public void commitChanges( SynchronizationBarrier barrier ) throws JMSException
    {
    	checkNotClosed();
    	checkTransactionLock();
    	
    	if (persistentStore != null)
    	{
    		long start = System.currentTimeMillis();
    		synchronized (storeLock)
			{
    			persistentStore.commitChanges(barrier);
			}
    		long end = System.currentTimeMillis();
    		notifyCommitTime(end-start);

    		// Clear pending commit flag
    		pendingChanges = false;
    	}
    }
    
    /** See RedeliveryTask */
    protected void redeliverMessage( AbstractMessage msg , MessageStore store , int handle )
    {
    	try
    	{
	    	synchronized (storeLock)
	        {
		    	// Unlock message in store
		   		store.unlock(handle);
		   		if (traceEnabled)
		        	log.trace("(Deferred) UNLOCKED "+msg.getJMSMessageID());
	        }
	    	
	    	// Dispatch notification
	    	sendAvailabilityNotification(msg);
    	}
    	catch (JMSException e)
    	{
    		ErrorTools.log(e, log);
    	}
    }

    /**
     * Get the first available message from this destination (matching an optional message selector).
     * If a message is found, the transaction set is updated accordingly.
     * @return a message or null if there is no available message (or queue is closed)
     */
    public AbstractMessage get( LocalSession localSession , 
				    			TransactionSet transactionSet , 
                                MessageSelector selector ) throws JMSException
    {
    	if (closed)
    		return null;
    	
    	this.lastActivity = System.currentTimeMillis();
    	
    	AbstractMessage msg = null;
    	
        // Search in volatile store first
        if (volatileStore != null)
        {
            msg = getFromStore(localSession, volatileStore, transactionSet, selector);
            
            // Then in persistent store
	        if (msg == null && persistentStore != null)
	        	msg = getFromStore(localSession, persistentStore, transactionSet, selector);
        }
        else
        if (persistentStore != null)
	        msg = getFromStore(localSession, persistentStore, transactionSet, selector);
        
        return msg;
    }
    
    /**
     * Browse a message in this queue 
     * @param cursor browser cursor
     * @param selector a message selector or null
     * @return a matching message or null
     * @throws JMSException on internal error
     */
    public AbstractMessage browse( LocalQueueBrowserCursor cursor , MessageSelector selector ) throws JMSException
    {
    	// Reset cursor to last known position
    	cursor.reset();
    	
    	// Search in volatile store first
        if (volatileStore != null)
        {
            AbstractMessage msg = browseStore(volatileStore, cursor, selector);
            if (msg != null)
            {
            	cursor.move();
                return msg;
            }
        }
        
        // Then in persistent store
        if (persistentStore != null)
        {
        	AbstractMessage msg = browseStore(persistentStore, cursor, selector);
        	if (msg != null)
            {
            	cursor.move();
                return msg;
            }
        }
        
        // Nothing found, set EOQ flag on cursor 
        cursor.setEndOfQueueReached();
        
        return null;
    }
    
    private AbstractMessage browseStore( MessageStore store , LocalQueueBrowserCursor cursor , MessageSelector selector ) throws JMSException
    {
    	AbstractMessage result = null;
    	List<Integer> expiredHandles = null;
    	
    	long now = System.currentTimeMillis();
    	synchronized (storeLock)
        {
            int current = store.first();
            
            // Skip to initial position
            while (current != -1 && cursor.position() > cursor.skipped())
            {
            	cursor.skip();
            	current = store.next(current);
            }
            
            while (current != -1)
            {
                // Skip locked messages
                if (!store.isLocked(current))
                {
                    // Retrieve the message
                    AbstractMessage msg = store.retrieve(current);
                    
                    // Check expiration
                    if (msg.getJMSExpiration() > 0 && msg.getJMSExpiration() < now)
                    {
                    	if (expiredHandles == null)
                    		expiredHandles = new ArrayList<>();
                    	store.lock(current);
                    	expiredHandles.add(Integer.valueOf(current));
                    	current = store.next(current);
                    	continue;
                    }
                    
                    // Check selector
                    if (selector == null)
                    {
                    	result = msg;
                        break;
                    }
                    else
                    {
                    	msg.ensureDeserializationLevel(MessageSerializationLevel.ALL_HEADERS);
                    	if (selector.matches(msg))
                    	{
                    		result = msg;
                            break;
                    	}
                    }
                }
                
                cursor.skip();
                current = store.next(current);
            }
        }
    	
    	// Take care of expired messages
        if (expiredHandles != null)
        {
        	openTransaction();
        	try
        	{
        		for (int i = 0; i < expiredHandles.size(); i++)
				{
					int expiredHandle = expiredHandles.get(i).intValue();
					synchronized (storeLock)
			        {
						store.delete(expiredHandle);
			        }
					expiredCount.incrementAndGet();
				}
        		commitChanges(null); // Async commit
        	}
        	finally
        	{
        		closeTransaction();
        	}
        }
        
        return result;
    }
    
    private AbstractMessage getFromStore( LocalSession localSession ,
                                          MessageStore store ,
                                          TransactionSet transactionSet ,
                                          MessageSelector selector ) throws JMSException
    {
    	AbstractMessage result = null;
    	List<Integer> expiredHandles = null;
    	
        synchronized (storeLock)
        {
            int current = store.first();
            while (current != -1)
            {
                // Skip locked messages
                if (!store.isLocked(current))
                {
                    // Retrieve the message
                    AbstractMessage msg = store.retrieve(current);
   
                    // Check expiration
                    if (msg.getJMSExpiration() > 0 && msg.getJMSExpiration() < lastActivity)
                    {
                    	if (expiredHandles == null)
                    		expiredHandles = new ArrayList<>();
                    	store.lock(current);
                    	expiredHandles.add(Integer.valueOf(current));
                    	current = store.next(current);
                    	continue;
                    }
                    
                    // Check selector
                    boolean matchesSelector;
                    if (selector != null)
                    {
                    	msg.ensureDeserializationLevel(MessageSerializationLevel.ALL_HEADERS);
                    	matchesSelector = selector.matches(msg);
                    }
                    else
                    	matchesSelector = true;
                    
                    if (matchesSelector)
                    {
                        store.lock(current);
                        
                        if (traceEnabled)
                        	log.trace(localSession+" LOCKED "+msg.getJMSMessageID());
                        
                        transactionSet.add(current,
                        		           msg.getJMSMessageID(),
                        		           store.getDeliveryMode(),
                        		           this);
                        receivedFromQueueCount.incrementAndGet();
                        result = msg;
                        break;
                    }
                }
                
                current = store.next(current);
            }
        }
        
        // Take care of expired messages
        if (expiredHandles != null)
        {
        	openTransaction();
        	try
        	{
        		for (int i = 0; i < expiredHandles.size(); i++)
				{
					int expiredHandle = expiredHandles.get(i).intValue();
					synchronized (storeLock)
			        {
						store.delete(expiredHandle);
			        }
					expiredCount.incrementAndGet();
				}
        		commitChanges(null); // Async commit
        	}
        	finally
        	{
        		closeTransaction();
        	}
        }
        
        return result;
    }
    
    /**
     * Purge some messages from the buffer
     */
    public void purge( MessageSelector selector ) throws JMSException
    {
        if (volatileStore != null)
            purgeStore(volatileStore,selector);
        if (persistentStore != null)
        {
        	openTransaction();
        	try
        	{
        		purgeStore(persistentStore,selector);
        		commitChanges();
        	}
        	finally
        	{
        		closeTransaction();
        	}
        }
    }
    
    private void purgeStore( MessageStore store , MessageSelector selector ) throws JMSException
    {
        synchronized (storeLock)
        {
            int current = store.first();
            while (current != -1)
            {
                int next = store.next(current);
                
                // Skip locked messages
                if (!store.isLocked(current))
                {
                	if (selector != null)
                	{
                		AbstractMessage msg = store.retrieve(current);
                		msg.ensureDeserializationLevel(MessageSerializationLevel.ALL_HEADERS);
                        if (selector.matches(msg))
                        	store.delete(current);
                	}
                	else
                		store.delete(current);
                }
                
                current = next;
            }
        }
    }
    
    /**
     * Notify a consumer that a message is probably available for it to retrieve
     */
    private void notifyConsumer( AbstractMessage message )
    {
    	LocalMessageConsumer singleConsumer = null;
    	CopyOnWriteList<LocalMessageConsumer> consumersSnapshot = null;
    	synchronized (localConsumers)
		{
    		switch (localConsumers.size())
    		{
    			case 0 : return; // Nobody's listening
    			case 1 : singleConsumer = localConsumers.get(0); break; // Single consumer
    			default : // Multiple consumers
    				consumersSnapshot = localConsumers.fastCopy();		
    		}
		}

    	if (singleConsumer != null)
    		notifySingleConsumer(singleConsumer,message);
    	else
    		notifyNextConsumer(consumersSnapshot,message);
    }
    
    private void notifySingleConsumer( LocalMessageConsumer consumer , AbstractMessage message )
    {
    	try
        {    		
	        // Check message selector
	        if (message != null)
	        {    
                MessageSelector consumerSelector = consumer.getReceiveSelector();
                if (consumerSelector != null)
                {
                	message.ensureDeserializationLevel(MessageSerializationLevel.ALL_HEADERS);
                	if (!consumerSelector.matches(message))
                		return;
                }
	        }
	
	    	consumer.wakeUp();
		}
        catch (JMSException e)
    	{
    		ErrorTools.log(e, log);
    	}
    }
    
    private void notifyNextConsumer( CopyOnWriteList<LocalMessageConsumer> consumersSnapshot , AbstractMessage message )
    {
    	// Find a consumer to notify
    	int localConsumersCount = consumersSnapshot.size();
    	int currentOffset = consumerOffset++; // Copy current offset (value is volatile and should not change during the following loop)
    	for (int n = 0 ; n < localConsumersCount ; n++)
	    {
            int offset = ((n+currentOffset) % localConsumersCount);
            LocalMessageConsumer consumer = consumersSnapshot.get(offset);
            
            // Check that the consumer connection is started
            if (!consumer.getSession().getConnection().isStarted())
            	continue;
            
            // Check message selector
            if (message != null)
            {
            	MessageSelector consumerSelector = consumer.getReceiveSelector();
            	if (consumerSelector != null)
                {
            		message.ensureDeserializationLevel(MessageSerializationLevel.ALL_HEADERS);
            		
	                try
	                {
                    	if (!consumerSelector.matches(message))
                    		continue;
	                }
	                catch (JMSException e)
	                {
	                	ErrorTools.log(e, log);
	                    continue;
	                }
                }
            }

            try
    		{
            	consumer.wakeUp();
            	break;
    		}
            catch (JMSException e)
        	{
        		ErrorTools.log(e, log);
        	}
	    }
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.local.destination.LocalDestinationMBean#getSize()
     */
    @Override
	public int getSize()
    {
        int size = 0;
        synchronized (storeLock)
        {
            if (volatileStore != null)
                size += volatileStore.size();
            if (persistentStore != null)
                size += persistentStore.size();
        }
        return size;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.local.destination.LocalQueueMBean#getMemoryStoreUsage()
     */
    @Override
    public int getMemoryStoreUsage()
    {
    	return volatileStore != null ? volatileStore.getStoreUsage() : -1;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.local.destination.LocalQueueMBean#getPersistentStoreUsage()
     */
    @Override
    public int getPersistentStoreUsage()
    {
    	return persistentStore != null ? persistentStore.getStoreUsage() : -1;
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.local.destination.LocalDestinationMBean#resetStats()
     */
    @Override
	public void resetStats()
    {
    	super.resetStats();
    	sentToQueueCount.set(0);
    	receivedFromQueueCount.set(0);
    	acknowledgedGetCount.set(0);
    	rollbackedGetCount.set(0);
    	expiredCount.set(0);
    }
    
    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString()
    {
    	StringBuilder sb = new StringBuilder();
       
       sb.append("Queue{");
       sb.append(getName());
       sb.append("}[size=");
       sb.append(getSize());
       sb.append(",consumers=");
       sb.append(localConsumers.size());
       sb.append(",in=");
       sb.append(sentToQueueCount);
       sb.append(",out=");
       sb.append(receivedFromQueueCount);
       sb.append(",ack=");
       sb.append(acknowledgedGetCount);
       sb.append(",rollback=");
       sb.append(rollbackedGetCount);
       sb.append(",expired=");
       sb.append(expiredCount);
       sb.append("]");
       
       return sb.toString();
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.local.destination.AbstractLocalDestination#close()
     */
    @Override
	public final void close() throws JMSException
    {
    	synchronized (closeLock)
		{
	    	if (closed)
	    		return;
	    	closed = true;
		}
    	
    	ActivityWatchdog.getInstance().unregister(this);
    	
    	synchronized (storeLock)
		{
	        if (volatileStore != null)
	        {
	            volatileStore.close();
	            
	            // Delete message store if the queue was temporary
	            if (queueDef.isTemporary())
	                volatileStore.delete();
	        }
	        if (persistentStore != null)
	        {
	            persistentStore.close();
	            
	            // Delete message store if the queue was temporary
	            if (queueDef.isTemporary())
	                persistentStore.delete();
	        }
		}
    	
    	if (!localConsumers.isEmpty()){
    	    CopyOnWriteList<LocalMessageConsumer> consumers = localConsumers.fastCopy();
    	    for (int n=0;n<consumers.size();n++)
            {
    	        LocalMessageConsumer consumer = consumers.get(n);
    	        try
    	        {
    	            consumer.close();
    	        }
    	        catch (JMSException e)
    	        {
    	            ErrorTools.log(e, log);
    	        }
            }
    	}
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.local.destination.LocalQueueMBean#getSentToQueueCount()
     */
    @Override
	public long getSentToQueueCount()
    {
        return sentToQueueCount.get();
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.local.destination.LocalQueueMBean#getReceivedFromQueueCount()
     */
    @Override
	public long getReceivedFromQueueCount()
    {
        return receivedFromQueueCount.get();
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.local.destination.LocalQueueMBean#getAcknowledgedGetCount()
     */
    @Override
	public long getAcknowledgedGetCount()
    {
        return acknowledgedGetCount.get();
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.local.destination.LocalQueueMBean#getRollbackedGetCount()
     */
    @Override
	public long getRollbackedGetCount()
    {
        return rollbackedGetCount.get();
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.local.destination.LocalQueueMBean#getExpiredCount()
     */
    @Override
	public long getExpiredCount()
	{
		return expiredCount.get();
	}
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.utils.watchdog.ActiveObject#getLastActivity()
     */
    @Override
	public long getLastActivity()
    {
    	return lastActivity;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.utils.watchdog.ActiveObject#getTimeoutDelay()
     */
    @Override
	public long getTimeoutDelay()
    {
    	return inactivityTimeout;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.utils.watchdog.ActiveObject#onActivityTimeout()
     */
    @Override
	public boolean onActivityTimeout() throws Exception
    {
    	// Called by watchdog if queue is inactive
    	if (closed)
    		return true;
    	
    	if (getSize() == 0)
    		return false; // Queue is empty
    	
    	// Notify next consumer
    	notifyConsumer(null);

    	return false;
    }
    
    protected void processAvailabilityNotificationQueue()
    {
    	while (!closed)
    	{
    		AbstractMessage message = notificationQueue.removeFirst();
    		if (message == null)
    			return;
    			
    		notifyConsumer(message);
    	}
    }
    
    private void sendAvailabilityNotification( AbstractMessage message ) throws JMSException
    {
    	if (localConsumers.isEmpty())
			return;
		
		try
		{
			// May block if FIFO is full
			notificationQueue.addLast(message);
		}
		catch (WaitTimeoutException e)
		{
			log.error("Cannot enqueue notification "+e);
			return;
		}
		
    	engine.getNotificationAsyncTaskManager().execute(notificationTask);
    }
    
    //-------------------------------------------------------------------------------
    
    private final class RedeliveryTask extends TimerTask
    {
    	// Attributes
    	private AbstractMessage msg;
    	private MessageStore store;
    	private int handle;
    	
    	/**
		 * Constructor
		 */
		public RedeliveryTask( AbstractMessage msg , MessageStore store , int handle )
		{
			this.msg = msg;
			this.store = store;
			this.handle = handle;
		}
    	
    	/* (non-Javadoc)
    	 * @see java.util.TimerTask#run()
    	 */
    	@Override
		public void run()
    	{
    		redeliverMessage(msg,store,handle);
    	}
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.local.destination.AbstractLocalDestination#hasPendingChanges()
     */
    @Override
    protected boolean hasPendingChanges()
    {
    	return pendingChanges;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.local.destination.AbstractLocalDestination#hasTransactionSupport()
     */
    @Override
    protected boolean requiresTransactionalUpdate()
    {
    	return persistentStore != null && persistentStore.isFailSafe();
    }
    
    //-------------------------------------------------------------------------------
    
    private final NotificationTask notificationTask = new NotificationTask();
    
    private final class NotificationTask implements AsyncTask
    {
    	/**
		 * Constructor
		 */
		public NotificationTask()
		{
			super();
		}
    	
    	/* (non-Javadoc)
    	 * @see net.timewalker.ffmq4.utils.async.AsyncTask#isMergeable()
    	 */
    	@Override
		public boolean isMergeable()
    	{
    		return true;
    	}
    	
    	/* (non-Javadoc)
    	 * @see net.timewalker.ffmq4.utils.async.AsyncTask#execute()
    	 */
    	@Override
		public void execute()
    	{
    		processAvailabilityNotificationQueue();
    	}
    }
}
