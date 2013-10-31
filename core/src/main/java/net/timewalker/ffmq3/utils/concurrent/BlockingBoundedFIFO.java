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

package net.timewalker.ffmq3.utils.concurrent;

import java.util.LinkedList;

/**
 * BlockingBoundedFIFO
 * Thread-safe blocking FIFO with a bounded size.
 */
public final class BlockingBoundedFIFO
{
	// Runtime
	private LinkedList buffer = new LinkedList();
	private Semaphore slots;
	private long timeout;
	
	/**
	 * Constructor
	 */
	public BlockingBoundedFIFO( int maxSize , long timeout )
	{
		this.slots = new Semaphore(maxSize);
		this.timeout = timeout;
	}
	
	public void addLast( Object value ) throws WaitTimeoutException
	{
		if (timeout > 0)
			slots.acquire(timeout);
		else
			slots.acquire();
		synchronized (buffer)
		{
			buffer.addLast(value);
		}
	}
	
	public Object removeFirst()
	{
		Object value;
		synchronized (buffer)
		{
			if (buffer.isEmpty())
				return null;
			value = buffer.removeFirst();
		}
		slots.release();
		return value;
	}
}
