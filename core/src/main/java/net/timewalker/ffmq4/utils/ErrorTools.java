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

import javax.jms.JMSException;

import org.apache.commons.logging.Log;

/**
 * ErrorTools
 */
public final class ErrorTools
{
	/**
	 * Log a JMS exception with an error level
	 * @param e
	 * @param log
	 */
	public static void log( JMSException e , Log log )
	{
		log(null,e,log);
	}
	
	/**
	 * Log a JMS exception with an error level
	 * @param e
	 * @param log
	 */
	public static void log( String context , JMSException e , Log log )
	{
		StringBuilder message = new StringBuilder();
		if (context != null)
		{
			message.append("[");
			message.append(context);
			message.append("] ");
		} 
		if (e.getErrorCode() != null)
		{
			message.append("error={");
			message.append(e.getErrorCode());
			message.append("} ");
		}
		message.append(e.getMessage());
		log.error(message.toString());
		if (e.getLinkedException() != null)
			log.error("Linked exception was :",e.getLinkedException());
	}
}
