package net.timewalker.ffmq4.test.remote;

import net.timewalker.ffmq4.test.local.LocalTopicSingleConnListenerTest;

/**
 * RemoteTopicSingleConnListenerTest
 */
public class RemoteTopicSingleConnListenerTest extends LocalTopicSingleConnListenerTest
{
    @Override
	protected boolean isRemote() { return true; }
}
