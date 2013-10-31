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
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.ServerSessionPool;

import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.remote.session.RemoteQueueSession;

/**
 * RemoteQueueConnection
 */
public final class RemoteQueueConnection extends RemoteConnection implements QueueConnection
{
    /**
     * Constructor
     */
    public RemoteQueueConnection(URI transportURI, String userName, String password, String clientID) throws JMSException
    {
        super(transportURI, userName, password, clientID);
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.QueueConnection#createConnectionConsumer(javax.jms.Queue, java.lang.String, javax.jms.ServerSessionPool, int)
     */
    public synchronized ConnectionConsumer createConnectionConsumer(Queue queue, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException
    {
        checkNotClosed();
        throw new FFMQException("Unsupported feature","UNSUPPORTED_FEATURE");
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.QueueConnection#createQueueSession(boolean, int)
     */
    public synchronized QueueSession createQueueSession(boolean transacted, int acknowledgeMode) throws JMSException
    {
        checkNotClosed();
        
        RemoteQueueSession session = new RemoteQueueSession(idProvider.createID(),
        		                                            this,
        		                                            transportHub.createEndpoint(),
        		                                            transacted,
        		                                            acknowledgeMode);
        registerSession(session);
        session.remoteInit();
        return session;
    }
}
