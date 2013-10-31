package net.timewalker.ffmq3.test.remote.session;

import net.timewalker.ffmq3.test.local.session.LocalSessionTest;

/**
 * RemoteSessionTest
 */
public class RemoteSessionTest extends LocalSessionTest
{
    @Override
	protected boolean isRemote()
    {
        return true;
    }
}
