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
package net.timewalker.ffmq3.storage.data;


/**
 * LinkedDataStore
 */
public interface LinkedDataStore extends DataStore
{
	/**
	 * Get the store usage amount (%)
	 */
	public int getStoreUsage();
	
	/**
	 * Initialize the data store
	 * @throws DataStoreException
	 */
	public void init() throws DataStoreException;
	
    /**
     * Get the first data handle stored
     * @return the first entry handle or -1 if the store is empty
     */
    public int first() throws DataStoreException;
    
    /**
     * Get the next data handle after the given one
     * @param handle block handle
     * @return the next entry handle or -1 if their is no successor
     */
    public int next( int handle ) throws DataStoreException;
    
    /**
     * Get the previous data handle after the given one
     * @param handle block handle
     * @return the previous entry handle or -1 if their is no predecessor
     */
    public int previous( int handle ) throws DataStoreException;
    
    /**
     * Put some data under in the store after the previous handle data
     * @param previousHandle previous entry handle, use 0 if the store is empty
     */
    public int store( Object obj , int previousHandle ) throws DataStoreException;

    /**
     * Replace data in the store at the given position
     * @param handle message handle
     * @param obj the message to store
     */
    public int replace( int handle , Object obj ) throws DataStoreException;
    
    /**
     * Retrieve the data associated to a given handle
     * @throws DataStoreException on storage error or invalid handle
     */
    public Object retrieve( int handle ) throws DataStoreException;
    
    /**
     * Delete data associated to the given handle from the store
     * @throws DataStoreException on storage error or invalid handle
     * @return the previous handle or -1
     */
    public int delete( int handle ) throws DataStoreException;
    
    /**
     * Lock the data associated to a given handle
     * @throws DataStoreException on storage error or invalid handle
     */
    public void lock( int handle ) throws DataStoreException;

    /**
     * Unlock the data associated to a given handle
     * @throws DataStoreException on storage error or invalid handle
     */
    public void unlock( int handle ) throws DataStoreException;
    
    /**
     * Check if the data associated with a given handle is locked
     * @throws DataStoreException on storage error or invalid handle
     */
    public boolean isLocked( int handle ) throws DataStoreException;
}
