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

import javax.jms.JMSException;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import net.timewalker.ffmq4.utils.id.IntegerID;

/**
 * RemoteTopicSubscriber
 */
public class RemoteTopicSubscriber extends RemoteMessageConsumer implements TopicSubscriber
{
    /**
     * Constructor
     */
    public RemoteTopicSubscriber( IntegerID consumerId , RemoteSession session , Topic topic, String messageSelector , boolean noLocal ) throws JMSException
    {
        super(consumerId,session,topic,messageSelector,noLocal);
    }

    /* (non-Javadoc)
     * @see javax.jms.TopicSubscriber#getNoLocal()
     */
    @Override
	public final boolean getNoLocal()
    {
        return noLocal;
    }

    /* (non-Javadoc)
     * @see javax.jms.TopicSubscriber#getTopic()
     */
    @Override
	public final Topic getTopic()
    {
        return (Topic)destination;
    }
}
