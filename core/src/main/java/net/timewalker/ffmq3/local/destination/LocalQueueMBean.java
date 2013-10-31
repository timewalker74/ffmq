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
package net.timewalker.ffmq3.local.destination;

import javax.jms.Queue;

/**
 * <p>JMX interface for a local JMS {@link Queue}</p>
 */
public interface LocalQueueMBean extends LocalDestinationMBean
{
    /**
     * Get the number of messages sent to this queue (since startup or last reset)
     */
    public long getSentToQueueCount();

    /**
     * Get the number of messages received from this queue (since startup or last reset)
     */
    public long getReceivedFromQueueCount();

    /**
     * Get the number of acknowledged get operations on this queue (since startup or last reset)
     */
    public long getAcknowledgedGetCount();

    /**
     * Get the number of rollbacked get operations on this queue (since startup or last reset)
     */
    public long getRollbackedGetCount();
    
    /**
	 * Get the number of messages that were removed from this queue because they expired (since startup or last reset)
	 */
	public long getExpiredCount();
	
	/**
	 * Get the usage amount (%) for the memory store
	 */
	public int getMemoryStoreUsage();
	
	/**
	 * Get the usage amount (%) for the memory store
	 */
	public int getPersistentStoreUsage();
}
