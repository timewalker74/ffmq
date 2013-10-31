package net.timewalker.ffmq4.test.remote;

import net.timewalker.ffmq4.test.local.LocalTopicMultiConnReceiverTest;

/**
 * RemoteTopicMultiConnReceiverTest
 */
public class RemoteTopicMultiConnReceiverTest extends LocalTopicMultiConnReceiverTest
{
    @Override
	protected boolean isRemote() { return true; }
}
