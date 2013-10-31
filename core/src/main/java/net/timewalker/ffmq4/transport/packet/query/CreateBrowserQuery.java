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

import javax.jms.Queue;

import net.timewalker.ffmq4.common.destination.DestinationSerializer;
import net.timewalker.ffmq4.transport.packet.PacketType;
import net.timewalker.ffmq4.utils.RawDataBuffer;
import net.timewalker.ffmq4.utils.id.IntegerID;

/**
 * CreateBrowserQuery
 */
public final class CreateBrowserQuery extends AbstractSessionQuery
{
	private IntegerID browserId;
    private Queue queue;
    private String messageSelector;

    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.network.packet.AbstractPacket#getType()
     */
    @Override
	public byte getType()
    {
        return PacketType.Q_CREATE_BROWSER;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.network.packet.AbstractPacket#serializeTo(net.timewalker.ffmq4.utils.RawDataOutputStream)
     */
    @Override
	protected void serializeTo(RawDataBuffer out)
    {
        super.serializeTo(out);
        out.writeInt(browserId.asInt());
        DestinationSerializer.serializeTo(queue, out);
        out.writeNullableUTF(messageSelector);
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.network.packet.AbstractPacket#unserializeFrom(net.timewalker.ffmq4.utils.RawDataInputStream)
     */
    @Override
	protected void unserializeFrom(RawDataBuffer in)
    {
        super.unserializeFrom(in);
        browserId = new IntegerID(in.readInt());
        queue = (Queue)DestinationSerializer.unserializeFrom(in);
        messageSelector = in.readNullableUTF();
    }

    /**
	 * @return the browserId
	 */
	public IntegerID getBrowserId()
	{
		return browserId;
	}
	
	/**
	 * @param browserId the browserId to set
	 */
	public void setBrowserId(IntegerID browserId)
	{
		this.browserId = browserId;
	}
    
	/**
	 * @return the queue
	 */
	public Queue getQueue()
	{
		return queue;
	}

	/**
	 * @param queue the queue to set
	 */
	public void setQueue(Queue queue)
	{
		this.queue = queue;
	}

	/**
     * @return the messageSelector
     */
    public String getMessageSelector()
    {
        return messageSelector;
    }
    
    /**
     * @param messageSelector the messageSelector to set
     */
    public void setMessageSelector(String messageSelector)
    {
        this.messageSelector = messageSelector;
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
        sb.append(" browserId=");
        sb.append(browserId);
        sb.append(" queue=");
        sb.append(queue);
        sb.append(" messageSelector=[");
        sb.append(messageSelector);
        sb.append("]");
        
        return sb.toString();
    }
}
