package net.timewalker.ffmq4.test.remote;

import net.timewalker.ffmq4.test.local.LocalQueueMultiConnReceiverTest;

/**
 * RemoteQueueMultiConnReceiverTest
 */
public class RemoteQueueMultiConnReceiverTest extends LocalQueueMultiConnReceiverTest
{        
    @Override
	protected boolean isRemote() { return true; }
}
