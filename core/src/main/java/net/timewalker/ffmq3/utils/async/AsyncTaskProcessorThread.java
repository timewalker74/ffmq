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

package net.timewalker.ffmq3.utils.async;

import java.util.concurrent.Semaphore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * AsyncTaskProcessorThread
 */
public class AsyncTaskProcessorThread extends Thread
{
	private static final Log log = LogFactory.getLog(AsyncTaskProcessorThread.class);
	
	// Attributes
	private AsyncTaskProcessorThreadListener listener;
	
	// Runtime
	private boolean stopRequired = false;
	private Semaphore waitLock = new Semaphore(0);
	private AsyncTask task;
	private boolean traceEnabled = log.isTraceEnabled();
	
	/**
	 * Constructor
	 */
	protected AsyncTaskProcessorThread( AsyncTaskProcessorThreadListener listener )
	{
		super("AsyncTaskProcessorThread");
		setDaemon(true);
		this.listener = listener;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run()
	{
		try
		{
			while (!stopRequired)
			{
				if (traceEnabled)
					log.trace("Thread entering passive wait : "+getName());
				
				// Passive wait
				waitLock.acquire();
				if (stopRequired)
					break;
				
				// Process the container
				if (traceEnabled)
					log.trace("Executing task "+task);

				// Execute task
				task.execute();
				
				// Notify listener
				listener.executionComplete(this);
			}
		}
		catch (Throwable ex)
		{
			log.error("Asynchronous execution thread failed",ex);
		}
		log.debug("Thread exits : "+getName());
	}
	
	/**
	 * Ask the thread to execute the current task
	 */
	protected void execute()
	{
		waitLock.release();
	}
	
	/**
	 * Set the next task to be executed by this thread
	 */
	protected void setTask(AsyncTask target)
	{
		this.task = target;
	}
	
	public void pleaseStop()
	{
		stopRequired = true;
		waitLock.release();
	}
}
