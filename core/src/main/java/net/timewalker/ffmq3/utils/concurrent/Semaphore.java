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

/**
 * Semaphore
 */
public class Semaphore
{
    private volatile int count;
    
    /**
     * Constructor
     */
    public Semaphore() 
    {
        this(0);
    }
       
    /**
     * Constructor
     */
    public Semaphore( int initialCount ) 
    {
        this.count = initialCount;
    }
       
    public synchronized void acquire()
    {
        while (count <= 0)
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

        count--;
    }
    
    public synchronized boolean tryAcquire()
    {
        if (count <= 0)
        	return false;

        count--;
        return true;
    }
    
    public synchronized void acquire( long timeout ) throws WaitTimeoutException
    {
        long now = System.currentTimeMillis();
        long startTime = now;
        
        while (count <= 0 && (now - startTime < timeout))
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
        if (count <= 0)
            throw new WaitTimeoutException();
            
        count--;
    }
    
    public synchronized boolean tryAcquire( long timeout )
    {
    	// If timeout is negative, fallback to unlimited wait behavior
    	if (timeout < 0)
    	{
    		acquire();
    		return true;
    	}
    	
    	// If timeout is zero, fallback to no wait
    	if (timeout == 0)
    		return tryAcquire();
    	
        long now = System.currentTimeMillis();
        long startTime = now;
        while (count <= 0 && (now - startTime < timeout))
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
        if (count <= 0)
            return false;
            
        count--;
        return true;
    }

    public synchronized void release()
    {
        ++count;
        notify();
    }
    
    public synchronized void release( int amount )
    {
        count += amount;
        if (amount > 1)
        	notifyAll();
        else
        	notify();
    }
    
    public synchronized int available()
    {
    	return count;
    }
    
    public synchronized void waitEmpty()
    {
        while (count > 0)
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
    }
}
