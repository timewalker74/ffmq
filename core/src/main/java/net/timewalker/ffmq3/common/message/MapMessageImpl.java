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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotWriteableException;

import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.storage.message.MessageSerializationLevel;
import net.timewalker.ffmq3.utils.ArrayTools;
import net.timewalker.ffmq3.utils.EmptyEnumeration;
import net.timewalker.ffmq3.utils.IteratorEnumeration;
import net.timewalker.ffmq3.utils.RawDataBuffer;

/**
 * <p>Implementation of a {@link MapMessage}</p>
 */
public final class MapMessageImpl extends AbstractMessage implements MapMessage
{
    private Map<String,Object> body;
    
    /**
     * Constructor
     */
    public MapMessageImpl()
    {
        super();
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.AbstractMessage#getType()
     */
    @Override
	protected byte getType()
    {
        return MessageType.MAP;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.AbstractMessage#unserializeBodyFrom(net.timewalker.ffmq3.utils.RawDataInputStream)
     */
    @Override
	protected void unserializeBodyFrom(RawDataBuffer in)
    {
    	this.body = readMapFrom(in);
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.AbstractMessage#serializeBodyTo(net.timewalker.ffmq3.utils.RawDataBuffer)
     */
    @Override
	protected void serializeBodyTo(RawDataBuffer out)
    {
    	writeMapTo(body, out);
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.MapMessage#getBoolean(java.lang.String)
     */
    @Override
	public boolean getBoolean(String name) throws JMSException
    {
        return MessageConvertTools.asBoolean(getObject(name));
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.MapMessage#getByte(java.lang.String)
     */
    @Override
	public byte getByte(String name) throws JMSException
    {
        return MessageConvertTools.asByte(getObject(name));
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.MapMessage#getBytes(java.lang.String)
     */
    @Override
	public byte[] getBytes(String name) throws JMSException
    {
        return MessageConvertTools.asBytes(getObject(name));
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.MapMessage#getChar(java.lang.String)
     */
    @Override
	public char getChar(String name) throws JMSException
    {
        return MessageConvertTools.asChar(getObject(name));
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.MapMessage#getDouble(java.lang.String)
     */
    @Override
	public double getDouble(String name) throws JMSException
    {
        return MessageConvertTools.asDouble(getObject(name));
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.MapMessage#getFloat(java.lang.String)
     */
    @Override
	public float getFloat(String name) throws JMSException
    {
        return MessageConvertTools.asFloat(getObject(name));
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.MapMessage#getInt(java.lang.String)
     */
    @Override
	public int getInt(String name) throws JMSException
    {
        return MessageConvertTools.asInt(getObject(name));
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.MapMessage#getLong(java.lang.String)
     */
    @Override
	public long getLong(String name) throws JMSException
    {
        return MessageConvertTools.asLong(getObject(name));
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.MapMessage#getMapNames()
     */
    @Override
	public Enumeration<String> getMapNames()
    {
    	if (body == null)
    		return new EmptyEnumeration<>();
    	
        return new IteratorEnumeration<>(body.keySet().iterator());
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.MapMessage#getObject(java.lang.String)
     */
    @Override
	public Object getObject(String name) throws JMSException
    {
        if (name == null || name.length() == 0)
            throw new FFMQException("Object name cannot be null or empty","INVALID_OBJECT_NAME");
            
        return body != null ? body.get(name) : null;
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.MapMessage#getShort(java.lang.String)
     */
    @Override
	public short getShort(String name) throws JMSException
    {
        return MessageConvertTools.asShort(getObject(name));
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.MapMessage#getString(java.lang.String)
     */
    @Override
	public String getString(String name) throws JMSException
    {
        return MessageConvertTools.asString(getObject(name));
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.MapMessage#itemExists(java.lang.String)
     */
    @Override
	public boolean itemExists(String name) throws JMSException
    {
        if (name == null || name.length() == 0)
            throw new FFMQException("Object name cannot be null or empty","INVALID_OBJECT_NAME");
        
        return body != null ? body.containsKey(name) : false;
    }

    private Object put(String name,Object value) throws JMSException
    {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Item name cannot be null");
        
        if (bodyIsReadOnly)
            throw new MessageNotWriteableException("Message body is read-only");
        
        assertDeserializationLevel(MessageSerializationLevel.FULL);
        if (body == null)
        	body = new HashMap<>();
        	
        return body.put(name,value);
    }
    
    /*
     * (non-Javadoc)
     * @see javax.jms.MapMessage#setBoolean(java.lang.String, boolean)
     */
    @Override
	public void setBoolean(String name, boolean value) throws JMSException
    {
        put(name,Boolean.valueOf(value));
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.MapMessage#setByte(java.lang.String, byte)
     */
    @Override
	public void setByte(String name, byte value) throws JMSException
    {
        put(name,new Byte(value));
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.MapMessage#setBytes(java.lang.String, byte[])
     */
    @Override
	public void setBytes(String name, byte[] value) throws JMSException
    {
        put(name,value != null ? value.clone() : null);
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.MapMessage#setBytes(java.lang.String, byte[], int, int)
     */
    @Override
	public void setBytes(String name, byte[] value, int offset, int length) throws JMSException
    {
        if (value != null)
        {
            byte[] reducedValue = new byte[length];
            System.arraycopy(value, offset, reducedValue, 0, length);
            put(name,reducedValue);
        }
        else
            put(name,null);
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.MapMessage#setChar(java.lang.String, char)
     */
    @Override
	public void setChar(String name, char value) throws JMSException
    {
        put(name,new Character(value));
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.MapMessage#setDouble(java.lang.String, double)
     */
    @Override
	public void setDouble(String name, double value) throws JMSException
    {
        put(name,new Double(value));
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.MapMessage#setFloat(java.lang.String, float)
     */
    @Override
	public void setFloat(String name, float value) throws JMSException
    {
        put(name,new Float(value));
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.MapMessage#setInt(java.lang.String, int)
     */
    @Override
	public void setInt(String name, int value) throws JMSException
    {
        put(name,new Integer(value));
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.MapMessage#setLong(java.lang.String, long)
     */
    @Override
	public void setLong(String name, long value) throws JMSException
    {
        put(name,new Long(value));
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.MapMessage#setObject(java.lang.String, java.lang.Object)
     */
    @Override
	public void setObject(String name, Object value) throws JMSException
    {
        if (value != null)
        {
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
            
            if (value instanceof byte[])
            	value = ArrayTools.copy((byte[])value); // [JMS Spec]
            
            put(name,value);
        }
        else
            put(name,null);
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.MapMessage#setShort(java.lang.String, short)
     */
    @Override
	public void setShort(String name, short value) throws JMSException
    {
        put(name,new Short(value));
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.MapMessage#setString(java.lang.String, java.lang.String)
     */
    @Override
	public void setString(String name, String value) throws JMSException
    {
        put(name,value);
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.Message#clearBody()
     */
    @Override
	public void clearBody()
    {
    	assertDeserializationLevel(MessageSerializationLevel.FULL);
        if (body != null) body.clear();
        bodyIsReadOnly = false;
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.AbstractMessage#copy()
     */
    @Override
	public AbstractMessage copy()
    {
        MapMessageImpl clone = new MapMessageImpl();
        copyCommonFields(clone);
        if (this.body != null)
        {
        	clone.body = new HashMap<>();
        	clone.body.putAll(this.body);
        }
        
        return clone;
    } 
}
