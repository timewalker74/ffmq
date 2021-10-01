package net.timewalker.ffmq4.test.local.session;

import java.util.Date;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.IllegalStateException;
import javax.jms.InvalidClientIDException;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageEOFException;
import javax.jms.MessageListener;
import javax.jms.MessageNotReadableException;
import javax.jms.MessageNotWriteableException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;

import net.timewalker.ffmq4.FFMQException;
import net.timewalker.ffmq4.storage.data.DataStoreFullException;
import net.timewalker.ffmq4.test.AbstractCommTest;
import net.timewalker.ffmq4.test.TestUtils;
import net.timewalker.ffmq4.utils.id.UUIDProvider;

/**
 * LocalSessionTest
 */
@SuppressWarnings("all")
public class LocalSessionTest extends AbstractCommTest
{
	protected Connection connection;
	protected int counter;
	protected Semaphore listenerLock = new Semaphore(0);
	protected volatile boolean state;
	
	private static final long RECV_TIMEOUT = 150;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		lastConnectionFailure = null;
		connection = isTopicTest() ? createTopicConnection() : createQueueConnection();
		purgeDestination(queue1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception
	{
		if (lastConnectionFailure != null)
			fail(lastConnectionFailure.toString());

		connection.close();
		super.tearDown();
	}

	public void testMessageOrder() throws Exception
	{
		Session session;
		Message msg;
		MessageProducer producer;
		MessageConsumer consumer;

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		counter = 0;
		consumer.setMessageListener(new MessageListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
			 */
			@Override
			public void onMessage(Message message)
			{
				try
				{
					int rank = message.getIntProperty("rank");
					if (counter == 0)
					{
						assertTrue(rank == 1);
					}
					else
					{
						assertTrue(rank == counter+1);
					}
					counter = rank;
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		});
		
		producer = session.createProducer(queue1);
		for (int i = 1; i <= 500; i++)
		{
			if (i == 100)
				connection.start();
		
			msg = session.createMessage();
			msg.setIntProperty("rank", i);
			producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		}
		
		int loops = 0;
		while (counter < 500 && loops++ < 200)
			Thread.sleep(100);
		System.out.println(loops+" "+counter);
		
		session.close();

		assertTrue(counter == 500);
	}
	
	public void testSetClientID() throws Exception
	{
		synchronized (LocalSessionTest.class)
		{
			try
			{
				connection.getClientID();
			}
			catch (InvalidClientIDException e)
			{
				assertTrue(e.getMessage().indexOf("not set") != -1);
			}

			connection.setClientID("foo");

			try
			{
				connection.setClientID("bar");
				fail("Should have failed");
			}
			catch (IllegalStateException e)
			{
				assertTrue(e.getMessage().indexOf("already set") != -1);
			}

			assertEquals("foo", connection.getClientID());

			Connection connection = createConnection();
			try
			{
				connection.setClientID("foo");
				fail("Should have failed");
			}
			catch (InvalidClientIDException e)
			{
				// Ok
			}
			connection.close();
		}

		if (lastConnectionFailure != null)
			fail(lastConnectionFailure.toString());
	}

	public void testDurableConsumer() throws Exception
	{
		Session session;
		Message msg;
		MessageProducer producer;
		MessageConsumer consumer;

		connection.setClientID("its_me");
		connection.start();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		producer = session.createProducer(topic1);

		consumer = session.createDurableSubscriber(topic1, "durable_sub");
		assertNull(consumer.receive(RECV_TIMEOUT));

		msg = session.createMessage();
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);

		assertNotNull(consumer.receive(RECV_TIMEOUT));
		assertNull(consumer.receive(RECV_TIMEOUT));

		consumer.close();

		msg = session.createMessage();
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);

		consumer = session.createDurableSubscriber(topic1, "durable_sub");

		assertNotNull(consumer.receive(RECV_TIMEOUT));
		assertNull(consumer.receive(RECV_TIMEOUT));

		consumer.close();

		session.unsubscribe("durable_sub");

		msg = session.createMessage();
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);

		consumer = session.createDurableSubscriber(topic1, "durable_sub");
		assertNull(consumer.receive(RECV_TIMEOUT));

		consumer.close();

		consumer = session.createDurableSubscriber(topic1, "durable_sub2");
		assertNull(consumer.receive(RECV_TIMEOUT));

		try
		{
			session.unsubscribe("durable_sub2");
			fail("Should have failed");
		}
		catch (FFMQException e)
		{
			assertTrue(e.getMessage().indexOf("in use") != -1);
		}

		consumer.close();

		session.unsubscribe("durable_sub");
		session.unsubscribe("durable_sub2");

		session.close();
	}

	public void testReceiveNoWait() throws Exception
	{
		Session session;
		Message msg;
		MessageProducer producer;
		MessageConsumer consumer;
		
		session = connection.createSession(true, Session.SESSION_TRANSACTED);
		msg = session.createMessage();
		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		session.commit();
		session.close();

		connection.start();

		Thread.sleep(RECV_TIMEOUT);

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		assertNotNull(consumer.receiveNoWait());
		assertNull(consumer.receive(RECV_TIMEOUT));
	}

	public void testAckTransactedSession() throws Exception
	{
		Session session;
		Message msg;
		MessageProducer producer;
		MessageConsumer consumer;

		session = connection.createSession(true, Session.SESSION_TRANSACTED);
		msg = session.createMessage();
		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		session.commit();
		session.close();

		connection.start();

		session = connection.createSession(true, Session.SESSION_TRANSACTED);
		consumer = session.createConsumer(queue1);
		msg = consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);

		msg.acknowledge(); // Should have no effect

		session.close();

		session = connection.createSession(true, Session.SESSION_TRANSACTED);
		consumer = session.createConsumer(queue1);
		msg = consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);
		session.commit();
		session.close();
	}

	public void testSendWithRuntimeDestination() throws Exception
	{
		Session session;
		Message msg;
		MessageProducer producer;
		MessageConsumer consumer;

		session = connection.createSession(true, Session.SESSION_TRANSACTED);
		msg = session.createMessage();
		producer = session.createProducer(null);
		producer.setDeliveryMode(TestUtils.DELIVERY_MODE);
		producer.setPriority(8);
		producer.setTimeToLive(2000);

		try
		{
			producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
			fail("Should have failed");
		}
		catch (UnsupportedOperationException e)
		{
			// Ok
		}

		long now = System.currentTimeMillis();
		producer.send(queue1, msg);

		session.commit();
		session.close();

		connection.start();

		session = connection.createSession(true, Session.SESSION_TRANSACTED);
		consumer = session.createConsumer(queue1);
		msg = consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);
		assertEquals(8, msg.getJMSPriority());
		assertTrue(Math.abs(now - msg.getJMSExpiration() + 2000) < 100);
		session.commit();
		session.close();
	}

	public void testConnectionStartStop() throws Exception
	{
		Session session;
		Message msg;
		MessageProducer producer;
		MessageConsumer consumer;

		session = connection.createSession(true, Session.SESSION_TRANSACTED);
		msg = session.createMessage();
		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		session.commit();
		session.close();

		session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		assertNull(consumer.receive(RECV_TIMEOUT));
		assertNull(consumer.receive(RECV_TIMEOUT));
		connection.start();
		assertNotNull(consumer.receive(RECV_TIMEOUT));

		connection.stop();
		connection.stop(); // Double stop

		consumer.close();
		session.close();
	}

	public void testConnectionStartAsync() throws Exception
    {
		Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
    	Message msg = session.createMessage();
    	MessageProducer producer = session.createProducer(queue1);
    	producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		producer.close();
    	
    	MessageConsumer consumer = session.createConsumer(queue1);
    	state = false;
    	consumer.setMessageListener(new MessageListener() {
			/*
			 * (non-Javadoc)
			 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
			 */
			@Override
			public void onMessage(Message msg)
			{
				try
				{
					Thread.sleep(500);
					state = true;
					listenerLock.release();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
    	connection.start();
    	assertFalse(state);
    	assertTrue(listenerLock.tryAcquire(1000,TimeUnit.MILLISECONDS));
    	assertTrue(state);
    	
    	session.close();
    }
	
	public void testConnectionStopWhileListening() throws Exception
    {
    	Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
    	Message msg = session.createMessage();
    	MessageProducer producer = session.createProducer(queue1);
    	producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		producer.close();
    	
    	MessageConsumer consumer = session.createConsumer(queue1);
    	state = false;
    	consumer.setMessageListener(new MessageListener() {
			/*
			 * (non-Javadoc)
			 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
			 */
			@Override
			public void onMessage(Message msg)
			{
				try
				{
					System.out.println("In listener");
					listenerLock.release();
					Thread.sleep(1000);
					msg.acknowledge();
					state = true;
					System.out.println("Leaving listener");
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
    	connection.start();
    	assertTrue(listenerLock.tryAcquire(2000,TimeUnit.MILLISECONDS));
    	
    	connection.stop();
    	System.out.println("Leaving stop");
    	assertTrue(state);
    	
    	session.close();
    }

	public void testConsumerPreClose() throws Exception
	{
		Session session;
		Message msg;
		MessageProducer producer;
		MessageConsumer consumer;

		session = connection.createSession(true, Session.SESSION_TRANSACTED);
		msg = session.createMessage();
		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		session.commit();
		session.close();

		session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		connection.start();
		assertNotNull(consumer.receive(RECV_TIMEOUT));
		consumer.close();

		consumer = session.createConsumer(queue1);

		long redeliveryDelay = engine.getSetup().getRedeliveryDelay();
		if (redeliveryDelay > 0)
			msg = consumer.receive(redeliveryDelay + RECV_TIMEOUT);
		else
			msg = consumer.receive(RECV_TIMEOUT);

		assertNotNull(msg);
		assertNotNull(consumer.receive(RECV_TIMEOUT));
		assertNull(consumer.receive(RECV_TIMEOUT));

		consumer.close();
		session.close();
	}

	public void testClose() throws Exception
	{
		Session session;
		Message msg;
		MessageProducer producer;
		MessageConsumer consumer;
		QueueBrowser browser;

		// Double close
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		session.close();
		session.close();

		// Cascade close
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		msg = session.createMessage();
		producer = session.createProducer(queue1);
		consumer = session.createConsumer(queue2);
		browser = session.createBrowser(queue1);
		session.close();
		try
		{
			producer.send(msg);
			fail("Should have failed");
		}
		catch (JMSException e)
		{
			assertTrue(e.getMessage().indexOf("closed") != -1);
		}

		assertNull(consumer.receive(RECV_TIMEOUT));

		try
		{
			browser.getEnumeration();
			fail("Should have failed");
		}
		catch (JMSException e)
		{
			assertTrue(e.getMessage().indexOf("closed") != -1);
		}

		try
		{
			session.createProducer(queue1);
			fail("Should have failed");
		}
		catch (JMSException e)
		{
			assertTrue(e.getMessage().indexOf("closed") != -1);
		}
		try
		{
			session.createConsumer(queue1);
			fail("Should have failed");
		}
		catch (JMSException e)
		{
			assertTrue(e.getMessage().indexOf("closed") != -1);
		}

		// With pending update (Unacknowledged)
		session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
		msg = session.createMessage();
		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		session.close();

		// With pending update (transacted)
		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		msg = session.createMessage();
		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		session.close();

		if (lastConnectionFailure != null)
			fail(lastConnectionFailure.toString());
	}

	public void testAcknowledge() throws Exception
	{
		Session session;
		Message msg;
		MessageProducer producer;
		MessageConsumer consumer;

		connection.start();

		session = connection.createSession(true, Session.SESSION_TRANSACTED);
		msg = session.createMessage();
		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		session.commit();
		session.close();

		session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		assertNotNull(consumer.receive(RECV_TIMEOUT));
		assertNull(consumer.receive(RECV_TIMEOUT));
		consumer.close();
		session.close();

		session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		long redeliveryDelay = engine.getSetup().getRedeliveryDelay();
		
		if (redeliveryDelay > 0)
			msg = consumer.receive(redeliveryDelay + RECV_TIMEOUT);
		else
			msg = consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);
		msg.acknowledge();
		
		assertNull(consumer.receive(RECV_TIMEOUT));
		session.close();

		session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		assertNull(consumer.receive(RECV_TIMEOUT));
		session.close();

		if (lastConnectionFailure != null)
			fail(lastConnectionFailure.toString());
	}

	public void testBeforeExpiration() throws Exception
	{
		Session session;
		MessageProducer producer;
		MessageConsumer consumer;

		connection.start();

		// Produce
		session = connection.createSession(true, Session.SESSION_TRANSACTED);
		producer = session.createProducer(queue1);
		
		TextMessage msg = session.createTextMessage("MSG_1");
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		assertEquals(0, msg.getJMSExpiration());

		session.commit();
		
		msg = session.createTextMessage("MSG_Expired");
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 500);
		long computedExpiration = msg.getJMSExpiration();
		long now = System.currentTimeMillis();
		assertTrue(now < computedExpiration);
		assertTrue((now - computedExpiration) < 500);

		msg = session.createTextMessage("MSG_2");
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		assertEquals(0, msg.getJMSExpiration());

		msg = session.createTextMessage("MSG_3");
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		assertEquals(0, msg.getJMSExpiration());

		session.commit();
		session.close();

		// Receive
		session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		msg = (TextMessage) consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);
		assertEquals("MSG_1", msg.getText());
		assertTrue(msg.getJMSExpiration() == 0);

		msg = (TextMessage) consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);
		assertEquals("MSG_Expired", msg.getText());
		assertEquals(computedExpiration, msg.getJMSExpiration());

		msg = (TextMessage) consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);
		assertEquals("MSG_2", msg.getText());
		assertTrue(msg.getJMSExpiration() == 0);

		msg = (TextMessage) consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);
		assertEquals("MSG_3", msg.getText());
		assertTrue(msg.getJMSExpiration() == 0);

		assertNull(consumer.receive(RECV_TIMEOUT));
		consumer.close();
		session.close();

		if (lastConnectionFailure != null)
			fail(lastConnectionFailure.toString());
	}

	public void testAfterExpiration() throws Exception
	{
		Session session;
		MessageProducer producer;
		MessageConsumer consumer;

		connection.start();

		// Produce
		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		producer = session.createProducer(queue1);

		TextMessage msg = session.createTextMessage("MSG_1");
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		assertEquals(0, msg.getJMSExpiration());

		msg = session.createTextMessage("MSG_Expired");
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 500);
		long computedExpiration = msg.getJMSExpiration();
		long now = System.currentTimeMillis();
		assertTrue(now < computedExpiration);
		assertTrue((now - computedExpiration) < 500);

		msg = session.createTextMessage("MSG_2");
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		assertEquals(0, msg.getJMSExpiration());

		msg = session.createTextMessage("MSG_3");
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		assertEquals(0, msg.getJMSExpiration());

		session.commit();
		session.close();

		// Wait for message expiration
		Thread.sleep(1000);

		// Receive
		session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);

		msg = (TextMessage) consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);
		assertEquals("MSG_1", msg.getText());
		assertEquals(0, msg.getJMSExpiration());

		msg = (TextMessage) consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);
		assertEquals("MSG_2", msg.getText());
		assertEquals(0, msg.getJMSExpiration());

		msg = (TextMessage) consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);
		assertEquals("MSG_3", msg.getText());
		assertEquals(0, msg.getJMSExpiration());

		assertNull(consumer.receive(RECV_TIMEOUT));
		consumer.close();
		session.close();

		if (lastConnectionFailure != null)
			fail(lastConnectionFailure.toString());
	}

	public void testCommit() throws Exception
	{
		Session session;
		Message msg;
		MessageProducer producer;
		MessageConsumer consumer;

		// session is not transacted
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		try
		{
			session.commit();
			fail("Should have failed");
		}
		catch (IllegalStateException e)
		{
			assertTrue(e.getMessage().indexOf("transacted") != -1);
		}
		session.close();

		// Double commit
		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		msg = session.createMessage();
		producer = session.createProducer(queue1);
		consumer = session.createConsumer(queue1);
		connection.start();
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		session.commit();
		assertNotNull(consumer.receive(RECV_TIMEOUT));
		assertNull(consumer.receive(RECV_TIMEOUT));
		session.commit();
		session.close();

		// Commit with pending update
		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		msg = session.createMessage();
		producer = session.createProducer(queue1);
		consumer = session.createConsumer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		assertNull(consumer.receive(RECV_TIMEOUT));
		session.commit();
		assertNotNull(consumer.receive(RECV_TIMEOUT));
		assertNull(consumer.receive(RECV_TIMEOUT));
		session.close();

		if (lastConnectionFailure != null)
			fail(lastConnectionFailure.toString());
	}

	public void testQueueBrowser() throws Exception
	{
		Session session;
		Message msg;
		MessageProducer producer;

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		msg = session.createMessage();
		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		session.close();

		Thread.sleep(RECV_TIMEOUT);

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		QueueBrowser browser = session.createBrowser(queue1);

		int count = 0;
		Enumeration<?> messages = browser.getEnumeration();
		while (messages.hasMoreElements())
		{
			Message browsedMsg = (Message) messages.nextElement();
			assertNotNull(browsedMsg);
			count++;
		}
		assertEquals(5, count);

		browser.close();
		session.close();

		if (lastConnectionFailure != null)
			fail(lastConnectionFailure.toString());
	}

	public void testRollback() throws Exception
	{
		Session session;
		Message msg;
		MessageProducer producer;
		MessageConsumer consumer;

		connection.start();

		// session is not transacted
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		try
		{
			session.rollback();
			fail("Should have failed");
		}
		catch (IllegalStateException e)
		{
			assertTrue(e.getMessage().indexOf("transacted") != -1);
		}
		session.close();

		// Double rollback
		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		msg = session.createMessage();
		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		session.rollback();
		session.rollback();
		session.close();

		// Rollback with pending send
		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		msg = session.createMessage();
		producer = session.createProducer(queue1);
		consumer = session.createConsumer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		assertNull(consumer.receive(RECV_TIMEOUT));
		session.rollback();
		session.close();

		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		assertNull(consumer.receive(RECV_TIMEOUT));
		session.rollback();
		session.close();

		// Rollback with pending get
		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		msg = session.createMessage();
		producer = session.createProducer(queue1);
		consumer = session.createConsumer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		assertFalse(msg.getJMSRedelivered());
		session.commit();
		msg = consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);
		assertFalse(msg.getJMSRedelivered());
		session.rollback();
		session.close();

		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);

		long redeliveryDelay = engine.getSetup().getRedeliveryDelay();
		if (redeliveryDelay > 0)
			msg = consumer.receive(redeliveryDelay + RECV_TIMEOUT);
		else
			msg = consumer.receive(RECV_TIMEOUT);

		assertNotNull(msg);
		assertTrue(msg.getJMSRedelivered());
		session.rollback();
		session.close();
	}

	public void testPrefetchRollback() throws Exception
	{
		Session session;
		Message msg;
		MessageProducer producer;
		MessageConsumer consumer;

		connection.start();

		for (int i = 0; i < 10; i++)
		{
			session = connection.createSession(true, Session.SESSION_TRANSACTED);
			msg = session.createMessage();
			producer = session.createProducer(queue1);
			producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
			producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
			session.commit();

			consumer = session.createConsumer(queue1);
			assertNotNull(consumer.receive(RECV_TIMEOUT));
			session.commit();
			consumer.close();

			consumer = session.createConsumer(queue1);
			assertNotNull(consumer.receive(RECV_TIMEOUT));
			consumer.close();
			session.commit();

			consumer = session.createConsumer(queue1);
			assertNull(consumer.receive(RECV_TIMEOUT));
			consumer.close();

			session.close();
		}
	}

	public void testCreateProducer() throws Exception
	{
		Session session;
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		session.createProducer(null);
		session.createProducer(queue1);
		session.close();
	}

	public void testRecover() throws Exception
	{
		Session session;
		Message msg;
		MessageProducer producer;
		MessageConsumer consumer;

		connection.start();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		msg = session.createMessage();
		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		session.close();

		session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		assertNotNull(consumer.receive(RECV_TIMEOUT));
		assertNull(consumer.receive(RECV_TIMEOUT));
		session.recover();

		long redeliveryDelay = engine.getSetup().getRedeliveryDelay();
		if (redeliveryDelay > 0)
			msg = consumer.receive(redeliveryDelay + RECV_TIMEOUT);
		else
			msg = consumer.receive(RECV_TIMEOUT);

		assertNotNull(msg);
		assertNull(consumer.receive(RECV_TIMEOUT));
		session.close();
	}

	public void testListener() throws Exception
	{
		Session session;
		Message msg;
		MessageProducer producer;
		MessageConsumer consumer;

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		msg = session.createMessage();
		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		session.close();

		// Early listener
		session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		counter = 0;
		consumer.setMessageListener(new MessageListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
			 */
			@Override
			public void onMessage(Message message)
			{
				try
				{
					counter++;
					if (counter == 1)
						message.acknowledge();
					else
						listenerLock.release();
				}
				catch (JMSException e)
				{
					lastConnectionFailure = e;
					listenerLock.release();
				}
			}
		});
		connection.start();
		assertTrue(listenerLock.tryAcquire(1000,TimeUnit.MILLISECONDS));
		Thread.sleep(200);
		assertEquals(2, counter);
		consumer.close();
		session.recover();

		// Late listener
		session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		counter = 0;
		consumer.setMessageListener(new MessageListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
			 */
			@Override
			public void onMessage(Message message)
			{
				try
				{
					counter++;
					message.acknowledge();
					listenerLock.release();
				}
				catch (JMSException e)
				{
					lastConnectionFailure = e;
					listenerLock.release();
				}
			}
		});
		assertTrue(listenerLock.tryAcquire(1000,TimeUnit.MILLISECONDS));
		Thread.sleep(200);
		assertEquals(1, counter);
		consumer.close();
	}

	public void testCreateTemporaryQueue() throws Exception
	{
		Session session;
		Message msg;
		MessageProducer producer;
		MessageConsumer consumer;

		connection.start();

		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		msg = session.createMessage();
		Queue tempQueue = session.createTemporaryQueue();
		producer = session.createProducer(tempQueue);
		consumer = session.createConsumer(tempQueue);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		session.commit();
		assertNotNull(consumer.receive(RECV_TIMEOUT));
		assertNull(consumer.receive(RECV_TIMEOUT));
		session.commit();
		session.close();
	}

	public void testCreateMessage() throws Exception
	{
		Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		assertNotNull(session.createMessage());
		session.close();
	}

	public void testCreateBytesMessage() throws Exception
	{
		Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		assertNotNull(session.createBytesMessage());
		session.close();
	}

	public void testCreateMapMessage() throws Exception
	{
		Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		assertNotNull(session.createMapMessage());
		session.close();
	}

	public void testCreateObjectMessage() throws Exception
	{
		Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		assertNotNull(session.createObjectMessage());
		session.close();
	}

	public void testCreateStreamMessage() throws Exception
	{
		Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		assertNotNull(session.createStreamMessage());
		session.close();
	}

	public void testCreateTextMessage() throws Exception
	{
		Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		assertNotNull(session.createTextMessage());
		session.close();
	}

	public void testCreateQueue() throws Exception
	{
		Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		assertNotNull(session.createQueue("foo"));
		assertNotNull(session.createQueue("bar"));
		session.close();
	}

	public void testGetAcknowledgeMode() throws Exception
	{
		Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		assertEquals(Session.SESSION_TRANSACTED, session.getAcknowledgeMode());
		session.close();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		assertEquals(Session.AUTO_ACKNOWLEDGE, session.getAcknowledgeMode());
		session.close();
	}

	public void testGetTransacted() throws Exception
	{
		Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		assertTrue(session.getTransacted());
		session.close();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		assertFalse(session.getTransacted());
		session.close();
	}

	public void testCreateObjectMessageSerializable() throws Exception
	{
		Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		assertNotNull(session.createObjectMessage("data"));
		session.close();
	}

	public void testCreateTextMessageString() throws Exception
	{
		Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		assertNotNull(session.createTextMessage("foo"));
		session.close();
	}

	public void testCreateConsumerDestinationString() throws Exception
	{
		Session session;
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		try
		{
			session.createConsumer(null, "foo");
			fail("Should have failed");
		}
		catch (JMSException e)
		{
			assertTrue(e.getMessage().indexOf("destination") != -1);
		}

		session.createConsumer(queue1, null);
		session.createConsumer(queue1, "JMSMessageID='toto'");
		session.close();
	}

	public void testCreateConsumerDestination() throws Exception
	{
		Session session;
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		try
		{
			session.createConsumer(null);
			fail("Should have failed");
		}
		catch (JMSException e)
		{
			assertTrue(e.getMessage().indexOf("destination") != -1);
		}

		session.createConsumer(queue1);
		session.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.timewalker.ffmq4.additional.AbstractCommTest#isListenerTest()
	 */
	@Override
	protected boolean isListenerTest()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.timewalker.ffmq4.additional.AbstractCommTest#isRemote()
	 */
	@Override
	protected boolean isRemote()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.timewalker.ffmq4.additional.AbstractCommTest#isTopicTest()
	 */
	@Override
	protected boolean isTopicTest()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.timewalker.ffmq4.additional.AbstractCommTest#useMultipleConnections()
	 */
	@Override
	protected boolean useMultipleConnections()
	{
		return false;
	}

	public void testCreateBrowser() throws Exception
	{
		Session session;
		Message msg;
		MessageProducer producer;

		// Fill-in some messages
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		producer = session.createProducer(queue1);
		msg = session.createTextMessage("MSG_1");
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		msg = session.createTextMessage("MSG_2");
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		msg = session.createTextMessage("MSG_3");
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);

		QueueBrowser browser = session.createBrowser(queue1);
		Enumeration<?> messages = browser.getEnumeration();
		int count = 0;
		while (messages.hasMoreElements())
		{
			TextMessage message = (TextMessage) messages.nextElement();
			assertEquals("MSG_" + (++count), message.getText());
		}
		count = 0;
		messages = browser.getEnumeration();
		while (messages.hasMoreElements())
		{
			TextMessage message = (TextMessage) messages.nextElement();
			assertEquals("MSG_" + (++count), message.getText());
		}
		browser.close();

		session.close();
	}

	public void testJMSDestination() throws Exception
	{
		Session session;
		Message msg;
		MessageProducer producer;
		MessageConsumer consumer;

		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		msg = session.createMessage();
		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		assertNotNull(msg.getJMSDestination());
		assertTrue(msg.getJMSDestination() instanceof Queue);
		assertEquals(queue1.getQueueName(), ((Queue) msg.getJMSDestination()).getQueueName());
		session.commit();
		session.close();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		connection.start();
		msg = consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);
		assertNotNull(msg.getJMSDestination());
		assertTrue(msg.getJMSDestination() instanceof Queue);
		assertEquals(queue1.getQueueName(), ((Queue) msg.getJMSDestination()).getQueueName());

		consumer.close();
		session.close();
	}

	public void testJMSDeliveryMode() throws Exception
	{
		Session session;
		Message msg;
		MessageProducer producer;
		MessageConsumer consumer;

		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		msg = session.createMessage();
		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		assertEquals(TestUtils.DELIVERY_MODE, msg.getJMSDeliveryMode());
		session.commit();
		session.close();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		connection.start();
		msg = consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);
		assertEquals(TestUtils.DELIVERY_MODE, msg.getJMSDeliveryMode());

		consumer.close();
		session.close();
	}

	public void testJMSMessageID() throws Exception
	{
		Session session;
		Message msg;
		MessageProducer producer;
		MessageConsumer consumer;

		// With messages IDs enabled
		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		msg = session.createMessage();
		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		String producerMsgID = msg.getJMSMessageID();
		assertNotNull(producerMsgID);
		assertTrue(producerMsgID.startsWith("ID:"));
		session.commit();
		session.close();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		connection.start();
		msg = consumer.receive(RECV_TIMEOUT);
		String consumerMsgID = msg.getJMSMessageID();
		assertNotNull(producerMsgID);
		assertTrue(producerMsgID.startsWith("ID:"));
		assertEquals(producerMsgID, consumerMsgID);
		consumer.close();
		session.close();

		// With messages IDs disabled
		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		msg = session.createMessage();
		producer = session.createProducer(queue1);
		producer.setDisableMessageID(true);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		producerMsgID = msg.getJMSMessageID();
		assertNotNull(producerMsgID); // FFMQ always sets the ID
		session.commit();
		session.close();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		connection.start();
		msg = consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);
		consumerMsgID = msg.getJMSMessageID();
		assertNotNull(consumerMsgID); // FFMQ always sets the ID
		consumer.close();
		session.close();
	}

	public void testJMSTimestamp() throws Exception
	{
		Session session;
		Message msg;
		MessageProducer producer;
		MessageConsumer consumer;

		// With timestamps enabled
		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		msg = session.createMessage();
		producer = session.createProducer(queue1);
		long now = System.currentTimeMillis();
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		long producerTimestamp = msg.getJMSTimestamp();
		assertTrue(producerTimestamp >= now);
		session.commit();
		session.close();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		connection.start();
		msg = consumer.receive(500);
		long consumerTimestamp = msg.getJMSTimestamp();
		assertEquals(producerTimestamp, consumerTimestamp);
		consumer.close();
		session.close();

		// With timestamps disabled
		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		msg = session.createMessage();
		producer = session.createProducer(queue1);
		producer.setDisableMessageTimestamp(true);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		producerTimestamp = msg.getJMSTimestamp();
		assertEquals(0, producerTimestamp);
		session.commit();
		session.close();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		connection.start();
		msg = consumer.receive(RECV_TIMEOUT);
		consumerTimestamp = msg.getJMSTimestamp();
		assertEquals(0, consumerTimestamp);
		consumer.close();
		session.close();
	}

	public void testJMSCorrelationID() throws Exception
	{
		Session session;
		Message msg;
		MessageProducer producer;
		MessageConsumer consumer;

		// With correl ID
		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		msg = session.createMessage();
		String correlID = UUIDProvider.getInstance().getUUID();
		msg.setJMSCorrelationID(correlID);
		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		assertEquals(correlID, msg.getJMSCorrelationID());
		session.commit();
		session.close();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		connection.start();
		msg = consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);
		assertEquals(correlID, msg.getJMSCorrelationID());
		consumer.close();
		session.close();

		// Without correl ID
		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		msg = session.createMessage();
		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		assertNull(msg.getJMSCorrelationID());
		session.commit();
		session.close();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		connection.start();
		msg = consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);
		assertNull(msg.getJMSCorrelationID());
		consumer.close();
		session.close();
	}

	public void testJMSReplyTo() throws Exception
	{
		Session session;
		Message msg;
		MessageProducer producer;
		MessageConsumer consumer;

		// with reply to
		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		msg = session.createMessage();
		msg.setJMSReplyTo(queue1);
		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		assertNotNull(msg.getJMSReplyTo());
		assertTrue(msg.getJMSReplyTo() instanceof Queue);
		assertEquals(queue1.getQueueName(), ((Queue) msg.getJMSReplyTo()).getQueueName());
		session.commit();
		session.close();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		connection.start();
		msg = consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);
		assertNotNull(msg.getJMSReplyTo());
		assertTrue(msg.getJMSReplyTo() instanceof Queue);
		assertEquals(queue1.getQueueName(), ((Queue) msg.getJMSReplyTo()).getQueueName());
		consumer.close();
		session.close();

		// without reply to
		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		msg = session.createMessage();
		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		assertNull(msg.getJMSReplyTo());
		session.commit();
		session.close();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		connection.start();
		msg = consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);
		assertNull(msg.getJMSReplyTo());
		consumer.close();
		session.close();
	}

	public void testJMSType() throws Exception
	{
		Session session;
		Message msg;
		MessageProducer producer;
		MessageConsumer consumer;

		// With type
		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		msg = session.createMessage();
		String type = UUIDProvider.getInstance().getUUID();
		msg.setJMSType(type);
		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		assertEquals(type, msg.getJMSType());
		session.commit();
		session.close();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		connection.start();
		msg = consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);
		assertEquals(type, msg.getJMSType());
		consumer.close();
		session.close();

		// Without type
		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		msg = session.createMessage();
		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		assertNull(msg.getJMSType());
		session.commit();
		session.close();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		connection.start();
		msg = consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);
		assertNull(msg.getJMSType());
		consumer.close();
		session.close();
	}

	public void testJMSPriority() throws Exception
	{
		Session session;
		Message msg;
		MessageProducer producer;
		MessageConsumer consumer;

		int[] priorities = new int[20];// { 6, 8, 0, 2, 9, 9, 4, 0, 6, 0 , 9 };
		Random random = new Random();
		
		for(int k=0;k<100;k++)
		{
			for(int j=0;j<priorities.length;j++)
			{
				priorities[j] = random.nextInt(10);
			}
		
			session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
			msg = session.createMessage();
			producer = session.createProducer(queue1);
			for (int n = 0; n < priorities.length; n++)
			{
				producer.send(msg, TestUtils.DELIVERY_MODE, priorities[n], 0);
				assertEquals(priorities[n], msg.getJMSPriority());
			}
			session.commit();
			session.close();
	
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			consumer = session.createConsumer(queue1);
			connection.start();
			int lastPriority = 9;
			for (int n = 0; n < priorities.length; n++)
			{
				msg = consumer.receive(RECV_TIMEOUT);
				assertNotNull(msg);
				assertTrue(lastPriority >= msg.getJMSPriority());
				lastPriority = msg.getJMSPriority();
				//System.out.println(lastPriority);
			}
			consumer.close();
			session.close();
			
			session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
			msg = session.createMessage();
			producer = session.createProducer(queue1);
			for (int n = 0; n < priorities.length; n++)
			{
				producer.send(msg, TestUtils.DELIVERY_MODE, priorities[n], 0);
				session.commit();
				assertEquals(priorities[n], msg.getJMSPriority());
			}
			session.close();
			
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			consumer = session.createConsumer(queue1);
			connection.start();
			lastPriority = 9;
			for (int n = 0; n < priorities.length; n++)
			{
				msg = consumer.receive(RECV_TIMEOUT);
				assertNotNull(msg);
				assertTrue(lastPriority >= msg.getJMSPriority());
				lastPriority = msg.getJMSPriority();
				// System.out.println(lastPriority);
			}
			consumer.close();
			session.close();
		}
	}

	public void testMessageNotWriteable() throws Exception
	{
		Session session;
		TextMessage msg;
		MessageProducer producer;
		MessageConsumer consumer;

		// Send message
		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		msg = session.createTextMessage("foo");
		msg.setIntProperty("iProp", 123);
		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 0, 0);
		session.commit();
		session.close();

		// Receive message
		session = connection.createSession(true, Session.SESSION_TRANSACTED);
		consumer = session.createConsumer(queue1);
		connection.start();
		msg = (TextMessage) consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);

		try
		{
			msg.setIntProperty("iProp", 456);
			fail("Should have failed");
		}
		catch (MessageNotWriteableException e)
		{
			// Ok
		}

		try
		{
			msg.setText("other");
			fail("Should have failed");
		}
		catch (MessageNotWriteableException e)
		{
			// Ok
		}

		msg.clearProperties();
		msg.setIntProperty("iProp", 456);

		msg.clearBody();
		msg.setText("other");

		session.rollback();

		// Check that the original message was not modified
		long redeliveryDelay = engine.getSetup().getRedeliveryDelay();
		if (redeliveryDelay > 0)
			msg = (TextMessage) consumer.receive(redeliveryDelay + RECV_TIMEOUT);
		else
			msg = (TextMessage) consumer.receive(RECV_TIMEOUT);

		assertNotNull(msg);
		assertEquals(123, msg.getIntProperty("iProp"));
		assertEquals("foo", msg.getText());

		consumer.close();
		session.close();
	}

	public void testSendForeignMessage() throws Exception
	{
		Session session;
		TextMessage msg;
		MessageProducer producer;
		MessageConsumer consumer;

		// Send message
		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);

		msg = new ForeignTextMessage();
		msg.setText("foobar");

		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 0, 0);
		session.commit();
		session.close();

		// Receive message
		session = connection.createSession(true, Session.SESSION_TRANSACTED);
		consumer = session.createConsumer(queue1);
		connection.start();
		msg = (TextMessage) consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);
		assertEquals("foobar", msg.getText());
		session.commit();

		consumer.close();
		session.close();
	}

	public void testTemporaryQueueLifecycle() throws Exception
	{
		Session session;
		TextMessage msg;
		MessageProducer producer;
		MessageConsumer consumer;

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		TemporaryQueue tempQueue = session.createTemporaryQueue();

		msg = session.createTextMessage("foobar");
		producer = session.createProducer(tempQueue);
		producer.send(msg, DeliveryMode.NON_PERSISTENT, 0, 0);
		producer.close();

		consumer = session.createConsumer(tempQueue);
		connection.start();
		msg = (TextMessage) consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);
		assertEquals("foobar", msg.getText());

		tempQueue.delete();
		session.close();
	}

	public void testTemporaryTopicLifecycle() throws Exception
	{
		Session session;
		TextMessage msg;
		MessageProducer producer;
		MessageConsumer consumer;

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		TemporaryTopic tempTopic = session.createTemporaryTopic();

		consumer = session.createConsumer(tempTopic);
		connection.start();

		msg = session.createTextMessage("foobar");
		producer = session.createProducer(tempTopic);
		producer.send(msg, DeliveryMode.NON_PERSISTENT, 0, 0);
		producer.close();

		msg = (TextMessage) consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);
		assertEquals("foobar", msg.getText());

		tempTopic.delete();
		session.close();
	}

	public void testJMSPropertiesTransmission() throws Exception
	{
		Session session;
		TextMessage msg;
		MessageProducer producer;
		MessageConsumer consumer;

		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		msg = session.createTextMessage("foobar");
		msg.setBooleanProperty("boolean", true);
		msg.setStringProperty("string", "foobar");
		msg.setByteProperty("byte", (byte) 1);
		msg.setShortProperty("short", (short) 2);
		msg.setIntProperty("int", 3);
		msg.setLongProperty("long", 4);
		msg.setFloatProperty("float", 1.23f);
		msg.setDoubleProperty("double", 4.56);

		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		session.commit();
		session.close();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		connection.start();
		msg = (TextMessage) consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);
		assertEquals("foobar", msg.getText());

		// Check properties
		assertEquals(true, msg.getBooleanProperty("boolean"));
		assertEquals("foobar", msg.getStringProperty("string"));
		assertEquals((byte) 1, msg.getByteProperty("byte"));
		assertEquals((short) 2, msg.getShortProperty("short"));
		assertEquals(3, msg.getIntProperty("int"));
		assertEquals(4, msg.getLongProperty("long"));
		assertEquals(1.23f, msg.getFloatProperty("float"), 0.000001);
		assertEquals(4.56, msg.getDoubleProperty("double"), 0.000001);

		// Make sure message is read-only
		try
		{
			msg.setBooleanProperty("xxx", true);
			fail("Should have failed");
		}
		catch (MessageNotWriteableException e)
		{
			// Ignore
		}

		msg.clearProperties();

		msg.setBooleanProperty("xxx", true);
		assertEquals(true, msg.getBooleanProperty("xxx"));

		consumer.close();
		session.close();
	}

	public void testTextMessageTransmission() throws Exception
	{
		Session session;
		TextMessage msg;
		MessageProducer producer;
		MessageConsumer consumer;

		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		msg = session.createTextMessage("foobar");

		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		session.commit();
		session.close();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		connection.start();
		msg = (TextMessage) consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);

		assertEquals("foobar", msg.getText());

		// Make sure message is read-only
		try
		{
			msg.setText("BOUM");
			fail("Should have failed");
		}
		catch (MessageNotWriteableException e)
		{
			// Ignore
		}

		msg.clearBody();

		msg.setText("foofoo");
		assertEquals("foofoo", msg.getText());

		consumer.close();
		session.close();
	}

	public void testByteMessageTransmission() throws Exception
	{
		Session session;
		BytesMessage msg;
		MessageProducer producer;
		MessageConsumer consumer;

		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		msg = session.createBytesMessage();

		byte[] data = new byte[1024];
		for (int n = 0; n < data.length; n++)
			data[n] = (byte) n;

		int BLOCKS = 10;

		for (int n = 0; n < BLOCKS; n++)
			msg.writeBytes(data);

		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		session.commit();
		session.close();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		connection.start();
		msg = (BytesMessage) consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);

		byte[] data2 = new byte[1024];
		for (int n = 0; n < BLOCKS; n++)
		{
			assertEquals(1024, msg.readBytes(data2));
			for (int k = 0; k < data2.length; k++)
				data2[k] = (byte) k;
		}
		assertEquals(-1, msg.readBytes(data2));

		// Make sure message is read-only
		try
		{
			msg.writeByte((byte) 12);
			fail("Should have failed");
		}
		catch (MessageNotWriteableException e)
		{
			// Ignore
		}

		msg.clearBody();

		msg.writeByte((byte) 12);

		// Make sure message is write-only
		try
		{
			msg.readByte();
			fail("Should have failed");
		}
		catch (MessageNotReadableException e)
		{
			// Ignore
		}

		msg.reset();

		// Make sure message is read-only
		try
		{
			msg.writeByte((byte) 12);
			fail("Should have failed");
		}
		catch (MessageNotWriteableException e)
		{
			// Ignore
		}

		assertEquals((byte) 12, msg.readByte());

		consumer.close();
		session.close();
	}

	public void testStreamMessageTransmission() throws Exception
	{
		Session session;
		StreamMessage msg;
		MessageProducer producer;
		MessageConsumer consumer;

		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		msg = session.createStreamMessage();

		byte[] data = new byte[1024];
		for (int n = 0; n < data.length; n++)
			data[n] = (byte) n;

		int BLOCKS = 10;

		for (int n = 0; n < BLOCKS; n++)
			msg.writeBytes(data);

		msg.writeBoolean(true);
		msg.writeByte((byte) 12);
		msg.writeChar('a');
		msg.writeDouble(1.23456789);
		msg.writeFloat(4.5678f);
		msg.writeInt(1234);
		msg.writeLong(1234567890);
		msg.writeObject(Integer.valueOf(444));
		msg.writeObject(null);
		msg.writeShort((short) 123);
		msg.writeString("foobar");

		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		session.commit();
		session.close();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		connection.start();
		msg = (StreamMessage) consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);

		byte[] data2 = new byte[1024];
		for (int n = 0; n < BLOCKS; n++)
		{
			assertEquals(1024, msg.readBytes(data2));
			assertEquals(-1, msg.readBytes(data2));
			for (int k = 0; k < data2.length; k++)
				data2[k] = (byte) k;
		}

		assertEquals(true, msg.readBoolean());
		assertEquals((byte) 12, msg.readByte());
		assertEquals('a', msg.readChar());
		assertEquals(1.23456789, msg.readDouble(), 0.0000001);
		assertEquals(4.5678f, msg.readFloat(), 0.000001);
		assertEquals(1234, msg.readInt());
		assertEquals(1234567890, msg.readLong());
		assertEquals(Integer.valueOf(444), msg.readObject());
		assertEquals(null, msg.readObject());
		assertEquals((short) 123, msg.readShort());
		assertEquals("foobar", msg.readString());

		try
		{
			msg.readBytes(data2);
			fail("Should have failed");
		}
		catch (MessageEOFException e)
		{
			// Ignore
		}

		// Make sure message is read-only
		try
		{
			msg.writeByte((byte) 12);
			fail("Should have failed");
		}
		catch (MessageNotWriteableException e)
		{
			// Ignore
		}

		msg.clearBody();

		msg.writeObject("foobar");

		// Make sure message is write-only
		try
		{
			msg.readObject();
			fail("Should have failed");
		}
		catch (MessageNotReadableException e)
		{
			// Ignore
		}

		msg.reset();

		// Make sure message is read-only
		try
		{
			msg.writeObject("dumb");
			fail("Should have failed");
		}
		catch (MessageNotWriteableException e)
		{
			// Ignore
		}

		assertEquals("foobar", msg.readObject());

		consumer.close();
		session.close();
	}

	public void testObjectMessageTransmission() throws Exception
	{
		Session session;
		ObjectMessage msg;
		MessageProducer producer;
		MessageConsumer consumer;

		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		msg = session.createObjectMessage();

		Date now = new Date();
		msg.setObject(now);

		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		session.commit();
		session.close();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		connection.start();
		msg = (ObjectMessage) consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);

		assertEquals(now, msg.getObject());

		// Make sure message is read-only
		try
		{
			msg.setObject("foobar");
			fail("Should have failed");
		}
		catch (MessageNotWriteableException e)
		{
			// Ignore
		}

		msg.clearBody();

		msg.setObject("foobar");
		assertEquals("foobar", msg.getObject());

		consumer.close();
		session.close();
	}

	public void testMapMessageTransmission() throws Exception
	{
		Session session;
		MapMessage msg;
		MessageProducer producer;
		MessageConsumer consumer;

		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		msg = session.createMapMessage();

		msg.setBoolean("boolean", true);
		msg.setByte("byte", (byte) 12);
		msg.setChar("char", 'a');
		msg.setDouble("double", 1.23456789);
		msg.setFloat("float", 4.5678f);
		msg.setInt("int", 1234);
		msg.setLong("long", 1234567890);
		msg.setObject("object", Integer.valueOf(444));
		msg.setObject("null", null);
		msg.setShort("short", (short) 123);
		msg.setString("string", "foobar");
		msg.setBytes("bytes", "foo".getBytes("ascii"));

		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		session.commit();
		session.close();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		connection.start();
		msg = (MapMessage) consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);

		assertEquals(true, msg.getBoolean("boolean"));
		assertEquals((byte) 12, msg.getByte("byte"));
		assertEquals('a', msg.getChar("char"));
		assertEquals(1.23456789, msg.getDouble("double"), 0.0000001);
		assertEquals(4.5678f, msg.getFloat("float"), 0.000001);
		assertEquals(1234, msg.getInt("int"));
		assertEquals(1234567890, msg.getLong("long"));
		assertEquals(Integer.valueOf(444), msg.getObject("object"));
		assertEquals(null, msg.getObject("null"));
		assertEquals((short) 123, msg.getShort("short"));
		assertEquals("foobar", msg.getString("string"));
		assertEquals("foo", new String(msg.getBytes("bytes"), "ascii"));

		// Make sure message is read-only
		try
		{
			msg.setObject("string", "foobar");
			fail("Should have failed");
		}
		catch (MessageNotWriteableException e)
		{
			// Ignore
		}

		msg.clearBody();

		msg.setObject("string", "foobar");
		assertEquals("foobar", msg.getObject("string"));

		consumer.close();
		session.close();
	}

	public void testMessageUpdateAfterSend() throws Exception
	{
		Session session;
		TextMessage msg;
		MessageProducer producer;
		MessageConsumer consumer;

		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		msg = session.createTextMessage("text1");

		producer = session.createProducer(queue1);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);

		msg.setText("text2");

		session.commit();
		session.close();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createConsumer(queue1);
		connection.start();
		msg = (TextMessage) consumer.receive(RECV_TIMEOUT);
		assertNotNull(msg);

		assertEquals("text1", msg.getText());

		consumer.close();
		session.close();

		if (lastConnectionFailure != null)
			fail(lastConnectionFailure.toString());
	}

	private void purgeDestination(Destination destination) throws JMSException
	{
		Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		MessageConsumer consumer = session.createConsumer(destination);
		connection.start();
		while (consumer.receive(RECV_TIMEOUT) != null)
			continue;
		consumer.close();
		session.commit();
		session.close();
		connection.stop();
	}

	public void testInvalidCreateSession() throws Exception
	{
		try
		{
			connection.createSession(false, Session.SESSION_TRANSACTED);
			fail("Should have failed");
		}
		catch (JMSException e)
		{
			// Ok
		}
	}
	
	public void testQueueFullTransacted() throws Exception
	{
		Session session;
		TextMessage msg;
		MessageProducer producer;
		MessageConsumer consumer;

		System.setProperty("ffmq4.producer.retryOnQueueFull", "false");
		
		session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);

		producer = session.createProducer(extrasmallqueue1);
		msg = session.createTextMessage("text1");
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		
		try
		{
			session.commit();
			fail("Should have failed");
		}
		catch (DataStoreFullException e)
		{
			// Ok
		}
		
		try
		{
			session.commit();
			fail("Should have failed");
		}
		catch (DataStoreFullException e)
		{
			// Ok
		}
		
		session.rollback();
		// Should no longer failed after rollback
		
		session.commit();
		session.close();
		
		System.clearProperty("ffmq4.producer.retryOnQueueFull");
	}
	
	public void testQueueFullNonTransactedAsync() throws Exception
	{
		if (!isRemote())
			return; // Use-case only supported on RemoteSession
		
		Session session;
		TextMessage msg;
		MessageProducer producer;

		System.setProperty("ffmq4.producer.retryOnQueueFull", "false");
		System.setProperty("ffmq4.producer.allowSendAsync", "true");
		
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		producer = session.createProducer(extrasmallqueue1);
		msg = session.createTextMessage("text1");
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
	    producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);  // Should not faild because of async send 
		session.close();
		
		System.clearProperty("ffmq4.producer.retryOnQueueFull");
    	System.clearProperty("ffmq4.producer.allowSendAsync");
	}

    public void testQueueFullNonTransactedSync() throws Exception
    {
    	Session session;
    	TextMessage msg;
    	MessageProducer producer;
    
    	System.setProperty("ffmq4.producer.retryOnQueueFull", "false");
    	System.setProperty("ffmq4.producer.allowSendAsync", "false");
    	
    	session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    
    	producer = session.createProducer(extrasmallqueue1);
    	msg = session.createTextMessage("text1");
    	producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
    	producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
    	try
    	{
    		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);  // Should fail in synchronous mode
    		fail("Should have failed");
    	}
    	catch (DataStoreFullException e)
    	{
    		// Ok
    	}
    	
    	session.recover(); // Should not have any effect on puts
    	
    	try
    	{
    		producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
    		fail("Should have failed");
    	}
    	catch (DataStoreFullException e)
    	{
    		// Ok
    	}
    	
    	MessageConsumer consumer = session.createConsumer(extrasmallqueue1);
    	connection.start();
    	while (consumer.receive(RECV_TIMEOUT) != null)
    		continue;
    	
    	producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
    	producer.send(msg, TestUtils.DELIVERY_MODE, 3, 0);
    	
    	while (consumer.receive(RECV_TIMEOUT) != null)
    		continue;
    	
    	session.close();
    	
    	System.clearProperty("ffmq4.producer.retryOnQueueFull");
    	System.clearProperty("ffmq4.producer.allowSendAsync");
    }

}
