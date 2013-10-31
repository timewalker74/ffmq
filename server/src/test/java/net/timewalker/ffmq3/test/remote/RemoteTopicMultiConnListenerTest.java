package net.timewalker.ffmq3.test.remote;

import net.timewalker.ffmq3.test.local.LocalTopicMultiConnListenerTest;

/**
 * RemoteTopicMultiConnListenerTest
 */
public class RemoteTopicMultiConnListenerTest extends LocalTopicMultiConnListenerTest
{
    protected boolean isRemote() { return true; }
}
