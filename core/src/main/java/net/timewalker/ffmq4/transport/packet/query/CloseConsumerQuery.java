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

import java.util.ArrayList;
import java.util.List;

import net.timewalker.ffmq4.transport.packet.PacketType;
import net.timewalker.ffmq4.utils.RawDataBuffer;

/**
 * CloseConsumerQuery
 */
public final class CloseConsumerQuery extends AbstractConsumerQuery
{
	private List<String> undeliveredMessageIDs;
	
	/**
	 * @return the undeliveredMessageIDs
	 */
	public List<String> getUndeliveredMessageIDs()
	{
		return undeliveredMessageIDs;
	}
	
	public void addUndeliveredMessageID( String msgID )
	{
		if (undeliveredMessageIDs == null)
			undeliveredMessageIDs = new ArrayList<>();
		undeliveredMessageIDs.add(msgID);
	}
	
	/* (non-Javadoc)
     * @see net.timewalker.ffmq4.network.packet.AbstractPacket#getType()
     */
    @Override
	public byte getType()
    {
        return PacketType.Q_CLOSE_CONSUMER;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.network.packet.AbstractPacket#serializeTo(net.timewalker.ffmq4.utils.RawDataOutputStream)
     */
    @Override
	protected void serializeTo(RawDataBuffer out)
    {
    	super.serializeTo(out);
    	if (undeliveredMessageIDs != null)
        {
    		int len = undeliveredMessageIDs.size(); 
            out.writeInt(len);
            for (int i = 0; i < len; i++)
            	out.writeUTF(undeliveredMessageIDs.get(i));
        }
        else
            out.writeInt(0);
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.network.packet.AbstractPacket#unserializeFrom(net.timewalker.ffmq4.utils.RawDataInputStream)
     */
    @Override
	protected void unserializeFrom(RawDataBuffer in)
    {
    	super.unserializeFrom(in);
    	int idCount = in.readInt();
        for (int i = 0; i < idCount; i++)
        {
        	String msgID = in.readUTF();
        	addUndeliveredMessageID(msgID);
        }
    }
}
