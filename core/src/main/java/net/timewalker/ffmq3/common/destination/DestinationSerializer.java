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
package net.timewalker.ffmq3.common.destination;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;

import net.timewalker.ffmq3.utils.RawDataBuffer;

/**
 * <p>
 *  Utility functions to serialize/de-serialize a destination reference
 *  in/from a raw data stream.
 * </p> 
 */
public final class DestinationSerializer
{
	private static final byte NO_DESTINATION = 0;
	private static final byte TYPE_QUEUE     = 1;
	private static final byte TYPE_TOPIC     = 2;
	
    /**
     * Serialize a destination to the given stream
     */
    public static void serializeTo( Destination destination , RawDataBuffer out )
    {
    	try
    	{
    		if (destination == null)
    		{
    			out.writeByte(NO_DESTINATION);
    		}
    		else
	        if (destination instanceof Queue)
	        {
	            out.writeByte(TYPE_QUEUE);
	            out.writeUTF(((Queue)destination).getQueueName());
	        }
	        else
            if (destination instanceof Topic)
            {
            	out.writeByte(TYPE_TOPIC);
                out.writeUTF(((Topic)destination).getTopicName());
            }
            else
                throw new IllegalArgumentException("Unsupported destination : "+destination);
    	}
    	catch (JMSException e)
    	{
    		throw new IllegalArgumentException("Cannot serialize destination : "+e.getMessage());
    	}
    }
    
    /**
     * Unserialize a destination from the given stream
     */
    public static DestinationRef unserializeFrom( RawDataBuffer in )
    {
        int type = in.readByte();
        if (type == NO_DESTINATION)
        	return null;
        
        String destinationName = in.readUTF();        
        switch (type)
        {
        	case TYPE_QUEUE : return new QueueRef(destinationName);
        	case TYPE_TOPIC : return new TopicRef(destinationName);
        	default:
        		throw new IllegalArgumentException("Unsupported destination type : "+type);
        }
    }
}
