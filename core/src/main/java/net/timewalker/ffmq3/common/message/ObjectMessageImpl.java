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

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.MessageNotWriteableException;
import javax.jms.ObjectMessage;

import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.storage.message.MessageSerializationLevel;
import net.timewalker.ffmq3.utils.RawDataBuffer;
import net.timewalker.ffmq3.utils.SerializationTools;

/**
 * <p>Implementation of an {@link ObjectMessage}</p>
 */
public final class ObjectMessageImpl extends AbstractMessage implements ObjectMessage
{
    private byte[] body;
    
    /**
     * Constructor
     */
    public ObjectMessageImpl()
    {
        super();
    }
    
    /**
     * Constructor
     */
    public ObjectMessageImpl( Serializable object ) throws JMSException
    {
        super();
        setObject(object);
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.AbstractMessage#getType()
     */
    @Override
	protected byte getType()
    {
        return MessageType.OBJECT;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.AbstractMessage#unserializeBodyFrom(net.timewalker.ffmq3.utils.RawDataInputStream)
     */
    @Override
	protected void unserializeBodyFrom(RawDataBuffer in)
    {
        body = in.readNullableByteArray();
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.AbstractMessage#serializeBodyTo(net.timewalker.ffmq3.utils.RawDataBuffer)
     */
    @Override
	protected final void serializeBodyTo(RawDataBuffer out)
    {
    	out.writeNullableByteArray(body);
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
     * @see javax.jms.ObjectMessage#getObject()
     */
    @Override
	public Serializable getObject() throws JMSException
    {
        if (body == null)
            return null;
        
        try
        {
        	return SerializationTools.fromByteArray(body);
        }
        catch (Exception e)
        {
        	throw new FFMQException("Cannot deserialize object message body","MESSAGE_ERROR",e);
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.ObjectMessage#setObject(java.io.Serializable)
     */
    @Override
	public void setObject(Serializable object) throws JMSException
    {
    	if (bodyIsReadOnly)
    		throw new MessageNotWriteableException("Message body is read-only");
    	
    	assertDeserializationLevel(MessageSerializationLevel.FULL);
    	
        if (object == null)
        {
            body = null;
            return;
        }
        
        try
        {
        	body = SerializationTools.toByteArray(object);
        }
        catch (Exception e)
        {
        	throw new FFMQException("Cannot serialize object message body","MESSAGE_ERROR",e);
        }
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.AbstractMessage#copy()
     */
    @Override
	public AbstractMessage copy()
    {
        ObjectMessageImpl clone = new ObjectMessageImpl();
        copyCommonFields(clone);
        clone.body = this.body;
        
        return clone;
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.AbstractMessage#toString()
     */
    @Override
	public String toString()
    {
        return super.toString()+" bodySize="+body.length;
    }
}
