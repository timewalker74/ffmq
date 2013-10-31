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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import net.timewalker.ffmq3.common.session.AbstractMessageProducer;
import net.timewalker.ffmq3.utils.id.IntegerID;

/**
 * RemoteMessageProducer
 */
public class RemoteMessageProducer extends AbstractMessageProducer
{
    /**
     * Constructor
     */
    public RemoteMessageProducer(RemoteSession session,
                                 Destination destination,
                                 IntegerID producerId)
    {
        super(session,destination,producerId);
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.session.AbstractMessageProducer#sendToDestination(javax.jms.Destination, boolean, javax.jms.Message, int, int, long)
     */
    @Override
	protected final void sendToDestination(Destination destination, boolean destinationOverride, Message srcMessage, int deliveryMode, int priority, long timeToLive) throws JMSException
    {
    	externalAccessLock.readLock().lock();
    	try
		{
	    	checkNotClosed();
	    	
	    	((RemoteSession)session).dispatch(srcMessage);
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }
}
