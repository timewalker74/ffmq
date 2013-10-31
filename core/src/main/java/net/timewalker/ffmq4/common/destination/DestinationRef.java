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
package net.timewalker.ffmq3.common.destination;

import java.io.Serializable;

import javax.jms.Destination;
import javax.naming.Referenceable;

/**
 * <p>
 * Base implementation for a Destination reference (not the real destination).
 * Implements both {@link Serializable} and {@link Referenceable} to enable 
 * remote usage over JNDI.
 * </p>
 */
public abstract class DestinationRef implements Destination, Serializable, Referenceable
{	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Get the resource name for this destination reference
	 */
    public abstract String getResourceName();
}
