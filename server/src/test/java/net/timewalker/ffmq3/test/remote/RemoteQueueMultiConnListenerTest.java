package net.timewalker.ffmq3.test.remote;

import net.timewalker.ffmq3.test.local.LocalQueueMultiConnListenerTest;

/**
 * RemoteQueueMultiConnListenerTest
 */
public class RemoteQueueMultiConnListenerTest extends LocalQueueMultiConnListenerTest
{    
    @Override
	protected boolean isRemote() { return true; }
}
