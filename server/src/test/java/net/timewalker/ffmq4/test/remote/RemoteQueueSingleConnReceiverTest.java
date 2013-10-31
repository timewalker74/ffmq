package net.timewalker.ffmq4.test.remote;

import net.timewalker.ffmq4.test.local.LocalQueueSingleConnReceiverTest;

/**
 * RemoteQueueSingleConnReceiverTest
 */
public class RemoteQueueSingleConnReceiverTest extends LocalQueueSingleConnReceiverTest
{        
    @Override
	protected boolean isRemote() { return true; }
}
