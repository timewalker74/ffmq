package net.timewalker.ffmq4.test.remote;

import net.timewalker.ffmq4.test.local.LocalQueueMultiConnListenerTest;

/**
 * RemoteQueueMultiConnListenerTest
 */
public class RemoteQueueMultiConnListenerTest extends LocalQueueMultiConnListenerTest
{    
    @Override
	protected boolean isRemote() { return true; }
}
