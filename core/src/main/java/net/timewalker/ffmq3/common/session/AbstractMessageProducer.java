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
package net.timewalker.ffmq3.common.session;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;

import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.common.message.MessageTools;
import net.timewalker.ffmq3.utils.id.IntegerID;
import net.timewalker.ffmq3.utils.id.UUIDProvider;

/**
 * <p>Base implementation for a {@link MessageProducer}</p>
 */
public abstract class AbstractMessageProducer extends AbstractMessageHandler implements MessageProducer
{
    // Settings
    protected int defaultDeliveryMode = Message.DEFAULT_DELIVERY_MODE;
    protected int defaultPriority = Message.DEFAULT_PRIORITY;
    protected long defaultTimeToLive = Message.DEFAULT_TIME_TO_LIVE;
    protected boolean disableMessageID;
    protected boolean disableMessageTimestamp;
    
    // Utils
    protected UUIDProvider uuidProvider = UUIDProvider.getInstance();
    
    /**
     * Constructor
     */
    public AbstractMessageProducer( AbstractSession session , Destination destination , IntegerID producerId )
    {
        super(session,destination,producerId);
    }
    
    /* (non-Javadoc)
     * @see javax.jms.MessageProducer#close()
     */
    public final void close() throws JMSException
    {
    	externalAccessLock.readLock().lock();
    	try
		{
	        if (closed)
	            return;
	        closed = true;
	        onProducerClose();
	    }
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }
 
    protected final void onProducerClose()
    {
    	session.unregisterProducer(this);
    }
    
    /* (non-Javadoc)
     * @see javax.jms.MessageProducer#getDeliveryMode()
     */
    public final int getDeliveryMode()
    {
        return defaultDeliveryMode;
    }

    /* (non-Javadoc)
     * @see javax.jms.MessageProducer#getDestination()
     */
    public final Destination getDestination()
    {
        return destination;
    }

    /* (non-Javadoc)
     * @see javax.jms.MessageProducer#getDisableMessageID()
     */
    public final boolean getDisableMessageID()
    {
        return disableMessageID;
    }

    /* (non-Javadoc)
     * @see javax.jms.MessageProducer#getDisableMessageTimestamp()
     */
    public final boolean getDisableMessageTimestamp()
    {
        return disableMessageTimestamp;
    }

    /* (non-Javadoc)
     * @see javax.jms.MessageProducer#getPriority()
     */
    public final int getPriority()
    {
        return defaultPriority;
    }

    /* (non-Javadoc)
     * @see javax.jms.MessageProducer#getTimeToLive()
     */
    public final long getTimeToLive()
    {
        return defaultTimeToLive;
    }

    /* (non-Javadoc)
     * @see javax.jms.MessageProducer#setDeliveryMode(int)
     */
    public final void setDeliveryMode(int deliveryMode) throws JMSException
    {
        if (deliveryMode != DeliveryMode.PERSISTENT &&
            deliveryMode != DeliveryMode.NON_PERSISTENT)
            throw new FFMQException("Invalid delivery mode : "+deliveryMode,"INVALID_DELIVERY_MODE");
        
        this.defaultDeliveryMode = deliveryMode;
    }

    /* (non-Javadoc)
     * @see javax.jms.MessageProducer#setDisableMessageID(boolean)
     */
    public final void setDisableMessageID(boolean disableMessageID)
    {
        this.disableMessageID = disableMessageID;
    }

    /* (non-Javadoc)
     * @see javax.jms.MessageProducer#setDisableMessageTimestamp(boolean)
     */
    public final void setDisableMessageTimestamp(boolean disableMessageTimestamp)
    {
        this.disableMessageTimestamp = disableMessageTimestamp;
    }

    /* (non-Javadoc)
     * @see javax.jms.MessageProducer#setPriority(int)
     */
    public final void setPriority(int priority) throws JMSException
    {
        if (priority < 0 || priority > 9)
            throw new FFMQException("Invalid priority value : "+priority,"INVALID_PRIORITY");
        
        this.defaultPriority = priority;
    }

    /* (non-Javadoc)
     * @see javax.jms.MessageProducer#setTimeToLive(long)
     */
    public final void setTimeToLive(long timeToLive)
    {
        this.defaultTimeToLive = timeToLive;
    }
    
    /*
     * (non-Javadoc)
     * @see javax.jms.MessageProducer#send(javax.jms.Message)
     */
    public final void send(Message message) throws JMSException
    {
    	if (this.destination == null)
    		throw new UnsupportedOperationException("Destination was not set at creation time");
    	
    	// Setup message fields
    	setupMessage(destination,message,defaultDeliveryMode,defaultPriority,defaultTimeToLive);
    	
    	// Handle foreign message implementations
        message = MessageTools.normalize(message);

        sendToDestination(destination,false,message,defaultDeliveryMode,defaultPriority,defaultTimeToLive);
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.MessageProducer#send(javax.jms.Destination, javax.jms.Message)
     */
    public final void send(Destination destination, Message message) throws JMSException
    {
    	if (this.destination != null)
    		throw new UnsupportedOperationException("Destination was set at creation time");
   
    	// Setup message fields
    	setupMessage(destination,message,defaultDeliveryMode,defaultPriority,defaultTimeToLive);
    	
    	// Handle foreign message implementations
        message = MessageTools.normalize(message);
        
    	sendToDestination(destination,true,message,defaultDeliveryMode,defaultPriority,defaultTimeToLive);
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.MessageProducer#send(javax.jms.Message, int, int, long)
     */
    public final void send(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException
    {
    	if (this.destination == null)
    		throw new UnsupportedOperationException("Destination was not set at creation time");

        // Setup message fields
    	setupMessage(destination,message,deliveryMode,priority,timeToLive);
    	
    	// Handle foreign message implementations
        message = MessageTools.normalize(message);
        
    	sendToDestination(destination,false,message,deliveryMode,priority,timeToLive);
    }
    
    /*
     * (non-Javadoc)
     * @see javax.jms.MessageProducer#send(javax.jms.Destination, javax.jms.Message, int, int, long)
     */
    public final void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException
    {
    	if (this.destination != null)
    		throw new UnsupportedOperationException("Destination was set at creation time");
    	
        // Setup message fields
    	setupMessage(destination,message,deliveryMode,priority,timeToLive);
    	
    	// Handle foreign message implementations
        message = MessageTools.normalize(message);    
    	
    	sendToDestination(destination,true,message,deliveryMode,priority,timeToLive);
    }
    
    protected final void setupMessage( Destination destinationRef , Message message , int deliveryMode , int priority , long timeToLive) throws JMSException
    {
        long now = System.currentTimeMillis();
        
        // Setup headers
        message.setJMSMessageID(uuidProvider.getUUID());
        message.setJMSTimestamp(disableMessageTimestamp ? 0 : now);
        message.setJMSDeliveryMode(deliveryMode);
        message.setJMSPriority(priority);
        message.setJMSExpiration(timeToLive > 0 ? timeToLive+now : 0);
        message.setJMSDestination(destinationRef);
    }
    
    protected abstract void sendToDestination(Destination destination, boolean destinationOverride, Message srcMessage, int deliveryMode, int priority, long timeToLive) throws JMSException;
}
