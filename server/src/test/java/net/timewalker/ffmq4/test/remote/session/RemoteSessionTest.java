package net.timewalker.ffmq4.test.remote.session;

import net.timewalker.ffmq4.test.local.session.LocalSessionTest;

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
