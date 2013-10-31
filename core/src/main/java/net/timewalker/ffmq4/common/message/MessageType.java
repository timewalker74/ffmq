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


/**
 * <p>
 *  FFMQ constants for a message type
 * </p>
 */
public final class MessageType
{
    public static final byte EMPTY  = 1;
    public static final byte BYTES  = 2;
    public static final byte MAP    = 3;
    public static final byte OBJECT = 4;
    public static final byte STREAM = 5;
    public static final byte TEXT   = 6;
    
    /**
     * Create a message instance of the given type
     */
    public static AbstractMessage createInstance( byte type )
    {
        switch (type)
        {
            case EMPTY:  return new EmptyMessageImpl();
            case BYTES:  return new BytesMessageImpl();
            case MAP:    return new MapMessageImpl();
            case OBJECT: return new ObjectMessageImpl();
            case STREAM: return new StreamMessageImpl();
            case TEXT:   return new TextMessageImpl();
                
            default:
                throw new IllegalArgumentException("Unsupported message type : "+type);
        }
    }
}
