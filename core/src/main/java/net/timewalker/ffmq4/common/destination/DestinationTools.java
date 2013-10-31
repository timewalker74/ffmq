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
package net.timewalker.ffmq4.common.destination;

import javax.jms.Destination;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;

import net.timewalker.ffmq4.FFMQConstants;
import net.timewalker.ffmq4.FFMQException;


/**
 * <p>Utility functions to create or check destinations names and references</p>
 */
public final class DestinationTools
{
    /**
     * Make sure the given destination is a light-weight serializable destination reference
     */
    public static DestinationRef asRef( Destination destination ) throws JMSException
    {
        if (destination == null)
            return null;
            
        if (destination instanceof DestinationRef)
            return (DestinationRef)destination;
        
        if (destination instanceof Queue)
            return new QueueRef(((Queue)destination).getQueueName());
        
        if (destination instanceof Topic)
            return new TopicRef(((Topic)destination).getTopicName());
        
        throw new InvalidDestinationException("Unsupported destination type : "+destination,"INVALID_DESTINATION");
    }
    
    /**
     * Make sure the given destination is a light-weight serializable destination reference
     */
    public static Queue asRef( Queue queue ) throws JMSException
    {
        if (queue == null)
            return null;
            
        if (queue instanceof QueueRef)
            return queue;
        
        return new QueueRef(queue.getQueueName());
    }
    
    /**
     * Make sure the given destination is a light-weight serializable destination reference
     */
    public static Topic asRef( Topic topic ) throws JMSException
    {
        if (topic == null)
            return null;
            
        if (topic instanceof TopicRef)
            return topic;
        
        return new TopicRef(topic.getTopicName());
    }
    
    /**
     * Get a queue name for a given topic consumer
     */
    public static String getQueueNameForTopicConsumer( String topicName , String consumerID )
    {
        return topicName+"-"+consumerID;
    }
    
    private static void checkDestinationName( String destinationName ) throws JMSException
    {
        for (int i = 0 ; i < destinationName.length() ; i++)
        {
            char c = destinationName.charAt(i);
            if (c >= 'a' && c <= 'z')
                continue;
            if (c >= 'A' && c <= 'Z')
                continue;
            if (c >= '0' && c <= '9')
                continue;
            if (c == '_' || c == '-')
                continue;
            
            throw new FFMQException("Destination name '"+destinationName+"' contains an invalid character : "+c,"INVALID_DESTINATION_NAME");
        }
    }
    
    /**
     * Check the validity of a queue name
     */
    public static void checkQueueName( String queueName ) throws JMSException
    {
        if (queueName == null)
            throw new FFMQException("Queue name is not set","INVALID_DESTINATION_NAME");            
        if (queueName.length() > FFMQConstants.MAX_QUEUE_NAME_SIZE)
            throw new FFMQException("Queue name '"+queueName+"' is too long ("+queueName.length()+" > "+FFMQConstants.MAX_QUEUE_NAME_SIZE+")","INVALID_DESTINATION_NAME");
        checkDestinationName(queueName);
    }
    
    /**
     * Check the validity of a topic name
     */
    public static void checkTopicName( String topicName ) throws JMSException
    {
        if (topicName == null)
            throw new FFMQException("Topic name is not set","INVALID_DESTINATION_NAME");      
        if (topicName.length() > FFMQConstants.MAX_TOPIC_NAME_SIZE)
            throw new FFMQException("Topic name '"+topicName+"' is too long ("+topicName.length()+" > "+FFMQConstants.MAX_TOPIC_NAME_SIZE+")","INVALID_DESTINATION_NAME");
        checkDestinationName(topicName);
    }
}   
