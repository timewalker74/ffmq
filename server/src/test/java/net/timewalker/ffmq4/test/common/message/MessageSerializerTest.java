package net.timewalker.ffmq3.test.common.message;

import junit.framework.TestCase;
import net.timewalker.ffmq3.common.message.AbstractMessage;
import net.timewalker.ffmq3.common.message.MessageSerializer;
import net.timewalker.ffmq3.test.utils.factory.MessageCreator;
import net.timewalker.ffmq3.utils.RawDataBuffer;

/**
 * MessageSerializerTest
 */
public class MessageSerializerTest extends TestCase
{
    private static final int MSG_SIZE = 100;
    
    private byte[] serialize( AbstractMessage message ) throws Exception
    {
        RawDataBuffer buffer = new RawDataBuffer(1024);
        MessageSerializer.serializeTo(message, buffer);
        return buffer.toByteArray();
    }
    
    private AbstractMessage unserialize( byte[] data ) throws Exception
    {
        RawDataBuffer buffer = new RawDataBuffer(data);
        return MessageSerializer.unserializeFrom(buffer, false);
    }
    
//    private void assertEquals( byte[] data1 , byte[] data2 )
//    {
//        assertEquals(data1.length,data2.length);
//        for (int n = 0 ; n < data1.length ; n++)
//            assertEquals(data1[n], data2[n]);
//    }
    
//    private void dump( byte[] data )
//    {
//        System.out.print("{");
//        for (int n = 0 ; n < data.length ; n++)
//            System.out.print(" "+data[n]);
//        System.out.println(" }");
//    }
    
    private long sum( byte[] data )
    {
        long sum = 0;
        for (int n = 0 ; n < data.length ; n++)
            sum += data[n];
        return sum;
    }
    
    private void consistencyTest( AbstractMessage msg ) throws Exception
    {
        byte[] data1 = serialize(msg);
        AbstractMessage msg2 = unserialize(data1);
        byte[] data2 = serialize(msg2);
//        System.out.println(msg);
//        System.out.println(msg2);
//        dump(data1);
//        dump(data2);
        assertEquals(sum(data1),sum(data2));
    }
    
    public void testEmptyMessageSerialization() throws Exception
    {
        consistencyTest(MessageCreator.createEmptyMessage());
    }
    
    public void testBytesMessageSerialization() throws Exception
    {
        consistencyTest(MessageCreator.createBytesMessage(MSG_SIZE));
    }
    
    public void testMapMessageSerialization() throws Exception
    {
        consistencyTest(MessageCreator.createMapMessage(MSG_SIZE));
    }
    
    public void testObjectMessageSerialization() throws Exception
    {
        consistencyTest(MessageCreator.createObjectMessage(MSG_SIZE));
    }
    
    public void testStreamMessageSerialization() throws Exception
    {
        consistencyTest(MessageCreator.createStreamMessage(MSG_SIZE));
    }
    
    public void testTextMessageSerialization() throws Exception
    {
        consistencyTest(MessageCreator.createTextMessage(MSG_SIZE));
    }
}
