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
package net.timewalker.ffmq3.common.message;

import java.util.Enumeration;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageEOFException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.jms.Topic;

import net.timewalker.ffmq3.common.destination.DestinationRef;
import net.timewalker.ffmq3.common.destination.QueueRef;
import net.timewalker.ffmq3.common.destination.TopicRef;

/**
 * <p>Utility functions to copy and normalize JMS messages.</p> 
 */
public final class MessageTools
{
    /**
     * Create an internal copy of the message if necessary
     */
    public static AbstractMessage makeInternalCopy( Message srcMessage ) throws JMSException
    {
        // Internal type copy
        if (srcMessage instanceof AbstractMessage)
        {
        	AbstractMessage msg = (AbstractMessage)srcMessage;
        	if (msg.isInternalCopy())
        		return msg;
        	
        	AbstractMessage dup = duplicate(srcMessage);
        	dup.setInternalCopy(true);
        	return dup;
        }
        
        AbstractMessage dup = duplicate(srcMessage);
        dup.setInternalCopy(true);
    	return dup;
    }
 
    /**
     * Convert the message to native type if necessary
     */
    public static AbstractMessage normalize( Message srcMessage ) throws JMSException
    {
    	// Already a native message ?
        if (srcMessage instanceof AbstractMessage)
            return (AbstractMessage)srcMessage;
        
        return duplicate(srcMessage);
    }
       
    /**
     * Create an independant copy of the given message
     */
    public static AbstractMessage duplicate( Message srcMessage ) throws JMSException
    {
    	AbstractMessage msgCopy;
    	
        // Internal type copy
        if (srcMessage instanceof AbstractMessage)
        	msgCopy = ((AbstractMessage)srcMessage).copy();
        else
        if (srcMessage instanceof TextMessage)
        	msgCopy = duplicateTextMessage((TextMessage)srcMessage);
        else
        if (srcMessage instanceof ObjectMessage)
        	msgCopy = duplicateObjectMessage((ObjectMessage)srcMessage);
        else
        if (srcMessage instanceof BytesMessage)
        	msgCopy = duplicateBytesMessage((BytesMessage)srcMessage);
        else
        if (srcMessage instanceof MapMessage)
        	msgCopy = duplicateMapMessage((MapMessage)srcMessage);
        else
        if (srcMessage instanceof StreamMessage)
        	msgCopy = duplicateStreamMessage((StreamMessage)srcMessage);
        else
        	msgCopy = duplicateMessage(srcMessage);

        return msgCopy;
    }
    
    private static AbstractMessage duplicateBytesMessage( BytesMessage srcMessage ) throws JMSException
    {
        BytesMessageImpl copy = new BytesMessageImpl();
        copyHeaders(srcMessage,copy);
        
        srcMessage.reset();
        int readAmount;
        byte[] buffer = new byte[1024];
        while ((readAmount = srcMessage.readBytes(buffer)) > 0)
            copy.writeBytes(buffer,0,readAmount);
        
        return copy;
    }
    
    private static AbstractMessage duplicateObjectMessage( ObjectMessage srcMessage ) throws JMSException
    {
        ObjectMessageImpl copy = new ObjectMessageImpl();
        copyHeaders(srcMessage,copy);
        copy.setObject(srcMessage.getObject());

        return copy;
    }
    
    private static AbstractMessage duplicateMapMessage( MapMessage srcMessage ) throws JMSException
    {
        MapMessageImpl copy = new MapMessageImpl();
        copyHeaders(srcMessage,copy);
        
        Enumeration<?> allNames = srcMessage.getMapNames();
        while (allNames.hasMoreElements())
        {
            String name = (String)allNames.nextElement();
            Object value = srcMessage.getObject(name);
            copy.setObject(name, value);
        }
        
        return copy;
    }
    
    private static AbstractMessage duplicateStreamMessage( StreamMessage srcMessage ) throws JMSException
    {
        StreamMessageImpl copy = new StreamMessageImpl();
        copyHeaders(srcMessage,copy);
        
        srcMessage.reset();
        try
        {
            while (true)
                copy.writeObject(srcMessage.readObject());
        }
        catch (MessageEOFException e)
        {
            // Complete
        }
        
        return copy;
    }
    
    private static AbstractMessage duplicateTextMessage( TextMessage srcMessage ) throws JMSException
    {
        TextMessageImpl copy = new TextMessageImpl();
        copyHeaders(srcMessage,copy);
        
        copy.setText(srcMessage.getText());
        
        return copy;
    }
    
    private static AbstractMessage duplicateMessage( Message srcMessage ) throws JMSException
    {
        EmptyMessageImpl copy = new EmptyMessageImpl();
        copyHeaders(srcMessage,copy);
        
        return copy;
    }
    
    private static void copyHeaders( Message srcMessage , Message dstMessage ) throws JMSException
    {
        dstMessage.setJMSCorrelationID(srcMessage.getJMSCorrelationID());
        dstMessage.setJMSDeliveryMode(srcMessage.getJMSDeliveryMode());
        
        Destination destination = srcMessage.getJMSDestination();
        if (destination != null)
        {
        	// Do our best to convert the ref. to one of our native types
        	if (destination instanceof DestinationRef)
        		dstMessage.setJMSDestination(destination);
        	else
        	if (destination instanceof Queue)
        		dstMessage.setJMSDestination(new QueueRef(((Queue)destination).getQueueName()));
        	else
        	if (destination instanceof Topic)
        		dstMessage.setJMSDestination(new TopicRef(((Topic)destination).getTopicName()));
        }
        
        dstMessage.setJMSExpiration(srcMessage.getJMSExpiration());
        dstMessage.setJMSMessageID(srcMessage.getJMSMessageID());
        dstMessage.setJMSPriority(srcMessage.getJMSPriority());
        dstMessage.setJMSRedelivered(srcMessage.getJMSRedelivered());
        
        Destination replyTo = srcMessage.getJMSReplyTo();
        if (replyTo != null)
        {
        	// Do our best to convert the ref. to one of our native types
        	if (replyTo instanceof DestinationRef)
        		dstMessage.setJMSReplyTo(replyTo);
        	else
        	if (replyTo instanceof Queue)
        		dstMessage.setJMSReplyTo(new QueueRef(((Queue)replyTo).getQueueName()));
        	else
        	if (replyTo instanceof Topic)
        		dstMessage.setJMSReplyTo(new TopicRef(((Topic)replyTo).getTopicName()));
        }
        
        dstMessage.setJMSTimestamp(srcMessage.getJMSTimestamp());
        dstMessage.setJMSType(srcMessage.getJMSType());
        
        Enumeration<?> allProps = dstMessage.getPropertyNames();
        while (allProps.hasMoreElements())
        {
            String propName = (String)allProps.nextElement();
            Object propValue = srcMessage.getObjectProperty(propName);
            
            if (propValue != null)
                dstMessage.setObjectProperty(propName, propValue);
        }
    }
}
