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
package net.timewalker.ffmq4.remote.session;

import java.util.NoSuchElementException;

import javax.jms.JMSException;
import javax.jms.Message;

import net.timewalker.ffmq4.common.message.AbstractMessage;
import net.timewalker.ffmq4.common.session.AbstractQueueBrowserEnumeration;
import net.timewalker.ffmq4.storage.message.MessageSerializationLevel;
import net.timewalker.ffmq4.transport.PacketTransportEndpoint;
import net.timewalker.ffmq4.transport.packet.query.CloseBrowserEnumerationQuery;
import net.timewalker.ffmq4.transport.packet.query.QueueBrowserFetchElementQuery;
import net.timewalker.ffmq4.transport.packet.query.QueueBrowserGetEnumerationQuery;
import net.timewalker.ffmq4.transport.packet.response.QueueBrowserFetchElementResponse;
import net.timewalker.ffmq4.transport.packet.response.QueueBrowserGetEnumerationResponse;
import net.timewalker.ffmq4.utils.ErrorTools;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * RemoteQueueBrowserEnumeration
 */
public final class RemoteQueueBrowserEnumeration extends AbstractQueueBrowserEnumeration
{
	private static final Log log = LogFactory.getLog(RemoteQueueBrowserEnumeration.class);
	
	// Parent connection
	private PacketTransportEndpoint transportEndpoint;
	// Parent session
	private RemoteSession session;
	
	// Attributes
	private AbstractMessage nextMessage;
	private boolean endOfQueueReached;
	
	/**
	 * Constructor
	 */
	public RemoteQueueBrowserEnumeration( RemoteSession session , RemoteQueueBrowser browser )
	{
		super(browser,null);
		this.transportEndpoint = session.getTransportEndpoint();
		this.session = session;
	}
	
	/**
     * Initialize the remote endpoint for this session
     */
    protected void remoteInit() throws JMSException
    {
        QueueBrowserGetEnumerationQuery query = new QueueBrowserGetEnumerationQuery();
        query.setSessionId(session.getId());
        query.setBrowserId(browser.getId());
        QueueBrowserGetEnumerationResponse response = (QueueBrowserGetEnumerationResponse)transportEndpoint.blockingRequest(query);
        this.id = response.getEnumId();
        log.debug("Remote queue browser enumeration ID is "+id);
    }

    private AbstractMessage fetchNext() throws JMSException
    {
    	if (nextMessage != null) // Already fetched ?
			return nextMessage;

    	// Lookup next candidate
    	QueueBrowserFetchElementQuery query = new QueueBrowserFetchElementQuery();
    	query.setSessionId(session.getId());
    	query.setBrowserId(browser.getId());
    	query.setEnumId(id);
    	QueueBrowserFetchElementResponse response = (QueueBrowserFetchElementResponse)transportEndpoint.blockingRequest(query);
		nextMessage = response.getMessage();
		
		if (nextMessage == null)
		{
			endOfQueueReached = true;
			close(); // Auto-close enumeration at end of queue
		}
		
		return nextMessage;
    }
    
	/* (non-Javadoc)
	 * @see java.util.Enumeration#hasMoreElements()
	 */
	@Override
	public boolean hasMoreElements()
	{
		if (endOfQueueReached)
			return false;
		try
		{
			checkNotClosed();
			Message msg = fetchNext();
			return msg != null;
		}
		catch (JMSException e)
		{
			throw new IllegalStateException(e.toString());
		}
	}

	/* (non-Javadoc)
	 * @see java.util.Enumeration#nextElement()
	 */
	@Override
	public AbstractMessage nextElement()
	{
		if (endOfQueueReached)
			throw new NoSuchElementException();
			
		try
		{
			checkNotClosed();
			AbstractMessage msg = fetchNext();
			if (msg != null)
			{
				nextMessage = null; // Consume fetched message

				// Make sure the message is fully deserialized and marked as read-only
				msg.ensureDeserializationLevel(MessageSerializationLevel.FULL);
				msg.markAsReadOnly();
				
				return msg;
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
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq4.common.session.AbstractQueueBrowserEnumeration#onQueueBrowserEnumerationClose()
	 */
	@Override
	protected void onQueueBrowserEnumerationClose()
	{
	    try
        {
    		CloseBrowserEnumerationQuery query = new CloseBrowserEnumerationQuery();
    		query.setSessionId(session.getId());
    		query.setBrowserId(browser.getId());
    		query.setEnumId(id);
    		transportEndpoint.blockingRequest(query);
        }
	    catch (JMSException e)
        {
            ErrorTools.log(e, log);
        }
	}
}
