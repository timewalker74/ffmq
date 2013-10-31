package net.timewalker.ffmq3.test.local;

import net.timewalker.ffmq3.test.BaseCommTest;

/**
 * LocalQueueSingleConnReceiverTest
 */
public class LocalQueueSingleConnReceiverTest extends BaseCommTest
{    
    protected boolean isRemote()               { return false; }
    protected boolean useMultipleConnections() { return false; }
    protected boolean isTopicTest()            { return false; }
    protected boolean isListenerTest()         { return false; }
}
