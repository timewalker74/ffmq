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
package net.timewalker.ffmq3.listeners;

import javax.jms.JMSException;

/**
 * Listener
 */
public interface ClientListener
{
	/**
	 * Get the listener name
	 */
	public String getName();
	
	/**
	 * Get the associated local engine name
	 */
	public String getEngineName();
	
	/**
	 * Start the listener
	 */
	public void start() throws JMSException;
	
	/**
	 * Stop the listener
	 */
	public void stop();
	
	/**
	 * Test if the listener is started
	 */
	public boolean isStarted();
	
	/**
	 * Get the current number of active clients for this listener
	 * @return the current number of active clients for this listener
	 */
	public int getActiveClients();
	
	/**
	 * Get the total number of clients accepted by this listener
	 * since startup or statistics reset
	 * @return the total number of clients accepted by this listener
	 */
	public int getAcceptedTotal();
	
	/**
	 * Get the total number of clients dropped by this listener
	 * since startup or statistics reset
	 * @return the total number of clients dropped by this listener
	 */
	public int getDroppedTotal();
	
	/**
	 * Get the peak number of active clients for this listener
	 * since startup or statistics reset
	 * @return the peak number of active clients for this listener
	 */
	public int getMaxActiveClients();
	
	/**
	 * Get the listener max capacity
	 * @return the listener max capacity
	 */
	public int getCapacity();
	
	/**
	 * Reset this listener's statistics
	 */
	public void resetStats();
}
