package net.timewalker.ffmq3.test.storage.data.impl;

import java.io.File;

import junit.framework.TestCase;
import net.timewalker.ffmq3.management.destination.definition.QueueDefinition;
import net.timewalker.ffmq3.storage.data.DataStoreException;
import net.timewalker.ffmq3.storage.data.LinkedDataStore;
import net.timewalker.ffmq3.storage.data.impl.BlockBasedDataStore;
import net.timewalker.ffmq3.storage.data.impl.BlockBasedDataStoreTools;
import net.timewalker.ffmq3.utils.Settings;
import net.timewalker.ffmq3.utils.StringTools;
import net.timewalker.ffmq3.utils.id.UUIDProvider;

/**
 * BlockBasedDataStoreTest
 */
public class BlockBasedDataStoreTest extends TestCase
{
	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		File dataDir = new File("target/test");
		dataDir.mkdir();
		
		System.setProperty("ffmq.dataStore.safeMode", "true");
	}
	
	public void testLongevity() throws Exception
	{
		String storeId = UUIDProvider.getInstance().getUUID();

		String msgBase = StringTools.rightPad("DATA-", 1000, 'X');
		byte[] data = msgBase.getBytes();
		
		BlockBasedDataStoreTools.create(storeId, new File("target/test"), 200, 512, true);
		LinkedDataStore store = createStore(storeId, new File("target/test"));
		
		for (int i = 0; i < 500; i++)
		{
			for (int j = 0; j < 10; j++)
			{
				int previous = store.first();
				previous = store.store(data, previous);
				if (previous == -1)
	                throw new IllegalStateException("No space left !");
			}
			store.commitChanges();
			for (int j = 0; j < 10; j++)
			{
				store.delete(store.first());
			}
			store.commitChanges();
		}
		
		store.close();
	}
	
	public void testLongevityNoCommit() throws Exception
	{
		String storeId = UUIDProvider.getInstance().getUUID();

		String msgBase = StringTools.rightPad("DATA-", 1000, 'X');
		byte[] data = msgBase.getBytes();
		
		BlockBasedDataStoreTools.create(storeId, new File("target/test"), 200, 512, true);
		LinkedDataStore store = createStore(storeId, new File("target/test"));
		
		for (int i = 0; i < 500; i++)
		{
			for (int j = 0; j < 10; j++)
			{
				int previous = store.first();
				previous = store.store(data, previous);
				if (previous == -1)
	                throw new IllegalStateException("No space left !");
			}
			//store.commitChanges();
			for (int j = 0; j < 10; j++)
			{
				store.delete(store.first());
			}
			//store.commitChanges();
		}
		
		store.close();
	}
	
	public void testFragmented() throws Exception
	{
		System.out.println("--------- Fragmented test ---------");
		doTest(512, 500, 1000);
	}

	public void testNonFragmented() throws Exception
	{
		System.out.println("--------- Non fragmented test ---------");
		doTest(1024, 1000, 1000);
	}

    protected LinkedDataStore createStore(String baseName, File dataFolder) throws DataStoreException
    {
        Settings settings = new Settings();
        settings.setStringProperty("name", baseName);
        settings.setStringProperty("persistentStore.dataFolder", dataFolder.getAbsolutePath());
        settings.setIntProperty("persistentStore.maxBlockCount", 10000);
        settings.setIntProperty("persistentStore.autoExtendAmount", 500);
        settings.setBooleanProperty("persistentStore.useJournal", false);        
        QueueDefinition queueDef = new QueueDefinition(settings);
        
    	LinkedDataStore dataStore = new BlockBasedDataStore(queueDef);
        dataStore.init();
        
		return dataStore;
    }

	private void doTest(int blockSize, int msgCount, int msgSize) throws Exception
	{
		String storeId = UUIDProvider.getInstance().getUUID();

		String msgBase = StringTools.rightPad("DATA-", msgSize, 'X');

		BlockBasedDataStoreTools.create(storeId, new File("target/test"),
		                                200, blockSize, true);
		LinkedDataStore store = createStore(storeId, new File("target/test"));

		// -------------------------------------------------------------------
		// Insert some messages

		long startTime = System.currentTimeMillis();
		int previous = -1;
		for (int n = 0; n < msgCount; n++)
		{
			byte[] data = (msgBase + n).getBytes();
			previous = store.store(data, previous);
			if (previous == -1)
			    throw new IllegalStateException("No space left !");
			// System.out.println(store);
		}
		store.commitChanges();
		long endTime = System.currentTimeMillis();
		System.out.println("Insertion time " + (endTime - startTime) + " ms");

		//System.out.println(store);
		store.close();

		// -------------------------------------------------------------------
		// Reload store

		store = createStore(storeId, new File("target/test"));

		//System.out.println(store);
		int count = 0;
		int current = store.first();
		while (current != -1)
		{
			store.retrieve(current);
			current = store.next(current);
			count++;
		}
		// System.out.println(store);
		assertEquals(msgCount, count);
		assertEquals(msgCount, store.size());
		// System.out.println(store);

		// -------------------------------------------------------------------
		// Delete half the messages

		count = 0;
		current = store.first();
		while (current != -1)
		{
			int next = store.next(current);
			if (next != -1)
				next = store.next(next);

			store.delete(current);
			count++;

			current = next;
		}
		store.commitChanges();
		assertEquals(msgCount - count, store.size());
		// System.out.println(store);
		store.close();

		// -------------------------------------------------------------------
		// Reload store
		store = createStore(storeId, new File("target/test"));
		assertEquals(msgCount - count, store.size());
		//System.out.println(store);

		// -------------------------------------------------------------------
		// Add back some messages

		int pos = -1;
		for (int n = 0; n < count; n++)
		{
			byte[] data = (msgBase + n).getBytes();
			previous = store.store(data, pos);
			if (previous == -1)
                throw new IllegalStateException("No space left !");
			
			pos = store.next(previous);
			if (pos != -1)
				pos = store.next(pos);
			// pos+=2+data.length/BLOCK_SIZE;
		}
		store.commitChanges();
		assertEquals(msgCount, store.size());
		System.out.println(store);

		store.close();

		// -------------------------------------------------------------------
		// Reload store

		store = createStore(storeId, new File("target/test"));
		assertEquals(msgCount, store.size());

		System.out.println(store);
		
		count = 0;
		current = store.first();
		while (current != -1)
		{
			store.retrieve(current);
			current = store.next(current);
			count++;
		}
		assertEquals(msgCount, count);
		assertEquals(msgCount, store.size());

		store.close();

		// -------------------------------------------------------------------
		// Delete everything

		store = createStore(storeId, new File("target/test"));
		assertEquals(msgCount, store.size());

		count = 0;
		startTime = System.currentTimeMillis();
		current = store.first();
		while (current != -1)
		{
			int next = store.next(current);
			store.delete(current);
			current = next;
			count++;
		}
		store.commitChanges();
		endTime = System.currentTimeMillis();
		System.out.println("Deletion time " + (endTime - startTime) + " ms");
		assertEquals(msgCount, count);
		assertEquals(0, store.size());
		assertEquals(-1, store.first());

		store.close();

		// -------------------------------------------------------------------
		// Reload store

		store = createStore(storeId, new File("target/test"));
		assertEquals(0, store.size());
		assertEquals(-1, store.first());

		store.close();
	}
}
