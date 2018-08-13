package net.timewalker.ffmq3.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.timewalker.ffmq3.test.common.message.BytesMessageImplTest;
import net.timewalker.ffmq3.test.common.message.EmptyMessageImplTest;
import net.timewalker.ffmq3.test.common.message.MapMessageImplTest;
import net.timewalker.ffmq3.test.common.message.StreamMessageImplTest;
import net.timewalker.ffmq3.test.common.message.selector.MessageSelectorParserTest;
import net.timewalker.ffmq3.test.common.message.selector.expression.utils.StringUtilsTest;
import net.timewalker.ffmq3.test.jndi.JndiTest;
import net.timewalker.ffmq3.test.local.TransactionSetTest;
import net.timewalker.ffmq3.test.local.destination.store.impl.BlockFileMessageStoreTest;
import net.timewalker.ffmq3.test.local.destination.store.impl.InMemoryMessageStoreTest;
import net.timewalker.ffmq3.test.local.destination.store.impl.JournalingBlockFileMessageStoreTest;
import net.timewalker.ffmq3.test.storage.data.impl.BlockBasedDataStoreTest;
import net.timewalker.ffmq3.test.storage.data.impl.InMemoryLinkedObjectStoreTest;
import net.timewalker.ffmq3.test.storage.data.impl.JournalingBlockBasedDataStoreTest;

/**
 * AllTests
 */
public class AllCoreTests extends TestCase
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite("All core tests");
        //$JUnit-BEGIN$
        suite.addTestSuite(BytesMessageImplTest.class);
        suite.addTestSuite(EmptyMessageImplTest.class);
        suite.addTestSuite(MapMessageImplTest.class);
        suite.addTestSuite(StreamMessageImplTest.class);
        suite.addTestSuite(TransactionSetTest.class);
        suite.addTestSuite(StringUtilsTest.class);
        suite.addTestSuite(MessageSelectorParserTest.class);
        suite.addTestSuite(JndiTest.class);
        suite.addTestSuite(BlockFileMessageStoreTest.class);
        suite.addTestSuite(JournalingBlockFileMessageStoreTest.class);
        suite.addTestSuite(InMemoryMessageStoreTest.class);
        suite.addTestSuite(InMemoryLinkedObjectStoreTest.class);
        suite.addTestSuite(BlockBasedDataStoreTest.class);
        suite.addTestSuite(JournalingBlockBasedDataStoreTest.class);
        //$JUnit-END$
        return suite;
    }
}
