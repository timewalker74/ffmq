package net.timewalker.ffmq4.test.remote.session;

import net.timewalker.ffmq4.test.local.session.LocalQueueSessionTest;

/**
 * RemoteQueueSessionTest
 */
public class RemoteQueueSessionTest extends LocalQueueSessionTest
{
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.local.session.LocalSessionTest#isRemote()
     */
    @Override
	protected boolean isRemote()
    {
        return true;
    }
}
