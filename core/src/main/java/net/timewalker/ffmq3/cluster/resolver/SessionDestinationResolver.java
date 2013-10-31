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
 * JMS session-based implementation of a DestinationResolver
 * </p>
 * @see DestinationResolver
 */
public class SessionDestinationResolver implements DestinationResolver 
{
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.cluster.resolver.DestinationResolver#getDestination(net.timewalker.ffmq3.management.peer.PeerDescriptor, net.timewalker.ffmq3.management.destination.DestinationReferenceDescriptor, javax.jms.Session)
	 */
	public Destination getDestination(PeerDescriptor peer, DestinationReferenceDescriptor destinationReference,Session session) throws JMSException
	{
		String destinationType = destinationReference.getDestinationType();
		String destinationName = destinationReference.getDestinationName();
		
		if (destinationType.equalsIgnoreCase("queue"))
		{
			return session.createQueue(destinationName);
		}
		else
		if (destinationType.equalsIgnoreCase("topic"))
		{
			return session.createTopic(destinationName);
		}
		else
			throw new IllegalArgumentException("Invalid destination type : "+destinationType);
	}
}
