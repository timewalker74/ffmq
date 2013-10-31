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

import java.util.ArrayList;
import java.util.List;

import net.timewalker.ffmq3.utils.RawDataBuffer;

/**
 * AbstractTransactionDemarcationQuery
 */
public abstract class AbstractTransactionDemarcationQuery extends AbstractSessionQuery
{
	private List deliveredMessageIDs;
	
	/**
	 * @return the deliveredMessageIDs
	 */
	public List getDeliveredMessageIDs()
	{
		return deliveredMessageIDs;
	}

	/**
	 * @param deliveredMessageIDs the deliveredMessageIDs to set
	 */
	public void setDeliveredMessageIDs(List deliveredMessageIDs)
	{
		this.deliveredMessageIDs = deliveredMessageIDs;
	}
	
	public void addDeliveredMessageID( String msgID )
	{
		if (deliveredMessageIDs == null)
			deliveredMessageIDs = new ArrayList();
		deliveredMessageIDs.add(msgID);
	}
	
	/* (non-Javadoc)
     * @see net.timewalker.ffmq3.network.packet.AbstractPacket#serializeTo(net.timewalker.ffmq3.utils.RawDataOutputStream)
     */
    protected void serializeTo(RawDataBuffer out)
    {
    	super.serializeTo(out);
    	if (deliveredMessageIDs != null)
        {
    		int len = deliveredMessageIDs.size(); 
            out.writeInt(len);
            for (int i = 0; i < len; i++)
            	out.writeUTF((String)deliveredMessageIDs.get(i));
        }
        else
            out.writeInt(0);
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.network.packet.AbstractPacket#unserializeFrom(net.timewalker.ffmq3.utils.RawDataInputStream)
     */
    protected void unserializeFrom(RawDataBuffer in)
    {
    	super.unserializeFrom(in);
    	int idCount = in.readInt();
        for (int i = 0; i < idCount; i++)
        {
        	String msgID = in.readUTF();
        	addDeliveredMessageID(msgID);
        }
    }
    
    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
       StringBuffer sb = new StringBuffer();
       
       sb.append(super.toString());
       sb.append(" ids=");
       sb.append(deliveredMessageIDs != null ? deliveredMessageIDs.size() : 0);
       
       return sb.toString();
    }
}
