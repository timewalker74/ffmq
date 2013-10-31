package net.timewalker.ffmq4.test.utils.factory;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;

import net.timewalker.ffmq4.common.destination.QueueRef;
import net.timewalker.ffmq4.common.message.AbstractMessage;
import net.timewalker.ffmq4.common.message.BytesMessageImpl;
import net.timewalker.ffmq4.common.message.EmptyMessageImpl;
import net.timewalker.ffmq4.common.message.MapMessageImpl;
import net.timewalker.ffmq4.common.message.ObjectMessageImpl;
import net.timewalker.ffmq4.common.message.StreamMessageImpl;
import net.timewalker.ffmq4.common.message.TextMessageImpl;
import net.timewalker.ffmq4.utils.id.UUIDProvider;

/**
 * MessageCreator
 */
public class MessageCreator
{
    private static void setDummyProperties( AbstractMessage msg ) throws JMSException
    {
    	msg.setJMSPriority(5);
    	msg.setJMSRedelivered(false);
    	msg.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
    	msg.setJMSExpiration(System.currentTimeMillis()+100000);
    	
        msg.setJMSMessageID(UUIDProvider.getInstance().getUUID());
        msg.setJMSCorrelationID("CORREL-"+UUIDProvider.getInstance().getUUID());
        msg.setJMSReplyTo(new QueueRef("dummy"));
        msg.setBooleanProperty("boolean", true);
        msg.setStringProperty("string", "foobar");
        msg.setByteProperty("byte", (byte)1);
        msg.setShortProperty("short", (short)2);
        msg.setIntProperty("int", 3);
        msg.setLongProperty("long", 4);
        msg.setFloatProperty("float", 1.23f);
        msg.setDoubleProperty("double", 4.56);
    }
    
    private static byte[] createDummyByteArray( int size )
    {
        byte[] array = new byte[size];
        for (int n = 0 ; n < size ; n++)
            array[n] = (byte)n;
        return array;
    }
    
    private static String createDummyString( int size )
    {
        StringBuffer sb = new StringBuffer(size);
        while (sb.length() < size)
            sb.append("foobar");
        return sb.toString();
    }
    
    public static EmptyMessageImpl createEmptyMessage() throws JMSException
    {
        EmptyMessageImpl msg = new EmptyMessageImpl();
        setDummyProperties(msg);
        return msg;
    }
    
    public static BytesMessageImpl createBytesMessage( int size ) throws JMSException
    {
        BytesMessageImpl msg = new BytesMessageImpl();
        setDummyProperties(msg);
        
        msg.writeBoolean(true);
        msg.writeUTF("foobar");
        msg.writeChar('c');
        msg.writeByte((byte)1);
        msg.writeShort((short)2);
        msg.writeInt(3);
        msg.writeLong(4);
        msg.writeFloat(1.23f);
        msg.writeDouble(4.56);
        msg.writeBytes(createDummyByteArray(size));
        
        return msg;
    } 
    
    public static MapMessageImpl createMapMessage( int size ) throws JMSException
    {
        MapMessageImpl msg = new MapMessageImpl();
        setDummyProperties(msg);
        
        msg.setBoolean("boolean", true);
        msg.setString("string", "foobar");
        msg.setChar("char", 'c');
        msg.setByte("byte", (byte)1);
        msg.setShort("short", (short)2);
        msg.setInt("int", 3);
        msg.setLong("long", 4);
        msg.setFloat("float", 1.23f);
        msg.setDouble("double", 4.56);
        msg.setBytes("bytearray", createDummyByteArray(size));
        
        return msg;
    } 
    
    public static ObjectMessageImpl createObjectMessage( int size ) throws JMSException
    {
        ObjectMessageImpl msg = new ObjectMessageImpl();
        setDummyProperties(msg);
        
        msg.setObject(createDummyByteArray(size));
        
        return msg;
    } 
    
    public static StreamMessageImpl createStreamMessage( int size ) throws JMSException
    {
        StreamMessageImpl msg = new StreamMessageImpl();
        setDummyProperties(msg);
        
        msg.writeBoolean(true);
        msg.writeString("foobar");
        msg.writeChar('c');
        msg.writeByte((byte)1);
        msg.writeShort((short)2);
        msg.writeInt(3);
        msg.writeLong(4);
        msg.writeFloat(1.23f);
        msg.writeDouble(4.56);
        msg.writeBytes(createDummyByteArray(size));
        
        return msg;
    } 
    
    public static TextMessageImpl createTextMessage( int size ) throws JMSException
    {
        TextMessageImpl msg = new TextMessageImpl();
        setDummyProperties(msg);

        msg.setText(createDummyString(size));
        
        return msg;
    }
}
