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
package net.timewalker.ffmq3.remote.session;

import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.Queue;

import net.timewalker.ffmq3.common.session.AbstractQueueBrowser;
import net.timewalker.ffmq3.transport.PacketTransportEndpoint;
import net.timewalker.ffmq3.transport.packet.query.CloseBrowserQuery;
import net.timewalker.ffmq3.transport.packet.query.CreateBrowserQuery;
import net.timewalker.ffmq3.utils.ErrorTools;
import net.timewalker.ffmq3.utils.id.IntegerID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * RemoteQueueBrowser
 */
public final class RemoteQueueBrowser extends AbstractQueueBrowser
{
	private static final Log log = LogFactory.getLog(RemoteQueueBrowser.class);
	
    // Parent connection
	protected final PacketTransportEndpoint transportEndpoint;
    
	/**
	 * Constructor
	 * @param queue a reference to the remote queue
	 * @param messageSelector a message selector or null
	 */
	public RemoteQueueBrowser( IntegerID browserId,RemoteSession session , Queue queue , String messageSelector  )
	{
		super(session,queue,messageSelector,browserId);
		this.transportEndpoint = session.getTransportEndpoint();
		log.debug("Remote browser ID is "+browserId);
	}
	
	/**
     * Initialize the remote endpoint for this session
     */
    protected void remoteInit() throws JMSException
    {
        CreateBrowserQuery query = new CreateBrowserQuery();
        query.setBrowserId(id);
        query.setSessionId(session.getId());
        query.setQueue(queue);
        query.setMessageSelector(messageSelector);
        transportEndpoint.blockingRequest(query);
    }
	
	/*
	 * (non-Javadoc)
	 * @see javax.jms.QueueBrowser#getEnumeration()
	 */
	@Override
	public Enumeration getEnumeration() throws JMSException
	{
		checkNotClosed();
		RemoteQueueBrowserEnumeration queueBrowserEnum = new RemoteQueueBrowserEnumeration((RemoteSession)session,this);
		queueBrowserEnum.remoteInit();
		registerEnumeration(queueBrowserEnum);
		return queueBrowserEnum;
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.common.session.AbstractQueueBrowser#onQueueBrowserClose()
	 */
	@Override
	protected void onQueueBrowserClose()
	{
		super.onQueueBrowserClose();
		
		try
		{
    		CloseBrowserQuery query = new CloseBrowserQuery();
    		query.setSessionId(session.getId());
    		query.setBrowserId(id);
    		transportEndpoint.blockingRequest(query);
		}
        catch (JMSException e)
        {
            ErrorTools.log(e, log);
        }
	}
}
