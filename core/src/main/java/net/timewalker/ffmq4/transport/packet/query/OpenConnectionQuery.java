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

import net.timewalker.ffmq4.transport.packet.AbstractQueryPacket;
import net.timewalker.ffmq4.transport.packet.PacketType;
import net.timewalker.ffmq4.utils.RawDataBuffer;

/**
 * OpenConnectionQuery
 */
public final class OpenConnectionQuery extends AbstractQueryPacket
{
    private String userName;
    private String password;
    private String clientID;
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.network.packet.AbstractPacket#getType()
     */
    @Override
	public byte getType()
    {
        return PacketType.Q_OPEN_CONNECTION;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.network.packet.AbstractPacket#serializeTo(net.timewalker.ffmq4.utils.RawDataOutputStream)
     */
    @Override
	protected void serializeTo(RawDataBuffer out)
    {
        super.serializeTo(out);
        out.writeNullableUTF(userName);
        out.writeNullableUTF(password);
        out.writeNullableUTF(clientID);
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.network.packet.AbstractPacket#unserializeFrom(net.timewalker.ffmq4.utils.RawDataInputStream)
     */
    @Override
	protected void unserializeFrom(RawDataBuffer in)
    {
        super.unserializeFrom(in);
        userName = in.readNullableUTF();
        password = in.readNullableUTF();
        clientID = in.readNullableUTF();
    }
    
    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }
    
    /**
     * @param password the password to set
     */
    public void setPassword(String password)
    {
        this.password = password;
    }
    
    /**
     * @return the userName
     */
    public String getUserName()
    {
        return userName;
    }
    
    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName)
    {
        this.userName = userName;
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

    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString()
    {
    	StringBuilder sb = new StringBuilder();
        
        sb.append(super.toString());
        sb.append(" userName=");
        sb.append(userName);
        sb.append(" password=???");
        
        return sb.toString();
    }
}
