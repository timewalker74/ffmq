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
package net.timewalker.ffmq4.management;

import javax.jms.DeliveryMode;
import javax.jms.Session;

/**
 * ManagementUtils
 */
public final class ManagementUtils
{
	public static String acknowledgeModeAsString( int acknowledgeMode )
	{
		switch (acknowledgeMode)
		{
			case Session.AUTO_ACKNOWLEDGE    : return "auto";
			case Session.CLIENT_ACKNOWLEDGE  : return "client";
			case Session.DUPS_OK_ACKNOWLEDGE : return "dups_ok";
			case Session.SESSION_TRANSACTED  : return "session_transacted";
			default:
				throw new IllegalArgumentException("Invalid acknowledge mode : "+acknowledgeMode);	
		}
	}
	
	public static int parseAcknowledgeMode( String acknowledgeModeAsString )
	{
		if (acknowledgeModeAsString.equalsIgnoreCase("auto"))
			return Session.AUTO_ACKNOWLEDGE;
		if (acknowledgeModeAsString.equalsIgnoreCase("client"))
			return Session.CLIENT_ACKNOWLEDGE;
		if (acknowledgeModeAsString.equalsIgnoreCase("dups_ok"))
			return Session.DUPS_OK_ACKNOWLEDGE;
		if (acknowledgeModeAsString.equalsIgnoreCase("session_transacted"))
			return Session.SESSION_TRANSACTED;
		throw new IllegalArgumentException("Invalid acknowledge mode : "+acknowledgeModeAsString);
	}
	
	public static String deliveryModeAsString( int deliveryMode )
	{
		switch (deliveryMode)
		{
			case DeliveryMode.PERSISTENT     : return "persistent";
			case DeliveryMode.NON_PERSISTENT : return "non_persistent";
			default:
				throw new IllegalArgumentException("Invalid delivery mode : "+deliveryMode);	
		}
	}
	
	public static int parseDeliveryMode( String deliveryModeAsString )
	{
		if (deliveryModeAsString.equalsIgnoreCase("persistent"))
			return DeliveryMode.PERSISTENT;
		if (deliveryModeAsString.equalsIgnoreCase("non_persistent"))
			return DeliveryMode.NON_PERSISTENT;
		throw new IllegalArgumentException("Invalid delivery mode : "+deliveryModeAsString);
	}
}
