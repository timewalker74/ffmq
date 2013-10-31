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
package net.timewalker.ffmq4.transport.packet;

import net.timewalker.ffmq4.utils.RawDataBuffer;

/**
 * PacketSerializer
 */
public final class PacketSerializer
{
    /**
     * Serialize a packet to the given output stream
     */
    public static void serializeTo( AbstractPacket packet , RawDataBuffer out )
    {
        out.writeByte(packet.getType());
        packet.serializeTo(out);
    }
    
    /**
     * Unserialize a packet from the given input stream
     */
    public static AbstractPacket unserializeFrom( RawDataBuffer in )
    {
        byte type = in.readByte(); 
        AbstractPacket packet = PacketType.createInstance(type);
        packet.unserializeFrom(in);
        
        return packet;
    }
}
