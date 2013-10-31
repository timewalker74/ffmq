package net.timewalker.ffmq3.test.remote;

import net.timewalker.ffmq3.test.local.LocalQueueMultiConnReceiverTest;

/**
 * RemoteQueueMultiConnReceiverTest
 */
public class RemoteQueueMultiConnReceiverTest extends LocalQueueMultiConnReceiverTest
{        
    @Override
	protected boolean isRemote() { return true; }
}
