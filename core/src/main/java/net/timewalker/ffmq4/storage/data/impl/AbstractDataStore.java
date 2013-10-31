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
import net.timewalker.ffmq3.storage.data.LinkedDataStore;
import net.timewalker.ffmq3.utils.FastBitSet;

/**
 * AbstractDataStore
 */
public abstract class AbstractDataStore implements LinkedDataStore
{
	// Flag for debugging/testing purposes
    protected static final boolean SAFE_MODE = System.getProperty("ffmq.dataStore.safeMode", "false").equals("true");
    
	// Attributes
	protected FastBitSet locks;
	
	/**
	 * Check handle validity
	 * @param handle
	 * @throws DataStoreException
	 */
	protected abstract void checkHandle( int handle ) throws DataStoreException;
	
	/*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.store.LinkedDataStore#isLocked(int)
     */
    @Override
	public final boolean isLocked(int handle) throws DataStoreException 
    {
        if (SAFE_MODE) checkHandle(handle);
		return locks.get(handle);
	}

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.store.LinkedDataStore#lock(int)
     */
    @Override
	public final void lock(int handle) throws DataStoreException 
    {
        if (SAFE_MODE) checkHandle(handle);
        if (!locks.flip(handle))
        {
            locks.flip(handle); // Restore state
            throw new DataStoreException("Handle already locked : "+handle);
        }
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.store.LinkedDataStore#unlock(int)
     */
    @Override
	public final void unlock(int handle) throws DataStoreException 
    {
        if (SAFE_MODE) checkHandle(handle);
        if (locks.flip(handle))
        {
            locks.flip(handle); // Restore state
            throw new DataStoreException("Handle was not locked : "+handle);
        }
    }
}
