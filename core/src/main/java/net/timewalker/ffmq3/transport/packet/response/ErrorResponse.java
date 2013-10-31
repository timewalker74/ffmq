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

import javax.jms.JMSException;

import net.timewalker.ffmq3.transport.packet.AbstractResponsePacket;
import net.timewalker.ffmq3.transport.packet.PacketType;
import net.timewalker.ffmq3.utils.RawDataBuffer;
import net.timewalker.ffmq3.utils.SerializationTools;

/**
 * ErrorResponse
 */
public final class ErrorResponse extends AbstractResponsePacket
{
    private JMSException error;
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.network.packet.AbstractPacket#getType()
     */
    @Override
	public byte getType()
    {
        return PacketType.R_ERROR;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.network.packet.AbstractResponsePacket#serializeTo(net.timewalker.ffmq3.utils.RawDataOutputStream)
     */
    @Override
	protected void serializeTo(RawDataBuffer out)
    {
        super.serializeTo(out);
    	out.writeByteArray(SerializationTools.toByteArray(error));
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.network.packet.AbstractResponsePacket#unserializeFrom(net.timewalker.ffmq3.utils.RawDataInputStream)
     */
    @Override
	protected void unserializeFrom(RawDataBuffer in)
    {
        super.unserializeFrom(in);
    	byte[] data = in.readByteArray();
        error = (JMSException)SerializationTools.fromByteArray(data);
    }
    
    /**
     * Constructor
     */
    public ErrorResponse()
    {
        super();
    }
    
    /**
     * Constructor
     */
    public ErrorResponse( JMSException error )
    {
        super();
        this.error = error;
    }

    /**
     * Respawn the original error
     */
    public void respawnError() throws JMSException
    {
        throw error;
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
        sb.append(" error=");
        sb.append(error.toString());
        
        return sb.toString();
    }
}
