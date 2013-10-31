package net.timewalker.ffmq3.test.local;

import net.timewalker.ffmq3.test.BaseCommTest;

/**
 * LocalTopicMultiConnListenerTest
 */
public class LocalTopicMultiConnListenerTest extends BaseCommTest
{
    protected boolean isRemote()               { return false; }
    protected boolean useMultipleConnections() { return true;  }
    protected boolean isTopicTest()            { return true;  }
    protected boolean isListenerTest()         { return true;  }
}
