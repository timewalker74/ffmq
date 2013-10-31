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
package net.timewalker.ffmq4.common.message;

import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotWriteableException;
import javax.jms.Session;

import net.timewalker.ffmq4.FFMQException;
import net.timewalker.ffmq4.common.destination.DestinationSerializer;
import net.timewalker.ffmq4.common.destination.DestinationTools;
import net.timewalker.ffmq4.common.session.AbstractSession;
import net.timewalker.ffmq4.storage.message.MessageSerializationLevel;
import net.timewalker.ffmq4.utils.EmptyEnumeration;
import net.timewalker.ffmq4.utils.IteratorEnumeration;
import net.timewalker.ffmq4.utils.RawDataBuffer;

/**
 * <p>Base implementation for a JMS message</p>
 */
public abstract class AbstractMessage implements Message
{
	// Persistent properties
	private String id;
	private String correlId;
	private int priority;
	private int deliveryMode;
    private Destination destination;
    private long expiration;
    private boolean redelivered;
    private Destination replyTo;
    private long timestamp;
    private String type;
    private Map<String,Object> propertyMap;

    // Serialization related
    private int unserializationLevel;
    private RawDataBuffer rawMessage;
    
    // Volatile properties
    private boolean propertiesAreReadOnly;
    protected boolean bodyIsReadOnly; 
    private transient WeakReference<AbstractSession> sessionRef; // Weak link to the parent session
    private transient boolean internalCopy = false;
    
    /**
     * Constructor
     */
    public AbstractMessage()
    {
        super();
    }
    
    /**
     * Get the raw message form if available
     * @return the raw message form if available
     */
    public final RawDataBuffer getRawMessage()
    {
        return rawMessage;
    }
    
    /**
     * Create an independant copy of this message
     */
    public abstract AbstractMessage copy();
    
    /**
     * Create an independant copy of this message
     */
    protected final void copyCommonFields( AbstractMessage clone )
    {
        clone.id = this.id;
        clone.correlId = this.correlId;
        clone.priority = this.priority;
        clone.deliveryMode = this.deliveryMode;
        clone.destination = this.destination;
        clone.expiration = this.expiration;
        clone.redelivered = this.redelivered;
        clone.replyTo = this.replyTo;
        clone.timestamp = this.timestamp;
        clone.type = this.type;            
        clone.propertyMap = this.propertyMap != null ? (Map<String,Object>)((HashMap<String,Object>)this.propertyMap).clone() : null;
        
        // Copy raw message cache if any
        clone.unserializationLevel = this.unserializationLevel; 
        if (this.rawMessage != null)
        	clone.rawMessage = this.rawMessage.copy();
    }
    
    /**
     * Set the message session
     */
    public final void setSession( AbstractSession session ) throws JMSException
    {
        if (session == null)
            this.sessionRef = null;
        else
        {
        	// Consistency check
        	if (sessionRef != null && sessionRef.get() != session)
        		throw new FFMQException("Message session already set","CONSISTENCY");
        		
            this.sessionRef = new WeakReference<>(session);
        }
    }
    
    /**
     * Get the parent session
     */
    protected final AbstractSession getSession() throws JMSException
    {
        if (sessionRef == null)
            throw new FFMQException("Message has no associated session","CONSISTENCY");
        
        AbstractSession session = sessionRef.get();
        if (session == null)
            throw new FFMQException("Message session is no longer valid","CONSISTENCY");
        
        return session;
    }
    
    /* (non-Javadoc)
     * @see javax.jms.Message#acknowledge()
     */
    @Override
	public final void acknowledge() throws JMSException
    {
        AbstractSession session = getSession();
        
        int acknowledgeMode = session.getAcknowledgeMode();
        if (acknowledgeMode != Session.CLIENT_ACKNOWLEDGE)
            return; // Ignore [JMS SPEC]
        
        session.acknowledge();       
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#clearProperties()
     */
    @Override
	public final void clearProperties()
    {
        if (propertyMap != null) propertyMap.clear();
        propertiesAreReadOnly = false;
    }
    
    /* (non-Javadoc)
     * @see javax.jms.Message#getBooleanProperty(java.lang.String)
     */
    @Override
	public final boolean getBooleanProperty(String name) throws JMSException
    {
        return MessageConvertTools.asBoolean(getProperty(name));
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getByteProperty(java.lang.String)
     */
    @Override
	public final byte getByteProperty(String name) throws JMSException
    {
        return MessageConvertTools.asByte(getProperty(name));
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getDoubleProperty(java.lang.String)
     */
    @Override
	public final double getDoubleProperty(String name) throws JMSException
    {
        return MessageConvertTools.asDouble(getProperty(name));
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getFloatProperty(java.lang.String)
     */
    @Override
	public final float getFloatProperty(String name) throws JMSException
    {
        return MessageConvertTools.asFloat(getProperty(name));
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getIntProperty(java.lang.String)
     */
    @Override
	public final int getIntProperty(String name) throws JMSException
    {
        return MessageConvertTools.asInt(getProperty(name));
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getJMSCorrelationID()
     */
    @Override
	public final String getJMSCorrelationID()
    {
    	assertDeserializationLevel(MessageSerializationLevel.ALL_HEADERS);
        return correlId;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getJMSCorrelationIDAsBytes()
     */
    @Override
	public final byte[] getJMSCorrelationIDAsBytes()
    {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getJMSDeliveryMode()
     */
    @Override
	public final int getJMSDeliveryMode()
    {
        return deliveryMode;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getJMSDestination()
     */
    @Override
	public final Destination getJMSDestination()
    {
        return destination;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getJMSExpiration()
     */
    @Override
	public final long getJMSExpiration()
    {
        return expiration;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getJMSMessageID()
     */
    @Override
	public final String getJMSMessageID()
    {
        return id != null ? "ID:"+id : null;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getJMSPriority()
     */
    @Override
	public final int getJMSPriority()
    {
        return priority;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getJMSRedelivered()
     */
    @Override
	public final boolean getJMSRedelivered()
    {
        return redelivered;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getJMSReplyTo()
     */
    @Override
	public final Destination getJMSReplyTo()
    {
    	assertDeserializationLevel(MessageSerializationLevel.ALL_HEADERS);
        return replyTo;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getJMSTimestamp()
     */
    @Override
	public final long getJMSTimestamp()
    {
    	assertDeserializationLevel(MessageSerializationLevel.ALL_HEADERS);
        return timestamp;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getJMSType()
     */
    @Override
	public final String getJMSType()
    {
    	assertDeserializationLevel(MessageSerializationLevel.ALL_HEADERS);
        return type;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getLongProperty(java.lang.String)
     */
    @Override
	public final long getLongProperty(String name) throws JMSException
    {
        return MessageConvertTools.asLong(getProperty(name));
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getObjectProperty(java.lang.String)
     */
    @Override
	public final Object getObjectProperty(String name)
    {
        return getProperty(name);
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getShortProperty(java.lang.String)
     */
    @Override
	public final short getShortProperty(String name) throws JMSException
    {
        return MessageConvertTools.asShort(getProperty(name));
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getStringProperty(java.lang.String)
     */
    @Override
	public final String getStringProperty(String name) throws JMSException
    {
        return MessageConvertTools.asString(getProperty(name));
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#propertyExists(java.lang.String)
     */
    @Override
	public final boolean propertyExists(String name)
    {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Empty property name");
        
        assertDeserializationLevel(MessageSerializationLevel.ALL_HEADERS);
        return propertyMap != null ? propertyMap.containsKey(name) : false;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setBooleanProperty(java.lang.String, boolean)
     */
    @Override
	public final void setBooleanProperty(String name, boolean value) throws JMSException
    {
        setProperty(name,Boolean.valueOf(value));
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setByteProperty(java.lang.String, byte)
     */
    @Override
	public final void setByteProperty(String name, byte value) throws JMSException
    {
        setProperty(name,Byte.valueOf(value));
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setDoubleProperty(java.lang.String, double)
     */
    @Override
	public final void setDoubleProperty(String name, double value) throws JMSException
    {
        setProperty(name,new Double(value));
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setFloatProperty(java.lang.String, float)
     */
    @Override
	public final void setFloatProperty(String name, float value) throws JMSException
    {
        setProperty(name,new Float(value));
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setIntProperty(java.lang.String, int)
     */
    @Override
	public final void setIntProperty(String name, int value) throws JMSException
    {
        setProperty(name,Integer.valueOf(value));
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setJMSCorrelationID(java.lang.String)
     */
    @Override
	public final void setJMSCorrelationID(String correlationID)
    {
    	assertDeserializationLevel(MessageSerializationLevel.FULL);
        this.correlId = correlationID;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setJMSCorrelationIDAsBytes(byte[])
     */
    @Override
	public final void setJMSCorrelationIDAsBytes(byte[] correlationID)
    {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setJMSDeliveryMode(int)
     */
    @Override
	public final void setJMSDeliveryMode(int deliveryMode) throws JMSException
    {
        if (deliveryMode != DeliveryMode.PERSISTENT &&
            deliveryMode != DeliveryMode.NON_PERSISTENT)
            throw new FFMQException("Invalid delivery mode : "+deliveryMode,"INVALID_DELIVERY_MODE");
            
        assertDeserializationLevel(MessageSerializationLevel.FULL);
        this.deliveryMode = deliveryMode;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setJMSDestination(javax.jms.Destination)
     */
    @Override
	public final void setJMSDestination(Destination destination) throws JMSException
    {
    	assertDeserializationLevel(MessageSerializationLevel.FULL);
        this.destination = DestinationTools.asRef(destination);
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setJMSExpiration(long)
     */
    @Override
	public final void setJMSExpiration(long expiration)
    {
    	assertDeserializationLevel(MessageSerializationLevel.FULL);
        this.expiration = expiration;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setJMSMessageID(java.lang.String)
     */
    @Override
	public final void setJMSMessageID(String id) throws JMSException
    {
    	assertDeserializationLevel(MessageSerializationLevel.FULL);
        this.id = id;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setJMSPriority(int)
     */
    @Override
	public final void setJMSPriority(int priority) throws JMSException
    {
        if (priority < 0 || priority > 9)
            throw new FFMQException("Invalid priority value : "+priority,"INVALID_PRIORITY");
            
        assertDeserializationLevel(MessageSerializationLevel.FULL);
        this.priority = priority;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setJMSRedelivered(boolean)
     */
    @Override
	public final void setJMSRedelivered(boolean redelivered)
    {
        this.redelivered = redelivered;
        
        // Update raw cache accordingly
        if (rawMessage != null)
        {
        	byte flags = rawMessage.readByte(1);
        	if (redelivered)
        		flags = (byte)(flags | (1 << 4));
        	else
        		flags = (byte)(flags & ~(1 << 4));
        	
        	rawMessage.writeByte(flags, 1);
        }
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setJMSReplyTo(javax.jms.Destination)
     */
    @Override
	public final void setJMSReplyTo(Destination replyTo)
    {
    	assertDeserializationLevel(MessageSerializationLevel.FULL);
        this.replyTo = replyTo;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setJMSTimestamp(long)
     */
    @Override
	public final void setJMSTimestamp(long timestamp)
    {
    	assertDeserializationLevel(MessageSerializationLevel.FULL);
        this.timestamp = timestamp;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setJMSType(java.lang.String)
     */
    @Override
	public final void setJMSType(String type)
    {
    	assertDeserializationLevel(MessageSerializationLevel.FULL);
        this.type = type;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setLongProperty(java.lang.String, long)
     */
    @Override
	public final void setLongProperty(String name, long value) throws JMSException
    {
        setProperty(name,Long.valueOf(value));
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setObjectProperty(java.lang.String, java.lang.Object)
     */
    @Override
	public final void setObjectProperty(String name, Object value) throws JMSException
    {
        if (value == null)
            throw new MessageFormatException("A property value cannot be null");
            
        // Check type
        if (!(value instanceof Boolean ||
              value instanceof Byte ||
              value instanceof Short ||
              value instanceof Integer ||
              value instanceof Long ||
              value instanceof Float ||
              value instanceof Double ||
              value instanceof String))
            throw new MessageFormatException("Unsupported property value type : "+value.getClass().getName());
        
        setProperty(name,value);
    }

    private void setProperty( String name , Object value ) throws JMSException
    {
        if (propertiesAreReadOnly)
            throw new MessageNotWriteableException("Message properties are read-only");

        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Empty property name");
        
        assertDeserializationLevel(MessageSerializationLevel.FULL);
        
        if (propertyMap == null)
        	propertyMap = new HashMap<>(17);
        propertyMap.put(name, value);
    }
    
    private Object getProperty(String name)
    {
    	assertDeserializationLevel(MessageSerializationLevel.ALL_HEADERS);
    	return propertyMap != null ? propertyMap.get(name) : null;
    }
    
    /* (non-Javadoc)
     * @see javax.jms.Message#setShortProperty(java.lang.String, short)
     */
    @Override
	public final void setShortProperty(String name, short value) throws JMSException
    {
        setProperty(name,Short.valueOf(value));
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setStringProperty(java.lang.String, java.lang.String)
     */
    @Override
	public final void setStringProperty(String name, String value) throws JMSException
    {
        setProperty(name,value);
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.Message#getPropertyNames()
     */
    @Override
	public final Enumeration<String> getPropertyNames()
    {
    	assertDeserializationLevel(MessageSerializationLevel.ALL_HEADERS);
    	
    	if (propertyMap == null)
    		return new EmptyEnumeration<>();
    		
        return new IteratorEnumeration<>(propertyMap.keySet().iterator());
    }
    
    /**
     * Test if this message is an internal copy
     */
    public final boolean isInternalCopy() 
    {
		return internalCopy;
	}

    /**
     * Set if this message is an internal copy
     */
	public final void setInternalCopy(boolean copy) 
	{
		this.internalCopy = copy;
	}

	/**
     * Get the type value for this message
     */
    protected abstract byte getType();
    
    /**
     * Write the message content to the given output stream
     */
    protected final void serializeTo( RawDataBuffer out )
    {
    	// Level 1 - Always unserialized
    	byte lvl1Flags = (byte)((priority & 0x0F)+
    	                        (redelivered ? (1 << 4) : 0)+
    	                        (deliveryMode == DeliveryMode.PERSISTENT ? (1 << 5) : 0)+
    	                        (expiration != 0 ? (1 << 6) : 0)+ // Expiration value present
    	                        (id != null ? (1 << 7) : 0)); // ID value present
    	out.writeByte(lvl1Flags);
    	if (expiration != 0)
    		out.writeLong(expiration);
    	if (id != null)
    		out.writeUTF(id);
    	DestinationSerializer.serializeTo(destination, out);
    	
        // Level 2 - Unserialized if required by a message selector
    	byte lvl2Flags = (byte)((correlId != null ? (1 << 0) : 0)+
    	                        (replyTo != null ? (1 << 1) : 0)+
    	                        (timestamp != 0 ? (1 << 2) : 0)+
    	                        (type != null ? (1 << 3) : 0)+
    	                        (propertyMap != null && !propertyMap.isEmpty() ? (1 << 4) : 0));
    	out.writeByte(lvl2Flags);
    	if (correlId != null)
    		out.writeUTF(correlId);
    	if (replyTo != null)
    		DestinationSerializer.serializeTo(replyTo, out);
    	if (timestamp != 0)
    		out.writeLong(timestamp);
    	if (type != null)
    		out.writeUTF(type);
    	if (propertyMap != null && !propertyMap.isEmpty())
    		writeMapTo(propertyMap, out);
        
        // Level 3 - Body - Only unserialized on the client side
        serializeBodyTo(out);
    }
    
    /**
     * Initialize the message from the given raw data
     */
    protected final void initializeFromRaw( RawDataBuffer rawMessage )
    {
    	this.rawMessage = rawMessage;
    	this.unserializationLevel = MessageSerializationLevel.BASE_HEADERS;
    	
    	// Only deserialize level 1 headers
    	byte lvl1Flags = rawMessage.readByte();
    	priority     = lvl1Flags & 0x0F;
    	redelivered  = (lvl1Flags & (1 << 4)) != 0;
    	deliveryMode = (lvl1Flags & (1 << 5)) != 0 ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT;
    	if ((lvl1Flags & (1 << 6)) != 0) expiration = rawMessage.readLong();
    	if ((lvl1Flags & (1 << 7)) != 0) id = rawMessage.readUTF();
    	destination = DestinationSerializer.unserializeFrom(rawMessage);
    }
    
    public final synchronized void ensureDeserializationLevel( int targetLevel )
    {
    	if (rawMessage == null)
    		return; // Not a serialized message or fully deserialized message 
    	
    	while (unserializationLevel < targetLevel)
    	{
    		if (unserializationLevel == MessageSerializationLevel.BASE_HEADERS)
    		{
    			// Read level 2 headers
    			byte lvl2Flags = rawMessage.readByte();
    			if ((lvl2Flags & (1 << 0)) != 0) correlId = rawMessage.readUTF();
    			if ((lvl2Flags & (1 << 1)) != 0) replyTo = DestinationSerializer.unserializeFrom(rawMessage);
    			if ((lvl2Flags & (1 << 2)) != 0) timestamp = rawMessage.readLong();
    			if ((lvl2Flags & (1 << 3)) != 0) type = rawMessage.readUTF();
    			if ((lvl2Flags & (1 << 4)) != 0) propertyMap = readMapFrom(rawMessage);
    			
    			unserializationLevel = MessageSerializationLevel.ALL_HEADERS;
    		}
    		else
			if (unserializationLevel == MessageSerializationLevel.ALL_HEADERS)
    		{	
				// Read level 3 - message body
				unserializeBodyFrom(rawMessage);
				unserializationLevel = MessageSerializationLevel.FULL;
				rawMessage = null; // Save memory
    		}
    	}
    }
    
    protected final synchronized void assertDeserializationLevel( int targetLevel )
    {
    	if (rawMessage == null)
    		return; // Not a serialized message or fully deserialized message 
    	
    	if (unserializationLevel < targetLevel)
    		throw new IllegalStateException("Message is not deserialized (level="+unserializationLevel+")");
    }
    
    public final void markAsReadOnly()
    {
    	propertiesAreReadOnly = true;
        bodyIsReadOnly = true;
    }
    
    protected abstract void serializeBodyTo( RawDataBuffer out );
    protected abstract void unserializeBodyFrom( RawDataBuffer in );
    
    /**
     * Write a map to the given output stream
     */
    protected final void writeMapTo( Map<String,Object> map , RawDataBuffer out )
    {
    	if (map == null)
    	{
    		out.writeInt(0);
    		return;
    	}
    	
        out.writeInt(map.size());
        if (!map.isEmpty())
        {
            Iterator<Map.Entry<String,Object>> allEntries = map.entrySet().iterator();
            while (allEntries.hasNext())
            {
                Map.Entry<String,Object> entry = allEntries.next();
                out.writeUTF(entry.getKey());
                out.writeGeneric(entry.getValue());
            }
        }
    }
    
    /**
     * Write a map to the given output stream
     */
    protected final Map<String,Object> readMapFrom( RawDataBuffer in )
    {
        int mapSize = in.readInt();
        if (mapSize == 0)
        	return null;
        
        Map<String,Object> map = new HashMap<>(Math.max(17,mapSize*4/3));
        for (int n = 0 ; n < mapSize ; n++)
        {
            String propName = in.readUTF();
            Object propValue = in.readGeneric();
            map.put(propName,propValue);
        }
        
        return map;
    }
    
    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString()
    {
       StringBuffer sb = new StringBuffer();
       
       sb.append("[messageId=");
       sb.append(id);
       sb.append(" priority=");
       sb.append(priority);
       sb.append(" correlId=");
       sb.append(correlId);
       sb.append(" deliveryMode=");
       sb.append(deliveryMode);
       sb.append(" destination=");
       sb.append(destination);
       sb.append(" expiration=");
       sb.append(expiration);
       sb.append(" redelivered=");
       sb.append(redelivered);
       sb.append(" replyTo=");
       sb.append(replyTo);
       sb.append(" timestamp=");
       sb.append(timestamp);
       sb.append(" type=");
       sb.append(type);
       
       if (propertyMap != null && propertyMap.size() > 0)
       {
           sb.append(" properties=");
           Iterator<String> allProps = propertyMap.keySet().iterator();
           int count = 0;
           while (allProps.hasNext())
           {
               String propName = allProps.next();
               Object propValue = propertyMap.get(propName);
               if (count++ > 0)
            	   sb.append(",");
               sb.append(propName);
               sb.append("=");
               sb.append(propValue);
           }
       }
       sb.append("]");
       
       return sb.toString();
    }
}
