package net.timewalker.ffmq4.test;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.Topic;
import javax.jms.TopicConnection;

import net.timewalker.ffmq4.common.destination.QueueRef;
import net.timewalker.ffmq4.common.destination.TopicRef;
import net.timewalker.ffmq4.local.destination.LocalQueue;
import net.timewalker.ffmq4.local.destination.LocalTopic;
import net.timewalker.ffmq4.test.utils.CommTestParameters;
import net.timewalker.ffmq4.test.utils.factory.DummyMessageFactory;
import net.timewalker.ffmq4.test.utils.factory.TextMessageFactory;
import net.timewalker.ffmq4.test.utils.queue.QueueListenerThread;
import net.timewalker.ffmq4.test.utils.queue.QueueReceiverThread;
import net.timewalker.ffmq4.test.utils.queue.QueueSenderThread;
import net.timewalker.ffmq4.test.utils.topic.TopicListenerThread;
import net.timewalker.ffmq4.test.utils.topic.TopicPublisherThread;
import net.timewalker.ffmq4.test.utils.topic.TopicSubscriberThread;
import net.timewalker.ffmq4.utils.JavaTools;
import net.timewalker.ffmq4.utils.StringTools;
import net.timewalker.ffmq4.utils.concurrent.SynchronizationPoint;

/**
 * AbstractCommTest
 */
@SuppressWarnings("all")
public abstract class AbstractCommTest extends AbstractQueuerTest
{   
    //---------------------------------------------------------------------------
    protected abstract boolean useMultipleConnections();
    protected abstract boolean isTopicTest();
    protected abstract boolean isListenerTest();
    //---------------------------------------------------------------------------
    
    private NumberFormat rateFormat = new DecimalFormat("###,###.###",new DecimalFormatSymbols(Locale.FRENCH));
    
    private static final DummyMessageFactory[] MSG_FACTORIES = {
//      new EmptyMessageFactory(),
//      new BytesMessageFactory(),
//      new MapMessageFactory(),
//      new ObjectMessageFactory(),
//      new StreamMessageFactory(),
      new TextMessageFactory()
    };
    
    /**
     * Best effort to terminate a misbehaving thread
     * @deprecated
     */
    private void terminateThread( Thread thread )
    {
        int retries = 0;
        while (thread.isAlive() && retries++ < 5)
        {
            try
            {
                thread.join(5*1000);
            }
            catch (InterruptedException e)
            {
                System.out.println("Wait was interrupted.");
            }
            thread.interrupt();
        }
        if (thread.isAlive())
        {
            System.err.println("Cannot properly terminate thread : "+thread);
            StackTraceElement[] stack = thread.getStackTrace();
            if (stack != null)
                for(int n=0;n<stack.length;n++)
                    System.err.println(" "+stack[n]);
            thread.stop();
        }
    }
    
    protected void singleQueueConnectionReceiverTest( CommTestParameters params , DummyMessageFactory msgFactory ) throws Exception
    {
    	QueueConnection connection = null;
    	try
    	{
	        Queue queue = new QueueRef(params.destinationName);
	        
	        SynchronizationPoint startSynchro = new SynchronizationPoint();
	        connection = createQueueConnection();
	        
	        // Start receivers
	        QueueReceiverThread[] receivers = new QueueReceiverThread[params.receiverCount];
	        for (int n = 0 ; n < receivers.length ; n++)
	            receivers[n] = new QueueReceiverThread("Receiver"+(n+1),
	                                                   startSynchro,
	                                                   connection,
	                                                   params.receiverTransacted,
	                                                   params.acknowledgeMode,
	                                                   queue,
	                                                   null);
	        for (int n = 0 ; n < receivers.length ; n++)
	        {
	            receivers[n].start();
	            receivers[n].waitForStartup();
	        }
	        
	        // Start senders
	        QueueSenderThread[] senders = new QueueSenderThread[params.senderCount];
	        int totalExpected = 0;
	        for (int n = 0 ; n < senders.length ; n++)
	        {
	            senders[n] = new QueueSenderThread("Sender"+(n+1),
	                                               msgFactory,
	                                               startSynchro,
	                                               connection,
	                                               params.senderTransacted,
	                                               params.messageCount/params.senderCount,
	                                               params.messageSize,
	                                               params.minDelay,
	                                               params.maxDelay,
	                                               queue,
	                                               params.deliveryMode,
	                                               params.priority,
	                                               params.timeToLive);
	            totalExpected += params.messageCount/params.senderCount;
	        }
	        for (int n = 0 ; n < senders.length ; n++)
	        {
	            senders[n].start();
	            senders[n].waitForStartup();
	        }
	        
	        connection.start();
	        
	        long startTime = System.currentTimeMillis();
	        startSynchro.reach();
	        
	        // Wait for senders to complete
	        for (int n = 0 ; n < senders.length ; n++)
	        {
	            senders[n].join(TestUtils.TEST_TIMEOUT*1000);
	            senders[n].close();
	            terminateThread(senders[n]);
	        }
 
	        // Wait for expected messages
	        long waitStart = System.currentTimeMillis();
	        while (System.currentTimeMillis()-waitStart < TestUtils.TEST_TIMEOUT*1000)
	        {
	            int totalReceived = 0;
	            for (int n = 0 ; n < receivers.length ; n++)
	                totalReceived += receivers[n].getReceivedCount();
	            if (totalReceived >= totalExpected)
	                break;
	            Thread.sleep(20);
	        }
	        
	        long endTime = System.currentTimeMillis();
	        double rate = (double)totalExpected*1000/(endTime-startTime);
	        System.out.println((endTime-startTime)+" ms ("+rateFormat.format(rate)+" msg/s)");

	        int totalReceived = 0;
	        for (int n = 0 ; n < receivers.length ; n++)
	            totalReceived += receivers[n].getReceivedCount();
	
	        // Close receivers
	        for (int n = 0 ; n < receivers.length ; n++)
	            receivers[n].close();
	        for (int n = 0 ; n < receivers.length ; n++)
	            terminateThread(receivers[n]);

	        // Close connection
	        connection.close();
	        
	        // Check for errors
	        for (int n = 0 ; n < receivers.length ; n++)
	            assertFalse(receivers[n].isInError());
	        for (int n = 0 ; n < senders.length ; n++)
	            assertFalse(senders[n].isInError());
	        
	        // Check received message count
	        if (totalExpected != totalReceived)
	        {
	        	System.out.println("Expected : "+totalExpected);
	        	System.out.println("Received : "+totalReceived);
	        	fail("Some messages were not received !");
	        }
	
	        // Check for remaining messages
	        LocalQueue localQueue = engine.getLocalQueue(params.destinationName);
	        assertEquals(0,localQueue.getSize());
    	}
    	catch (Exception e)
    	{
    		if (connection != null)
    			connection.close();
    			
    		throw e;
    	}
    	
    	if (lastConnectionFailure != null)
    	    fail(lastConnectionFailure.toString());
    }
    
    protected void singleQueueConnectionListenerTest( CommTestParameters params , DummyMessageFactory msgFactory  ) throws Exception
    {
    	QueueConnection listenerConnection = null;
    	QueueConnection producerConnection = null;
    	try
    	{
	        Queue queue = new QueueRef(params.destinationName);
	        
	        SynchronizationPoint startSynchro = new SynchronizationPoint();
	        listenerConnection = createQueueConnection();
	        producerConnection = createQueueConnection();
	        
	        // Start receivers
	        QueueListenerThread[] receivers = new QueueListenerThread[params.receiverCount];
	        for (int n = 0 ; n < receivers.length ; n++)
	            receivers[n] = new QueueListenerThread("Receiver"+(n+1),
	                                                   startSynchro,
	                                                   listenerConnection,
	                                                   params.receiverTransacted,
	                                                   params.acknowledgeMode,
	                                                   queue,
	                                                   null);
	        for (int n = 0 ; n < receivers.length ; n++)
	        {
	            receivers[n].start();
	            receivers[n].waitForStartup();
	        }
	        
	        // Start senders
	        QueueSenderThread[] senders = new QueueSenderThread[params.senderCount];
	        int totalExpected = 0;
	        for (int n = 0 ; n < senders.length ; n++)
	        {
	            senders[n] = new QueueSenderThread("Sender"+(n+1),
	                                               msgFactory,
	                                               startSynchro,
	                                               producerConnection,
	                                               params.senderTransacted,
	                                               params.messageCount/params.senderCount,
	                                               params.messageSize,
	                                               params.minDelay,
	                                               params.maxDelay,
	                                               queue,
	                                               params.deliveryMode,
	                                               params.priority,
	                                               params.timeToLive);
	            totalExpected += params.messageCount/params.senderCount;
	        }
	        for (int n = 0 ; n < senders.length ; n++)
	        {
	            senders[n].start();
	            senders[n].waitForStartup();
	        }
	        
	        listenerConnection.start();
	        
	        long startTime = System.currentTimeMillis();
	        startSynchro.reach();
	        
	        // Wait for senders to complete
	        for (int n = 0 ; n < senders.length ; n++)
	            senders[n].join(TestUtils.TEST_TIMEOUT*1000);
	        for (int n = 0 ; n < senders.length ; n++)
	            terminateThread(senders[n]);
	        
	        // Wait for expected messages
	        long waitStart = System.currentTimeMillis();
	        while (System.currentTimeMillis()-waitStart < TestUtils.TEST_TIMEOUT*1000 && lastConnectionFailure == null)
	        {
	            int totalReceived = 0;
	            for (int n = 0 ; n < receivers.length ; n++)
	                totalReceived += receivers[n].getReceivedCount();
	            if (totalReceived >= totalExpected)
	                break;
	            Thread.sleep(100);
	        }
	        
	        // When in non-transacted mode, the ack is sent _after_ message processing so we need to wait a bit
	        LocalQueue localQueue = engine.getLocalQueue(params.destinationName);
	        if (!params.receiverTransacted)
	        {
	        	while (localQueue.getSize() > 0 && lastConnectionFailure == null)
	        		Thread.sleep(10);
	        }
	        
	        long endTime = System.currentTimeMillis();
	        double rate = (double)totalExpected*1000/(endTime-startTime);
	        System.out.println((endTime-startTime)+" ms ("+rateFormat.format(rate)+" msg/s)");

	        int totalReceived = 0;
	        for (int n = 0 ; n < receivers.length ; n++)
	            totalReceived += receivers[n].getReceivedCount();
	
	        // Close receivers
	        for (int n = 0 ; n < receivers.length ; n++)
	            receivers[n].close();
	        for (int n = 0 ; n < receivers.length ; n++)
	            terminateThread(receivers[n]);

	        // Close connection
	        listenerConnection.close();
	        producerConnection.close();
	        
	        // Check for errors
	        for (int n = 0 ; n < receivers.length ; n++)
	            assertFalse(receivers[n].isInError());
	        for (int n = 0 ; n < senders.length ; n++)
	            assertFalse(senders[n].isInError());
	        
	        // Check received message count
	        if (totalExpected != totalReceived)
	        {
	        	System.out.println("Expected : "+totalExpected);
	        	System.out.println("Received : "+totalReceived);
	        	fail("Some messages were not received !");
	        }
	        
	        // Check for remaining messages
            assertEquals(0,localQueue.getSize());
    	}
    	catch (Exception e)
    	{
    		if (listenerConnection != null)
    			listenerConnection.close();
    		if (producerConnection != null)
    			producerConnection.close();
    		
    		throw e;
    	}
    }
    
    protected void multiQueueConnectionReceiverTest( CommTestParameters params , DummyMessageFactory msgFactory ) throws Exception
    {
    	QueueConnection[] receiverConnections = null;
    	QueueConnection[] sendersConnections = null;
    	try
    	{
	        Queue queue = new QueueRef(params.destinationName);
	        
	        SynchronizationPoint startSynchro = new SynchronizationPoint();
	        receiverConnections = new QueueConnection[params.receiverCount];
	        for (int n = 0 ; n < receiverConnections.length ; n++)
	            receiverConnections[n] = createQueueConnection();
	        
	        // Start receivers
	        QueueReceiverThread[] receivers = new QueueReceiverThread[params.receiverCount];
	        for (int n = 0 ; n < receivers.length ; n++)
	            receivers[n] = new QueueReceiverThread("Receiver"+(n+1),
	                                                   startSynchro,
	                                                   receiverConnections[n],
	                                                   params.receiverTransacted,
	                                                   params.acknowledgeMode,
	                                                   queue,
	                                                   null);
	        for (int n = 0 ; n < receivers.length ; n++)
	        {
	            receivers[n].start();
	            receivers[n].waitForStartup();
	        }
	        
	        sendersConnections = new QueueConnection[params.senderCount];
	        for (int n = 0 ; n < sendersConnections.length ; n++)
	            sendersConnections[n] = createQueueConnection();
	        
	        // Start senders
	        QueueSenderThread[] senders = new QueueSenderThread[params.senderCount];
	        int totalExpected = 0;
	        for (int n = 0 ; n < senders.length ; n++)
	        {
	            senders[n] = new QueueSenderThread("Sender"+(n+1),
	                                               msgFactory,
	                                               startSynchro,
	                                               sendersConnections[n],
	                                               params.senderTransacted,
	                                               params.messageCount/params.senderCount,
	                                               params.messageSize,
	                                               params.minDelay,
	                                               params.maxDelay,
	                                               queue,
	                                               params.deliveryMode,
	                                               params.priority,
	                                               params.timeToLive);
	            totalExpected += params.messageCount/params.senderCount;
	        }
	        for (int n = 0 ; n < senders.length ; n++)
	        {
	            senders[n].start();
	            senders[n].waitForStartup();
	        }
	        
	        for (int n = 0 ; n < receiverConnections.length ; n++)
	            receiverConnections[n].start();
	        
	        long startTime = System.currentTimeMillis();
	        startSynchro.reach();
	        
	        // Wait for senders to complete
	        for (int n = 0 ; n < senders.length ; n++)
	        {
	            senders[n].join(TestUtils.TEST_TIMEOUT*1000);
	            senders[n].close();
	            terminateThread(senders[n]);
	        }
 
	        // Wait for expected messages
	        long waitStart = System.currentTimeMillis();
	        while (System.currentTimeMillis()-waitStart < TestUtils.TEST_TIMEOUT*1000)
	        {
	            int totalReceived = 0;
	            for (int n = 0 ; n < receivers.length ; n++)
	                totalReceived += receivers[n].getReceivedCount();
	            if (totalReceived >= totalExpected)
	                break;
	            Thread.sleep(100);
	        }
	        
	        long endTime = System.currentTimeMillis();
	        double rate = (double)totalExpected*1000/(endTime-startTime);
	        System.out.println((endTime-startTime)+" ms ("+rateFormat.format(rate)+" msg/s)");

	        int totalReceived = 0;
	        for (int n = 0 ; n < receivers.length ; n++)
	            totalReceived += receivers[n].getReceivedCount();
	
	        // Close receivers
	        for (int n = 0 ; n < receivers.length ; n++)
	            receivers[n].close();
	        for (int n = 0 ; n < receivers.length ; n++)
	            terminateThread(receivers[n]);
	        
	        // Close connection
	        for (int n = 0 ; n < receiverConnections.length ; n++)
	            receiverConnections[n].close();
	        for (int n = 0 ; n < sendersConnections.length ; n++)
	            sendersConnections[n].close();
	        
	        // Check for errors
	        for (int n = 0 ; n < receivers.length ; n++)
	            assertFalse(receivers[n].isInError());
	        for (int n = 0 ; n < senders.length ; n++)
	            assertFalse(senders[n].isInError());
	        
	        // Check received message count
	        if (totalExpected != totalReceived)
	        {
	        	System.out.println("Expected : "+totalExpected);
	        	System.out.println("Received : "+totalReceived);
	        	fail("Some messages were not received !");
	        }
	        
	        // Check for remaining messages
            LocalQueue localQueue = engine.getLocalQueue(params.destinationName);
            assertEquals(0,localQueue.getSize());
    	}
    	catch (Exception e)
    	{
    		if (receiverConnections != null)
    		{
    			for (int i = 0; i < receiverConnections.length; i++)
				{
					if (receiverConnections[i] != null)
						receiverConnections[i].close();
				}
    		}
    		if (sendersConnections != null)
    		{
    			for (int i = 0; i < sendersConnections.length; i++)
				{
					if (sendersConnections[i] != null)
						sendersConnections[i].close();
				}
    		}
    		throw e;
    	}
    }
    
    protected void multiQueueConnectionListenerTest( CommTestParameters params , DummyMessageFactory msgFactory ) throws Exception
    {
    	QueueConnection[] receiverConnections = null;
    	QueueConnection[] sendersConnections = null;
    	try
    	{
	        Queue queue = new QueueRef(params.destinationName);
	        
	        SynchronizationPoint startSynchro = new SynchronizationPoint();
	        receiverConnections = new QueueConnection[params.receiverCount];
	        for (int n = 0 ; n < receiverConnections.length ; n++)
	            receiverConnections[n] = createQueueConnection();
	
	        // Start receivers
	        QueueListenerThread[] receivers = new QueueListenerThread[params.receiverCount];
	        for (int n = 0 ; n < receivers.length ; n++)
	            receivers[n] = new QueueListenerThread("Receiver"+(n+1),
	                                                   startSynchro,
	                                                   receiverConnections[n],
	                                                   params.receiverTransacted,
	                                                   params.acknowledgeMode,
	                                                   queue,
	                                                   null);
	        for (int n = 0 ; n < receivers.length ; n++)
	        {
	            receivers[n].start();
	            receivers[n].waitForStartup();
	        }
	        
	        sendersConnections = new QueueConnection[params.senderCount];
	        for (int n = 0 ; n < sendersConnections.length ; n++)
	            sendersConnections[n] = createQueueConnection();
	        
	        // Start senders
	        QueueSenderThread[] senders = new QueueSenderThread[params.senderCount];
	        int totalExpected = 0;
	        for (int n = 0 ; n < senders.length ; n++)
	        {
	            senders[n] = new QueueSenderThread("Sender"+(n+1),
	                                               msgFactory,
	                                               startSynchro,
	                                               sendersConnections[n],
	                                               params.senderTransacted,
	                                               params.messageCount/params.senderCount,
	                                               params.messageSize,
	                                               params.minDelay,
	                                               params.maxDelay,
	                                               queue,
	                                               params.deliveryMode,
	                                               params.priority,
	                                               params.timeToLive);
	            totalExpected += params.messageCount/params.senderCount;
	        }
	        for (int n = 0 ; n < senders.length ; n++)
	        {
	            senders[n].start();
	            senders[n].waitForStartup();
	        }
	        
	        for (int n = 0 ; n < receiverConnections.length ; n++)
	            receiverConnections[n].start();
	        
	        long startTime = System.currentTimeMillis();
	        startSynchro.reach();
	        
	        // Wait for senders to complete
	        for (int n = 0 ; n < senders.length ; n++)
	            senders[n].join(TestUtils.TEST_TIMEOUT*1000);
	        for (int n = 0 ; n < senders.length ; n++)
	            terminateThread(senders[n]);
	        
	        // Wait for expected messages
	        long waitStart = System.currentTimeMillis();
	        while (System.currentTimeMillis()-waitStart < TestUtils.TEST_TIMEOUT*1000)
	        {
	            int totalReceived = 0;
	            for (int n = 0 ; n < receivers.length ; n++)
	                totalReceived += receivers[n].getReceivedCount();
	            if (totalReceived >= totalExpected)
	                break;
	            Thread.sleep(100);
	        }
	        
	        // When in non-transacted mode, the ack is sent _after_ message processing so we need to wait a bit
	        LocalQueue localQueue = engine.getLocalQueue(params.destinationName);
	        if (!params.receiverTransacted)
	        {
	        	while (localQueue.getSize() > 0)
	        		Thread.sleep(10);
	        }
	        
	        long endTime = System.currentTimeMillis();
	        double rate = (double)totalExpected*1000/(endTime-startTime);
	        System.out.println((endTime-startTime)+" ms ("+rateFormat.format(rate)+" msg/s)");

	        int totalReceived = 0;
	        for (int n = 0 ; n < receivers.length ; n++)
	            totalReceived += receivers[n].getReceivedCount();

	        // Close receivers
	        for (int n = 0 ; n < receivers.length ; n++)
	            receivers[n].close();
	        for (int n = 0 ; n < receivers.length ; n++)
	            receivers[n].join(5*1000);

	        // Close connection
	        for (int n = 0 ; n < receiverConnections.length ; n++)
	            receiverConnections[n].close();
	        for (int n = 0 ; n < sendersConnections.length ; n++)
	            sendersConnections[n].close();
	        
	        // Check for errors
	        for (int n = 0 ; n < receivers.length ; n++)
	            assertFalse(receivers[n].isInError());
	        for (int n = 0 ; n < senders.length ; n++)
	            assertFalse(senders[n].isInError());
	        
	        // Check received message count
	        if (totalExpected != totalReceived)
	        {
	        	System.out.println("Expected : "+totalExpected);
	        	System.out.println("Received : "+totalReceived);
	        	fail("Some messages were not received !");
	        }
	        
	        // Check for remaining messages
            assertEquals(0,localQueue.getSize());
    	}
    	catch (Exception e)
    	{
    		if (receiverConnections != null)
    		{
    			for (int i = 0; i < receiverConnections.length; i++)
				{
					if (receiverConnections[i] != null)
						receiverConnections[i].close();
				}
    		}
    		if (sendersConnections != null)
    		{
    			for (int i = 0; i < sendersConnections.length; i++)
				{
					if (sendersConnections[i] != null)
						sendersConnections[i].close();
				}
    		}
    		throw e;
    	}
    }
    
    private void singleTopicConnectionReceiverTest( CommTestParameters params , DummyMessageFactory msgFactory ) throws Exception
    {
    	TopicConnection connection = null;
    	try
    	{
	        Topic topic = new TopicRef(params.destinationName);
	        
	        SynchronizationPoint startSynchro = new SynchronizationPoint();
	        connection = createTopicConnection();
	        
	        // Start receivers
	        TopicSubscriberThread[] receivers = new TopicSubscriberThread[params.receiverCount];
	        for (int n = 0 ; n < receivers.length ; n++)
	            receivers[n] = new TopicSubscriberThread("Receiver"+(n+1),
	            		                                 startSynchro,
	                                                     connection,
	                                                     params.receiverTransacted,
	                                                     params.acknowledgeMode,
	                                                     topic,
	                                                     null,
	                                                     false);
	        for (int n = 0 ; n < receivers.length ; n++)
	        {
	            receivers[n].start();
	            receivers[n].waitForStartup();
	        }

	        // Start senders
	        TopicPublisherThread[] senders = new TopicPublisherThread[params.senderCount];
	        int totalExpected = params.messageCount*params.receiverCount;
	        for (int n = 0 ; n < senders.length ; n++)
	        {
	            senders[n] = new TopicPublisherThread("Sender"+(n+1),
	                                                  msgFactory,
	                                                  startSynchro,
	                                                  connection,
	                                                  params.senderTransacted,
	                                                  params.messageCount/params.senderCount,
	                                                  params.messageSize,
	                                                  params.minDelay,
	                                                  params.maxDelay,
	                                                  topic,
	                                                  params.deliveryMode,
	                                                  params.priority,
	                                                  params.timeToLive);
	        }
	        for (int n = 0 ; n < senders.length ; n++)
	        {
	            senders[n].start();
	            senders[n].waitForStartup();
	        }
	        connection.start();
	        
	        long startTime = System.currentTimeMillis();
	        startSynchro.reach();
	        
	        // Wait for senders to complete
	        for (int n = 0 ; n < senders.length ; n++)
	        {
	            senders[n].join(TestUtils.TEST_TIMEOUT*1000);
	            senders[n].close();
	            terminateThread(senders[n]);
	        }
	        
	        // Wait for expected messages
	        long waitStart = System.currentTimeMillis();
	        while (System.currentTimeMillis()-waitStart < TestUtils.TEST_TIMEOUT*1000)
	        {
	            int totalReceived = 0;
	            for (int n = 0 ; n < receivers.length ; n++)
	                totalReceived += receivers[n].getReceivedCount();
	            if (totalReceived >= totalExpected)
	                break;
	            Thread.sleep(100);
	        }
	        
	        // When in non-transacted mode, the ack is sent _after_ message processing so we need to wait a bit
	        LocalTopic localTopic = engine.getLocalTopic(params.destinationName);
	        if (!params.receiverTransacted)
	        {
	            while (localTopic.getSize() > 0)
	                Thread.sleep(10);
	        }

	        long endTime = System.currentTimeMillis();
	        double rate = (double)totalExpected*1000/(endTime-startTime);
	        System.out.println((endTime-startTime)+" ms ("+rateFormat.format(rate)+" msg/s)");

	        int totalReceived = 0;
	        for (int n = 0 ; n < receivers.length ; n++)
	            totalReceived += receivers[n].getReceivedCount();
	        
	        int topicSize = localTopic.getSize();
	        if (topicSize > 0)
	        {
	            System.out.println("Expected : "+totalExpected);
	            System.out.println("Received : "+totalReceived);
	            System.out.println(localTopic);
	            System.out.println(localTopic.getConsumersSummary());               
	            TestUtils.dumpThreads();
	            TestUtils.hang();
	        }
	        
	        // Close receivers
	        for (int n = 0 ; n < receivers.length ; n++)
	            receivers[n].close();
	        for (int n = 0 ; n < receivers.length ; n++)
	            terminateThread(receivers[n]);

	        // Close connection
	        connection.close();
	        
	        // Check for errors
	        for (int n = 0 ; n < receivers.length ; n++)
	            assertFalse(receivers[n].isInError());
	        for (int n = 0 ; n < senders.length ; n++)
	            assertFalse(senders[n].isInError());
	        
	        // Check received message count
	        if (totalExpected != totalReceived)
	        {
	        	for (int n = 0 ; n < receivers.length ; n++)
	                System.out.println("["+n+"] "+receivers[n].getReceivedCount());
	        	
	        	System.out.println("Expected : "+totalExpected);
	        	System.out.println("Received : "+totalReceived);
	        	System.out.println("Topic size : "+topicSize);
	        	
	        	fail("Some messages were not received or too many messages received !");
	        }
    	}
    	catch (Exception e)
    	{
    		if (connection != null)
    			connection.close();
    			
    		throw e;
    	}
    }
    
    protected void singleTopicConnectionListenerTest( CommTestParameters params , DummyMessageFactory msgFactory ) throws Exception
    {
        Topic topic = new TopicRef(params.destinationName);
        
        SynchronizationPoint startSynchro = new SynchronizationPoint();
        TopicConnection connection = createTopicConnection();

        // Start receivers
        TopicListenerThread[] receivers = new TopicListenerThread[params.receiverCount];
        for (int n = 0 ; n < receivers.length ; n++)
            receivers[n] = new TopicListenerThread("Receiver"+(n+1),
            		                               startSynchro,
                                                   connection,
                                                   params.receiverTransacted,
                                                   params.acknowledgeMode,
                                                   topic,
                                                   null,
                                                   false);
        for (int n = 0 ; n < receivers.length ; n++)
        {
            receivers[n].start();
            receivers[n].waitForStartup();
        }
        
        // Start senders
        TopicPublisherThread[] senders = new TopicPublisherThread[params.senderCount];
        int totalExpected = params.messageCount*params.receiverCount;
        for (int n = 0 ; n < senders.length ; n++)
        {
            senders[n] = new TopicPublisherThread("Sender"+(n+1),
                                                  msgFactory,
                                                  startSynchro,
                                                  connection,
                                                  params.senderTransacted,
                                                  params.messageCount/params.senderCount,
                                                  params.messageSize,
                                                  params.minDelay,
                                                  params.maxDelay,
                                                  topic,
                                                  params.deliveryMode,
                                                  params.priority,
                                                  params.timeToLive);
        }
        for (int n = 0 ; n < senders.length ; n++)
        {
            senders[n].start();
            senders[n].waitForStartup();
        }
        
        connection.start();

        long startTime = System.currentTimeMillis();
        startSynchro.reach();
        
        // Wait for senders to complete
        for (int n = 0 ; n < senders.length ; n++)
            senders[n].join(TestUtils.TEST_TIMEOUT*1000);
        for (int n = 0 ; n < senders.length ; n++)
            terminateThread(senders[n]);
  
        // Wait for expected messages
        long waitStart = System.currentTimeMillis();
        while (System.currentTimeMillis()-waitStart < TestUtils.TEST_TIMEOUT*1000)
        {
            int totalReceived = 0;
            for (int n = 0 ; n < receivers.length ; n++)
                totalReceived += receivers[n].getReceivedCount();
            if (totalReceived >= totalExpected)
                break;
            Thread.sleep(100);
        }

        // When in non-transacted mode, the ack is sent _after_ message processing so we need to wait a bit
        LocalTopic localTopic = engine.getLocalTopic(params.destinationName);
        if (!params.receiverTransacted)
        {
        	while (localTopic.getSize() > 0)
        		Thread.sleep(10);
        }
        
        long endTime = System.currentTimeMillis();
        double rate = (double)totalExpected*1000/(endTime-startTime);
        System.out.println((endTime-startTime)+" ms ("+rateFormat.format(rate)+" msg/s)");

        int totalReceived = 0;
        for (int n = 0 ; n < receivers.length ; n++)
            totalReceived += receivers[n].getReceivedCount();

        int topicSize = localTopic.getSize();
        if (topicSize > 0)
        {
            System.out.println("Expected : "+totalExpected);
            System.out.println("Received : "+totalReceived);
            System.out.println(localTopic);
            System.out.println(localTopic.getConsumersSummary());               
            TestUtils.dumpThreads();
            TestUtils.hang();
        }
        
        // Close receivers
        for (int n = 0 ; n < receivers.length ; n++)
            receivers[n].close();
        for (int n = 0 ; n < receivers.length ; n++)
            receivers[n].join(5*1000);

        // Close connection
        connection.close();
        
        // Check for errors
        for (int n = 0 ; n < receivers.length ; n++)
            assertFalse(receivers[n].isInError());
        for (int n = 0 ; n < senders.length ; n++)
            assertFalse(senders[n].isInError());
        
        // Check received message count
        if (totalExpected != totalReceived)
        {
        	for (int n = 0 ; n < receivers.length ; n++)
                System.out.println("["+n+"] "+receivers[n].getReceivedCount());
        	
        	System.out.println("Expected : "+totalExpected);
        	System.out.println("Received : "+totalReceived);
        	System.out.println("Topic size : "+topicSize);
        	fail("Some messages were not received or too many messages received !");
        }
    }
    
    protected void multiTopicConnectionReceiverTest( CommTestParameters params , DummyMessageFactory msgFactory ) throws Exception
    {
        Topic topic = new TopicRef(params.destinationName);
        
        SynchronizationPoint startSynchro = new SynchronizationPoint();
        TopicConnection[] receiverConnections = new TopicConnection[params.receiverCount];
        for (int n = 0 ; n < receiverConnections.length ; n++)
            receiverConnections[n] = createTopicConnection();
        
        // Start receivers
        TopicSubscriberThread[] receivers = new TopicSubscriberThread[params.receiverCount];
        for (int n = 0 ; n < receivers.length ; n++)
            receivers[n] = new TopicSubscriberThread("Receiver"+(n+1),
            		                               startSynchro,
                                                   receiverConnections[n],
                                                   params.receiverTransacted,
                                                   params.acknowledgeMode,
                                                   topic,
                                                   null,
                                                   true);
        for (int n = 0 ; n < receivers.length ; n++)
        {
            receivers[n].start();
            receivers[n].waitForStartup();
        }
        
        TopicConnection[] sendersConnections = new TopicConnection[params.senderCount];
        for (int n = 0 ; n < sendersConnections.length ; n++)
            sendersConnections[n] = createTopicConnection();
        
        // Start senders
        TopicPublisherThread[] senders = new TopicPublisherThread[params.senderCount];
        int totalExpected = params.messageCount*params.receiverCount;
        for (int n = 0 ; n < senders.length ; n++)
        {
            senders[n] = new TopicPublisherThread("Sender"+(n+1),
                                                  msgFactory,
                                                  startSynchro,
                                                  sendersConnections[n],
                                                  params.senderTransacted,
                                                  params.messageCount/params.senderCount,
                                                  params.messageSize,
                                                  params.minDelay,
                                                  params.maxDelay,
                                                  topic,
                                                  params.deliveryMode,
                                                  params.priority,
                                                  params.timeToLive);
        }
        for (int n = 0 ; n < senders.length ; n++)
        {
            senders[n].start();
            senders[n].waitForStartup();
        }
        
        for (int n = 0 ; n < receiverConnections.length ; n++)
            receiverConnections[n].start();
        
        long startTime = System.currentTimeMillis();
        startSynchro.reach();
        
        // Wait for senders to complete
        for (int n = 0 ; n < senders.length ; n++)
        {
            senders[n].join(TestUtils.TEST_TIMEOUT*1000);
            senders[n].close();
            terminateThread(senders[n]);
        }

        // Wait for expected messages
        long waitStart = System.currentTimeMillis();
        while (System.currentTimeMillis()-waitStart < TestUtils.TEST_TIMEOUT*1000)
        {
            int totalReceived = 0;
            for (int n = 0 ; n < receivers.length ; n++)
                totalReceived += receivers[n].getReceivedCount();
            if (totalReceived >= totalExpected)
                break;
            Thread.sleep(100);
        }

        // When in non-transacted mode, the ack is sent _after_ message processing so we need to wait a bit
        LocalTopic localTopic = engine.getLocalTopic(params.destinationName);
        if (!params.receiverTransacted)
        {
            while (localTopic.getSize() > 0)
                Thread.sleep(10);
        }
        
        long endTime = System.currentTimeMillis();
        double rate = (double)totalExpected*1000/(endTime-startTime);
        System.out.println((endTime-startTime)+" ms ("+rateFormat.format(rate)+" msg/s)");

        int totalReceived = 0;
        for (int n = 0 ; n < receivers.length ; n++)
            totalReceived += receivers[n].getReceivedCount();

        int topicSize = localTopic.getSize();
        if (topicSize > 0)
        {
            System.out.println("Expected : "+totalExpected);
            System.out.println("Received : "+totalReceived);
            System.out.println(localTopic);
            System.out.println(localTopic.getConsumersSummary());               
            TestUtils.dumpThreads();
            TestUtils.hang();
        }

        // Close receivers
        for (int n = 0 ; n < receivers.length ; n++)
            receivers[n].close();
        for (int n = 0 ; n < receivers.length ; n++)
            terminateThread(receivers[n]);
       
        // Close connection
        for (int n = 0 ; n < receiverConnections.length ; n++)
            receiverConnections[n].close();
        for (int n = 0 ; n < sendersConnections.length ; n++)
            sendersConnections[n].close();
        
        // Check for errors
        for (int n = 0 ; n < receivers.length ; n++)
            assertFalse(receivers[n].isInError());
        for (int n = 0 ; n < senders.length ; n++)
            assertFalse(senders[n].isInError());
        
        // Check received message count
        if (totalExpected != totalReceived)
        {
        	System.out.println("Expected : "+totalExpected);
        	System.out.println("Received : "+totalReceived);
        	System.out.println("Topic size : "+topicSize);
        	fail("Some messages were not received or too many messages received !");
        }
    }
    
    protected void multiTopicConnectionListenerTest( CommTestParameters params , DummyMessageFactory msgFactory ) throws Exception
    {
        Topic topic = new TopicRef(params.destinationName);
        
        SynchronizationPoint startSynchro = new SynchronizationPoint();
        TopicConnection[] receiverConnections = new TopicConnection[params.receiverCount];
        for (int n = 0 ; n < receiverConnections.length ; n++)
            receiverConnections[n] = createTopicConnection();

        // Start receivers
        TopicListenerThread[] receivers = new TopicListenerThread[params.receiverCount];
        for (int n = 0 ; n < receivers.length ; n++)
            receivers[n] = new TopicListenerThread("Receiver"+(n+1),
            		                               startSynchro,
                                                   receiverConnections[n],
                                                   params.receiverTransacted,
                                                   params.acknowledgeMode,
                                                   topic,
                                                   null,
                                                   false);
        for (int n = 0 ; n < receivers.length ; n++)
        {
            receivers[n].start();
            receivers[n].waitForStartup();
        }
        
        TopicConnection[] sendersConnections = new TopicConnection[params.senderCount];
        for (int n = 0 ; n < sendersConnections.length ; n++)
            sendersConnections[n] = createTopicConnection();
        
        // Start senders
        TopicPublisherThread[] senders = new TopicPublisherThread[params.senderCount];
        int totalExpected = params.messageCount*params.receiverCount;
        for (int n = 0 ; n < senders.length ; n++)
        {
            senders[n] = new TopicPublisherThread("Sender"+(n+1),
                                                  msgFactory,
                                                  startSynchro,
                                                  sendersConnections[n],
                                                  params.senderTransacted,
                                                  params.messageCount/params.senderCount,
                                                  params.messageSize,
                                                  params.minDelay,
                                                  params.maxDelay,
                                                  topic,
                                                  params.deliveryMode,
                                                  params.priority,
                                                  params.timeToLive);
        }
        for (int n = 0 ; n < senders.length ; n++)
        {
            senders[n].start();
            senders[n].waitForStartup();
        }
        
        for (int n = 0 ; n < receiverConnections.length ; n++)
            receiverConnections[n].start();
        
        long startTime = System.currentTimeMillis();
        startSynchro.reach();
        
        // Wait for senders to complete
        for (int n = 0 ; n < senders.length ; n++)
            senders[n].join(TestUtils.TEST_TIMEOUT*1000);
        for (int n = 0 ; n < senders.length ; n++)
            terminateThread(senders[n]);

        // Wait for expected messages
        long waitStart = System.currentTimeMillis();
        while (System.currentTimeMillis()-waitStart < TestUtils.TEST_TIMEOUT*1000)
        {
            int totalReceived = 0;
            for (int n = 0 ; n < receivers.length ; n++)
                totalReceived += receivers[n].getReceivedCount();
            if (totalReceived >= totalExpected)
                break;
            Thread.sleep(100);
        }

        // When in non-transacted mode, the ack is sent _after_ message processing so we need to wait a bit
        LocalTopic localTopic = engine.getLocalTopic(params.destinationName);
        if (!params.receiverTransacted)
        {
        	while (localTopic.getSize() > 0)
        		Thread.sleep(10);
        }
        
        long endTime = System.currentTimeMillis();
        double rate = (double)totalExpected*1000/(endTime-startTime);
        System.out.println((endTime-startTime)+" ms ("+rateFormat.format(rate)+" msg/s)");

        int totalReceived = 0;
        for (int n = 0 ; n < receivers.length ; n++)
            totalReceived += receivers[n].getReceivedCount();

        int topicSize = localTopic.getSize();
        if (topicSize > 0)
        {
            System.out.println("Expected : "+totalExpected);
            System.out.println("Received : "+totalReceived);
            System.out.println(localTopic);
            System.out.println(localTopic.getConsumersSummary());               
            TestUtils.dumpThreads();
            TestUtils.hang();
        }

        // Close receivers
        for (int n = 0 ; n < receivers.length ; n++)
            receivers[n].close();
        for (int n = 0 ; n < receivers.length ; n++)
            terminateThread(receivers[n]);

        // Close connection
        for (int n = 0 ; n < receiverConnections.length ; n++)
            receiverConnections[n].close();
        for (int n = 0 ; n < sendersConnections.length ; n++)
            sendersConnections[n].close();
        
        // Check for errors
        for (int n = 0 ; n < receivers.length ; n++)
            assertFalse(receivers[n].isInError());
        for (int n = 0 ; n < senders.length ; n++)
            assertFalse(senders[n].isInError());
        
        // Check received message count
        if (totalExpected != totalReceived)
        {
        	System.out.println("Expected : "+totalExpected);
        	System.out.println("Received : "+totalReceived);
        	System.out.println("Topic size : "+topicSize);
        	fail("Some messages were not received or too many messages received !");
        }
    }
    
    protected void doTest( CommTestParameters params ) throws Exception
    {
        for (int i = 0 ; i < TestUtils.TEST_ITERATIONS; i++)
        {
            for (int n = 0 ; n < MSG_FACTORIES.length ; n++)
            {
                String testName = JavaTools.getCallerMethodName(1);
                System.out.print(StringTools.rightPad(testName+" ["+MSG_FACTORIES[n]+"]",90,' '));
                System.out.flush();
                pressAKey();
                
                if (useMultipleConnections())
                {
                    if (isTopicTest())
                    {
                        if (isListenerTest())
                            multiTopicConnectionListenerTest(params,MSG_FACTORIES[n]);
                        else
                            multiTopicConnectionReceiverTest(params,MSG_FACTORIES[n]);
                    }
                    else
                    {
                        if (isListenerTest())
                            multiQueueConnectionListenerTest(params,MSG_FACTORIES[n]);
                        else
                            multiQueueConnectionReceiverTest(params,MSG_FACTORIES[n]);
                    }
                }
                else
                {
                    if (isTopicTest())
                    {
                        if (isListenerTest())
                            singleTopicConnectionListenerTest(params,MSG_FACTORIES[n]);
                        else
                            singleTopicConnectionReceiverTest(params,MSG_FACTORIES[n]);
                    }
                    else
                    {
                        if (isListenerTest())
                            singleQueueConnectionListenerTest(params,MSG_FACTORIES[n]);
                        else
                            singleQueueConnectionReceiverTest(params,MSG_FACTORIES[n]);
                    }
                }
                
                if (lastConnectionFailure != null)
                    fail(lastConnectionFailure.toString());
                
                System.gc();
                System.runFinalization();
                //Thread.sleep(500);
            }
        }
    }
    
    protected void pressAKey()
    {
        if (TestUtils.INTERACTIVE_MODE)
        {
            System.out.println("Press enter ...");
            try
            {
                while (System.in.available() > 0)
                    System.in.read();
                
                System.in.read();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
