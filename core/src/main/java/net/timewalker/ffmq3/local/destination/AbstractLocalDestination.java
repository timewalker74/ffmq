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
package net.timewalker.ffmq3.local.destination;

import java.util.concurrent.locks.ReentrantLock;

import javax.jms.Destination;
import javax.jms.JMSException;

import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.common.message.AbstractMessage;
import net.timewalker.ffmq3.local.MessageLockSet;
import net.timewalker.ffmq3.local.session.LocalMessageConsumer;
import net.timewalker.ffmq3.local.session.LocalSession;
import net.timewalker.ffmq3.management.destination.definition.AbstractDestinationDefinition;
import net.timewalker.ffmq3.storage.data.DataStoreException;
import net.timewalker.ffmq3.utils.Committable;
import net.timewalker.ffmq3.utils.concurrent.CopyOnWriteList;
import net.timewalker.ffmq3.utils.concurrent.SynchronizationBarrier;

/**
 * <p>Base implementation for a local JMS destination</p>
 */
public abstract class AbstractLocalDestination implements Destination, LocalDestinationMBean, Committable
{
    // Destination definition
    protected AbstractDestinationDefinition destinationDef;
    
    // Registered consumers
    protected CopyOnWriteList localConsumers = new CopyOnWriteList();
    
    // Transaction handling
    protected ReentrantLock transactionLock = new ReentrantLock();
    
    // Runtime
    private long cumulativeCommitTime;
    private long commitCount;
    private long minCommitTime = Integer.MAX_VALUE;
    private long maxCommitTime = 0;
    protected boolean closed;
    protected Object closeLock = new Object();
    
    /**
     * Constructor
     */
    public AbstractLocalDestination( AbstractDestinationDefinition destinationDef )
    {
        this.destinationDef = destinationDef;
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.local.destination.LocalDestinationMBean#getName()
     */
    public final String getName()
    {
        return destinationDef.getName();
    }
    
    /**
     * Register a message consumer on this queue
     */
    public void registerConsumer( LocalMessageConsumer consumer )
    {
        localConsumers.add(consumer);
    }
    
    /**
     * Unregister a message listener
     */
    public void unregisterConsumer( LocalMessageConsumer consumer )
    {
        localConsumers.remove(consumer);
    }
    
    /**
	 * @return the closed
	 */
	public final boolean isClosed()
	{
		return closed;
	}
	
	protected final void checkNotClosed() throws JMSException
	{
		if (closed)
			throw new FFMQException("Destination is closed","DESTINATION_IS_CLOSED");
	}
	
	protected final void checkTransactionLock() throws JMSException
	{
		if (requiresTransactionalUpdate() && !transactionLock.isHeldByCurrentThread())
			throw new FFMQException("Destination is not locked for update","DESTINATION_NOT_LOCKED");
	}
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.local.destination.LocalQueueMBean#getRegisteredConsumersCount()
     */
    public final int getRegisteredConsumersCount()
    {
        return localConsumers.size();
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.local.destination.LocalDestinationMBean#isTemporary()
     */
    public final boolean isTemporary()
    {
        return destinationDef.isTemporary();
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.local.destination.LocalDestinationMBean#getStorageSyncMethod()
     */
    public int getStorageSyncMethod()
    {
    	return destinationDef.getStorageSyncMethod();
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.local.destination.LocalDestinationMBean#getBlockCount()
     */
    public final int getInitialBlockCount()
    {
        return destinationDef.getInitialBlockCount();
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.local.destination.LocalDestinationMBean#getMaxBlockCount()
     */
    public int getMaxBlockCount()
    {
        return destinationDef.getMaxBlockCount();
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.local.destination.LocalDestinationMBean#isUseJournal()
     */
    public boolean isUseJournal()
    {
        return destinationDef.isUseJournal();
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.local.destination.LocalDestinationMBean#getBlockSize()
     */
    public final int getBlockSize()
    {
        return destinationDef.getBlockSize();
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.local.destination.LocalDestinationMBean#getMaxNonPersistentMessages()
     */
    public final int getMaxNonPersistentMessages()
    {
        return destinationDef.getMaxNonPersistentMessages();
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.management.destination.DestinationDescriptorMBean#getAutoExtendAmount()
     */
    public int getAutoExtendAmount()
    {
    	return destinationDef.getAutoExtendAmount();
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.management.destination.DestinationDescriptorMBean#getJournalOutputBuffer()
     */
    public int getJournalOutputBuffer()
    {
    	return destinationDef.getJournalOutputBuffer();
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.management.destination.DestinationDescriptorMBean#getMaxJournalSize()
     */
    public long getMaxJournalSize()
    {
    	return destinationDef.getMaxJournalSize();
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.management.destination.DestinationDescriptorMBean#getMaxUncommittedJournalSize()
     */
    public int getMaxUnflushedJournalSize()
    {
    	return destinationDef.getMaxUnflushedJournalSize();
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.management.destination.DestinationDescriptorMBean#getMaxUncommittedStoreSize()
     */
    public int getMaxUncommittedStoreSize()
    {
    	return destinationDef.getMaxUncommittedStoreSize();
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.management.destination.DestinationDescriptorMBean#getMaxWriteBatchSize()
     */
    public int getMaxWriteBatchSize()
    {
    	return destinationDef.getMaxWriteBatchSize();
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.management.destination.DestinationDescriptorMBean#isPreAllocateFiles()
     */
    public boolean isPreAllocateFiles()
    {
    	return destinationDef.isPreAllocateFiles();
    }
    
    protected final LocalMessageConsumer lookupConsumer( String consumerID )
    {
    	synchronized (localConsumers)
		{
	    	for (int i = 0; i < localConsumers.size(); i++)
			{
				LocalMessageConsumer consumer = (LocalMessageConsumer)localConsumers.get(i);
				if (consumer.getSubscriberId().equals(consumerID))
					return consumer;
			}
	    	return null;
		}
    }
    
    protected final boolean isConsumerRegistered( String consumerID )
    {
    	return lookupConsumer(consumerID) != null;
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.local.destination.LocalDestinationMBean#getMinCommitTime()
     */
	public final long getMinCommitTime()
	{
		return commitCount == 0 ? 0 : minCommitTime;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.local.destination.LocalDestinationMBean#getMaxCommitTime()
	 */
	public final long getMaxCommitTime()
	{
		return maxCommitTime;
	}
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.local.destination.LocalQueueMBean#getAverageCommitTime()
     */
    public final double getAverageCommitTime()
    {
    	long commits = commitCount;
    	if (commits == 0)
    		return 0;
    	return (double)cumulativeCommitTime/commits;
    }
    
    protected final void notifyCommitTime( long duration )
    {
    	if (duration > maxCommitTime) maxCommitTime = duration;
		if (duration < minCommitTime) minCommitTime = duration;
		cumulativeCommitTime += duration;
		commitCount++;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.local.destination.LocalDestinationMBean#resetStats()
     */
    public void resetStats()
    {
    	minCommitTime = Integer.MAX_VALUE;
    	maxCommitTime = 0;
    	cumulativeCommitTime = 0;
    	commitCount = 0;
    }
    
    /**
     * Release destination resources
     */
    public abstract void close() throws JMSException;
    
    /**
     * Test if this destination requires transactional semantics to be updated
     * @return true if a transaction is required
     */
    protected abstract boolean requiresTransactionalUpdate();
    
    /**
     * Test if this destinations still has some uncommitted changes
     * @return true if this destinations still has some uncommitted changes
     */
    protected abstract boolean hasPendingChanges();
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.local.destination.AbstractLocalDestination#openTransaction()
     */
    public final void openTransaction()
    {
    	if (requiresTransactionalUpdate())
    		transactionLock.lock();
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.local.destination.AbstractLocalDestination#closeTransaction()
     */
    public void closeTransaction()
    {
    	boolean pendingChanges = hasPendingChanges();
    	
    	if (requiresTransactionalUpdate())
    		transactionLock.unlock();
    	
    	if (pendingChanges)
    		throw new IllegalStateException("Pending changes not commited.");
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.Committable#commitChanges()
     */
    public void commitChanges() throws JMSException
    {
    	try
    	{
	    	SynchronizationBarrier barrier = new SynchronizationBarrier();
	    	commitChanges(barrier);
	    	barrier.waitFor();
    	}
    	catch (InterruptedException e)
    	{
    		throw new DataStoreException("Wait for commit barrier was interrupted");
    	}
    }
    
    /**
     * Put a new message in the destination. The message is locked and the lock registered in the provided lock set
     * @return true if a commit is required to ensure data safety
     */
    public abstract boolean putLocked( AbstractMessage message , LocalSession session , MessageLockSet locks ) throws JMSException;
}
