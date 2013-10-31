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

package net.timewalker.ffmq4.storage.data.impl.journal;

import java.util.HashMap;
import java.util.Map;

/**
 * DirtyBlockTable
 */
public final class DirtyBlockTable
{
	// Attributes
	private Map<Integer,DirtyBlockEntry> dirtyBlocksMap = new HashMap<>();
	private int size;
	
	/**
	 * Constructor
	 */
	public DirtyBlockTable()
	{
		super();
	}
	
	public synchronized void markDirty( int blockIndex , byte[] blockData )
	{
		Integer key = Integer.valueOf(blockIndex);
		DirtyBlockEntry entry = dirtyBlocksMap.get(key);
		if (entry == null)
		{
			entry = new DirtyBlockEntry();
			entry.modCount = 1;
			dirtyBlocksMap.put(key, entry);
		}
		else
			entry.modCount++;
		
		entry.latestData = blockData;
		size++;
	}
	
	public synchronized void blockFlushed( int blockIndex )
	{
		Integer key = Integer.valueOf(blockIndex);
		DirtyBlockEntry entry = dirtyBlocksMap.get(key);
		if (entry == null)
			throw new IllegalArgumentException("Not a dirty block : "+blockIndex);
		
		entry.modCount--;
		if (entry.modCount == 0)
			dirtyBlocksMap.remove(key);
		
		size--;
	}
	
	public synchronized byte[] get( int blockIndex )
	{
		Integer key = Integer.valueOf(blockIndex);
		DirtyBlockEntry entry = dirtyBlocksMap.get(key);
		return entry != null ? entry.latestData : null;
	}
	
	/**
	 * @return the size
	 */
	public int getSize()
	{
		return size;
	}
	
	//-------------------------------------------------------
	
	private final static class DirtyBlockEntry
	{
		protected int modCount;
		protected byte[] latestData;

		protected DirtyBlockEntry()
		{
			super();
		}
	}
}
