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

package net.timewalker.ffmq3.local;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.jms.Session;

import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.local.destination.LocalQueue;

/**
 * <p>
 * Set of {@link TransactionItem} objects. 
 * Used by the JMS {@link Session} implementation to keep track of the changes made in the current transaction.
 * May be fully or partially cleared on commit/rollback operations.
 * Thread-safe implementation.
 * </p>
 */
public final class TransactionSet
{
	private LinkedList items = new LinkedList();
    
    /**
     * Constructor
     */
    public TransactionSet()
    {
        super();
    }
    
    /**
     * Add an handle to the list
     * @param handle
     */
    public synchronized void add( int handle , String messageID , int deliveryMode , LocalQueue destination )
    {
    	// Create a new transaction item
    	TransactionItem item = new TransactionItem(handle,
    	                                           messageID,
    	                                           deliveryMode,
    	                                           destination);
    	items.add(item);
    }
    
    /**
     * Add an handle to the list
     * @param handle
     */
    public synchronized void add( TransactionItem item )
    {
    	items.add(item);
    }
    
    /**
     * Remove all pending updates for the given queue
     * @param queueName the queue name
     */
    public synchronized void removeUpdatesForQueue( String queueName )
    {
    	Iterator entries = items.iterator();
    	while (entries.hasNext())
		{
    		TransactionItem item = (TransactionItem)entries.next();
    		if (item.getDestination().getName().equals(queueName))
    		    entries.remove();
		}
    }
    
    /**
     * Get the list size
     * @return the list size
     */
    public synchronized int size()
    {
    	return items.size();
    }
    
    /**
     * Clear items by IDs from the transaction set and return a snapshot of the items
     */
    public synchronized TransactionItem[] clear( List deliveredMessageIDs ) throws FFMQException
    {
    	int len = deliveredMessageIDs.size();
    	TransactionItem[] itemsSnapshot = new TransactionItem[len];
    	for(int n=0;n<len;n++)
    	{
    		String deliveredMessageID = (String)deliveredMessageIDs.get(len-n-1);
    		
    		boolean found = false;
    		Iterator entries = items.iterator();
        	while (entries.hasNext())
    		{
        		TransactionItem item = (TransactionItem)entries.next();
        		if (item.getMessageId().equals(deliveredMessageID))
        		{
        			found = true;
        			itemsSnapshot[n] = item; // Store in snapshot
        			entries.remove();
        			break;
        		}
    		}
    		
    		if (!found)
    			throw new FFMQException("Message does not belong to transaction : "+deliveredMessageID,"INTERNAL_ERROR");
    	}
    	return itemsSnapshot;
    }
    
    /**
     * Clear the set and return a snapshot of its content
     */
    public synchronized TransactionItem[] clear()
    {
    	// Create snapshot
    	TransactionItem[] itemsSnapshot = (TransactionItem[]) items.toArray(new TransactionItem[items.size()]);
   	
    	// Clear
    	items.clear();
    	
    	return itemsSnapshot;
    }
    
    /**
     * Compute a list of queues that were updated in this transaction set
     */
    public synchronized List updatedQueues()
    {
    	List updatedQueues = new ArrayList(items.size());
    	for (int i = 0 ; i < items.size() ; i++)
        {
    		TransactionItem item = (TransactionItem)items.get(i);
            LocalQueue localQueue = item.getDestination();
            if (!updatedQueues.contains(localQueue))
                updatedQueues.add(localQueue);
        }
    	
    	return updatedQueues;
    }
    
    /**
     * Compute a list of queues that were updated in this transaction set
     */
    public synchronized List updatedQueues( List deliveredMessageIDs ) throws FFMQException
    {
    	int len = deliveredMessageIDs.size();
    	List updatedQueues = new ArrayList(len);
    	for(int n=0;n<len;n++)
    	{
    		String deliveredMessageID = (String)deliveredMessageIDs.get(len-n-1);
    		
    		boolean found = false;
    		Iterator entries = items.iterator();
        	while (entries.hasNext())
    		{
        		TransactionItem item = (TransactionItem)entries.next();
        		if (item.getMessageId().equals(deliveredMessageID))
        		{
        			found = true;
        			
        			LocalQueue localQueue = item.getDestination();
                    if (!updatedQueues.contains(localQueue))
                        updatedQueues.add(localQueue);

        			break;
        		}
    		}
    		
    		if (!found)
    			throw new FFMQException("Message does not belong to transaction : "+deliveredMessageID,"INTERNAL_ERROR");
    	}
    	return updatedQueues;
    }
}
