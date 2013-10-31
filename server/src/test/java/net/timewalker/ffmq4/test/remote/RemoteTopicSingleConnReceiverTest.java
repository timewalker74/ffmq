package net.timewalker.ffmq4.test.remote;

import net.timewalker.ffmq4.test.local.LocalTopicSingleConnReceiverTest;

/**
 * RemoteTopicSingleConnReceiverTest
 */
public class RemoteTopicSingleConnReceiverTest extends LocalTopicSingleConnReceiverTest
{
    @Override
	protected boolean isRemote() { return true; }
}
