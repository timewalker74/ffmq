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

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TemporaryTopic;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import net.timewalker.ffmq3.common.destination.DestinationTools;
import net.timewalker.ffmq3.remote.connection.RemoteConnection;
import net.timewalker.ffmq3.transport.PacketTransportEndpoint;
import net.timewalker.ffmq3.utils.id.IntegerID;

/**
 * RemoteQueueSession
 */
public final class RemoteQueueSession extends RemoteSession implements QueueSession
{
    /**
     * Constructor
     */
    public RemoteQueueSession(IntegerID sessionId,RemoteConnection connection, PacketTransportEndpoint transportEndpoint, boolean transacted, int acknowledgeMode)
    {
        super(sessionId,connection, transportEndpoint, transacted, acknowledgeMode);
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.QueueSession#createReceiver(javax.jms.Queue)
     */
    @Override
	public QueueReceiver createReceiver(Queue queue) throws JMSException
    {
        return createReceiver(queue,null);
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.QueueSession#createReceiver(javax.jms.Queue, java.lang.String)
     */
    @Override
	public QueueReceiver createReceiver(Queue queue, String messageSelector) throws JMSException
    {
    	externalAccessLock.readLock().lock();
    	try
		{
	        checkNotClosed();
	        
	        RemoteQueueReceiver receiver =  new RemoteQueueReceiver(idProvider.createID(),
	        		                                                this,
	                                                                DestinationTools.asRef(queue),
	                                                                messageSelector);
	        registerConsumer(receiver);
	        receiver.remoteInit();
	        return receiver;
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.QueueSession#createSender(javax.jms.Queue)
     */
    @Override
	public QueueSender createSender(Queue queue) throws JMSException
    {
    	externalAccessLock.readLock().lock();
    	try
		{
	        checkNotClosed();
	        
	        RemoteQueueSender sender =  new RemoteQueueSender(this,
	                                                          DestinationTools.asRef(queue),
	                                                          idProvider.createID());
	        registerProducer(sender);
	        return sender;
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.session.AbstractSession#createDurableSubscriber(javax.jms.Topic, java.lang.String)
     */
    @Override
	public TopicSubscriber createDurableSubscriber(Topic topic, String name) throws JMSException
    {
    	throw new IllegalStateException("Method not available on this domain.");
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.remote.session.RemoteSession#createDurableSubscriber(javax.jms.Topic, java.lang.String, java.lang.String, boolean)
     */
    @Override
	public TopicSubscriber createDurableSubscriber(Topic topic, String name, String messageSelector, boolean noLocal) throws JMSException
    {
    	throw new IllegalStateException("Method not available on this domain.");
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.remote.session.RemoteSession#createTemporaryTopic()
     */
    @Override
	public TemporaryTopic createTemporaryTopic() throws JMSException
    {
    	throw new IllegalStateException("Method not available on this domain.");
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.session.AbstractSession#createTopic(java.lang.String)
     */
    @Override
	public Topic createTopic(String topicName) throws JMSException
    {
    	throw new IllegalStateException("Method not available on this domain.");
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.remote.session.RemoteSession#unsubscribe(java.lang.String)
     */
    @Override
	public void unsubscribe(String subscriptionName) throws JMSException
    {
    	throw new IllegalStateException("Method not available on this domain.");
    }
}
