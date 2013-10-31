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
package net.timewalker.ffmq4.local.connection;

import javax.jms.ConnectionConsumer;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.ServerSessionPool;
import javax.jms.Topic;

import net.timewalker.ffmq4.FFMQException;
import net.timewalker.ffmq4.local.FFMQEngine;
import net.timewalker.ffmq4.local.session.LocalQueueSession;
import net.timewalker.ffmq4.security.SecurityContext;

/**
 * <p>Local implementation of a JMS {@link QueueConnection}</p>
 */
public final class LocalQueueConnection extends LocalConnection implements QueueConnection
{
    /**
     * Constructor
     */
    public LocalQueueConnection( FFMQEngine engine , SecurityContext securityContext , String clientID )
    {
        super(engine,securityContext,clientID);
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.QueueConnection#createConnectionConsumer(javax.jms.Queue, java.lang.String, javax.jms.ServerSessionPool, int)
     */
    @Override
	public synchronized ConnectionConsumer createConnectionConsumer(Queue queue, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException
    {
        checkNotClosed();
        throw new FFMQException("Unsupported feature","UNSUPPORTED_FEATURE");
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.QueueConnection#createQueueSession(boolean, int)
     */
    @Override
	public synchronized QueueSession createQueueSession(boolean transacted, int acknowledgeMode) throws JMSException
    {
        checkNotClosed();
        LocalQueueSession session =  new LocalQueueSession(idProvider.createID(),this,engine,transacted,acknowledgeMode);
        registerSession(session);
        return session;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.common.connection.AbstractConnection#createDurableConnectionConsumer(javax.jms.Topic, java.lang.String, java.lang.String, javax.jms.ServerSessionPool, int)
     */
    @Override
	public ConnectionConsumer createDurableConnectionConsumer(Topic topic, String subscriptionName, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException
    {
    	throw new IllegalStateException("Method not available on this domain.");
    }
}
