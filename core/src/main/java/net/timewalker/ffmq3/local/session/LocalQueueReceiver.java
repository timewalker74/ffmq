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

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueReceiver;

import net.timewalker.ffmq3.local.FFMQEngine;
import net.timewalker.ffmq3.utils.id.IntegerID;

/**
 * <p>Queue specific implementation of a local {@link MessageConsumer}</p>
 */
public final class LocalQueueReceiver extends LocalMessageConsumer implements QueueReceiver
{
    /**
     * Constructor
     */
    public LocalQueueReceiver( FFMQEngine engine, 
                               LocalQueueSession session,
                               Queue queue,
                               String messageSelector,
                               IntegerID receiverId ) throws JMSException
    {
        super(engine,session,queue,messageSelector,false,receiverId,null);
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.QueueReceiver#getQueue()
     */
    public Queue getQueue()
    {
        return (Queue)destination;
    }
}
