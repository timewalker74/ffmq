package net.timewalker.ffmq4.test.local.destination.store.impl;

import java.util.Random;

import javax.jms.DeliveryMode;
import javax.jms.Message;

import junit.framework.TestCase;
import net.timewalker.ffmq4.common.message.AbstractMessage;
import net.timewalker.ffmq4.common.message.TextMessageImpl;
import net.timewalker.ffmq4.storage.message.MessageStore;
import net.timewalker.ffmq4.utils.async.AsyncTaskManager;

/**
 * AbstractMessageStoreTest
 */
public abstract class AbstractMessageStoreTest extends TestCase
{
	protected AsyncTaskManager asyncTaskManager;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		
		this.asyncTaskManager = new AsyncTaskManager("test", 
				1, 
				5, 
				10);
		
		System.setProperty("ffmq.dataStore.safeMode", "true");
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception
	{
		asyncTaskManager.close();
		super.tearDown();
	}
	
	protected abstract MessageStore createMessageStore( boolean createStoreFiles ) throws Exception;
	
	private void assertOrdered( MessageStore msgStore ) throws Exception
	{
		int lastPriority = 9;
		int current = msgStore.first();
		boolean failed = false;			
		while (current != -1)
		{
			AbstractMessage msg = msgStore.retrieve(current);
			System.out.println(current+" PRIO "+msg.getJMSPriority());
			
			if (lastPriority < msg.getJMSPriority())
				failed = true;
			
			if (msg.getJMSPriority() < lastPriority)
				lastPriority = msg.getJMSPriority();
			current = msgStore.next(current);
		}
		
		if (failed)
			fail("Not ordered !");
	}
	
	public void testPriorityBasic() throws Exception
	{
		MessageStore msgStore = createMessageStore(true);
		
		Random rand = new Random(System.currentTimeMillis());
		
		long start = System.currentTimeMillis();
		int msgCount = 178;
		for (int i = 0; i < msgCount; i++)
		{
			AbstractMessage msg = new TextMessageImpl("msg"+i);
			msg.setJMSMessageID("ID:FOO"+i);
			msg.setJMSPriority(rand.nextInt(10));
			assertTrue(msgStore.store(msg) != -1);
			//msgStore.commitChanges();
		}
		msgStore.commitChanges();
		long end = System.currentTimeMillis();
		System.out.println("testPriorityBasic: "+(end-start));
		assertEquals(msgCount, msgStore.size());
		
		assertOrdered(msgStore);
		
		// Delete half the queue
		int count = 0;
		int current = msgStore.first();
		while (current != -1 && count < (msgCount/2))
		{
			int next = msgStore.next(current);
			msgStore.delete(current);
			count++;
			current = next;
		}
		msgStore.commitChanges();
		assertEquals(msgCount/2, msgStore.size());
		
		assertOrdered(msgStore);
		
		//System.out.println(msgStore.toString());
		
		for (int i = 0; i < msgCount/2; i++)
		{
			AbstractMessage msg = new TextMessageImpl("other_msg"+i);
			msg.setJMSMessageID("ID:BAR"+i);
			msg.setJMSPriority(rand.nextInt(10));
			assertTrue(msgStore.store(msg) != -1);
		}
		msgStore.commitChanges();
		assertEquals(msgCount, msgStore.size());
		
		//System.out.println(msgStore);
		assertOrdered(msgStore);
		
		msgStore.close();
		
		if (msgStore.getDeliveryMode() == DeliveryMode.PERSISTENT)
		{
			// Re-open
			msgStore = createMessageStore(false);
			
			assertEquals(msgCount, msgStore.size());
			assertOrdered(msgStore);
			
			count = 0;
			current = msgStore.first();
			while (current != -1)
			{
				int next = msgStore.next(current);
				msgStore.delete(current);
				count++;
				current = next;
			}
			msgStore.commitChanges();
			assertEquals(msgCount, count);
			
			msgStore.close();
			
			msgStore = createMessageStore(false);
			assertEquals(0, msgStore.size());
			msgStore.close();
		}
	}
	
	public void testPriorityInterlaced() throws Exception
	{
		MessageStore msgStore = createMessageStore(true);

		for (int i = 0; i < 10; i++)
		{		
			AbstractMessage msg = new TextMessageImpl("msg"+i);
			msg.setJMSMessageID("ID:FOO"+i);
			msg.setJMSPriority(i);
			msg.setJMSCorrelationID("ID"+i);
			msgStore.store(msg);
			msgStore.commitChanges();
		}
		assertEquals(10, msgStore.size());
		
		assertOrdered(msgStore);
		
		int current = msgStore.first();
		for(int n=0;n<5;n++)
			current = msgStore.next(current);
		Message removedMsg = msgStore.retrieve(current);
		msgStore.delete(current);
		msgStore.commitChanges();
		
		assertOrdered(msgStore);
		
		AbstractMessage msg = new TextMessageImpl("msgNEW");
		msg.setJMSMessageID("ID:XXX");
		msg.setJMSPriority(removedMsg.getJMSPriority()+1);
		msgStore.store(msg);
		msgStore.commitChanges();
		
		msg = new TextMessageImpl("msgNEW2");
		msg.setJMSMessageID("ID:YYY");
		msg.setJMSPriority(removedMsg.getJMSPriority());
		msgStore.store(msg);
		msgStore.commitChanges();
		
		//System.out.println(msgStore);
		
		assertOrdered(msgStore);
		msgStore.close();
	}
}
