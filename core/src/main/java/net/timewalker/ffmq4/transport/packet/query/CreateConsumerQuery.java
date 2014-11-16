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

import javax.jms.Destination;

import net.timewalker.ffmq4.common.destination.DestinationSerializer;
import net.timewalker.ffmq4.transport.packet.PacketType;
import net.timewalker.ffmq4.utils.RawDataBuffer;
import net.timewalker.ffmq4.utils.id.IntegerID;

/**
 * CreateConsumerQuery
 */
public final class CreateConsumerQuery extends AbstractSessionQuery
{
	private IntegerID consumerId;
    private Destination destination;
    private String messageSelector;
    private boolean noLocal;
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.network.packet.AbstractPacket#getType()
     */
    @Override
	public byte getType()
    {
        return PacketType.Q_CREATE_CONSUMER;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.network.packet.AbstractPacket#serializeTo(net.timewalker.ffmq4.utils.RawDataOutputStream)
     */
    @Override
	protected void serializeTo(RawDataBuffer out)
    {
        super.serializeTo(out);
        out.writeInt(consumerId.asInt());
        DestinationSerializer.serializeTo(destination, out);
        out.writeNullableUTF(messageSelector);
        out.writeBoolean(noLocal);
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.network.packet.AbstractPacket#unserializeFrom(net.timewalker.ffmq4.utils.RawDataInputStream)
     */
    @Override
	protected void unserializeFrom(RawDataBuffer in)
    {
        super.unserializeFrom(in);
        consumerId = new IntegerID(in.readInt());
        destination = DestinationSerializer.unserializeFrom(in);
        messageSelector = in.readNullableUTF();
        noLocal = in.readBoolean();
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
     * @return the destination
     */
    public Destination getDestination()
    {
        return destination;
    }
    
    /**
     * @param destination the destination to set
     */
    public void setDestination(Destination destination)
    {
        this.destination = destination;
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
    
    /**
     * @return the noLocal
     */
    public boolean isNoLocal()
    {
        return noLocal;
    }
    
    /**
     * @param noLocal the noLocal to set
     */
    public void setNoLocal(boolean noLocal)
    {
        this.noLocal = noLocal;
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
        sb.append(" consumerId=");
        sb.append(consumerId);
        sb.append(" destination=");
        sb.append(destination);
        sb.append(" messageSelector=[");
        sb.append(messageSelector);
        sb.append("] noLocal=");
        sb.append(noLocal);
        
        return sb.toString();
    }
}
