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
import javax.jms.QueueBrowser;
import javax.jms.TemporaryQueue;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import net.timewalker.ffmq3.common.destination.DestinationTools;
import net.timewalker.ffmq3.remote.connection.RemoteConnection;
import net.timewalker.ffmq3.transport.PacketTransportEndpoint;
import net.timewalker.ffmq3.utils.id.IntegerID;

/**
 * RemoteTopicSession
 */
public final class RemoteTopicSession extends RemoteSession implements TopicSession
{
    /**
     * Constructor
     */
    public RemoteTopicSession(IntegerID sessionId,RemoteConnection connection, PacketTransportEndpoint transportEndpoint, boolean transacted, int acknowledgeMode)
    {
        super(sessionId,connection, transportEndpoint, transacted, acknowledgeMode);
    }

    /* (non-Javadoc)
     * @see javax.jms.TopicSession#createPublisher(javax.jms.Topic)
     */
    @Override
	public TopicPublisher createPublisher(Topic topic) throws JMSException
    {
    	externalAccessLock.readLock().lock();
    	try
		{
	        checkNotClosed();
	        
	        RemoteTopicPublisher publisher =  new RemoteTopicPublisher(this,
	                                                                   DestinationTools.asRef(topic),
	                                                                   idProvider.createID());
	        registerProducer(publisher);
	        return publisher;
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }

    /* (non-Javadoc)
     * @see javax.jms.TopicSession#createSubscriber(javax.jms.Topic, java.lang.String, boolean)
     */
    @Override
	public TopicSubscriber createSubscriber(Topic topic, String messageSelector, boolean noLocal) throws JMSException
    {
    	externalAccessLock.readLock().lock();
    	try
		{
	        checkNotClosed();
	        
	        RemoteTopicSubscriber subscriber =  new RemoteTopicSubscriber(idProvider.createID(),
	        		                                                      this,
	                                                                      DestinationTools.asRef(topic),
	                                                                      messageSelector,
	                                                                      noLocal);
	        registerConsumer(subscriber);
	        subscriber.remoteInit();
	        return subscriber;
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }

    /* (non-Javadoc)
     * @see javax.jms.TopicSession#createSubscriber(javax.jms.Topic)
     */
    @Override
	public TopicSubscriber createSubscriber(Topic topic) throws JMSException
    {
        return createSubscriber(topic,null,false);
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.session.AbstractSession#createBrowser(javax.jms.Queue)
     */
    @Override
	public QueueBrowser createBrowser(Queue queue) throws JMSException
    {
    	throw new IllegalStateException("Method not available on this domain.");
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.remote.session.RemoteSession#createBrowser(javax.jms.Queue, java.lang.String)
     */
    @Override
	public QueueBrowser createBrowser(Queue queue, String messageSelector) throws JMSException
    {
    	throw new IllegalStateException("Method not available on this domain.");
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.session.AbstractSession#createQueue(java.lang.String)
     */
    @Override
	public Queue createQueue(String queueName) throws JMSException
    {
    	throw new IllegalStateException("Method not available on this domain.");
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.remote.session.RemoteSession#createTemporaryQueue()
     */
    @Override
	public TemporaryQueue createTemporaryQueue() throws JMSException
    {
    	throw new IllegalStateException("Method not available on this domain.");
    }
}
