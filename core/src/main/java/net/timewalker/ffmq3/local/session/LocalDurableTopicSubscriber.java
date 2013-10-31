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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.TopicSubscriber;

import net.timewalker.ffmq3.local.FFMQEngine;
import net.timewalker.ffmq3.utils.id.IntegerID;

/**
 * <p>Implementation of a local durable JMS {@link TopicSubscriber}</p>
 */
public final class LocalDurableTopicSubscriber extends LocalTopicSubscriber
{
    /**
     * Constructor
     */
    public LocalDurableTopicSubscriber(FFMQEngine engine, LocalSession session, Destination destination, String messageSelector, boolean noLocal, IntegerID consumerId, String subscriberId) throws JMSException
    {
        super(engine,session,destination,messageSelector,noLocal,consumerId,subscriberId);
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.local.session.LocalMessageConsumer#isDurable()
     */
    public boolean isDurable()
    {
        return true;
    }
}
