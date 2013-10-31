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

package net.timewalker.ffmq3.local.destination.subscription;

import java.util.Hashtable;
import java.util.Map;

import net.timewalker.ffmq3.local.FFMQEngine;

/**
 * <p>Manager for durable subscriptions. 
 * Keeps track of the active durable subscriptions in a given {@link FFMQEngine}.</p>
 */
public final class DurableSubscriptionManager
{
    // Attributes
	private Map<String,DurableTopicSubscription> subscriptions = new Hashtable<>();
	
	/**
	 * Register a new durable subscription
	 * @param clientID
	 * @param subscriptionName
	 */
	public boolean register( String clientID , String subscriptionName )
	{
		String key = clientID+"-"+subscriptionName;
		synchronized (subscriptions)
		{
			if (subscriptions.containsKey(key))
				return false;
			
			subscriptions.put(key, new DurableTopicSubscription(System.currentTimeMillis(),
					                                            clientID,
					                                            subscriptionName));
			return true;
		}
	}
	
	/**
	 * Unregister a durable subscription
	 * @param clientID
	 * @param subscriptionName
	 * @return true if the subscription was removed, false if not found
	 */
	public boolean unregister( String clientID , String subscriptionName  )
	{
		String key = clientID+"-"+subscriptionName;
		return subscriptions.remove(key) != null;
	}

	/**
	 * Test if a durable subscription exists
	 * @param clientID
	 * @param subscriptionName
	 * @return true if the subscription exists
	 */
	public boolean isRegistered( String clientID , String subscriptionName )
	{
		String key = clientID+"-"+subscriptionName;
		return subscriptions.containsKey(key);
	}
}
