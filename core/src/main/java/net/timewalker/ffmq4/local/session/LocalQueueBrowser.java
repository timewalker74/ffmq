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

import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.QueueBrowser;

import net.timewalker.ffmq4.FFMQConstants;
import net.timewalker.ffmq4.FFMQException;
import net.timewalker.ffmq4.common.destination.TemporaryDestination;
import net.timewalker.ffmq4.common.message.MessageSelector;
import net.timewalker.ffmq4.common.session.AbstractQueueBrowser;
import net.timewalker.ffmq4.local.connection.LocalConnection;
import net.timewalker.ffmq4.local.destination.LocalQueue;
import net.timewalker.ffmq4.security.Action;
import net.timewalker.ffmq4.security.Resource;
import net.timewalker.ffmq4.utils.StringTools;
import net.timewalker.ffmq4.utils.id.IntegerID;
import net.timewalker.ffmq4.utils.id.UUIDProvider;

/**
 * <p>Implementation of a local JMS {@link QueueBrowser}</p>
 */
public final class LocalQueueBrowser extends AbstractQueueBrowser
{
	private MessageSelector parsedSelector;
	
	/**
	 * Constructor
	 */
	public LocalQueueBrowser( LocalSession session , LocalQueue queue , String messageSelector , IntegerID browserId ) throws JMSException
	{
		super(session,queue,messageSelector,browserId);
		this.parsedSelector = (StringTools.isNotEmpty(messageSelector) ? new MessageSelector(messageSelector) : null);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.jms.QueueBrowser#getEnumeration()
	 */
	@Override
	public Enumeration getEnumeration() throws JMSException 
	{
		checkNotClosed();
		
		// Security check
		checkDestinationPermission();
		
		LocalQueueBrowserEnumeration queueBrowserEnum = new LocalQueueBrowserEnumeration(this,(LocalQueue)queue,parsedSelector,UUIDProvider.getInstance().getShortUUID());
		registerEnumeration(queueBrowserEnum);
		return queueBrowserEnum;
	}
	
	private final void checkDestinationPermission() throws JMSException
	{
	    if (!(queue instanceof TemporaryDestination))
	    {
    	    boolean adminRequest = false;
            String queueName = queue.getQueueName();
            if (queueName.equals(FFMQConstants.ADM_REQUEST_QUEUE))
            {
                // Only the internal admin thread can browse this queue
                LocalConnection conn = (LocalConnection)session.getConnection();
                if (conn.getSecurityContext() != null)
                    throw new FFMQException("Access denied to administration queue "+queueName,"ACCESS_DENIED");
                adminRequest = true;
            }
            else
            if (queueName.equals(FFMQConstants.ADM_REPLY_QUEUE))
            {
                LocalConnection conn = (LocalConnection)session.getConnection();
                conn.checkPermission(Resource.SERVER, Action.REMOTE_ADMIN);
                adminRequest = true;
            }
            
            // For all other destinations
            if (!adminRequest)
                ((LocalConnection)session.getConnection()).checkPermission(queue,Action.BROWSE);
	    }
	}
}
