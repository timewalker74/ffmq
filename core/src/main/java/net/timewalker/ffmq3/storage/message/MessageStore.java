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
package net.timewalker.ffmq3.storage.message;

import javax.jms.JMSException;

import net.timewalker.ffmq3.common.message.AbstractMessage;
import net.timewalker.ffmq3.utils.concurrent.SynchronizationBarrier;

/**
 * MessageStore
 */
public interface MessageStore
{
	/**
	 * Get the store usage amount (%)
	 * (Ratio of used space over currently allocated space)
	 */
	public int getStoreUsage();
	
	/**
	 * Get the absolute store usage amount (%)
	 * (Ratio of used space over maximum allocatable space)
	 */
	public int getAbsoluteStoreUsage();
	
    /**
     * Get the first message handle stored
     * @return the first entry handle or -1 if the store is empty
     */
    public int first() throws JMSException;
    
    /**
     * Get the next message handle after the given one
     * @param handle block handle
     * @return the next entry handle or -1 if their is no successor
     */
    public int next( int handle ) throws JMSException;
    
    /**
     * Get the previous message handle after the given one
     * @param handle message handle
     * @return the previous entry handle or -1 if their is no successor
     */
    public int previous( int handle ) throws JMSException;
    
    /**
     * Put some message under in the store after the last message with the same priority
     * @param message the message to store
     */
    public int store( AbstractMessage message ) throws JMSException;
    
    /**
     * Replace a message in the store
     * @param handle message handle
     * @param message the message to store
     */
    public int replace( int handle , AbstractMessage message ) throws JMSException;

    /**
     * Delete message associated to the given handle from the store
     * @throws JMSException on storage error or invalid handle
     */
    public void delete( int handle ) throws JMSException;

    /**
     * Retrieve the message associated to a given handle
     * @throws JMSException on storage error or invalid handle
     */
    public AbstractMessage retrieve( int handle ) throws JMSException;
    
    /**
     * Lock the message associated to a given handle
     * @throws JMSException on storage error or invalid handle
     */
    public void lock( int handle ) throws JMSException;

    /**
     * Unlock the message associated to a given handle
     * @throws JMSException on storage error or invalid handle
     */
    public void unlock( int handle ) throws JMSException;
    
    /**
     * Check if the message associated with a given handle is locked
     * @throws JMSException on storage error or invalid handle
     */
    public boolean isLocked( int handle ) throws JMSException;
    
    /**
     * Get the number of messages in the store
     */
    public int size();
    
    /**
     * Ensure everything is persisted (asynchronous)
     */
    public void commitChanges( SynchronizationBarrier barrier ) throws JMSException;
    
    /**
     * Ensure everything is persisted (synchronous)
     */
    public void commitChanges() throws JMSException;
    
    /**
     * Initialize the message store
     */
    public void init()  throws JMSException;
    
    /**
     * Close the store releasing associated system resources 
     */
    public void close() throws JMSException;
    
    /**
     * Delete the store 
     */
    public void delete() throws JMSException;
    
    /**
     * Test if the store is syncing on write
     */
    public boolean isFailSafe();
    
    /**
     * Get the delivery mode for this store
     */
    public int getDeliveryMode();
}
