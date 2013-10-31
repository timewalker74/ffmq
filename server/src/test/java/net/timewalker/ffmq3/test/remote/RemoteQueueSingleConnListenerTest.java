package net.timewalker.ffmq3.test.remote;

import net.timewalker.ffmq3.test.local.LocalQueueSingleConnListenerTest;

/**
 * RemoteQueueSingleConnListenerTest
 */
public class RemoteQueueSingleConnListenerTest extends LocalQueueSingleConnListenerTest
{    
    protected boolean isRemote() { return true; }
}
