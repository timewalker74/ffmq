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

import net.timewalker.ffmq4.common.message.AbstractMessage;
import net.timewalker.ffmq4.common.message.MessageSerializer;
import net.timewalker.ffmq4.utils.RawDataBuffer;
import net.timewalker.ffmq4.utils.id.IntegerID;


/**
 * NotificationPacket
 */
public final class NotificationPacket extends AbstractPacket
{
    private IntegerID sessionId;
    private IntegerID consumerId;
    private AbstractMessage message;
    private boolean donePrefetching;
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.network.packet.AbstractPacket#getType()
     */
    @Override
	public byte getType()
    {
        return PacketType.NOTIFICATION;
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.network.packet.AbstractPacket#serializeTo(net.timewalker.ffmq4.utils.RawDataOutputStream)
     */
    @Override
	protected void serializeTo(RawDataBuffer out)
    {
        super.serializeTo(out);
        out.writeInt(sessionId.asInt());
        out.writeInt(consumerId.asInt());
        MessageSerializer.serializeTo(message, out);
        out.writeBoolean(donePrefetching);
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.network.packet.AbstractPacket#unserializeFrom(net.timewalker.ffmq4.utils.RawDataInputStream)
     */
    @Override
	protected void unserializeFrom(RawDataBuffer in)
    {
        super.unserializeFrom(in);
        sessionId = new IntegerID(in.readInt());
        consumerId = new IntegerID(in.readInt());
        message = MessageSerializer.unserializeFrom(in, false);
        donePrefetching = in.readBoolean();
    }

    /**
     * @return the sessionId
     */
    public IntegerID getSessionId()
    {
        return sessionId;
    }

    /**
     * @param sessionId the sessionId to set
     */
    public void setSessionId(IntegerID sessionId)
    {
        this.sessionId = sessionId;
    }

    /**
     * @return the consumerId
     */
    public IntegerID getConsumerId()
    {
        return consumerId;
    }

    /**
     * @param consumerId the consumerId to set
     */
    public void setConsumerId(IntegerID consumerId)
    {
        this.consumerId = consumerId;
    }

    /**
	 * @return the message
	 */
	public AbstractMessage getMessage()
	{
		return message;
	}
	
	/**
	 * @param message the message to set
	 */
	public void setMessage(AbstractMessage message)
	{
		this.message = message;
	}
       
	/**
	 * @return the donePrefetching
	 */
	public boolean isDonePrefetching()
	{
		return donePrefetching;
	}
	
	/**
	 * @param donePrefetching the donePrefetching to set
	 */
	public void setDonePrefetching(boolean donePrefetching)
	{
		this.donePrefetching = donePrefetching;
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
       sb.append(" consumerId=");
       sb.append(consumerId);
       sb.append(" message=");
       sb.append(message.getJMSMessageID());
       sb.append(" donePrefetching=");
       sb.append(donePrefetching);
       
       return sb.toString();
    }
}
