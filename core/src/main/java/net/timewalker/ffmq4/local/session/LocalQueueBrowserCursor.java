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
package net.timewalker.ffmq4.local.session;

import javax.jms.Queue;
import javax.jms.QueueBrowser;

/**
 * <p>
 *  Internal implementation of a local {@link QueueBrowser} cursor.
 *  A queue browser cursor keeps track the current browsing position in a given {@link Queue} 
 *  for the local queue browser enumeration implementation. 
 * </p>
 * @see LocalQueueBrowserEnumeration
 */
public final class LocalQueueBrowserCursor
{
    // Attributes
	private int position = 0;
	private int skipped = 0;
	private boolean endOfQueueReached = false;
	
	public boolean endOfQueueReached()
	{
		return endOfQueueReached;
	}
	
	public void setEndOfQueueReached()
	{
		endOfQueueReached = true;
	}
	
	public int position()
	{
		return position;
	}
	
	public int skipped()
	{
		return skipped;
	}
	
	public void skip()
	{
		skipped++;
	}
	
	public void move()
	{
		position = skipped+1;
		skipped = 0;
	}
	
	public void reset()
	{
		skipped = 0;
	}
}
