package net.timewalker.ffmq3.test.local;

import net.timewalker.ffmq3.test.BaseCommTest;

/**
 * LocalTopicSingleConnListenerTest
 */
public class LocalTopicSingleConnListenerTest extends BaseCommTest
{
    protected boolean isRemote()               { return false; }
    protected boolean useMultipleConnections() { return false; }
    protected boolean isTopicTest()            { return true;  }
    protected boolean isListenerTest()         { return true;  }
}
