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

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TemporaryTopic;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import net.timewalker.ffmq3.local.FFMQEngine;
import net.timewalker.ffmq3.local.connection.LocalQueueConnection;
import net.timewalker.ffmq3.utils.id.IntegerID;

/**
 * <p>Queue specific implementation of a local {@link Session}</p>
 * @see QueueSession
 */
public final class LocalQueueSession extends LocalSession implements QueueSession
{
    /**
     * Constructor
     */
    public LocalQueueSession(IntegerID id , LocalQueueConnection connection, FFMQEngine engine, boolean transacted, int acknowlegdeMode)
    {
        super(id,connection, engine, transacted, acknowlegdeMode);
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
	        LocalQueueReceiver receiver = new LocalQueueReceiver(engine,this,queue,messageSelector,idProvider.createID());
	        registerConsumer(receiver);
	        receiver.initDestination();
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
	        LocalQueueSender sender = new LocalQueueSender(this,queue,idProvider.createID());
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
     * @see net.timewalker.ffmq3.local.session.LocalSession#createDurableSubscriber(javax.jms.Topic, java.lang.String, java.lang.String, boolean)
     */
    @Override
	public TopicSubscriber createDurableSubscriber(Topic topic, String subscriptionName, String messageSelector, boolean noLocal) throws JMSException
    {
    	throw new IllegalStateException("Method not available on this domain.");
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.local.session.LocalSession#createTemporaryTopic()
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
     * @see net.timewalker.ffmq3.local.session.LocalSession#unsubscribe(java.lang.String)
     */
    @Override
	public void unsubscribe(String subscriptionName) throws JMSException
    {
    	throw new IllegalStateException("Method not available on this domain.");
    }
}
