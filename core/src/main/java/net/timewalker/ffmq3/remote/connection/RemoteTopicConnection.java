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
package net.timewalker.ffmq3.remote.connection;

import java.net.URI;

import javax.jms.ConnectionConsumer;
import javax.jms.JMSException;
import javax.jms.ServerSessionPool;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;

import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.remote.session.RemoteTopicSession;

/**
 * RemoteTopicConnection
 */
public final class RemoteTopicConnection extends RemoteConnection implements TopicConnection
{
    /**
     * Constructor
     */
    public RemoteTopicConnection(URI transportURI, String userName, String password, String clientID) throws JMSException
    {
        super(transportURI, userName, password, clientID);
    }

    /* (non-Javadoc)
     * @see javax.jms.TopicConnection#createConnectionConsumer(javax.jms.Topic, java.lang.String, javax.jms.ServerSessionPool, int)
     */
    public synchronized ConnectionConsumer createConnectionConsumer(Topic topic, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException
    {
        checkNotClosed();
        throw new FFMQException("Unsupported feature","UNSUPPORTED_FEATURE");
    }

    /* (non-Javadoc)
     * @see javax.jms.TopicConnection#createTopicSession(boolean, int)
     */
    public synchronized TopicSession createTopicSession(boolean transacted, int acknowledgeMode) throws JMSException
    {
        checkNotClosed();
        
        RemoteTopicSession session = new RemoteTopicSession(idProvider.createID(),
        		                                            this,
        		                                            transportHub.createEndpoint(),
        		                                            transacted,
        		                                            acknowledgeMode);
        registerSession(session);
        session.remoteInit();
        return session;
    }
}
