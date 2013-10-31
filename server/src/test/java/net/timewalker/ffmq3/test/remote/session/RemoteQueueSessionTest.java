package net.timewalker.ffmq3.test.remote.session;

import net.timewalker.ffmq3.test.local.session.LocalQueueSessionTest;

/**
 * RemoteQueueSessionTest
 */
public class RemoteQueueSessionTest extends LocalQueueSessionTest
{
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.local.session.LocalSessionTest#isRemote()
     */
    protected boolean isRemote()
    {
        return true;
    }
}
