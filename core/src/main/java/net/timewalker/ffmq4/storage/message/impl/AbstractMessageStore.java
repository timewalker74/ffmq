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
package net.timewalker.ffmq3.storage.message.impl;

import javax.jms.JMSException;

import net.timewalker.ffmq3.common.message.AbstractMessage;
import net.timewalker.ffmq3.management.destination.definition.QueueDefinition;
import net.timewalker.ffmq3.storage.data.LinkedDataStore;
import net.timewalker.ffmq3.storage.message.MessageStore;
import net.timewalker.ffmq3.utils.concurrent.SynchronizationBarrier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * AbstractMessageStore
 */
public abstract class AbstractMessageStore implements MessageStore
{
	private static final Log log = LogFactory.getLog(AbstractMessageStore.class);
	
	// Attributes
	protected QueueDefinition queueDef;
	protected LinkedDataStore dataStore;
	
	// Runtime
    private int[] handleByPriority = new int[10];
 
    /**
     * Constructor
     */
    public AbstractMessageStore( QueueDefinition queueDef )
    {
        this.queueDef = queueDef;
    }
    
    protected abstract LinkedDataStore createDataStore();
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.message.MessageStore#init()
     */
    @Override
	public void init() throws JMSException
    {
    	// Initialize the data store
        this.dataStore = createDataStore();
        dataStore.init();
        
        // Re-build priority table
    	initPriorityTable();
    }
    
    private void initPriorityTable() throws JMSException
    {
        for (int n = 0 ; n < handleByPriority.length ; n++)
            handleByPriority[n] = -1;
        
        if (dataStore.size() > 10000)
            log.warn("["+queueDef.getName()+"] Re-creating priority table, this may take a while ... ("+dataStore.size()+" messages)");
        
        // Scan data store to initialize priority table
        log.debug("["+queueDef.getName()+"] Scanning datastore to initialize priority table ...");
        int handle = dataStore.first();
        while (handle != -1)
        {
        	int priority = retrieveMessagePriority(handle);
        	handleByPriority[priority] = handle;
        	handle = dataStore.next(handle);
        }
        if (log.isTraceEnabled())
        {
        	log.trace("["+queueDef.getName()+"] Priority table :");
        	for (int n = 0 ; n < handleByPriority.length ; n++)
        		log.trace("["+queueDef.getName()+"] Priority "+n+" : "+handleByPriority[n]);
        }
        log.debug("["+queueDef.getName()+"] Scan complete.");
    }

    /**
     * Find the nearest known handle of the preceding message, taking priorities into account
     */
    private int getNearestHandle( int priority )
    {
        for (int n = priority+1 ; n < 10  ; n++)
        {
            int handle = handleByPriority[n];
            if (handle != -1)
                return handle;
        }
        return -1;
    }

    /**
     * Find the handle of the preceding message, taking priorities into account
     */
    private int getLastHandleForPriority( int priority )
    {
        int lastHandle = handleByPriority[priority];
        if (lastHandle != -1)
            return lastHandle;
        
        return getNearestHandle(priority);
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.message.MessageStore#getStoreUsage()
     */
    @Override
	public int getStoreUsage()
    {
    	return dataStore.getStoreUsage();
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.local.destination.store.MessageStore#previous(int)
     */
    @Override
	public final int previous(int handle) throws JMSException
    {
		return dataStore.previous(handle);
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.local.destination.store.MessageStore#delete(int)
     */
    @Override
	public final void delete(int handle) throws JMSException
    {
    	// Update the datastore
    	int previousHandle = dataStore.delete(handle);
    	
    	// Update the priority indexes
    	for (int n = 0; n < handleByPriority.length; n++) 
    		if (handleByPriority[n] == handle)
    		{
    			if (n == 9)
    				handleByPriority[n] = previousHandle;
    			else
    			{
    				// Check if the deleted message was the last one in its priority rank
					if (getNearestHandle(n) != previousHandle)
						handleByPriority[n] = previousHandle;
					else
						handleByPriority[n] = -1;
    			}
    			break;
    		}
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.local.destination.store.MessageStore#first()
     */
    @Override
	public final int first() throws JMSException
    {
    	return dataStore.first();
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.local.destination.store.MessageStore#next(int)
     */
    @Override
	public final int next(int handle) throws JMSException
    {
        return dataStore.next(handle);
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.local.destination.store.MessageStore#size()
     */
    @Override
	public final int size()
    {
        return dataStore.size();
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.message.MessageStore#commit(net.timewalker.ffmq3.utils.concurrent.SynchronizationBarrier)
     */
    @Override
	public final void commitChanges(SynchronizationBarrier barrier) throws JMSException
    {
    	dataStore.commitChanges(barrier);
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.message.MessageStore#commitChanges()
     */
    @Override
	public final void commitChanges() throws JMSException
    {
    	dataStore.commitChanges();
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.local.destination.store.MessageStore#close()
     */
    @Override
	public final void close()
    {
        dataStore.close();
    }
    
    /**
     * Retrieve the message with the given handle
     */
    protected abstract AbstractMessage retrieveMessage( int handle ) throws JMSException;
    
    /**
     * Retrieve the priority of the message with the given handle
     */
    protected abstract int retrieveMessagePriority( int handle ) throws JMSException;
    
    /**
     * Store a message right after the given handle
     */
    protected abstract int storeMessage( AbstractMessage message , int previousHandle ) throws JMSException;
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.message.MessageStore#retrieve(int)
     */
    @Override
	public final AbstractMessage retrieve(int handle) throws JMSException
    {
    	return retrieveMessage(handle);
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.message.MessageStore#replace(int, net.timewalker.ffmq3.common.message.AbstractMessage)
     */
    @Override
	public final int replace(int handle, AbstractMessage message) throws JMSException
    {
    	int newHandle = replaceMessage(handle, message);
    	if (newHandle == -1)
    		return -1;
    	
    	if (newHandle != handle)
    	{
    		// Update the priority indexes
        	for (int n = 0; n < handleByPriority.length; n++) 
        		if (handleByPriority[n] == handle)
        		{
        			handleByPriority[n] = newHandle;
        			break;
        		}
    	}
    	
    	return newHandle;
    }
    
    protected abstract int replaceMessage(int handle, AbstractMessage message) throws JMSException;
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.local.destination.store.MessageStore#store(net.timewalker.ffmq3.common.message.AbstractMessage)
     */
    @Override
	public final int store(AbstractMessage message) throws JMSException
    {
    	int priority = message.getJMSPriority();
        int previousHandle = getLastHandleForPriority(priority);	        
        int newHandle = storeMessage(message,previousHandle);
        if (newHandle == -1)
        	return -1;
        
        // Update priority table
        handleByPriority[priority] = newHandle;
        
        return newHandle;
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.local.destination.store.MessageStore#isLocked(int)
     */
	@Override
	public final boolean isLocked(int handle) throws JMSException 
	{
		return dataStore.isLocked(handle);
	}

	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.local.destination.store.MessageStore#lock(int)
	 */
	@Override
	public final void lock(int handle) throws JMSException 
	{
		dataStore.lock(handle);
	}

	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.local.destination.store.MessageStore#unlock(int)
	 */
	@Override
	public final void unlock(int handle) throws JMSException 
	{
		dataStore.unlock(handle);
	}

	/*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
	@Override
	public String toString() 
	{
		StringBuffer sb = new StringBuffer();
		sb.append("Priority index table :\n");
		sb.append("----------------------\n");
		for (int i = 0; i < handleByPriority.length; i++) 
		{
			sb.append(i);
			sb.append(" - ");
			sb.append(handleByPriority[i]);
			sb.append("\n");
		}
		sb.append(dataStore);
		
		return sb.toString();
	}    
}
