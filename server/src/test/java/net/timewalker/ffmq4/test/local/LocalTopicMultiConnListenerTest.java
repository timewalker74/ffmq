package net.timewalker.ffmq4.test.local;

import net.timewalker.ffmq4.test.BaseCommTest;

/**
 * LocalTopicMultiConnListenerTest
 */
public class LocalTopicMultiConnListenerTest extends BaseCommTest
{
    @Override
	protected boolean isRemote()               { return false; }
    @Override
	protected boolean useMultipleConnections() { return true;  }
    @Override
	protected boolean isTopicTest()            { return true;  }
    @Override
	protected boolean isListenerTest()         { return true;  }
}
