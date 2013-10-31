package net.timewalker.ffmq3.test.remote;

import net.timewalker.ffmq3.test.local.LocalQueueSingleConnReceiverTest;

/**
 * RemoteQueueSingleConnReceiverTest
 */
public class RemoteQueueSingleConnReceiverTest extends LocalQueueSingleConnReceiverTest
{        
    @Override
	protected boolean isRemote() { return true; }
}
