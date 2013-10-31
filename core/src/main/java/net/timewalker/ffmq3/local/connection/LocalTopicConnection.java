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
package net.timewalker.ffmq3.local.connection;

import javax.jms.ConnectionConsumer;
import javax.jms.JMSException;
import javax.jms.ServerSessionPool;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;

import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.local.FFMQEngine;
import net.timewalker.ffmq3.local.session.LocalTopicSession;
import net.timewalker.ffmq3.security.SecurityContext;

/**
 * <p>Local implementation of a JMS {@link TopicConnection}</p>
 */
public final class LocalTopicConnection extends LocalConnection implements TopicConnection
{
    /**
     * Constructor
     */
    public LocalTopicConnection(FFMQEngine engine, SecurityContext securityContext, String clientID)
    {
        super(engine, securityContext, clientID);
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.TopicConnection#createConnectionConsumer(javax.jms.Topic, java.lang.String, javax.jms.ServerSessionPool, int)
     */
    public synchronized ConnectionConsumer createConnectionConsumer(Topic topic, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException
    {
        checkNotClosed();
        throw new FFMQException("Unsupported feature","UNSUPPORTED_FEATURE");
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.TopicConnection#createTopicSession(boolean, int)
     */
    public synchronized TopicSession createTopicSession(boolean transacted, int acknowledgeMode) throws JMSException
    {
        checkNotClosed();
        LocalTopicSession session =  new LocalTopicSession(idProvider.createID(),this,engine,transacted,acknowledgeMode);
        registerSession(session);
        return session;
    }

}
