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
package net.timewalker.ffmq4.common.message;

import net.timewalker.ffmq4.utils.RawDataBuffer;

/**
 * <p>
 *  Utility functions to serialize/de-serialize a JMS message
 *  in/from a raw data stream.
 * </p> 
 */
public final class MessageSerializer
{
	/**
     * Serialize a message
     */
    public static byte[] serialize( AbstractMessage message , int typicalSize )
    {
        RawDataBuffer rawMsg = message.getRawMessage();
        if (rawMsg != null)
            return rawMsg.toByteArray();

        RawDataBuffer buffer = new RawDataBuffer(typicalSize);
        buffer.writeByte(message.getType());
        message.serializeTo(buffer);
        
        return buffer.toByteArray();
    }
    
    /**
     * Unserialize a message
     */
    public static AbstractMessage unserialize( byte[] rawData , boolean asInternalCopy )
    {
    	RawDataBuffer rawIn = new RawDataBuffer(rawData);
        byte type = rawIn.readByte();
        AbstractMessage message = MessageType.createInstance(type);
        message.initializeFromRaw(rawIn);
        if (asInternalCopy)
        	message.setInternalCopy(true);
        
        return message;
    }
    
    /**
     * Serialize a message to the given output stream
     */
    public static void serializeTo( AbstractMessage message , RawDataBuffer out )
    {
        // Do we have a cached version of the raw message ?
    	RawDataBuffer rawMsg = message.getRawMessage();
        if (rawMsg != null)
        {
        	out.writeInt(rawMsg.size());
        	rawMsg.writeTo(out);
        }
        else
        {
            // Serialize the message
        	out.writeInt(0); // Write a dummy size
        	int startPos = out.size();
            out.writeByte(message.getType());
            message.serializeTo(out);
            int endPos = out.size();
            out.writeInt(endPos-startPos,startPos-4); // Update the actual size
        }
    }

    /**
     * Unserialize a message from the given input stream
     */
    public static AbstractMessage unserializeFrom( RawDataBuffer rawIn , boolean asInternalCopy )
    {
    	int size = rawIn.readInt();
    	
    	// Extract a sub buffer
    	RawDataBuffer rawMessage = new RawDataBuffer(rawIn.readBytes(size));
        byte type = rawMessage.readByte(); 
        AbstractMessage message = MessageType.createInstance(type);
        message.initializeFromRaw(rawMessage); 
        if (asInternalCopy)
            message.setInternalCopy(true);

        return message;
    }
}
