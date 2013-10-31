package net.timewalker.ffmq3.test.local;

import net.timewalker.ffmq3.test.BaseCommTest;

/**
 * LocalTopicMultiConnReceiverTest
 */
public class LocalTopicMultiConnReceiverTest extends BaseCommTest
{    
    protected boolean isRemote()               { return false; }
    protected boolean useMultipleConnections() { return true;  }
    protected boolean isTopicTest()            { return true;  }
    protected boolean isListenerTest()         { return false; }
}
