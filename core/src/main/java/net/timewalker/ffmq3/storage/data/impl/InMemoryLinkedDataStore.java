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
package net.timewalker.ffmq3.storage.data.impl;

import net.timewalker.ffmq3.storage.data.DataStoreException;
import net.timewalker.ffmq3.utils.FastBitSet;
import net.timewalker.ffmq3.utils.concurrent.SynchronizationBarrier;

/**
 * InMemoryLinkedObjectStore
 */
public final class InMemoryLinkedDataStore extends AbstractDataStore
{
    // Setup
	private String name;
    private int maxSize;
    
    // Allocation table
    private int[] nextEntry;
    private int[] previousEntry;
    private int firstEntry;

    // Data table
    private Object[] data;
    
    // Runtime
    private int size;
    private int lastEmpty;
    
    /**
     * Constructor
     */
    public InMemoryLinkedDataStore( String name , int initialSize , int maxSize )
    {
    	this.name = name;
        this.maxSize = maxSize;
        this.nextEntry = new int[initialSize];
        this.previousEntry = new int[initialSize];
        this.firstEntry = -1;
        this.data = new Object[initialSize];
        this.locks = new FastBitSet(initialSize);
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.LinkedDataStore#init()
     */
    public void init() throws DataStoreException
    {
    	// Nothing
    }
    
    private boolean reallocate() throws DataStoreException
    {
        int actualSize = data.length;
        if (actualSize >= maxSize)
            return false; // Store is full
        
        int newSize = Math.min(actualSize * 2,maxSize);
        try
        {
            int[] newNextEntry = new int[newSize];
            int[] newPreviousEntry = new int[newSize];
            Object[] newData = new Object[newSize];
            
            System.arraycopy(nextEntry, 0, newNextEntry, 0, actualSize);
            System.arraycopy(previousEntry, 0, newPreviousEntry, 0, actualSize);
            System.arraycopy(data, 0, newData, 0, actualSize);
    
            this.nextEntry = newNextEntry;
            this.previousEntry = newPreviousEntry;
            this.data = newData;
            this.locks.ensureCapacity(newSize);
        }
        catch (OutOfMemoryError e)
        {
            throw new DataStoreException("["+name+"] Cannot extend in-memory datastore to "+newSize);
        }
        
        return true;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.impl.AbstractDataStore#checkHandle(int)
     */
    protected void checkHandle(int handle) throws DataStoreException
    {
        if (handle < 0 ||
            handle >= data.length ||
            data[handle] == null)
            throw new DataStoreException(name+" : Invalid handle : "+handle);
    }
    
    private int findEmpty()
    {
        int pos = lastEmpty; 
        for(int n=0;n<data.length;n++)
        {
        	if (pos >= data.length)
                pos = 0;
            if (data[pos] == null)
            {
                lastEmpty = pos+1;
                return pos;
            }
            pos++;
        }
        return -1;
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.store.LinkedObjectStore#retrieve(int)
     */
    public Object retrieve(int handle) throws DataStoreException
    {
        if (SAFE_MODE) checkHandle(handle);
        return data[handle];
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.LinkedDataStore#replace(int, java.lang.Object)
     */
    public int replace(int handle, Object obj) throws DataStoreException
    {
        if (SAFE_MODE) checkHandle(handle);
    	data[handle] = obj;
    	return handle;
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.store.LinkedObjectStore#store(java.lang.Object, int)
     */
    public int store(Object obj, int previousHandle) throws DataStoreException
    {
    	if (data.length == size)
            if (!reallocate())
            	return -1; // Store is full
    	
        int nextHandle;
        if (previousHandle != -1)
        {
            if (SAFE_MODE) checkHandle(previousHandle);
            nextHandle = nextEntry[previousHandle];
        }
        else
            nextHandle = firstEntry;
        
        int newHandle = findEmpty();
        if (newHandle == -1)
        	return -1;
        
        // Store data
        previousEntry[newHandle] = previousHandle;
        nextEntry[newHandle] = nextHandle;
        data[newHandle] = obj;
        
        // Connect to list
        if (previousHandle != -1)
            nextEntry[previousHandle] = newHandle;
        if (nextHandle != -1)
            previousEntry[nextHandle] = newHandle;
        
        // Update first entry if necessary
        if (previousHandle == -1)
            firstEntry = newHandle;
        
        size++;
        
        return newHandle;
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.store.LinkedStore#delete(int)
     */
    public int delete(int handle) throws DataStoreException
    {
        if (SAFE_MODE) checkHandle(handle);
        
        int previousHandle = previousEntry[handle];
        int nextHandle = nextEntry[handle];
        
        // Reconnect list
        if (previousHandle != -1)
            nextEntry[previousHandle] = nextHandle;
        if (nextHandle != -1)
            previousEntry[nextHandle] = previousHandle;
 
        // Clear data
        previousEntry[handle] = -1;
        nextEntry[handle] = -1;
        data[handle] = null;
        locks.clear(handle);
        
        if (firstEntry == handle)
            firstEntry = nextHandle;
        
        size--;
        
        return previousHandle;
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.store.LinkedStore#first()
     */
    public int first() throws DataStoreException
    {
        return firstEntry;
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.store.LinkedStore#next(int)
     */
    public int next(int handle) throws DataStoreException
    {
        if (SAFE_MODE) checkHandle(handle);
        return nextEntry[handle];
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.store.LinkedStore#previous(int)
     */
    public int previous(int handle) throws DataStoreException
    {
        if (SAFE_MODE) checkHandle(handle);
        return previousEntry[handle];
    }
	
	/*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.store.LinkedStore#size()
     */
    public int size()
    {
        return size;
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.DataStore#commit(net.timewalker.ffmq3.utils.concurrent.SynchronizationBarrier)
     */
    public void commitChanges(SynchronizationBarrier barrier) throws DataStoreException
    {
    	// Nothing to do
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.DataStore#commit()
     */
    public void commitChanges() throws DataStoreException
    {
    	// Nothing to do
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.store.LinkedStore#close()
     */
    public void close()
    {
        // Nothing to do
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.LinkedDataStore#getStoreUsage()
     */
    public int getStoreUsage()
    {
    	long ratio = maxSize > 0 ? (long)size*100/maxSize : 0;
    	return (int)ratio;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append("Allocation Table (size="+size+")\n");
        sb.append("------------------------------------\n");
        sb.append("first entry index : ");
        sb.append(firstEntry);
        sb.append("\n");
        for (int n = 0 ; n < data.length ; n++)
        {
            sb.append(n);
            sb.append(": ");
            if (data[n] == null)
                sb.append("(free)\n");
            else
            {
                sb.append(previousEntry[n]);
                sb.append("\t");
                sb.append(nextEntry[n]);
                sb.append("\t");
                sb.append(data[n]);
                sb.append("\n");
            }
        }
        sb.append("------------------------------------\n");
        
        return sb.toString();
    }
}
