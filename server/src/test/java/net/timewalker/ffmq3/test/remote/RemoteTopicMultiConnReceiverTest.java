package net.timewalker.ffmq3.test.remote;

import net.timewalker.ffmq3.test.local.LocalTopicMultiConnReceiverTest;

/**
 * RemoteTopicMultiConnReceiverTest
 */
public class RemoteTopicMultiConnReceiverTest extends LocalTopicMultiConnReceiverTest
{
    @Override
	protected boolean isRemote() { return true; }
}
