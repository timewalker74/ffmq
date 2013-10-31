package net.timewalker.ffmq4.test.remote;

import net.timewalker.ffmq4.test.local.LocalTopicMultiConnListenerTest;

/**
 * RemoteTopicMultiConnListenerTest
 */
public class RemoteTopicMultiConnListenerTest extends LocalTopicMultiConnListenerTest
{
    @Override
	protected boolean isRemote() { return true; }
}
