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

package net.timewalker.ffmq3.cluster.resolver;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import net.timewalker.ffmq3.management.destination.DestinationReferenceDescriptor;
import net.timewalker.ffmq3.management.peer.PeerDescriptor;

/**
 * <p>
 * Interface for a Destination resolver, ie. an object providing a destination reference
 * given a PeerDescriptor, a DestinationReferenceDescriptor and a target JMS session.
 * </p>
 * @see PeerDescriptor
 * @see DestinationReferenceDescriptor
 */
public interface DestinationResolver
{
	/**
	 * Get a destination reference by name an type (queue or topic)
	 * @param peer JMS peer descriptor
	 * @param destinationReference a destination reference descriptor
	 * @param session a JMS session
	 * @return a destination object reference
	 */
	public Destination getDestination( PeerDescriptor peer , DestinationReferenceDescriptor destinationReference , Session session ) throws JMSException;
}
