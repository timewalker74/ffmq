package net.timewalker.ffmq4.test.remote;

import net.timewalker.ffmq4.test.local.LocalQueueSingleConnListenerTest;

/**
 * RemoteQueueSingleConnListenerTest
 */
public class RemoteQueueSingleConnListenerTest extends LocalQueueSingleConnListenerTest
{    
    @Override
	protected boolean isRemote() { return true; }
}
