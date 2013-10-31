package net.timewalker.ffmq3.test.local;

import net.timewalker.ffmq3.test.BaseCommTest;

/**
 * LocalQueueSingleConnReceiverTest
 */
public class LocalQueueSingleConnReceiverTest extends BaseCommTest
{    
    @Override
	protected boolean isRemote()               { return false; }
    @Override
	protected boolean useMultipleConnections() { return false; }
    @Override
	protected boolean isTopicTest()            { return false; }
    @Override
	protected boolean isListenerTest()         { return false; }
}
