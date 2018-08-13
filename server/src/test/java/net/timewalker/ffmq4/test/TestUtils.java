package net.timewalker.ffmq4.test;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.naming.Context;
import javax.naming.NamingException;

import net.timewalker.ffmq4.FFMQConstants;
import net.timewalker.ffmq4.transport.PacketTransportType;
import net.timewalker.ffmq4.utils.JNDITools;

/**
 * TestUtils
 */
public class TestUtils
{
    private static final String USERNAME = "test";
    private static final String PASSWORD = "test";
    private static final String CONNECTION_FACTORY = FFMQConstants.JNDI_CONNECTION_FACTORY_NAME;
    private static final String QUEUE_CONNECTION_FACTORY = FFMQConstants.JNDI_QUEUE_CONNECTION_FACTORY_NAME;
    private static final String TOPIC_CONNECTION_FACTORY = FFMQConstants.JNDI_TOPIC_CONNECTION_FACTORY_NAME;
    
    public static final int VOLUME_TEST_SIZE = 3000;
    public static final int VOLUME_TEST_MSGSIZE = 1024*4;
    public static final int TEST_TIMEOUT = 30; // seconds
    
    //public static final int CONSUMER_ROLLBACK_RATE = 0;
    public static final int CONSUMER_ROLLBACK_RATE = 19;
    
    public static final int TEST_ITERATIONS = 5;
    public static final boolean INTERACTIVE_MODE = false;
    
    public static final boolean USE_EXTERNAL_SERVER = false;
    
    public static final boolean USE_SAFE_MODE = true;
    public static final int DELIVERY_MODE = DeliveryMode.NON_PERSISTENT;
    public static final int PRIORITY = -1;  // Use -1 for random
    
    public static final int TEST_SERVER_PORT = FFMQConstants.DEFAULT_SERVER_PORT;
    
    public static final String LOCAL_ENGINE_NAME = "engine1";
    public static final String TCP_TRANSPORT_URI = PacketTransportType.TCP+"://"+FFMQConstants.DEFAULT_SERVER_HOST+":"+TEST_SERVER_PORT;
    private static final String VM_TRANSPORT_URI = PacketTransportType.VM+"://"+LOCAL_ENGINE_NAME;
    
    private static QueueConnectionFactory getQueueConnectionFactory( String transportURI ) throws JMSException
    {
        try
        {
            Context context = JNDITools.getContext(FFMQConstants.JNDI_CONTEXT_FACTORY,transportURI,null);
            return (QueueConnectionFactory)context.lookup(QUEUE_CONNECTION_FACTORY);
        }
        catch (NamingException e)
        {
            throw new JMSException("JNDI error : "+e.getMessage());
        }
    }
    
    private static TopicConnectionFactory getTopicConnectionFactory( String transportURI ) throws JMSException
    {
        try
        {
            Context context = JNDITools.getContext(FFMQConstants.JNDI_CONTEXT_FACTORY,transportURI,null);
            return (TopicConnectionFactory)context.lookup(TOPIC_CONNECTION_FACTORY);
        }
        catch (NamingException e)
        {
            throw new JMSException("JNDI error : "+e.getMessage());
        }
    }
    
    private static ConnectionFactory getConnectionFactory( String transportURI ) throws JMSException
    {
        try
        {
            Context context = JNDITools.getContext(FFMQConstants.JNDI_CONTEXT_FACTORY,transportURI,null);
            return (ConnectionFactory)context.lookup(CONNECTION_FACTORY);
        }
        catch (NamingException e)
        {
            throw new JMSException("JNDI error : "+e.getMessage());
        }
    }
    
    public static QueueConnection openLocalQueueConnection() throws JMSException
    {
        return getQueueConnectionFactory(VM_TRANSPORT_URI).createQueueConnection(USERNAME,PASSWORD);
    }
    
    public static TopicConnection openLocalTopicConnection() throws JMSException
    {
        return getTopicConnectionFactory(VM_TRANSPORT_URI).createTopicConnection(USERNAME,PASSWORD);
    }
    
    public static Connection openLocalConnection() throws JMSException
    {
        return getConnectionFactory(VM_TRANSPORT_URI).createConnection(USERNAME,PASSWORD);
    }
    
    public static QueueConnection openRemoteQueueConnection() throws JMSException
    {
        return getQueueConnectionFactory(TCP_TRANSPORT_URI).createQueueConnection(USERNAME,PASSWORD);
    }
    
    public static TopicConnection openRemoteTopicConnection() throws JMSException
    {
        return getTopicConnectionFactory(TCP_TRANSPORT_URI).createTopicConnection(USERNAME,PASSWORD);
    }
    
    public static Connection openRemoteConnection() throws JMSException
    {
        return getConnectionFactory(TCP_TRANSPORT_URI).createConnection(USERNAME,PASSWORD);
    }
    
    public static void dumpThreads()
    {
    	// JAVA 1.4 ?
//        Map threadMap = Thread.getAllStackTraces();
//        Iterator threads = threadMap.keySet().iterator();
//        System.err.println("------------------------------------------------");
//        while (threads.hasNext())
//        {
//            Thread thread = (Thread)threads.next();
//            StackTraceElement[] stackTrace = (StackTraceElement[])threadMap.get(thread);
//            
//            System.err.println();
//            System.err.println(thread+" ["+thread.getState()+"]");
//            for (int i = 0 ; i < stackTrace.length ; i++)
//                System.err.println("  at "+stackTrace[i]);
//        }
//        System.err.println("------------------------------------------------");
    }
    
    private static final Object LOCK = new Object();
    public static void hang()
    {
    	synchronized (LOCK)
		{
    		try
			{
				LOCK.wait();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
    }
}
