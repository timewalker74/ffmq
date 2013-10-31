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

package net.timewalker.ffmq3.utils;

import javax.jms.JMSException;

import net.timewalker.ffmq3.utils.concurrent.SynchronizationBarrier;

/**
 * Committable
 */
public interface Committable
{
	/**
     * Get the committable name
     */
    public String getName();
    
	/**
     * Lock the destination for transactional update
     */
    public void openTransaction();

    /**
     * Unlock the destination for transactional update
     */
    public void closeTransaction();
    
    /**
     * Commit all changes (make sure they are really applied)
     */
    public void commitChanges() throws JMSException;
    
	/**
     * Commit all changes (make sure they are really applied) (ASYNCHRONOUS)
     * @param barrier a synchronization barrier
     */
    public void commitChanges( SynchronizationBarrier barrier ) throws JMSException;
}
