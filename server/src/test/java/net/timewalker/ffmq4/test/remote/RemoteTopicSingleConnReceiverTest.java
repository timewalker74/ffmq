package net.timewalker.ffmq3.test.remote;

import net.timewalker.ffmq3.test.local.LocalTopicSingleConnReceiverTest;

/**
 * RemoteTopicSingleConnReceiverTest
 */
public class RemoteTopicSingleConnReceiverTest extends LocalTopicSingleConnReceiverTest
{
    @Override
	protected boolean isRemote() { return true; }
}
