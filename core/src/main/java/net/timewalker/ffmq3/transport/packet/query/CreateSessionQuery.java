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
import net.timewalker.ffmq3.utils.id.IntegerID;

/**
 * OpenSessionQuery
 */
public final class CreateSessionQuery extends AbstractQueryPacket
{
	private IntegerID sessionId;
    private boolean transacted;
    private int acknowledgeMode;
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.network.packet.AbstractPacket#getType()
     */
    @Override
	public byte getType()
    {
        return PacketType.Q_CREATE_SESSION;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.network.packet.AbstractPacket#serializeTo(net.timewalker.ffmq3.utils.RawDataOutputStream)
     */
    @Override
	protected void serializeTo(RawDataBuffer out)
    {
        super.serializeTo(out);
        out.writeInt(sessionId.asInt());
        out.writeBoolean(transacted);
        out.writeInt(acknowledgeMode);
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.network.packet.AbstractPacket#unserializeFrom(net.timewalker.ffmq3.utils.RawDataInputStream)
     */
    @Override
	protected void unserializeFrom(RawDataBuffer in)
    {
        super.unserializeFrom(in);
        sessionId = new IntegerID(in.readInt());
        transacted = in.readBoolean();
        acknowledgeMode = in.readInt();
    }
    
    /**
	 * @param sessionId the sessionId to set
	 */
	public void setSessionId(IntegerID sessionId)
	{
		this.sessionId = sessionId;
	}
	
	/**
	 * @return the sessionId
	 */
	public IntegerID getSessionId()
	{
		return sessionId;
	}
    
    /**
     * @return Returns the transacted.
     */
    public boolean isTransacted()
    {
        return transacted;
    }

    /**
     * @param transacted The transacted to set.
     */
    public void setTransacted(boolean transacted)
    {
        this.transacted = transacted;
    }
    
    /**
     * @return the acknowledgeMode
     */
    public int getAcknowledgeMode()
    {
        return acknowledgeMode;
    }

    /**
     * @param acknowledgeMode the acknowledgeMode to set
     */
    public void setAcknowledgeMode(int acknowledgeMode)
    {
        this.acknowledgeMode = acknowledgeMode;
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
        sb.append(" sessionId=");
        sb.append(sessionId);
        sb.append(" transacted=");
        sb.append(transacted);
        sb.append(" acknowledgeMode=");
        sb.append(acknowledgeMode);
        
        return sb.toString();
    }
}
