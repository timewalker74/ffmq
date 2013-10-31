package net.timewalker.ffmq3.test.remote;

import net.timewalker.ffmq3.test.local.LocalTopicSingleConnReceiverTest;

/**
 * RemoteTopicSingleConnReceiverTest
 */
public class RemoteTopicSingleConnReceiverTest extends LocalTopicSingleConnReceiverTest
{
    protected boolean isRemote() { return true; }
}
