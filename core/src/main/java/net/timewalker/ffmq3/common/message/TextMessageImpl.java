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

import javax.jms.JMSException;
import javax.jms.MessageNotWriteableException;
import javax.jms.TextMessage;

import net.timewalker.ffmq3.storage.message.MessageSerializationLevel;
import net.timewalker.ffmq3.utils.RawDataBuffer;

/**
 * <p>Implementation of a {@link TextMessage}</p>
 */
public final class TextMessageImpl extends AbstractMessage implements TextMessage
{
    private String body;
    
    /**
     * Constructor
     */
    public TextMessageImpl()
    {
        super();
    }

    /**
     * Constructor
     */
    public TextMessageImpl( String text ) throws JMSException
    {
        super();
        setText(text);
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.AbstractMessage#getType()
     */
    @Override
	protected byte getType()
    {
        return MessageType.TEXT;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.AbstractMessage#unserializeBodyFrom(net.timewalker.ffmq3.utils.RawDataInputStream)
     */
    @Override
	protected void unserializeBodyFrom(RawDataBuffer in)
    {
        body = in.readNullableUTF();
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.AbstractMessage#serializeBodyTo(net.timewalker.ffmq3.utils.RawDataBuffer)
     */
    @Override
	protected final void serializeBodyTo(RawDataBuffer out)
    {
    	out.writeNullableUTF(body);
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.common.AbstractMessage#clearBody()
     */
    @Override
	public void clearBody()
    {
    	assertDeserializationLevel(MessageSerializationLevel.FULL);
        body = null;
        bodyIsReadOnly = false;
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.TextMessage#getText()
     */
    @Override
	public String getText() throws JMSException
    {
        return body;
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.TextMessage#setText(java.lang.String)
     */
    @Override
	public void setText(String body) throws JMSException
    {
    	if (bodyIsReadOnly)
            throw new MessageNotWriteableException("Message body is read-only");
        
    	assertDeserializationLevel(MessageSerializationLevel.FULL);
    	
        this.body = body;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.AbstractMessage#copy()
     */
    @Override
	public AbstractMessage copy()
    {
        TextMessageImpl clone = new TextMessageImpl();
        copyCommonFields(clone);
        clone.body = this.body;
        
        return clone;
    }
    
    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString()
    {
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append(" body=");
        sb.append(body);
        
        return sb.toString();
    }
}
