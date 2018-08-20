package net.timewalker.ffmq4.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.timewalker.ffmq4.test.common.message.MessageSerializerTest;
import net.timewalker.ffmq4.test.local.LocalQueueMultiConnListenerTest;
import net.timewalker.ffmq4.test.local.LocalQueueMultiConnReceiverTest;
import net.timewalker.ffmq4.test.local.LocalQueueSingleConnListenerTest;
import net.timewalker.ffmq4.test.local.LocalQueueSingleConnReceiverTest;
import net.timewalker.ffmq4.test.local.LocalTopicMultiConnListenerTest;
import net.timewalker.ffmq4.test.local.LocalTopicMultiConnReceiverTest;
import net.timewalker.ffmq4.test.local.LocalTopicSingleConnListenerTest;
import net.timewalker.ffmq4.test.local.LocalTopicSingleConnReceiverTest;
import net.timewalker.ffmq4.test.local.selector.MessageSelectionTest;
import net.timewalker.ffmq4.test.local.session.LocalQueueSessionTest;
import net.timewalker.ffmq4.test.local.session.LocalSessionTest;
import net.timewalker.ffmq4.test.remote.RemoteQueueMultiConnListenerTest;
import net.timewalker.ffmq4.test.remote.RemoteQueueMultiConnReceiverTest;
import net.timewalker.ffmq4.test.remote.RemoteQueueSingleConnListenerTest;
import net.timewalker.ffmq4.test.remote.RemoteQueueSingleConnReceiverTest;
import net.timewalker.ffmq4.test.remote.RemoteToLocalTest;
import net.timewalker.ffmq4.test.remote.RemoteTopicMultiConnListenerTest;
import net.timewalker.ffmq4.test.remote.RemoteTopicMultiConnReceiverTest;
import net.timewalker.ffmq4.test.remote.RemoteTopicSingleConnListenerTest;
import net.timewalker.ffmq4.test.remote.RemoteTopicSingleConnReceiverTest;
import net.timewalker.ffmq4.test.remote.session.RemoteQueueSessionTest;
import net.timewalker.ffmq4.test.remote.session.RemoteSessionTest;

/**
 * AllTests
 */
public class AllServerTests extends TestCase
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite("All server tests");
        //$JUnit-BEGIN$
        suite.addTestSuite(MessageSerializerTest.class);
        
        suite.addTestSuite(LocalSessionTest.class);
        suite.addTestSuite(LocalQueueSessionTest.class);
        suite.addTestSuite(RemoteSessionTest.class);
        suite.addTestSuite(RemoteQueueSessionTest.class);
        suite.addTestSuite(MessageSelectionTest.class);
        
        suite.addTestSuite(RemoteToLocalTest.class);
        
        suite.addTestSuite(LocalQueueSingleConnReceiverTest.class);
        suite.addTestSuite(LocalQueueSingleConnListenerTest.class);
        suite.addTestSuite(LocalQueueMultiConnReceiverTest.class);
        suite.addTestSuite(LocalQueueMultiConnListenerTest.class);
        suite.addTestSuite(LocalTopicSingleConnReceiverTest.class);
        suite.addTestSuite(LocalTopicSingleConnListenerTest.class);
        suite.addTestSuite(LocalTopicMultiConnReceiverTest.class);
        suite.addTestSuite(LocalTopicMultiConnListenerTest.class);
        
        suite.addTestSuite(RemoteQueueSingleConnReceiverTest.class);
        suite.addTestSuite(RemoteQueueSingleConnListenerTest.class);
        suite.addTestSuite(RemoteQueueMultiConnReceiverTest.class);
        suite.addTestSuite(RemoteQueueMultiConnListenerTest.class);
        suite.addTestSuite(RemoteTopicSingleConnReceiverTest.class);
        suite.addTestSuite(RemoteTopicSingleConnListenerTest.class);
        suite.addTestSuite(RemoteTopicMultiConnReceiverTest.class);
        suite.addTestSuite(RemoteTopicMultiConnListenerTest.class);
        
        //$JUnit-END$
        return suite;
    }
}
