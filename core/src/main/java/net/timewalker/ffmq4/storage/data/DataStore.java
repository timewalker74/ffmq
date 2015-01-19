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
package net.timewalker.ffmq4.storage.data;

import net.timewalker.ffmq4.utils.concurrent.SynchronizationBarrier;

/**
 * DataStore
 */
public interface DataStore
{
    /**
     * Ensure everything's persisted (asynchronous)
     */
    public void commitChanges( SynchronizationBarrier barrier ) throws DataStoreException;
    
    /**
     * Ensure everything's persisted (synchronous)
     */
    public void commitChanges() throws DataStoreException;
    
    /**
     * Close the store releasing associated system resources 
     */
    public void close();
    
    /**
     * Get the number of entries in the store
     */
    public int size();
 
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
}
