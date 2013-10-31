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
 * SetClientIDQuery
 */
public final class SetClientIDQuery extends AbstractQueryPacket
{
    private String clientID;

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.network.packet.AbstractPacket#getType()
     */
    @Override
	public byte getType()
    {
        return PacketType.Q_SET_CLIENT_ID;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.network.packet.AbstractPacket#serializeTo(net.timewalker.ffmq3.utils.RawDataOutputStream)
     */
    @Override
	protected void serializeTo(RawDataBuffer out)
    {
        super.serializeTo(out);
        out.writeUTF(clientID);
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.network.packet.AbstractPacket#unserializeFrom(net.timewalker.ffmq3.utils.RawDataInputStream)
     */
    @Override
	protected void unserializeFrom(RawDataBuffer in)
    {
        super.unserializeFrom(in);
        clientID = in.readUTF();
    }
    
    /**
     * @return the clientID
     */
    public String getClientID()
    {
        return clientID;
    }

    /**
     * @param clientID the clientID to set
     */
    public void setClientID(String clientID)
    {
        this.clientID = clientID;
    }
}
