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
import java.util.List;

import net.timewalker.ffmq3.common.message.AbstractMessage;
import net.timewalker.ffmq3.local.destination.LocalQueue;

/**
 * HandleSet
 */
public final class MessageLockSet
{
	private List<MessageLock> items;
    
    /**
     * Constructor
     */
    public MessageLockSet( int initialSize )
    {
        super();
        items = new ArrayList<>(initialSize);
    }
    
    /**
     * Add an handle to the list
     * @param handle
     */
    public void add( int handle , int deliveryMode , LocalQueue destination , AbstractMessage message )
    {
    	items.add(new MessageLock(handle,deliveryMode,destination,message));
    }

    /**
     * Get the list size
     * @return the list size
     */
    public int size()
    {
    	return items.size();
    }
    
    /**
     * Get the nth item
     */
    public MessageLock get( int n )
    {
    	return items.get(n);
    }
}
