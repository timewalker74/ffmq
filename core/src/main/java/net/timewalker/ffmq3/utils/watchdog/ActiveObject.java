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
package net.timewalker.ffmq3.utils.watchdog;

/**
 * <p>An active object is supposed to have regular activity.</p>
 * <p>
 * If the object last activity gets too old (older than the configured timeout delay), 
 * the activity watchdog will kick in and call the onActivityTimeout() method of
 * the object.
 * </p>
 */
public interface ActiveObject
{
	/**
	 * Get a timestamp of this object's last activity
	 * @return a java timestamp
	 */
	public long getLastActivity();
	
	/**
	 * Get the maximum allowed time between activities
	 * @return a delay in milliseconds
	 */
	public long getTimeoutDelay();
	
	/**
	 * Called if the object has been inactive for more
	 * @return true if the object should be unregistered immediately upon return
	 * @throws Exception
	 */
	public boolean onActivityTimeout() throws Exception;
}
