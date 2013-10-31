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

package net.timewalker.ffmq4.utils.async;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.jms.JMSException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * AsyncTaskManager
 */
public final class AsyncTaskManager implements AsyncTaskProcessorThreadListener, AsyncTaskManagerMBean
{
	private static final Log log = LogFactory.getLog(AsyncTaskManager.class);
	
	// Runtime
	private String name;
	private AsyncTaskProcessorThreadPool threadPool;
	private Set<AsyncTask> taskSet = new HashSet<>();
	private LinkedList<AsyncTask> taskQueue = new LinkedList<>();

	/**
	 * Constructor
	 */
	public AsyncTaskManager( String name , 
							 int threadPoolMinSize ,
							 int threadPoolMaxIdle ,
							 int threadPoolMaxSize ) throws JMSException
	{
		log.debug("Initializing "+name);
		this.name = name;
		this.threadPool = new AsyncTaskProcessorThreadPool(name,
													       threadPoolMinSize,
				                                           threadPoolMaxIdle,
				                                           threadPoolMaxSize,
				                                           this);
	}
	
	/**
	 * Cancel a task for the manager queue
	 * @param task
	 */
	public synchronized void cancelTask( AsyncTask task )
	{
		taskQueue.remove(task);
	}
	
	/**
	 * Asynchronously execute the given task
	 */
	public synchronized void execute( AsyncTask task ) throws JMSException
	{		
		AsyncTaskProcessorThread thread = threadPool.borrow(); // Dispatch using new borrowed thread
		if (thread != null)
		{
			thread.setTask(task);
			thread.execute();
		}
		else
		{
			// All threads are busy ...
			
			if (task.isMergeable())
			{
				if (!taskSet.add(task))
					return; // Already queued
			}
			
			// Enqueue task
			taskQueue.add(task);
		}
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq4.utils.async.AsyncTaskProcessorThreadListener#executionComplete(net.timewalker.ffmq4.utils.async.AsyncTaskProcessorThread)
	 */
	@Override
	public synchronized void executionComplete( AsyncTaskProcessorThread thread )
	{
		// If tasks are waiting, unqueue them
		if (!taskQueue.isEmpty())
		{
			AsyncTask nextTask = taskQueue.removeFirst();
			if (nextTask.isMergeable())
				taskSet.remove(nextTask);
			
			thread.setTask(nextTask);
			thread.execute();
		}
		else
			threadPool.release(thread); // Release the thread
	}
	
	/**
	 * Close manager resources
	 */
	public void close()
	{
		threadPool.close();
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq4.utils.async.AsyncTaskManagerMBean#getName()
	 */
	@Override
	public String getName()
	{
	    return name;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq4.utils.async.AsyncTaskManagerMBean#getTaskQueueSize()
	 */
	@Override
	public int getTaskQueueSize()
	{
	    return taskQueue.size();
	}

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.utils.pool.ObjectPoolMBean#getThreadPoolMaxIdle()
     */
    @Override
	public int getThreadPoolMaxIdle()
    {
        return threadPool.getThreadPoolMaxIdle();
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.utils.pool.ObjectPoolMBean#getThreadPoolMinSize()
     */
    @Override
	public int getThreadPoolMinSize()
    {
        return threadPool.getThreadPoolMinSize();
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.utils.pool.ObjectPoolMBean#getThreadPoolMaxSize()
     */
    @Override
	public int getThreadPoolMaxSize()
    {
        return threadPool.getThreadPoolMaxSize();
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.utils.pool.ObjectPoolMBean#getThreadPoolExhaustionPolicy()
     */
    @Override
	public int getThreadPoolExhaustionPolicy()
    {
        return threadPool.getThreadPoolExhaustionPolicy();
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.utils.pool.ObjectPoolMBean#getThreadPoolWaitTimeout()
     */
    @Override
	public long getThreadPoolWaitTimeout()
    {
        return threadPool.getThreadPoolWaitTimeout();
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.utils.pool.ObjectPoolMBean#getThreadPoolAvailableCount()
     */
    @Override
	public int getThreadPoolAvailableCount()
    {
        return threadPool.getThreadPoolAvailableCount();
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.utils.pool.ObjectPoolMBean#getThreadPoolPendingWaits()
     */
    @Override
	public int getThreadPoolPendingWaits()
    {
        return threadPool.getThreadPoolPendingWaits();
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.utils.pool.ObjectPoolMBean#getThreadPoolSize()
     */
    @Override
	public int getThreadPoolSize()
    {
        return threadPool.getThreadPoolSize();
    }
}
