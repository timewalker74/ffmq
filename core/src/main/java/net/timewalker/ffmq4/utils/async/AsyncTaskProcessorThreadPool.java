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

import javax.jms.JMSException;

import net.timewalker.ffmq4.utils.pool.ObjectPool;

/**
 * AsyncTaskProcessorThreadPool
 */
public final class AsyncTaskProcessorThreadPool extends ObjectPool<AsyncTaskProcessorThread>
{
	private String name;
	private AsyncTaskProcessorThreadListener listener;
	
	/**
	 * Constructor
	 */
	protected AsyncTaskProcessorThreadPool( String name , 
			                                int minSize,
										    int maxIdle,
										    int maxSize ,
										    AsyncTaskProcessorThreadListener listener ) throws JMSException
	{
		super(minSize,maxIdle,maxSize,ObjectPool.WHEN_EXHAUSTED_RETURN_NULL,0);
		this.name = name;
		this.listener = listener;
		init();
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq4.utils.pool.ObjectPool#createPoolObject()
	 */
	@Override
	protected AsyncTaskProcessorThread createPoolObject() throws Exception
	{
		AsyncTaskProcessorThread thread = new AsyncTaskProcessorThread(listener);
		thread.setName(name);
		thread.start();	
		return thread;
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq4.utils.pool.ObjectPool#destroyPoolObject(java.lang.Object)
	 */
	@Override
	protected void destroyPoolObject(AsyncTaskProcessorThread thread) throws Exception
	{
		thread.pleaseStop();
	}
}
