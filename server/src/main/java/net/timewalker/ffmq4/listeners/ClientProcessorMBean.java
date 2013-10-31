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

import java.util.Date;

/**
 * ClientProcessorMBean
 */
public interface ClientProcessorMBean
{
	/**
	 * Get the client ID
	 * @return the client ID
	 */
	public String getClientID();
	
	/**
	 * Get the client peer description
	 * @return the client peer description
	 */
	public String getPeerDescription();
	
	/**
	 * Test if the client is authenticated
	 * @return true if the client is authenticated
	 */
	public boolean isAuthenticated();
	
	/**
	 * Get the client last activity timestamp
	 * @return the client last activity timestamp
	 */
	public Date getConnectionDate();
	
	/**
	 * Get the number of sessions in use for this client
	 * @return the number of sessions in use for this client
	 */
	public int getSessionsCount();
	
	/**
	 * Get the number of consumers in use for this client
	 * @return the number of consumers in use for this client
	 */
	public int getConsumersCount();
	
	/**
	 * Get the number of producers in use for this client
	 * @return the number of producers in use for this client
	 */
	public int getProducersCount();
	
	/**
	 * Get a description of entities held by this client
	 * @return a description of entities held by this client
	 */
	public String getEntitiesDescription();
}
