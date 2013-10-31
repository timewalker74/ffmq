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

import javax.jms.Topic;

import net.timewalker.ffmq3.common.destination.DestinationSerializer;
import net.timewalker.ffmq3.transport.packet.PacketType;
import net.timewalker.ffmq3.utils.RawDataBuffer;
import net.timewalker.ffmq3.utils.id.IntegerID;

/**
 * CreateDurableSubscriberQuery
 */
public final class CreateDurableSubscriberQuery extends AbstractSessionQuery
{
	private IntegerID consumerId;
    private Topic topic;
    private String messageSelector;
    private boolean noLocal;
    private String name;
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.network.packet.AbstractPacket#getType()
     */
    public byte getType()
    {
        return PacketType.Q_CREATE_DURABLE_SUBSCRIBER;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.network.packet.AbstractPacket#serializeTo(net.timewalker.ffmq3.utils.RawDataOutputStream)
     */
    protected void serializeTo(RawDataBuffer out)
    {
        super.serializeTo(out);
        out.writeInt(consumerId.asInt());
        DestinationSerializer.serializeTo(topic, out);
        out.writeNullableUTF(messageSelector);
        out.writeBoolean(noLocal);
        out.writeUTF(name);
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.network.packet.AbstractPacket#unserializeFrom(net.timewalker.ffmq3.utils.RawDataInputStream)
     */
    protected void unserializeFrom(RawDataBuffer in)
    {
        super.unserializeFrom(in);
        consumerId = new IntegerID(in.readInt());
        topic = (Topic)DestinationSerializer.unserializeFrom(in);
        messageSelector = in.readNullableUTF();
        noLocal = in.readBoolean();
        name = in.readUTF();
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
     * @return the topic
     */
    public Topic getTopic()
    {
        return topic;
    }
    
    /**
     * @param topic the topic to set
     */
    public void setTopic(Topic topic)
    {
        this.topic = topic;
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
    
    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append(super.toString());
        sb.append(" consumerId=");
        sb.append(consumerId);
        sb.append(" topic=");
        sb.append(topic);
        sb.append(" messageSelector=[");
        sb.append(messageSelector);
        sb.append("] noLocal=");
        sb.append(noLocal);
        sb.append(" name=");
        sb.append(name);
        
        return sb.toString();
    }
}
