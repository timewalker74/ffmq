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
package net.timewalker.ffmq3.local.session;

import java.util.NoSuchElementException;

import javax.jms.JMSException;
import javax.jms.QueueBrowser;

import net.timewalker.ffmq3.common.message.AbstractMessage;
import net.timewalker.ffmq3.common.message.MessageSelector;
import net.timewalker.ffmq3.common.message.MessageTools;
import net.timewalker.ffmq3.common.session.AbstractQueueBrowserEnumeration;
import net.timewalker.ffmq3.local.destination.LocalQueue;
import net.timewalker.ffmq3.storage.message.MessageSerializationLevel;

/**
 * <p>
 *  Implementation of a local {@link QueueBrowser} enumeration.
 *  Allows to browse messages in a local queue.
 * </p>
 * @see LocalQueueBrowser
 */
public final class LocalQueueBrowserEnumeration extends AbstractQueueBrowserEnumeration
{
	private LocalQueue localQueue;
	private MessageSelector parsedSelector;
	private LocalQueueBrowserCursor cursor = new LocalQueueBrowserCursor();
	
	// Runtime
	private AbstractMessage nextMessage;
	
	/**
	 * Constructor (package-private)
	 */
	protected LocalQueueBrowserEnumeration( LocalQueueBrowser browser , LocalQueue localQueue , MessageSelector parsedSelector , String enumId )
	{
		super(browser,enumId);
		this.localQueue = localQueue;
		this.parsedSelector = parsedSelector;
	}
	
	/**
	 * Fetch the next browsable message in the associated queue
	 * @return a message or null
	 * @throws JMSException on queue browsing error
	 */
	private AbstractMessage fetchNext() throws JMSException
	{
		if (nextMessage != null)
			return nextMessage; // Already fetched
		nextMessage = localQueue.browse(cursor, parsedSelector); // Lookup next candidate
		if (nextMessage == null)
			close(); // Auto-close enumeration at end of queue
		return nextMessage;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.Enumeration#hasMoreElements()
	 */
	@Override
	public boolean hasMoreElements()
	{
		if (cursor.endOfQueueReached())
			return false;
		
		try
		{
			checkNotClosed();
			AbstractMessage msg = fetchNext();
			return msg != null;
		}
		catch (JMSException e)
		{
			throw new IllegalStateException(e.toString());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Enumeration#nextElement()
	 */
	@Override
	public AbstractMessage nextElement()
	{
		if (cursor.endOfQueueReached())
			throw new NoSuchElementException();
		
		try
		{
			checkNotClosed();
			AbstractMessage msg = fetchNext();
			if (msg != null)
			{
				nextMessage = null; // Consume fetched message
				AbstractMessage msgCopy = MessageTools.duplicate(msg);
				msgCopy.ensureDeserializationLevel(MessageSerializationLevel.FULL);
				return msgCopy;
			}
			
			throw new NoSuchElementException();
		}
		catch (NoSuchElementException e)
		{
			throw e;
		}
		catch (JMSException e)
		{
			throw new IllegalStateException(e.toString());
		}
	}
}
