package net.timewalker.ffmq3.test.local;

import net.timewalker.ffmq3.test.BaseCommTest;

/**
 * LocalTopicMultiConnReceiverTest
 */
public class LocalTopicMultiConnReceiverTest extends BaseCommTest
{    
    @Override
	protected boolean isRemote()               { return false; }
    @Override
	protected boolean useMultipleConnections() { return true;  }
    @Override
	protected boolean isTopicTest()            { return true;  }
    @Override
	protected boolean isListenerTest()         { return false; }
}
