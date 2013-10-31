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

package net.timewalker.ffmq3.utils.pool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jms.JMSException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ObjectPool
 */
public abstract class ObjectPool<T> implements ObjectPoolMBean
{
	private static final Log log = LogFactory.getLog(ObjectPool.class);
	
	/**
	 * Exhaustion policies
	 */
	public static final int WHEN_EXHAUSTED_FAIL              = 0;
	public static final int WHEN_EXHAUSTED_BLOCK             = 1;
	public static final int WHEN_EXHAUSTED_WAIT              = 2;
	public static final int WHEN_EXHAUSTED_RETURN_NULL       = 3;
	public static final int WHEN_EXHAUSTED_WAIT_RETURN_NULL  = 4;
	
	// Attribute
	private int maxIdle;
	private int minSize;
	private int maxSize;
	private int exhaustionPolicy;
	private long waitTimeout;
	
	// Runtime
	private Set<T> all;
	private List<T> available;
	private boolean closed;
	private int pendingWaits;
	private Object closeLock = new Object();
	
	/**
	 * Constructor
	 */
	public ObjectPool( int minSize,
			           int maxIdle,
			           int maxSize ,
			           int exhaustionPolicy ,
			           long waitTimeout ) throws JMSException
	{
		// Check parameters
		if (minSize < 0)
			throw new ObjectPoolException("minSize cannot be negative");
		if (minSize > maxSize)
			throw new ObjectPoolException("minSize should be <= maxSize");
		if (maxIdle < minSize || maxIdle > maxSize)
			throw new ObjectPoolException("maxIdle should be between minSize and maxSize");
		switch (exhaustionPolicy)
		{
			case WHEN_EXHAUSTED_FAIL  : 
			case WHEN_EXHAUSTED_BLOCK : 
			case WHEN_EXHAUSTED_WAIT  : 
			case WHEN_EXHAUSTED_RETURN_NULL : 
			case WHEN_EXHAUSTED_WAIT_RETURN_NULL : break;
			default:
				throw new ObjectPoolException("Invalid exhaustion policy : "+exhaustionPolicy);
		}
		if (waitTimeout < 0)
			throw new ObjectPoolException("waitTimeout cannot be negative");
		
		this.minSize = minSize;
		this.maxIdle = maxIdle;
		this.maxSize = maxSize;
		this.exhaustionPolicy = exhaustionPolicy;
		this.waitTimeout = waitTimeout;
		this.all = new HashSet<>(maxSize);
		this.available = new ArrayList<>(maxSize);
	}
	
	protected void init() throws JMSException
	{
		for (int i = 0; i < minSize; i++)
		{
			T poolObject = extendPool();
			available.add(poolObject);
		}
	}
	
	/**
	 * Borrow an object from the pool
	 * @return a pooled object
	 */
	public synchronized T borrow() throws JMSException
	{
		if (closed)
			throw new ObjectPoolException("Object pool is closed");
		
		// Object immediately available ?
		int availableCount = available.size();
		if (availableCount > 0)
			return available.remove(availableCount-1);
		
		// Can we create one more ?
		if (all.size() < maxSize)
			return extendPool();
		
		// Pool is exhausted
		switch (exhaustionPolicy)
		{
			case WHEN_EXHAUSTED_FAIL  : throw new ObjectPoolException("Pool is exhausted (maxSize="+maxSize+")");
			case WHEN_EXHAUSTED_BLOCK : return waitForAvailability();
			case WHEN_EXHAUSTED_WAIT  : return waitForAvailability(waitTimeout,true);
			case WHEN_EXHAUSTED_RETURN_NULL : return null;
			case WHEN_EXHAUSTED_WAIT_RETURN_NULL : return waitForAvailability(waitTimeout,false);
			
			default:
				throw new ObjectPoolException("Invalid exhaustion policy : "+exhaustionPolicy);
		}
	}
	
	private T waitForAvailability() throws JMSException
	{
		pendingWaits++;
		try
		{
			while (!closed)
			{
				// Object immediatly available ?
				int availableCount = available.size();
				if (availableCount > 0)
					return available.remove(availableCount-1);
				
				// Can we create one more ?
				if (all.size() < maxSize)
					return extendPool();

				try
				{
					wait();
				}
				catch (InterruptedException e)
				{
					log.error("waitForAvailability() was interrupted",e);
				}
			}
			
			throw new ObjectPoolException("Object pool was closed");
		}
		finally
		{
			pendingWaits--;
		}
	}
	
	private T waitForAvailability( long waitTimeout , boolean throwExceptionOnTimeout ) throws JMSException
	{
		pendingWaits++;
		try
		{
			long now = System.currentTimeMillis();
	        long startTime = now;
			
			while (!closed && (now-startTime) < waitTimeout)
			{
				// Object immediatly available ?
				int availableCount = available.size();
				if (availableCount > 0)
					return available.remove(availableCount-1);
				// Can we create one more ?
				if (all.size() < maxSize)
					return extendPool();

				try
				{
					wait(waitTimeout-now+startTime);
				}
				catch (InterruptedException e)
				{
					log.error("waitForAvailability() was interrupted",e);
				}

				now = System.currentTimeMillis();
			}
			
			if (closed)
				throw new ObjectPoolException("Object pool was closed"); 
			else
			{
				if (throwExceptionOnTimeout)
					throw new ObjectPoolException("Timeout waiting for an available object");
				else
					return null;
			}
		}
		finally
		{
			pendingWaits--;
		}
	}
	
	/**
	 * Return an object to the pool
	 * @param poolObject a pooled object to be returned
	 */
	public synchronized void release( T poolObject )
	{
		if (closed)
			return;
		
		// Someone's waiting ?
		if (pendingWaits > 0)
		{
			available.add(poolObject);
			notifyAll();
		}
		else
		{
			// Pool is too large, destroy object
			if (available.size() >= maxIdle)
			{
				all.remove(poolObject);
				internalDestroyPoolObject(poolObject);
			}
			else
				available.add(poolObject); // Recycle object
		}
	}

	private T extendPool() throws JMSException
	{
		T newPoolObject;
		try
		{
			newPoolObject = createPoolObject();
		}
		catch (JMSException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new ObjectPoolException("Cannot create new pooled object",e);
		}
		all.add(newPoolObject);
		return newPoolObject;
	}

	private void internalDestroyPoolObject( T poolObject )
	{
		try
		{
			destroyPoolObject(poolObject);
		}
		catch (Exception e)
		{
			log.error("Cannot destroy new pooled object",e);
		}
	}
	
	/**
	 * Close the pool, destroying all objects
	 */
	public void close()
	{
		synchronized (closeLock)
		{
			if (closed)
				return;		
			closed = true;
		}
		
		synchronized (this)
		{
			Iterator<T> allObjects = all.iterator();
			while (allObjects.hasNext())
			{
				T poolObject = allObjects.next();
				internalDestroyPoolObject(poolObject);
			}
			
			all.clear();
			available.clear();
			
			// Unlock all waiting threads
			notifyAll();
		}
	}
	
	/**
	 * Create a new pool object
	 * @return an new pool object
	 * @throws Exception on creation error
	 */
	protected abstract T createPoolObject() throws Exception;
	
	/**
	 * Destroy a pool object
	 * @param poolObject the object to destroy
	 * @throws Exception on release error
	 */
	protected abstract void destroyPoolObject( T poolObject ) throws Exception;
	
	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.utils.pool.ObjectPoolMBean#getThreadPoolMaxIdle()
	 */
    @Override
	public int getThreadPoolMaxIdle()
    {
        return maxIdle;
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.pool.ObjectPoolMBean#getThreadPoolMinSize()
     */
    @Override
	public int getThreadPoolMinSize()
    {
        return minSize;
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.pool.ObjectPoolMBean#getThreadPoolMaxSize()
     */
    @Override
	public int getThreadPoolMaxSize()
    {
        return maxSize;
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.pool.ObjectPoolMBean#getThreadPoolExhaustionPolicy()
     */
    @Override
	public int getThreadPoolExhaustionPolicy()
    {
        return exhaustionPolicy;
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.pool.ObjectPoolMBean#getThreadPoolWaitTimeout()
     */
    @Override
	public long getThreadPoolWaitTimeout()
    {
        return waitTimeout;
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.pool.ObjectPoolMBean#getThreadPoolAvailableCount()
     */
    @Override
	public int getThreadPoolAvailableCount()
    {
        return available.size();
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.pool.ObjectPoolMBean#getThreadPoolPendingWaits()
     */
    @Override
	public int getThreadPoolPendingWaits()
    {
        return pendingWaits;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.pool.ObjectPoolMBean#getThreadPoolSize()
     */
    @Override
	public int getThreadPoolSize()
    {
        return all.size();
    }
    
    public static String exhaustionPolicyAsString( int exhaustionPolicy )
    {
    	switch (exhaustionPolicy)
		{
			case WHEN_EXHAUSTED_FAIL  : return "Fail";
			case WHEN_EXHAUSTED_BLOCK : return "Block";
			case WHEN_EXHAUSTED_WAIT  : return "Wait";
			case WHEN_EXHAUSTED_RETURN_NULL : return "Return null";
			case WHEN_EXHAUSTED_WAIT_RETURN_NULL : return "Wait then return null";
			default:
				throw new IllegalArgumentException("Invalid exhaustion policy : "+exhaustionPolicy);
		}
    }
}
