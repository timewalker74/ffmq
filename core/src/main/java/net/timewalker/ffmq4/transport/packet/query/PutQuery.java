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
package net.timewalker.ffmq4.transport.packet.query;

import net.timewalker.ffmq4.common.message.AbstractMessage;
import net.timewalker.ffmq4.common.message.MessageSerializer;
import net.timewalker.ffmq4.transport.packet.PacketType;
import net.timewalker.ffmq4.utils.RawDataBuffer;

/**
 * PutQuery
 */
public final class PutQuery extends AbstractSessionQuery
{
    private AbstractMessage message;
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.network.packet.AbstractPacket#getType()
     */
    @Override
	public byte getType()
    {
        return PacketType.Q_PUT;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.network.packet.AbstractPacket#serializeTo(net.timewalker.ffmq4.utils.RawDataOutputStream)
     */
    @Override
	protected void serializeTo(RawDataBuffer out)
    {
        super.serializeTo(out);
        MessageSerializer.serializeTo(message, out);
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.network.packet.AbstractPacket#unserializeFrom(net.timewalker.ffmq4.utils.RawDataInputStream)
     */
    @Override
	protected void unserializeFrom(RawDataBuffer in)
    {
        super.unserializeFrom(in);
        message = MessageSerializer.unserializeFrom(in, true);
    }

    /**
     * @return Returns the message.
     */
    public AbstractMessage getMessage()
    {
        return message;
    }

    /**
     * @param message The message to set.
     */
    public void setMessage(AbstractMessage message)
    {
        this.message = message;
    }

    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString()
    {
    	StringBuilder sb = new StringBuilder();
        
        sb.append(super.toString());
        sb.append(" message=");
        sb.append(message);
        
        return sb.toString();
    }
}
