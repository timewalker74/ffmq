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

package net.timewalker.ffmq4.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * <p>Inet utilities.</p>
 */
public final class InetUtils
{
	public static String findInterfaceAddress( String interfaceName )
	{
		try
		{
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			if (interfaces != null)
			{
		        while (interfaces.hasMoreElements())
		        {
		            NetworkInterface iface = interfaces.nextElement();
		            if (iface.getName().equals(interfaceName))
		            {
		            	Enumeration<InetAddress> addresses = iface.getInetAddresses();
		            	if (!addresses.hasMoreElements())
		            		throw new IllegalArgumentException("Network interface "+interfaceName+" has no attached address");
		            	
		            	InetAddress address = addresses.nextElement();
		            	return address.getHostAddress();
		            }
		        }
			}
	        throw new IllegalArgumentException("No network interface with that name : "+interfaceName);
		}
		catch (IllegalArgumentException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new IllegalStateException("Cannot find interface address : "+e);
		}
	}
	
	public static String resolveAutoInterfaceAddress( String address )
    {
    	if (address == null)
    		return null;
    	
    	if (address.startsWith("auto:"))
    		return InetUtils.findInterfaceAddress(address.substring(5));
    	
    	return address;
    }
}