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

package net.timewalker.ffmq3.test.storage.data.impl;

import java.io.File;

import net.timewalker.ffmq3.management.destination.definition.QueueDefinition;
import net.timewalker.ffmq3.storage.data.DataStoreException;
import net.timewalker.ffmq3.storage.data.LinkedDataStore;
import net.timewalker.ffmq3.storage.data.impl.BlockBasedDataStoreTools;
import net.timewalker.ffmq3.storage.data.impl.JournalingBlockBasedDataStore;
import net.timewalker.ffmq3.utils.Settings;
import net.timewalker.ffmq3.utils.StringTools;
import net.timewalker.ffmq3.utils.async.AsyncTaskManager;
import net.timewalker.ffmq3.utils.id.UUIDProvider;

/**
 * JournalingBlockBasedDataStoreTest
 */
public class JournalingBlockBasedDataStoreTest extends BlockBasedDataStoreTest
{
	private AsyncTaskManager asyncTaskManager;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		super.setUp();
		asyncTaskManager = new AsyncTaskManager("testAsyncTaskManager", 
				1, 
				2, 
				10);
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		asyncTaskManager.close();
		super.tearDown();
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.test.storage.data.impl.BlockBasedDataStoreTest#createStore(java.lang.String, java.io.File)
	 */
	protected LinkedDataStore createStore(String baseName, File dataFolder) throws DataStoreException
	{
	    Settings settings = new Settings();
        settings.setStringProperty("name", baseName);
        settings.setStringProperty("persistentStore.dataFolder", dataFolder.getAbsolutePath());
        settings.setBooleanProperty("persistentStore.useJournal", true);
        settings.setIntProperty("persistentStore.maxBlockCount", 10000);
        settings.setIntProperty("persistentStore.autoExtendAmount", 500);
        QueueDefinition queueDef = new QueueDefinition(settings);
	    
		LinkedDataStore dataStore = new JournalingBlockBasedDataStore(queueDef,asyncTaskManager);
        dataStore.init();
        
		return dataStore;
	}
	
	public void testJournalReplay() throws Exception
	{
		String storeId = UUIDProvider.getInstance().getUUID();

		String msgBase = StringTools.rightPad("DATA-", 1000, 'X');
		byte[] data = msgBase.getBytes();
		
		System.setProperty("ffmq.dataStore.keepJournalFiles", "true");
		BlockBasedDataStoreTools.create(storeId, new File("target/test"), 2000, 512, true);
		LinkedDataStore store = createStore(storeId, new File("target/test"));
		for (int i = 0; i < 100; i++)
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
		assertEquals(0, store.size());
		store.close();
		System.setProperty("ffmq.dataStore.keepJournalFiles", "false");
		
		store = createStore(storeId, new File("target/test"));
		assertEquals(0, store.size());
		store.close();
		
		store = createStore(storeId, new File("target/test"));
        assertEquals(0, store.size());
        store.close();
	}
	
	public void testJournalReplay2() throws Exception
	{
		String storeId = UUIDProvider.getInstance().getUUID();

		String msgBase = StringTools.rightPad("DATA-", 1000, 'X');
		byte[] data = msgBase.getBytes();
		
		System.setProperty("ffmq.dataStore.keepJournalFiles", "true");
		BlockBasedDataStoreTools.create(storeId, new File("target/test"), 2000, 512, true);
		LinkedDataStore store = createStore(storeId, new File("target/test"));
		for (int i = 0; i < 10; i++)
		{
			for (int j = 0; j < 10; j++)
			{
				int previous = store.first();
				previous = store.store(data, previous);
				if (previous == -1)
	                throw new IllegalStateException("No space left !");
			}
			store.commitChanges();
		}
		assertEquals(100, store.size());
		store.close();
		System.setProperty("ffmq.dataStore.keepJournalFiles", "false");
		
		store = createStore(storeId, new File("target/test"));
		assertEquals(100, store.size());
		for (int i = 0; i < 10; i++)
		{
			for (int j = 0; j < 10; j++)
			{
				store.delete(store.first());
			}
			store.commitChanges();
		}
		assertEquals(0, store.size());
		store.close();
		
		store = createStore(storeId, new File("target/test"));
        assertEquals(0, store.size());
        store.close();
	}
}
