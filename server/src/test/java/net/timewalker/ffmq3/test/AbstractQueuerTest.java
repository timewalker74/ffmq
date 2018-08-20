/* 
 * ===================================================================
 * This document and/or file is OVERKIZ property. All information
 * it contains is strictly confidential. This document and/or file
 * shall not be used, reproduced or passed on in any way, in full
 * or in part without OVERKIZ prior written approval.
 * All rights reserved.
 * ===================================================================
 */
package net.timewalker.ffmq3.test;

import java.io.FileInputStream;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;

import org.apache.log4j.PropertyConfigurator;

import junit.framework.TestCase;
import net.timewalker.ffmq3.FFMQConstants;
import net.timewalker.ffmq3.common.destination.QueueRef;
import net.timewalker.ffmq3.common.destination.TopicRef;
import net.timewalker.ffmq3.listeners.AbstractClientListener;
import net.timewalker.ffmq3.listeners.tcp.io.TcpListener;
import net.timewalker.ffmq3.listeners.tcp.nio.NIOTcpListener;
import net.timewalker.ffmq3.local.FFMQEngine;
import net.timewalker.ffmq3.local.destination.LocalQueue;
import net.timewalker.ffmq3.local.destination.LocalTopic;
import net.timewalker.ffmq3.utils.Settings;

/**
 * AbstractQueuerTest
 * @author spognant
 */
public abstract class AbstractQueuerTest extends TestCase implements ExceptionListener
{
	private static boolean log4jConfigured;
	
	protected FFMQEngine engine;
    protected AbstractClientListener listener;
    protected Queue queue1;
    protected Queue queue2;
    protected Topic topic1;
    protected Topic topic2;
    protected Topic topic3;
    protected Topic vtopic1;
    
	public Throwable lastConnectionFailure;
	
	//---------------------------------------------------------------------------
    protected abstract boolean isRemote();
    //---------------------------------------------------------------------------
    
    /*
     * (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
	protected void setUp() throws Exception
    {
        super.setUp();
        
        lastConnectionFailure = null;
        
        // Force safe mode
        if (TestUtils.USE_SAFE_MODE)
            System.setProperty("ffmq.dataStore.safeMode", "true");
        
        if (TestUtils.USE_EXTERNAL_SERVER)
        {
        	queue1 = new QueueRef("TEST1");
            queue2 = new QueueRef("TEST2");
            topic1 = new TopicRef("TEST1");
            topic2 = new TopicRef("TEST2");
            topic3 = new TopicRef("TEST3");
            vtopic1 = new TopicRef("VTEST1");
            
            // Purge queues first
            Connection connection = createConnection();
            Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer consumer = session.createConsumer(queue1);
            while (consumer.receiveNoWait() != null)
            	continue;
            consumer.close();
            consumer = session.createConsumer(queue2);
            while (consumer.receiveNoWait() != null)
            	continue;
            consumer.close();
            session.commit();
            session.close();
        }
        else
        {
        	// Application home
        	String ffmqHome = System.getProperty("FFMQ_HOME");
        	if (ffmqHome == null)
        	{
        		ffmqHome = "..";
        		System.setProperty("FFMQ_HOME",ffmqHome);
        	}
        	
        	// Application base
        	String ffmqBase = System.getProperty("FFMQ_BASE");
        	if (ffmqBase == null)
        	{
        		ffmqBase = ffmqHome;
        		System.setProperty("FFMQ_BASE",ffmqBase);
        	}
        	
	        Properties testSettings = new Properties();
	        FileInputStream in = new FileInputStream(ffmqBase+"/conf/ffmq-server.properties");
	        testSettings.load(in);
	        in.close();
	        
	        if (!log4jConfigured)
	        {
	        	PropertyConfigurator.configure(testSettings);
	        	log4jConfigured = true;
	        }
	
	        Settings settings = new Settings(testSettings);
	        
	        if (listener != null)
	        {
	            listener.stop();
	            listener = null;
	        }
       
	        try
	        {
	            FFMQEngine.getDeployedInstance(TestUtils.LOCAL_ENGINE_NAME).undeploy();
	        }
	        catch (JMSException e)
	        {
	            // Ignore
	        }
	        
	        engine = new FFMQEngine(TestUtils.LOCAL_ENGINE_NAME,settings,null);
	        engine.deploy();
	        
//	        engine.deleteQueue("TEST1");
//	        engine.deleteQueue("TEST2");
//	        engine.deleteTopic("TEST1");
//	        engine.deleteTopic("TEST2");
	        
	        queue1 = engine.getLocalQueue("TEST1");
	        queue2 = engine.getLocalQueue("TEST2");
	        topic1 = engine.getLocalTopic("TEST1");
	        topic2 = engine.getLocalTopic("TEST2");
	        topic3 = engine.getLocalTopic("TEST3");
	        vtopic1 = engine.getLocalTopic("VTEST1");
	        
	        ((LocalQueue)queue1).purge(null);
	        ((LocalQueue)queue2).purge(null);
	        ((LocalTopic)topic1).resetStats();
	        //topic2.resetStats();
	        
	        if (isRemote())
	        {
	        	boolean useNIO = settings.getBooleanProperty("listener.tcp.useNIO",false);
	        	if (useNIO)
	        	{
		        	listener = new NIOTcpListener(engine,
                        	                      FFMQConstants.DEFAULT_SERVER_HOST,
                        	                      TestUtils.TEST_SERVER_PORT,
                        	                      settings);
	        	}
	        	else
	        	{
		            listener = new TcpListener(engine,
		                                       FFMQConstants.DEFAULT_SERVER_HOST,
		                                       TestUtils.TEST_SERVER_PORT,
		                                       settings);
	        	}
	        	
	            // Start the server thread
	        	try
	        	{
	        		listener.start();
	        	}
	        	catch (JMSException e)
	        	{
	        		if (e.getLinkedException() != null)
	        			throw e.getLinkedException();
	        		else
	        			throw e;
	        	}
	        }
        }
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
	protected void tearDown() throws Exception
    {
    	if (TestUtils.USE_EXTERNAL_SERVER)
        {
    		// Nothing
        }
    	else
    	{
	        if (isRemote())
	        {
	            listener.stop();
	        }

	        engine.undeploy();     
    	}
    	// HELP the GC release mmapped memory
    	engine = null;
    	listener = null;
    	queue1 = null;
    	queue2 = null;
    	topic1 = null;
    	topic2 = null;
        super.tearDown();
    }
	
	protected final Connection createConnection() throws Exception
	{
		Connection connection;
		if (isRemote())
			connection = TestUtils.openRemoteConnection();
		else
			connection = TestUtils.openLocalConnection();

		connection.setExceptionListener(this);

		return connection;
	}
	
	protected final QueueConnection createQueueConnection() throws Exception
    {
        QueueConnection connection;
        if (isRemote())
            connection = TestUtils.openRemoteQueueConnection();
        else
            connection = TestUtils.openLocalQueueConnection();
        
        connection.setExceptionListener(this);
        
        return connection;
    }
    
	protected final TopicConnection createTopicConnection() throws Exception
    {
        TopicConnection connection;
        if (isRemote())
            connection = TestUtils.openRemoteTopicConnection();
        else
            connection = TestUtils.openLocalTopicConnection();
        
        connection.setExceptionListener(this);
        
        return connection;
    }
	
	public void onException(JMSException exception)
    {
        this.lastConnectionFailure = exception;
    }
}
