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
package net.timewalker.ffmq3.management;

import net.timewalker.ffmq3.FFMQException;

/**
 * <p>Exception thrown when an invalid desciptor is found.</p>
 */
public final class InvalidDescriptorException extends FFMQException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * @param message
	 * @param linkedException
	 */
	public InvalidDescriptorException(String message, Throwable linkedException)
	{
		super(message, "INVALID_DESCRIPTOR", linkedException);
	}

	/**
	 * Constructor
	 * @param message
	 */
	public InvalidDescriptorException(String message)
	{
		super(message, "INVALID_DESCRIPTOR");
	}
}
