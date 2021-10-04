/* 
 * ===================================================================
 * This document and/or file is OVERKIZ property. All information
 * it contains is strictly confidential. This document and/or file
 * shall not be used, reproduced or passed on in any way, in full
 * or in part without OVERKIZ prior written approval.
 * All rights reserved.
 * ===================================================================
 */
package net.timewalker.ffmq4.test.local.selector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import net.timewalker.ffmq4.test.AbstractQueuerTest;

/**
 * MessageSelectionTest
 */
public class MessageSelectionTest extends AbstractQueuerTest
{
	private static long WAIT_TIMEOUT = 100;
	
	private static int TEST_AMOUNT = 200000;
	
	public void testIndexingScalability() throws Exception
	{
		Connection connection = createConnection();
		connection.start();
		
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		
		String key = "idx1";
		
		final AtomicLong received = new AtomicLong();
		final Semaphore sem = new Semaphore(0);
		
		List<MessageConsumer> consumers = new ArrayList<>();
		for(int n=0;n<5000;n++)
		{
			MessageConsumer consumer = session.createConsumer(vtopic1, key+"='foo"+n+"'");
			consumer.setMessageListener(new MessageListener() {
				
				@Override
				public void onMessage(Message message)
				{
					received.incrementAndGet();
					sem.release();
				}
			});
			consumers.add(consumer);
		}
		
		Random rand = new Random();
		for(int k=0;k<10;k++)
		{
    		long startTime = System.currentTimeMillis();
    		MessageProducer producer = session.createProducer(vtopic1);
    		for(int n=0;n<TEST_AMOUNT;n++)
    		{
    			TextMessage msg = session.createTextMessage("test"+n);
    			msg.setStringProperty(key, "foo"+rand.nextInt(5000));
    			
    			producer.send(msg,DeliveryMode.NON_PERSISTENT,Message.DEFAULT_PRIORITY,Message.DEFAULT_TIME_TO_LIVE);
    		}
		
    		sem.acquire(TEST_AMOUNT);
    		long endTime = System.currentTimeMillis();
    		System.out.println(endTime-startTime);
		}
		
		session.close();
		connection.close();
		
		assertNull(lastConnectionFailure);
	}
	
	public void testTopicSelectionWithoutIndex() throws Exception
	{
		testTopicSelection(false);
	}
	
	public void testTopicSelectionWithIndex() throws Exception
	{
		testTopicSelection(true);
	}
	
	private void testTopicSelection( boolean useIndex ) throws Exception
	{
		Connection connection = createConnection();
		connection.start();
		
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		
		String key1 = useIndex ? "idx1" : "key1";
		String key2 = useIndex ? "idx2" : "key2";
		
		MessageConsumer consumer1 = session.createConsumer(topic1, key1+"='foo'");
		MessageConsumer consumer2 = session.createConsumer(topic1, key1+"='bar'");
		MessageConsumer consumer3 = session.createConsumer(topic1, key2+"='aa'");
		MessageConsumer consumer4 = session.createConsumer(topic1, key1+"='foo' and "+key2+"='x'");
		MessageConsumer consumer5 = session.createConsumer(topic1, key1+" in ('foo','bar')");
		MessageConsumer consumerAll = session.createConsumer(topic1);
		
		MessageProducer producer = session.createProducer(topic1);
		TextMessage msg = session.createTextMessage("test");
		msg.setStringProperty(key1, "foo");
		producer.send(msg);
		
		Message recvMessage;
		recvMessage = consumerAll.receive(WAIT_TIMEOUT);
		assertNotNull(recvMessage);
		recvMessage = consumer1.receive(WAIT_TIMEOUT);
		assertNotNull(recvMessage);
		recvMessage = consumer2.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer3.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer4.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer5.receive(WAIT_TIMEOUT);
		assertNotNull(recvMessage);
		
		msg = session.createTextMessage("test");
		msg.setStringProperty(key1, "bar");
		producer.send(msg);
		
		recvMessage = consumerAll.receive(WAIT_TIMEOUT);
		assertNotNull(recvMessage);
		recvMessage = consumer1.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer2.receive(WAIT_TIMEOUT);
		assertNotNull(recvMessage);
		recvMessage = consumer3.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer4.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer5.receive(WAIT_TIMEOUT);
		assertNotNull(recvMessage);
		
		msg = session.createTextMessage("test");
		msg.setStringProperty(key2, "aa");
		producer.send(msg);
		
		recvMessage = consumerAll.receive(WAIT_TIMEOUT);
		assertNotNull(recvMessage);
		recvMessage = consumer1.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer2.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer3.receive(WAIT_TIMEOUT);
		assertNotNull(recvMessage);
		recvMessage = consumer4.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer5.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		
		msg = session.createTextMessage("test");
		msg.setStringProperty(key2, "bbb");
		producer.send(msg);
		
		recvMessage = consumerAll.receive(WAIT_TIMEOUT);
		assertNotNull(recvMessage);
		recvMessage = consumer1.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer2.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer3.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer4.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer5.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		
		msg = session.createTextMessage("test");
		msg.setStringProperty(key1, "foo");
		msg.setStringProperty(key2, "x");
		producer.send(msg);
		
		recvMessage = consumerAll.receive(WAIT_TIMEOUT);
		assertNotNull(recvMessage);
		recvMessage = consumer1.receive(WAIT_TIMEOUT);
		assertNotNull(recvMessage);
		recvMessage = consumer2.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer3.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer4.receive(WAIT_TIMEOUT);
		assertNotNull(recvMessage);
		recvMessage = consumer5.receive(WAIT_TIMEOUT);
		assertNotNull(recvMessage);
		
		msg = session.createTextMessage("test");
		msg.setStringProperty("a", "sss");
		producer.send(msg);
		
		recvMessage = consumerAll.receive(WAIT_TIMEOUT);
		assertNotNull(recvMessage);
		recvMessage = consumer1.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer2.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer3.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer4.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer5.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		
		msg = session.createTextMessage("test");
		producer.send(msg);
		
		recvMessage = consumerAll.receive(WAIT_TIMEOUT);
		assertNotNull(recvMessage);
		recvMessage = consumer1.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer2.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer3.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer4.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer5.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		
		consumer1.close();
		consumer2.close();
		consumer3.close();
		consumer4.close();
		consumer5.close();
		consumerAll.close();
		
		session.close();
		connection.close();
		
		assertNull(lastConnectionFailure);
	}
	
	public void testCorrelIdSelectionWithIndex() throws Exception
	{
		testCorrelIdSelection(true);
	}
	
	public void testCorrelIdSelectionWithoutIndex() throws Exception
	{
		testCorrelIdSelection(false);
	}
	
	public void testCorrelIdSelection( boolean useIndex ) throws Exception
	{
		Connection connection = createConnection();
		connection.start();
		
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		
		Topic topic =  useIndex ? topic3 : topic1;
		
		MessageConsumer consumer1 = session.createConsumer(topic, "JMSCorrelationID='foo'");
		MessageConsumer consumer2 = session.createConsumer(topic, "JMSCorrelationID='bar'");
		MessageConsumer consumer3 = session.createConsumer(topic, "JMSCorrelationID='foo' and t='x'");
		MessageConsumer consumer4 = session.createConsumer(topic, "JMSCorrelationID in ('foo','xyz')");
		MessageConsumer consumerAll = session.createConsumer(topic);
		
		MessageProducer producer = session.createProducer(topic);
		TextMessage msg = session.createTextMessage("test");
		msg.setJMSCorrelationID("foo");
		producer.send(msg);
		
		Message recvMessage;
		recvMessage = consumerAll.receive(WAIT_TIMEOUT);
		assertNotNull(recvMessage);
		recvMessage = consumer1.receive(WAIT_TIMEOUT);
		assertNotNull(recvMessage);
		recvMessage = consumer2.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer3.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer4.receive(WAIT_TIMEOUT);
		assertNotNull(recvMessage);
		
		msg = session.createTextMessage("test");
		msg.setJMSCorrelationID("bar");
		producer.send(msg);
		
		recvMessage = consumerAll.receive(WAIT_TIMEOUT);
		assertNotNull(recvMessage);
		recvMessage = consumer1.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer2.receive(WAIT_TIMEOUT);
		assertNotNull(recvMessage);
		recvMessage = consumer3.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer4.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
	
		msg = session.createTextMessage("test");
		msg.setJMSCorrelationID("bbb");
		producer.send(msg);
		
		recvMessage = consumerAll.receive(WAIT_TIMEOUT);
		assertNotNull(recvMessage);
		recvMessage = consumer1.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer2.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer3.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer4.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		
		msg = session.createTextMessage("test");
		msg.setJMSCorrelationID("foo");
		msg.setStringProperty("t", "x");
		producer.send(msg);
		
		recvMessage = consumerAll.receive(WAIT_TIMEOUT);
		assertNotNull(recvMessage);
		recvMessage = consumer1.receive(WAIT_TIMEOUT);
		assertNotNull(recvMessage);
		recvMessage = consumer2.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer3.receive(WAIT_TIMEOUT);
		assertNotNull(recvMessage);
		recvMessage = consumer4.receive(WAIT_TIMEOUT);
		assertNotNull(recvMessage);
		
		msg = session.createTextMessage("test");
		msg.setStringProperty("a", "sss");
		producer.send(msg);
		
		recvMessage = consumerAll.receive(WAIT_TIMEOUT);
		assertNotNull(recvMessage);
		recvMessage = consumer1.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer2.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer3.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer4.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		
		msg = session.createTextMessage("test");
		producer.send(msg);
		
		recvMessage = consumerAll.receive(WAIT_TIMEOUT);
		assertNotNull(recvMessage);
		recvMessage = consumer1.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer2.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer3.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		recvMessage = consumer4.receive(WAIT_TIMEOUT);
		assertNull(recvMessage);
		
		consumer1.close();
		consumer2.close();
		consumer3.close();
		consumerAll.close();
		
		session.close();
		connection.close();
		
		assertNull(lastConnectionFailure);
	}
	
	public void testIndexCleanup() throws Exception
	{
		Connection connection = createConnection();
		connection.start();
		
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		
		Topic topic =  topic3;
		
		
		assertEquals(0, engine.getLocalTopic(topic.getTopicName()).getSubscriptionsCount());
		assertEquals(0, engine.getLocalTopic(topic.getTopicName()).getIndexedSubscriptionsCount());
		
		MessageConsumer consumer1 = session.createConsumer(topic, "JMSCorrelationID='foo'");
		
		assertEquals(1, engine.getLocalTopic(topic.getTopicName()).getSubscriptionsCount());
		assertEquals(1, engine.getLocalTopic(topic.getTopicName()).getIndexedSubscriptionsCount());
		
		consumer1.close();
		
		// ----
		
		assertEquals(0, engine.getLocalTopic(topic.getTopicName()).getSubscriptionsCount());
		assertEquals(0, engine.getLocalTopic(topic.getTopicName()).getIndexedSubscriptionsCount());
		
		consumer1 = session.createConsumer(topic, "JMSCorrelationID in ('foo','bar')");
		
		assertEquals(1, engine.getLocalTopic(topic.getTopicName()).getSubscriptionsCount());
		assertEquals(1, engine.getLocalTopic(topic.getTopicName()).getIndexedSubscriptionsCount());
		
		consumer1.close();
		
		assertEquals(0, engine.getLocalTopic(topic.getTopicName()).getSubscriptionsCount());
		assertEquals(0, engine.getLocalTopic(topic.getTopicName()).getIndexedSubscriptionsCount());
		
		// ----
		
		assertEquals(0, engine.getLocalTopic(topic.getTopicName()).getSubscriptionsCount());
		assertEquals(0, engine.getLocalTopic(topic.getTopicName()).getIndexedSubscriptionsCount());
		
		consumer1 = session.createConsumer(topic, "JMSCorrelationID in ('foo','foo')");
		
		assertEquals(1, engine.getLocalTopic(topic.getTopicName()).getSubscriptionsCount());
		assertEquals(1, engine.getLocalTopic(topic.getTopicName()).getIndexedSubscriptionsCount());
		
		consumer1.close();
		
		assertEquals(0, engine.getLocalTopic(topic.getTopicName()).getSubscriptionsCount());
		assertEquals(0, engine.getLocalTopic(topic.getTopicName()).getIndexedSubscriptionsCount());
				
		
		session.close();
		connection.close();
		
		assertNull(lastConnectionFailure);
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq4.test.AbstractQueuerTest#isRemote()
	 */
	@Override
	protected boolean isRemote()
	{
		return false;
	}
}
