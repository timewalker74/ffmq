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
package net.timewalker.ffmq4.local.destination;

import net.timewalker.ffmq4.management.destination.DestinationDescriptorMBean;

/**
 * <p>JMX interface for a local JMS destination</p>
 */
public interface LocalDestinationMBean extends DestinationDescriptorMBean
{
    /**
     * Reset statistics on this destination
     */
    public void resetStats();
    
    /**
     * Get the destination size (number of contained messages)
     */
    public int getSize();
    
    /**
     * Get the number of currently registered consumers on this destination
     */
    public int getRegisteredConsumersCount();
    
    /**
     * Get the minimum commit time for this queue (milliseconds)
     */
	public long getMinCommitTime();
	
	/**
     * Get the maximum commit time for this queue (milliseconds)
     */
	public long getMaxCommitTime();
    
    /**
     * Get the average commit time for this queue (milliseconds)
     */
    public double getAverageCommitTime();
}
