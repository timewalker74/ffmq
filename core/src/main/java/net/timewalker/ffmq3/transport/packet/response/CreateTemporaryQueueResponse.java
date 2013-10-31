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
package net.timewalker.ffmq3.transport.packet.response;

import net.timewalker.ffmq3.transport.packet.AbstractResponsePacket;
import net.timewalker.ffmq3.transport.packet.PacketType;
import net.timewalker.ffmq3.utils.RawDataBuffer;

/**
 * CreateTemporaryQueueResponse
 */
public final class CreateTemporaryQueueResponse extends AbstractResponsePacket
{
    private String queueName;

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.network.packet.AbstractPacket#getType()
     */
    @Override
	public byte getType()
    {
        return PacketType.R_CREATE_TEMP_QUEUE;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.network.packet.AbstractResponsePacket#serializeTo(net.timewalker.ffmq3.utils.RawDataOutputStream)
     */
    @Override
	protected void serializeTo(RawDataBuffer out)
    {
        super.serializeTo(out);
        out.writeUTF(queueName);
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.network.packet.AbstractResponsePacket#unserializeFrom(net.timewalker.ffmq3.utils.RawDataInputStream)
     */
    @Override
	protected void unserializeFrom(RawDataBuffer in)
    {
        super.unserializeFrom(in);
        queueName = in.readUTF();
    }
    
    /**
     * @return the queueName
     */
    public String getQueueName()
    {
        return queueName;
    }

    /**
     * @param queueName the queueName to set
     */
    public void setQueueName(String queueName)
    {
        this.queueName = queueName;
    }
    
    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString()
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append(super.toString());
        sb.append(" queueName=");
        sb.append(queueName);
        
        return sb.toString();
    }
}
