/*
 * This file is part of FFMQ.
 *
 * FFMQ is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * FFMQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FFMQ; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package net.timewalker.ffmq4.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.timewalker.ffmq4.test.remote.RemoteTopicMultiConnListenerTest;
import net.timewalker.ffmq4.test.remote.RemoteTopicMultiConnReceiverTest;
import net.timewalker.ffmq4.test.remote.RemoteTopicSingleConnListenerTest;
import net.timewalker.ffmq4.test.remote.RemoteTopicSingleConnReceiverTest;

/**
 * AllRemoteTopicTests
 */
public class AllRemoteTopicTests extends TestCase
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite("All tests");
        //$JUnit-BEGIN$
        suite.addTestSuite(RemoteTopicSingleConnReceiverTest.class);
        suite.addTestSuite(RemoteTopicSingleConnListenerTest.class);
        suite.addTestSuite(RemoteTopicMultiConnReceiverTest.class);
        suite.addTestSuite(RemoteTopicMultiConnListenerTest.class);
        
        //$JUnit-END$
        return suite;
    }
}
