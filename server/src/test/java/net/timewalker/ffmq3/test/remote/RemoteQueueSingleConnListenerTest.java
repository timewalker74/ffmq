package net.timewalker.ffmq3.test.remote;

import net.timewalker.ffmq3.test.local.LocalQueueSingleConnListenerTest;

/**
 * RemoteQueueSingleConnListenerTest
 */
public class RemoteQueueSingleConnListenerTest extends LocalQueueSingleConnListenerTest
{    
    @Override
	protected boolean isRemote() { return true; }
}
