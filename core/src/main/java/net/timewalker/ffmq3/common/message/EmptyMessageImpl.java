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
package net.timewalker.ffmq3.common.message;

import javax.jms.Message;

import net.timewalker.ffmq3.utils.RawDataBuffer;

/**
 * <p>Implementation of an empty {@link Message} (message without body)</p>
 */
public final class EmptyMessageImpl extends AbstractMessage
{
    /**
     * Constructor
     */
    public EmptyMessageImpl()
    {
        super();
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.AbstractMessage#getType()
     */
    @Override
	protected byte getType()
    {
        return MessageType.EMPTY;
    }
    
    /*
     * (non-Javadoc)
     * @see javax.jms.Message#clearBody()
     */
    @Override
	public void clearBody()
    {
        // Nothing to do
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.AbstractMessage#serializeBodyTo(net.timewalker.ffmq3.utils.RawDataBuffer)
     */
    @Override
	protected void serializeBodyTo(RawDataBuffer out)
    {
    	// No body
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.AbstractMessage#unserializeBodyFrom(net.timewalker.ffmq3.utils.RawDataInputStream)
     */
    @Override
	protected void unserializeBodyFrom(RawDataBuffer in)
    {
    	// No body
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.AbstractMessage#copy()
     */
    @Override
	public AbstractMessage copy()
    {
        EmptyMessageImpl clone = new EmptyMessageImpl();
        copyCommonFields(clone);
        return clone;
    }
}
