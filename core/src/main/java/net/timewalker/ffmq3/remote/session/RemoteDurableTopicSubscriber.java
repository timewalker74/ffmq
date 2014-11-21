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

import javax.jms.JMSException;
import javax.jms.Topic;

import net.timewalker.ffmq3.transport.packet.query.CreateDurableSubscriberQuery;
import net.timewalker.ffmq3.utils.id.IntegerID;

/**
 * RemoteDurableTopicSubscriber
 */
public final class RemoteDurableTopicSubscriber extends RemoteTopicSubscriber
{
    // Attributes
    private String subscriptionName;
    
    /**
     * Constructor
     */
    public RemoteDurableTopicSubscriber(IntegerID consumerId, RemoteSession session, Topic topic, String messageSelector, boolean noLocal, String subscriptionName) throws JMSException
    {
        super(consumerId,session, topic, messageSelector, noLocal);
        this.subscriptionName = subscriptionName;
        this.transportEndpoint = session.getTransportEndpoint();
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.remote.session.RemoteMessageConsumer#remoteInit()
     */
    protected final void remoteInit() throws JMSException
    {
        CreateDurableSubscriberQuery query = new CreateDurableSubscriberQuery();
        query.setSessionId(session.getId());
        query.setConsumerId(id);
        query.setTopic((Topic)destination);
        query.setMessageSelector(messageSelector);
        query.setNoLocal(noLocal);
        query.setName(subscriptionName);
        transportEndpoint.blockingRequest(query);
    }
}
