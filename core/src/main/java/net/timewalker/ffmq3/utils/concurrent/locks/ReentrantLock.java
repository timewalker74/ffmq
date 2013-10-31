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

package net.timewalker.ffmq3.utils.concurrent.locks;

import net.timewalker.ffmq3.utils.concurrent.WaitTimeoutException;

/**
 * ReentrantLock
 */
public final class ReentrantLock
{
	// Runtime
	private Thread owner;
	private int count;
	
	public synchronized void lock()
    {
		Thread currentThread = Thread.currentThread();
		if (owner == currentThread)
		{
			count++;
			if (count < 0)
				throw new Error("Maximum lock count exceeded");
		}
		else
		{
	        while (owner != null)
	        {
	            try
	            {
	                wait();
	            }
	            catch (InterruptedException e)
	            {
	                // Ignore
	            }
	        }
	        
	        owner = currentThread;
        	count = 1;
		}
    }
    
    public synchronized boolean tryLock()
    {
    	Thread currentThread = Thread.currentThread();
    	if (owner == currentThread)
		{
			count++;
			if (count < 0)
				throw new Error("Maximum lock count exceeded");
		}
		else
		{
			if (owner != null)
				return false; // Already owned
			
			owner = currentThread;
        	count = 1;
		}
        
        return true;
    }
    
    public synchronized void lock( long timeout ) throws WaitTimeoutException
    {
    	Thread currentThread = Thread.currentThread();
        if (owner == currentThread)
		{
			count++;
			if (count < 0)
				throw new Error("Maximum lock count exceeded");
		}
		else
		{
			long now = System.currentTimeMillis();
	        long startTime = now;
	        
	        while (owner != null && (now - startTime < timeout))
	        {
	            try
	            {
	                wait(timeout - (now - startTime));
	            }
	            catch (InterruptedException e)
	            {
	                // Ignore
	            }
	            
	            now = System.currentTimeMillis();
	        }
	        if (owner != null)
	            throw new WaitTimeoutException();
	            
	        owner = currentThread;
        	count = 1;
		}
    }
    
    public synchronized boolean tryLock( long timeout )
    {
    	// If timeout is negative, fallback to unlimited wait behavior
    	if (timeout < 0)
    	{
    		lock();
    		return true;
    	}
    	
    	// If timeout is zero, fallback to no wait
    	if (timeout == 0)
    		return tryLock();
    	
    	// Standard use-case
    	Thread currentThread = Thread.currentThread();
        if (owner == currentThread)
		{
			count++;
			if (count < 0)
				throw new Error("Maximum lock count exceeded");
		}
		else
		{   	
	        long now = System.currentTimeMillis();
	        long startTime = now;
	        while (owner != null && (now - startTime < timeout))
	        {
	            try
	            {
	                wait(timeout - (now - startTime));
	            }
	            catch (InterruptedException e)
	            {
	                // Ignore
	            }
	            
	            now = System.currentTimeMillis();
	        }
	        if (owner != null)
	            return false;
	        
	        owner = currentThread;
        	count = 1;
    	}
        
	    return true;
    }

    public synchronized void unlock()
    {
    	Thread currentThread = Thread.currentThread();
        if (owner != currentThread)
        	throw new IllegalMonitorStateException();
        
        count--;
        if (count == 0)
        {
        	owner = null;
        	notify();
        }
    }
    
    public synchronized boolean isHeldByCurrentThread()
    {
		return owner == Thread.currentThread();
    }
}
