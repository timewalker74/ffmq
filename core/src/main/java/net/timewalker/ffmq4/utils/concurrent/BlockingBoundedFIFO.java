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

package net.timewalker.ffmq4.utils.concurrent;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * BlockingBoundedFIFO
 * Thread-safe blocking FIFO with a bounded size.
 */
public final class BlockingBoundedFIFO<T>
{
	// Runtime
	private LinkedList<T> buffer = new LinkedList<>();
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
	
	public void addLast( T value ) throws WaitTimeoutException
	{
		try
		{
			if (timeout >= 0)
			{
				if (!slots.tryAcquire(timeout,TimeUnit.MILLISECONDS))
					throw new WaitTimeoutException();
			}
			else
				slots.acquire();
		}
		catch (InterruptedException e)
		{
			throw new WaitTimeoutException();
		}
		
		synchronized (buffer)
		{
			buffer.addLast(value);
		}
	}
	
	public T removeFirst()
	{
		T value;
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
