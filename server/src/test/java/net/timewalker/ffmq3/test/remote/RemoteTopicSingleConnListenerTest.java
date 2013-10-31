package net.timewalker.ffmq3.test.remote;

import net.timewalker.ffmq3.test.local.LocalTopicSingleConnListenerTest;

/**
 * RemoteTopicSingleConnListenerTest
 */
public class RemoteTopicSingleConnListenerTest extends LocalTopicSingleConnListenerTest
{
    protected boolean isRemote() { return true; }
}
