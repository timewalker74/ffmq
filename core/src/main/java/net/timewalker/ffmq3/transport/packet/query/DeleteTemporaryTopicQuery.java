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
package net.timewalker.ffmq3.transport.packet.query;

import net.timewalker.ffmq3.transport.packet.AbstractQueryPacket;
import net.timewalker.ffmq3.transport.packet.PacketType;
import net.timewalker.ffmq3.utils.RawDataBuffer;

/**
 * DeleteTemporaryTopicQuery
 */
public final class DeleteTemporaryTopicQuery extends AbstractQueryPacket
{
    private String topicName;

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.network.packet.AbstractPacket#getType()
     */
    public byte getType()
    {
        return PacketType.Q_DELETE_TEMP_TOPIC;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.network.packet.AbstractPacket#serializeTo(net.timewalker.ffmq3.utils.RawDataOutputStream)
     */
    protected void serializeTo(RawDataBuffer out)
    {
        super.serializeTo(out);
        out.writeUTF(topicName);
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.network.packet.AbstractPacket#unserializeFrom(net.timewalker.ffmq3.utils.RawDataInputStream)
     */
    protected void unserializeFrom(RawDataBuffer in)
    {
        super.unserializeFrom(in);
        topicName = in.readUTF();
    }
    
    /**
     * @return the topicName
     */
    public String getTopicName()
    {
        return topicName;
    }

    /**
     * @param topicName the topicName to set
     */
    public void setTopicName(String topicName)
    {
        this.topicName = topicName;
    }
    
    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append(super.toString());
        sb.append(" topicName=");
        sb.append(topicName);
        
        return sb.toString();
    }
}
