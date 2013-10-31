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

import net.timewalker.ffmq3.utils.RawDataBuffer;
import net.timewalker.ffmq3.utils.id.IntegerID;

/**
 * AbstractBrowserQuery
 */
public abstract class AbstractQueueBrowserQuery extends AbstractSessionQuery
{
	private IntegerID browserId;
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.network.packet.AbstractPacket#serializeTo(net.timewalker.ffmq3.utils.RawDataOutputStream)
     */
    protected void serializeTo(RawDataBuffer out)
    {
    	super.serializeTo(out);
        out.writeInt(browserId.asInt());
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.network.packet.AbstractPacket#unserializeFrom(net.timewalker.ffmq3.utils.RawDataInputStream)
     */
    protected void unserializeFrom(RawDataBuffer in)
    {
    	super.unserializeFrom(in);
    	browserId = new IntegerID(in.readInt());   
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

	/*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append(super.toString());
        sb.append(" browserId=");
        sb.append(browserId);
        
        return sb.toString();
    }
}
