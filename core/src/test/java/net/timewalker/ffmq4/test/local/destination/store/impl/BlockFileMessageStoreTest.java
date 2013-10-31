package net.timewalker.ffmq3.test.local.destination.store.impl;

import java.io.File;

import net.timewalker.ffmq3.management.destination.definition.QueueDefinition;
import net.timewalker.ffmq3.storage.data.impl.BlockBasedDataStoreTools;
import net.timewalker.ffmq3.storage.message.MessageStore;
import net.timewalker.ffmq3.storage.message.impl.BlockFileMessageStore;
import net.timewalker.ffmq3.utils.Settings;
import net.timewalker.ffmq3.utils.id.UUIDProvider;

/**
 * BlockFileMessageStoreTest
 */
public class BlockFileMessageStoreTest extends AbstractMessageStoreTest
{	
	protected String id = UUIDProvider.getInstance().getUUID();
	
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
	protected void setUp() throws Exception
    {
        super.setUp();
        File dataDir = new File("target/test");
        dataDir.mkdir();
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.test.local.destination.store.impl.AbstractMessageStoreTest#createMessageStore(boolean)
     */
    @Override
	protected MessageStore createMessageStore(boolean createStoreFiles) throws Exception 
	{
    	if (createStoreFiles)
    		BlockBasedDataStoreTools.create(id,new File("target/test"), 200, 512, true);
		
		Settings settings = new Settings();
        settings.setStringProperty("name", id);
        settings.setStringProperty("persistentStore.dataFolder", new File("target/test").getAbsolutePath());
        settings.setBooleanProperty("persistentStore.useJournal", false);
        QueueDefinition queueDef = new QueueDefinition(settings);
		
        MessageStore store = new BlockFileMessageStore(queueDef,asyncTaskManager);
        store.init();
        
        return store;
	}
}
