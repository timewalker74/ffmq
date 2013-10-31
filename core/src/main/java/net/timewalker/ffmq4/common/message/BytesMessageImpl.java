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
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MessageEOFException;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotReadableException;
import javax.jms.MessageNotWriteableException;

import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.storage.message.MessageSerializationLevel;
import net.timewalker.ffmq3.utils.RawDataBuffer;

/**
 * <p>Implementation of a {@link BytesMessage}</p>
 */
public final class BytesMessageImpl extends AbstractMessage implements BytesMessage
{
    private byte[] body;
    private transient DataInputStream input;
    private transient DataOutputStream output;
    private transient ByteArrayInputStream inputBuf;
    private transient ByteArrayOutputStream outputBuf;
    
    /**
     * Constructor
     */
    public BytesMessageImpl()
    {
        super();
    }
    
    private void tidyUp()
    {
    	if (outputBuf != null)
    	{
    		body = outputBuf.toByteArray();
    		outputBuf = null;
    	}
    	output = null;
    	inputBuf = null;
    	input = null;
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.AbstractMessage#getType()
     */
    @Override
	protected byte getType()
    {
        return MessageType.BYTES;
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
	protected void serializeBodyTo(RawDataBuffer out)
    {
    	tidyUp();
        out.writeNullableByteArray(body);
    }

    private void backupState()
    {
    	if (inputBuf != null)
    		inputBuf.mark(-1);
    }
    
    private void restoreState()
    {
    	if (inputBuf != null)
    		inputBuf.reset();
    }
    
    private DataInputStream getInput() throws JMSException
    {
        if (!bodyIsReadOnly)
            throw new MessageNotReadableException("Message body is write-only");
        
        if (input == null)
        {
        	inputBuf = new ByteArrayInputStream(body != null ? body : new byte[0]);
            input = new DataInputStream(inputBuf);
        }
                
        return input;
    }
    
    private DataOutputStream getOutput() throws JMSException
    {
        if (bodyIsReadOnly)
            throw new MessageNotWriteableException("Message body is read-only");
        
        if (output == null)
        {
            outputBuf = new ByteArrayOutputStream(1024);
            output = new DataOutputStream(outputBuf);
        }
                
        return output;
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
        input = null;
        output = null;
        inputBuf = null;
        outputBuf = null;
        bodyIsReadOnly = false;
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.BytesMessage#getBodyLength()
     */
    @Override
	public long getBodyLength() throws JMSException
    {
        if (!bodyIsReadOnly)
            throw new MessageNotReadableException("Message body is write-only");
        
        return body != null ? body.length : 0;
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.BytesMessage#readBoolean()
     */
    @Override
	public boolean readBoolean() throws JMSException
    {
    	backupState();
        try
        {
            return getInput().readBoolean();
        }
        catch (EOFException e)
        {
        	restoreState();
            throw new MessageEOFException("End of body reached");
        }
        catch (IOException e)
        {
        	restoreState();
            throw new FFMQException("Cannot read message body","IO_ERROR",e);
        }
        catch (RuntimeException e)
        {
        	restoreState();
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.BytesMessage#readByte()
     */
    @Override
	public byte readByte() throws JMSException
    {
    	backupState();
        try
        {
            return getInput().readByte();
        }
        catch (EOFException e)
        {
        	restoreState();
            throw new MessageEOFException("End of body reached");
        }
        catch (IOException e)
        {
        	restoreState();
        	throw new FFMQException("Cannot read message body","IO_ERROR",e);
        }
        catch (RuntimeException e)
        {
        	restoreState();
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.BytesMessage#readBytes(byte[])
     */
    @Override
	public int readBytes(byte[] value) throws JMSException
    {
    	backupState();
        try
        {
            return getInput().read(value);
        }
        catch (EOFException e)
        {
        	restoreState();
            throw new MessageEOFException("End of body reached");
        }
        catch (IOException e)
        {
        	restoreState();
            throw new FFMQException("Cannot read message body","IO_ERROR",e);
        }
        catch (RuntimeException e)
        {
        	restoreState();
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.BytesMessage#readBytes(byte[], int)
     */
    @Override
	public int readBytes(byte[] value, int length) throws JMSException
    {
    	backupState();
        try
        {
            return getInput().read(value,0,length);
        }
        catch (EOFException e)
        {
        	restoreState();
            throw new MessageEOFException("End of body reached");
        }
        catch (IOException e)
        {
        	restoreState();
            throw new FFMQException("Cannot read message body","IO_ERROR",e);
        }
        catch (RuntimeException e)
        {
        	restoreState();
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.BytesMessage#readChar()
     */
    @Override
	public char readChar() throws JMSException
    {
    	backupState();
        try
        {
            return getInput().readChar();
        }
        catch (EOFException e)
        {
        	restoreState();
            throw new MessageEOFException("End of body reached");
        }
        catch (IOException e)
        {
        	restoreState();
            throw new FFMQException("Cannot read message body","IO_ERROR",e);
        }
        catch (RuntimeException e)
        {
        	restoreState();
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.BytesMessage#readDouble()
     */
    @Override
	public double readDouble() throws JMSException
    {
    	backupState();
        try
        {
            return getInput().readDouble();
        }
        catch (EOFException e)
        {
        	restoreState();
            throw new MessageEOFException("End of body reached");
        }
        catch (IOException e)
        {
        	restoreState();
            throw new FFMQException("Cannot read message body","IO_ERROR",e);
        }
        catch (RuntimeException e)
        {
        	restoreState();
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.BytesMessage#readFloat()
     */
    @Override
	public float readFloat() throws JMSException
    {
    	backupState();
        try
        {
            return getInput().readFloat();
        }
        catch (EOFException e)
        {
        	restoreState();
            throw new MessageEOFException("End of body reached");
        }
        catch (IOException e)
        {
        	restoreState();
            throw new FFMQException("Cannot read message body","IO_ERROR",e);
        }
        catch (RuntimeException e)
        {
        	restoreState();
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.BytesMessage#readInt()
     */
    @Override
	public int readInt() throws JMSException
    {
    	backupState();
        try
        {
            return getInput().readInt();
        }
        catch (EOFException e)
        {
        	restoreState();
            throw new MessageEOFException("End of body reached");
        }
        catch (IOException e)
        {
        	restoreState();
            throw new FFMQException("Cannot read message body","IO_ERROR",e);
        }
        catch (RuntimeException e)
        {
        	restoreState();
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.BytesMessage#readLong()
     */
    @Override
	public long readLong() throws JMSException
    {
    	backupState();
        try
        {
            return getInput().readLong();
        }
        catch (EOFException e)
        {
        	restoreState();
            throw new MessageEOFException("End of body reached");
        }
        catch (IOException e)
        {
        	restoreState();
            throw new FFMQException("Cannot read message body","IO_ERROR",e);
        }
        catch (RuntimeException e)
        {
        	restoreState();
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.BytesMessage#readShort()
     */
    @Override
	public short readShort() throws JMSException
    {
    	backupState();
        try
        {
            return getInput().readShort();
        }
        catch (EOFException e)
        {
        	restoreState();
            throw new MessageEOFException("End of body reached");
        }
        catch (IOException e)
        {
        	restoreState();
            throw new FFMQException("Cannot read message body","IO_ERROR",e);
        }
        catch (RuntimeException e)
        {
        	restoreState();
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.BytesMessage#readUTF()
     */
    @Override
	public String readUTF() throws JMSException
    {
    	backupState();
        try
        {
            return getInput().readUTF();
        }
        catch (EOFException e)
        {
        	restoreState();
            throw new MessageEOFException("End of body reached");
        }
        catch (IOException e)
        {
        	restoreState();
            throw new FFMQException("Cannot read message body","IO_ERROR",e);
        }
        catch (RuntimeException e)
        {
        	restoreState();
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.BytesMessage#readUnsignedByte()
     */
    @Override
	public int readUnsignedByte() throws JMSException
    {
    	backupState();
        try
        {
            return getInput().readUnsignedByte();
        }
        catch (EOFException e)
        {
        	restoreState();
            throw new MessageEOFException("End of body reached");
        }
        catch (IOException e)
        {
        	restoreState();
            throw new FFMQException("Cannot read message body","IO_ERROR",e);
        }
        catch (RuntimeException e)
        {
        	restoreState();
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.BytesMessage#readUnsignedShort()
     */
    @Override
	public int readUnsignedShort() throws JMSException
    {
    	backupState();
        try
        {
            return getInput().readUnsignedShort();
        }
        catch (EOFException e)
        {
        	restoreState();
            throw new MessageEOFException("End of body reached");
        }
        catch (IOException e)
        {
        	restoreState();
            throw new FFMQException("Cannot read message body","IO_ERROR",e);
        }
        catch (RuntimeException e)
        {
        	restoreState();
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.BytesMessage#reset()
     */
    @Override
	public void reset()
    {
        assertDeserializationLevel(MessageSerializationLevel.FULL);
        
        tidyUp();
        bodyIsReadOnly = true;
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.BytesMessage#writeBoolean(boolean)
     */
    @Override
	public void writeBoolean(boolean value) throws JMSException
    {
        try
        {
            getOutput().writeBoolean(value);
        }
        catch (IOException e)
        {
        	throw new FFMQException("Cannot write message body","IO_ERROR",e);
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.BytesMessage#writeByte(byte)
     */
    @Override
	public void writeByte(byte value) throws JMSException
    {
        try
        {
            getOutput().writeByte(value);
        }
        catch (IOException e)
        {
            throw new FFMQException("Cannot write message body","IO_ERROR",e);
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.BytesMessage#writeBytes(byte[])
     */
    @Override
	public void writeBytes(byte[] value) throws JMSException
    {
        try
        {
            getOutput().write(value);
        }
        catch (IOException e)
        {
            throw new FFMQException("Cannot write message body","IO_ERROR",e);
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.BytesMessage#writeBytes(byte[], int, int)
     */
    @Override
	public void writeBytes(byte[] value, int offset, int length) throws JMSException
    {
        try
        {
            getOutput().write(value,offset,length);
        }
        catch (IOException e)
        {
            throw new FFMQException("Cannot write message body","IO_ERROR",e);
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.BytesMessage#writeChar(char)
     */
    @Override
	public void writeChar(char value) throws JMSException
    {
        try
        {
            getOutput().writeChar(value);
        }
        catch (IOException e)
        {
            throw new FFMQException("Cannot write message body","IO_ERROR",e);
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.BytesMessage#writeDouble(double)
     */
    @Override
	public void writeDouble(double value) throws JMSException
    {
        try
        {
            getOutput().writeDouble(value);
        }
        catch (IOException e)
        {
            throw new FFMQException("Cannot write message body","IO_ERROR",e);
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.BytesMessage#writeFloat(float)
     */
    @Override
	public void writeFloat(float value) throws JMSException
    {
        try
        {
            getOutput().writeFloat(value);
        }
        catch (IOException e)
        {
            throw new FFMQException("Cannot write message body","IO_ERROR",e);
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.BytesMessage#writeInt(int)
     */
    @Override
	public void writeInt(int value) throws JMSException
    {
        try
        {
            getOutput().writeInt(value);
        }
        catch (IOException e)
        {
            throw new FFMQException("Cannot write message body","IO_ERROR",e);
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.BytesMessage#writeLong(long)
     */
    @Override
	public void writeLong(long value) throws JMSException
    {
        try
        {
            getOutput().writeLong(value);
        }
        catch (IOException e)
        {
            throw new FFMQException("Cannot write message body","IO_ERROR",e);
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.BytesMessage#writeObject(java.lang.Object)
     */
    @Override
	public void writeObject(Object value) throws JMSException
    {
        if (value == null)
            throw new NullPointerException(); // [JMS Spec]
        
        if (value instanceof Boolean)
            writeBoolean(((Boolean)value).booleanValue());
        else
        if (value instanceof Byte)
            writeByte(((Byte)value).byteValue());
        else
        if (value instanceof Short)
            writeShort(((Short)value).shortValue());
        else
        if (value instanceof Integer)
            writeInt(((Integer)value).intValue());
        else
        if (value instanceof Long)
            writeLong(((Long)value).longValue());
        else
        if (value instanceof Float)
            writeFloat(((Float)value).floatValue());
        else
        if (value instanceof Double)
            writeDouble(((Double)value).doubleValue());
        else
        if (value instanceof String)
            writeUTF((String)value);
        else
        if (value instanceof byte[])
            writeBytes((byte[])value);
        else
            throw new MessageFormatException("Unsupported property value type : "+value.getClass().getName());
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.BytesMessage#writeShort(short)
     */
    @Override
	public void writeShort(short value) throws JMSException
    {
        try
        {
            getOutput().writeShort(value);
        }
        catch (IOException e)
        {
            throw new FFMQException("Cannot write message body","IO_ERROR",e);
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.BytesMessage#writeUTF(java.lang.String)
     */
    @Override
	public void writeUTF(String value) throws JMSException
    {
        try
        {
            getOutput().writeUTF(value);
        }
        catch (IOException e)
        {
            throw new FFMQException("Cannot write message body","IO_ERROR",e);
        }
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.AbstractMessage#copy()
     */
    @Override
	public AbstractMessage copy()
    {
        BytesMessageImpl clone = new BytesMessageImpl();
        copyCommonFields(clone);
        tidyUp();
        clone.body = this.body;
        
        return clone;
    }
}
