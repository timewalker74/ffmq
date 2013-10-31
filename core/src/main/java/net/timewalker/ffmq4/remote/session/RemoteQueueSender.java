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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueSender;

import net.timewalker.ffmq4.utils.id.IntegerID;

/**
 * RemoteQueueSender
 */
public final class RemoteQueueSender extends RemoteMessageProducer implements QueueSender
{
    /**
     * Constructor
     */
    public RemoteQueueSender( RemoteSession session , Queue queue , IntegerID producerId )
    {
        super(session,queue,producerId);
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.QueueSender#getQueue()
     */
    @Override
	public Queue getQueue()
    {
        return (Queue)destination;
    }
    
    /* (non-Javadoc)
     * @see javax.jms.QueueSender#send(javax.jms.Queue, javax.jms.Message, int, int, long)
     */
    @Override
	public void send(Queue queue, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException
    {
        send((Destination)queue, message, deliveryMode, priority, timeToLive);
    }

    /* (non-Javadoc)
     * @see javax.jms.QueueSender#send(javax.jms.Queue, javax.jms.Message)
     */
    @Override
	public void send(Queue queue, Message message) throws JMSException
    {
        send((Destination)queue,message,defaultDeliveryMode,defaultPriority,defaultTimeToLive);
    }
}
