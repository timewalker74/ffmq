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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Vector;

import javax.jms.JMSException;
import javax.jms.MessageEOFException;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotReadableException;
import javax.jms.MessageNotWriteableException;
import javax.jms.StreamMessage;

import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.storage.message.MessageSerializationLevel;
import net.timewalker.ffmq3.utils.RawDataBuffer;

/**
 * <p>Implementation of a {@link StreamMessage}</p>
 */
public final class StreamMessageImpl extends AbstractMessage implements StreamMessage
{
    private Vector body = new Vector();
    private transient int readPos;
    private transient ByteArrayInputStream currentByteInputStream;
    
    // For rollback
    private transient int readPosBackup;
    private transient ByteArrayInputStream currentByteInputStreamBackup;
    
    /**
     * Constructor
     */
    public StreamMessageImpl()
    {
        super();
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.AbstractMessage#getType()
     */
    protected byte getType()
    {
        return MessageType.STREAM;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.AbstractMessage#unserializeBodyFrom(net.timewalker.ffmq3.utils.RawDataInputStream)
     */
    protected void unserializeBodyFrom(RawDataBuffer in)
    {
        int size = in.readInt();
        body.ensureCapacity(size);
        for (int n = 0 ; n < size ; n++)
        {
            Object value = in.readGeneric();
            body.add(value);
        }
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.AbstractMessage#serializeBodyTo(net.timewalker.ffmq3.utils.RawDataBuffer)
     */
    protected final void serializeBodyTo(RawDataBuffer out)
    {
    	out.writeInt(body.size());
        for (int n = 0 ; n < body.size() ; n++)
        {
            Object value = body.get(n);
            out.writeGeneric(value);
        }
    }
    
    private void backupState()
    {
    	readPosBackup = readPos;
    	currentByteInputStreamBackup = currentByteInputStream;
    	if (currentByteInputStream != null)
    		currentByteInputStream.mark(-1);
    }
    
    private void restoreState()
    {
    	readPos = readPosBackup;
    	currentByteInputStream = currentByteInputStreamBackup;
    	if (currentByteInputStream != null)
    		currentByteInputStream.reset();
    }
    
    /*
     * (non-Javadoc)
     * @see javax.jms.StreamMessage#readBoolean()
     */
    public boolean readBoolean() throws JMSException
    {
    	backupState();
    	try
    	{
    		return MessageConvertTools.asBoolean(internalReadObject());
    	}
    	catch (JMSException e)
    	{
    		restoreState();
    		throw e;	
    	}
    	catch (RuntimeException e)
    	{
    		restoreState();
    		throw e;
    	}
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.StreamMessage#readByte()
     */
    public byte readByte() throws JMSException
    {
    	backupState();
    	try
    	{
    		return MessageConvertTools.asByte(internalReadObject());
    	}
    	catch (JMSException e)
    	{
    		restoreState();
    		throw e;	
    	}
    	catch (RuntimeException e)
    	{
    		restoreState();
    		throw e;
    	}
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.StreamMessage#readBytes(byte[])
     */
    public int readBytes(byte[] value) throws JMSException
    {
    	backupState();
    	try
    	{
	        if (currentByteInputStream == null)
	        {
	            byte[] base = MessageConvertTools.asBytes(internalReadObject());
	            if (base == null)
	            	return -1; // [JMS Spec]
	            currentByteInputStream = new ByteArrayInputStream(base);
	        }
	        
	        try
	        {        	
	            int readAmount = currentByteInputStream.read(value);
	            if (readAmount < value.length)
	            {
	                currentByteInputStream = null; // end of stream reached
	            }
	            
	            return readAmount;
	        }
	        catch (IOException e)
	        {
	            throw new FFMQException("Cannot read stream message body","IO_ERROR",e);
	        }
    	}
    	catch (JMSException e)
    	{
    		restoreState();
    		throw e;	
    	}
    	catch (RuntimeException e)
    	{
    		restoreState();
    		throw e;
    	}
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.StreamMessage#readChar()
     */
    public char readChar() throws JMSException
    {
    	backupState();
    	try
    	{
    		return MessageConvertTools.asChar(internalReadObject());
    	}
    	catch (JMSException e)
    	{
    		restoreState();
    		throw e;	
    	}
    	catch (RuntimeException e)
    	{
    		restoreState();
    		throw e;
    	}
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.StreamMessage#readDouble()
     */
    public double readDouble() throws JMSException
    {
    	backupState();
    	try
    	{
    		return MessageConvertTools.asDouble(internalReadObject());
    	}
    	catch (JMSException e)
    	{
    		restoreState();
    		throw e;	
    	}
    	catch (RuntimeException e)
    	{
    		restoreState();
    		throw e;
    	}
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.StreamMessage#readFloat()
     */
    public float readFloat() throws JMSException
    {
    	backupState();
		try
		{
			return MessageConvertTools.asFloat(internalReadObject());
		}
    	catch (JMSException e)
    	{
    		restoreState();
    		throw e;	
    	}
    	catch (RuntimeException e)
    	{
    		restoreState();
    		throw e;
    	}	
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.StreamMessage#readInt()
     */
    public int readInt() throws JMSException
    {
    	backupState();
		try
		{
			return MessageConvertTools.asInt(internalReadObject());
		}
    	catch (JMSException e)
    	{
    		restoreState();
    		throw e;	
    	}
    	catch (RuntimeException e)
    	{
    		restoreState();
    		throw e;
    	}
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.StreamMessage#readLong()
     */
    public long readLong() throws JMSException
    {
    	backupState();
		try
		{
			return MessageConvertTools.asLong(internalReadObject());
		}
    	catch (JMSException e)
    	{
    		restoreState();
    		throw e;	
    	}
    	catch (RuntimeException e)
    	{
    		restoreState();
    		throw e;
    	}
    }

    /* (non-Javadoc)
     * @see javax.jms.StreamMessage#readObject()
     */
    public Object readObject() throws JMSException
    {
    	return internalReadObject();
    }
    
    /*
     * (non-Javadoc)
     * @see javax.jms.StreamMessage#internalReadObject()
     */
    public Object internalReadObject() throws JMSException
    {
        if (!bodyIsReadOnly)
            throw new MessageNotReadableException("Message body is write only");
            
        if (readPos >= body.size())
            throw new MessageEOFException("End of stream reached");
        
        if (currentByteInputStream != null)
            throw new MessageFormatException("Cannot read another object before the end of the byte array");
        
        return body.get(readPos++);
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.StreamMessage#readShort()
     */
    public short readShort() throws JMSException
    {
    	backupState();
		try
		{
			return MessageConvertTools.asShort(internalReadObject());
		}
    	catch (JMSException e)
    	{
    		restoreState();
    		throw e;	
    	}
    	catch (RuntimeException e)
    	{
    		restoreState();
    		throw e;
    	}
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.StreamMessage#readString()
     */
    public String readString() throws JMSException
    {
    	backupState();
		try
		{
			return MessageConvertTools.asString(internalReadObject());
		}
    	catch (JMSException e)
    	{
    		restoreState();
    		throw e;	
    	}
    	catch (RuntimeException e)
    	{
    		restoreState();
    		throw e;
    	}
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.StreamMessage#reset()
     */
    public void reset()
    {
        bodyIsReadOnly = true;
        readPos = 0;
        currentByteInputStream = null;
    }

    private void write( Object value ) throws JMSException
    {
    	assertDeserializationLevel(MessageSerializationLevel.FULL);
        if (bodyIsReadOnly)
            throw new MessageNotWriteableException("Message body is read-only");
        body.add(value);
    }
    
    /*
     * (non-Javadoc)
     * @see javax.jms.StreamMessage#writeBoolean(boolean)
     */
    public void writeBoolean(boolean value) throws JMSException
    {
        write(Boolean.valueOf(value));
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.StreamMessage#writeByte(byte)
     */
    public void writeByte(byte value) throws JMSException
    {
        write(new Byte(value));
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.StreamMessage#writeBytes(byte[])
     */
    public void writeBytes(byte[] value) throws JMSException
    {
        write(value.clone());
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.StreamMessage#writeBytes(byte[], int, int)
     */
    public void writeBytes(byte[] value, int offset, int length) throws JMSException
    {
        byte[] reducedValue = new byte[length];
        System.arraycopy(value, offset, reducedValue, 0, length);
        write(reducedValue);
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.StreamMessage#writeChar(char)
     */
    public void writeChar(char value) throws JMSException
    {
        write(new Character(value));
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.StreamMessage#writeDouble(double)
     */
    public void writeDouble(double value) throws JMSException
    {
        write(new Double(value));
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.StreamMessage#writeFloat(float)
     */
    public void writeFloat(float value) throws JMSException
    {
        write(new Float(value));
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.StreamMessage#writeInt(int)
     */
    public void writeInt(int value) throws JMSException
    {
        write(new Integer(value));
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.StreamMessage#writeLong(long)
     */
    public void writeLong(long value) throws JMSException
    {
        write(new Long(value));
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.StreamMessage#writeObject(java.lang.Object)
     */
    public void writeObject(Object value) throws JMSException
    {
        if (value != null)
        {
        	// Check supported types
	        if (!(value instanceof Boolean ||
	              value instanceof Byte ||
	              value instanceof Character ||
	              value instanceof Short ||
	              value instanceof Integer ||
	              value instanceof Long ||
	              value instanceof Float ||
	              value instanceof Double ||
	              value instanceof String ||
	              value instanceof byte[]))
	            throw new MessageFormatException("Unsupported value type : "+value.getClass().getName());
        }
        
        write(value);
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.StreamMessage#writeShort(short)
     */
    public void writeShort(short value) throws JMSException
    {
        write(new Short(value));
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.StreamMessage#writeString(java.lang.String)
     */
    public void writeString(String value) throws JMSException
    {
        write(value);
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.Message#clearBody()
     */
    public void clearBody()
    {
    	assertDeserializationLevel(MessageSerializationLevel.FULL);
        bodyIsReadOnly = false;
        body.clear();
        readPos = 0;
        currentByteInputStream = null;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.AbstractMessage#copy()
     */
    public AbstractMessage copy()
    {
        StreamMessageImpl clone = new StreamMessageImpl();
        copyCommonFields(clone);
        clone.body = (Vector)this.body.clone();
        
        return clone;
    }
}
