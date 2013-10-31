package net.timewalker.ffmq3.test.local.destination.store.impl;

import net.timewalker.ffmq3.management.destination.definition.QueueDefinition;
import net.timewalker.ffmq3.storage.message.MessageStore;
import net.timewalker.ffmq3.storage.message.impl.InMemoryMessageStore;
import net.timewalker.ffmq3.utils.Settings;

/**
 * InMemoryMessageStoreTest
 */
public class InMemoryMessageStoreTest extends AbstractMessageStoreTest
{
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.test.local.destination.store.impl.AbstractMessageStoreTest#createMessageStore(boolean)
	 */
	@Override
	protected MessageStore createMessageStore(boolean createStoreFiles) throws Exception
	{
	    Settings settings = new Settings();
        settings.setStringProperty("name", getClass().getName());
        settings.setIntProperty("memoryStore.maxMessages", 10000);
        QueueDefinition queueDef = new QueueDefinition(settings);
	    
        MessageStore store = new InMemoryMessageStore(queueDef);
        store.init();
        
        return store;
	}
}
