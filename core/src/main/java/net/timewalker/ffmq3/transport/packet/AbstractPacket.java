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
package net.timewalker.ffmq3.transport.packet;

import net.timewalker.ffmq3.utils.RawDataBuffer;

/**
 * AbstractPacket
 */
public abstract class AbstractPacket
{
    private int endpointId = -1;
    
    /**
     * Constructor
     */
    public AbstractPacket()
    {
        // Nothing
    }
    
    public final void setEndpointId(int endpointId)
    {
        this.endpointId = endpointId;
    }
    
    public final int getEndpointId()
    {
        return endpointId;
    }
    
    /**
     * Get the type value for this packet
     */
    public abstract byte getType();

    /**
     * Write the packet content to the given output stream
     */
    protected void serializeTo(RawDataBuffer out)
    {
        out.writeInt(endpointId);
    }

    /**
     * Read the packet content to the given input stream
     */
    protected void unserializeFrom(RawDataBuffer in)
    {
        endpointId = in.readInt();
    }

    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        String className = getClass().getName(); 
        int dotIdx = className.lastIndexOf('.');
        return "{"+(dotIdx != -1 ? className.substring(dotIdx+1) : className)+"}";
    }
}
