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
package net.timewalker.ffmq4.common.session;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import net.timewalker.ffmq4.FFMQException;
import net.timewalker.ffmq4.common.message.AbstractMessage;
import net.timewalker.ffmq4.storage.message.MessageSerializationLevel;
import net.timewalker.ffmq4.utils.ErrorTools;
import net.timewalker.ffmq4.utils.id.IntegerID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Base implementation for a {@link MessageConsumer}</p>
 */
public abstract class AbstractMessageConsumer extends AbstractMessageHandler implements MessageConsumer
{
	private static final Log log = LogFactory.getLog(AbstractMessageConsumer.class);
	
	// Attributes
    protected String messageSelector;
    protected boolean noLocal;
    protected MessageListener messageListener;
    protected boolean autoAcknowledge;

    /**
     * Constructor
     */
    public AbstractMessageConsumer( AbstractSession session,
                                    Destination destination,
                                    String messageSelector,
                                    boolean noLocal,
                                    IntegerID consumerId ) throws JMSException
    {
        super(session,destination,consumerId);
        this.messageSelector = messageSelector;
        this.noLocal = noLocal;
        this.autoAcknowledge = 
        	(session.getAcknowledgeMode() == Session.AUTO_ACKNOWLEDGE ||
        	 session.getAcknowledgeMode() == Session.DUPS_OK_ACKNOWLEDGE);
        
        if (destination == null)
            throw new FFMQException("Message consumer destination cannot be null","INVALID_DESTINATION");
    }
    
    protected abstract boolean shouldLogListenersFailures();
    
    /* (non-Javadoc)
     * @see javax.jms.MessageProducer#close()
     */
    @Override
	public final void close() throws JMSException
    {
    	externalAccessLock.writeLock().lock();
    	try
    	{
    		if (closed)
    			return;
    		closed = true;
    		onConsumerClose();
    	}
    	finally
    	{
    		externalAccessLock.writeLock().unlock();
    	}
    	onConsumerClosed();
    }
    
    protected void onConsumerClose()
    {
    	session.unregisterConsumer(this);
    }
    
    protected void onConsumerClosed()
    {
    	// Nothing
    }
 
    /* (non-Javadoc)
     * @see javax.jms.MessageConsumer#getMessageSelector()
     */
    @Override
	public final String getMessageSelector()
    {
        return messageSelector;
    }
    
    /* (non-Javadoc)
     * @see javax.jms.MessageConsumer#getMessageListener()
     */
    @Override
	public final MessageListener getMessageListener()
    {
        return messageListener;
    }
    
    /* (non-Javadoc)
     * @see javax.jms.MessageConsumer#setMessageListener(javax.jms.MessageListener)
     */
    @Override
	public void setMessageListener(MessageListener messageListener) throws JMSException
    {
    	externalAccessLock.readLock().lock();
    	try
		{
    		checkNotClosed();
    		this.messageListener = messageListener;
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }
    
    /* (non-Javadoc)
     * @see javax.jms.MessageConsumer#receive()
     */
    @Override
	public final Message receive() throws JMSException
    {
        return receive(-1);
    }
    
    /* (non-Javadoc)
     * @see javax.jms.MessageConsumer#receiveNoWait()
     */
    @Override
	public final Message receiveNoWait() throws JMSException
    {
        return receive(0);
    }
    
    /*
     * (non-Javadoc)
     * @see javax.jms.MessageConsumer#receive(long)
     */
    @Override
	public final Message receive(long timeout) throws JMSException
    {
        if (messageListener != null)
            throw new FFMQException("Cannot receive messages while a listener is active","INVALID_OPERATION"); 
        
        AbstractMessage message = receiveFromDestination(timeout,true);
        if (message != null)
        {
        	message.ensureDeserializationLevel(MessageSerializationLevel.FULL);
        	
            message.setSession(session);
            
            // Auto acknowledge message
            if (autoAcknowledge)
                session.acknowledge();
        }
        return message;
    }
    
    /**
     * Receive a message from a destination
     */
    protected abstract AbstractMessage receiveFromDestination( long timeout , boolean duplicateRequired ) throws JMSException;

    /**
     * Wake up the consumer message listener
     */
    public final void wakeUpMessageListener()
    {
    	try
    	{
	    	while (!closed)
	    	{
		    	synchronized (session.deliveryLock) // [JMS spec]
				{
		    		AbstractMessage message = receiveFromDestination(0,true);
		    		if (message == null)
		    			break;
		    		
		    		// Make sure the message is properly deserialized
		    		message.ensureDeserializationLevel(MessageSerializationLevel.FULL);
		    		
		    		// Make sure the message's session is set
		            message.setSession(session);
		            
		            // Call the message listener
		            boolean listenerFailed = false;
	                try
	                {
	                    messageListener.onMessage(message);
	                }
	                catch (Throwable e)
	                {
	                	listenerFailed = true;
	                	if (shouldLogListenersFailures())
	                	    log.error("Message listener failed",e);
	                }
	                
		            // Auto acknowledge message
		            if (autoAcknowledge)
		            {
		            	if (listenerFailed)
		            		session.recover();
		            	else
		            		session.acknowledge();
		            }
	    		}
	    	}
    	}
    	catch (JMSException e)
    	{
    		ErrorTools.log(e, log);
    	}
    }
    
    /**
     * Wake up the consumer (SYNCHRONOUS)
     */
    protected abstract void wakeUp() throws JMSException;
}
