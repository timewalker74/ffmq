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

package net.timewalker.ffmq4.test.local.destination.store.impl;

import java.io.File;

import net.timewalker.ffmq4.management.destination.definition.QueueDefinition;
import net.timewalker.ffmq4.storage.data.impl.BlockBasedDataStoreTools;
import net.timewalker.ffmq4.storage.message.MessageStore;
import net.timewalker.ffmq4.storage.message.impl.BlockFileMessageStore;
import net.timewalker.ffmq4.utils.Settings;

/**
 * FailSafeBlockFileMessageStoreTest
 */
public class JournalingBlockFileMessageStoreTest extends BlockFileMessageStoreTest
{
	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq4.local.destination.store.impl.AbstractMessageStoreTest#createMessageStore()
	 */
	protected MessageStore createMessageStore() throws Exception 
	{
		BlockBasedDataStoreTools.create(id,new File("target/test"), 200, 512, true);
		
		Settings settings = new Settings();
        settings.setStringProperty("name", id);
        settings.setStringProperty("persistentStore.dataFolder", new File("target/test").getAbsolutePath());
        settings.setBooleanProperty("persistentStore.useJournal", true);
        QueueDefinition queueDef = new QueueDefinition(settings);
        
        MessageStore store = new BlockFileMessageStore(queueDef,asyncTaskManager);
        store.init();
	    
	    return store;
	}
}
