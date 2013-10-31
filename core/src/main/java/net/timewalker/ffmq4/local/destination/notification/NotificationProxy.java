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

package net.timewalker.ffmq4.local.destination.notification;

import net.timewalker.ffmq4.common.message.AbstractMessage;
import net.timewalker.ffmq4.utils.id.IntegerID;

/**
 * <p>
 *  Interface for a notification proxy. 
 *  A notification proxy propagates message availability events to upper layers of the implementation.
 * </p>
 */
public interface NotificationProxy
{
	/**
     * Send a notification packet through this proxy
     */
    public void addNotification( IntegerID consumerId , AbstractMessage prefetchedMessage );
    
    /**
     * Flush buffered notifications
     */
    public void flush();
}
