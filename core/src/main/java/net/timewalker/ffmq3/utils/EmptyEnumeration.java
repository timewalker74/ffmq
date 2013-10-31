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

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * EmptyEnumeration
 */
public final class EmptyEnumeration implements Enumeration
{
	/**
	 * Constructor
	 */
	public EmptyEnumeration()
	{
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Enumeration#hasMoreElements()
	 */
	public boolean hasMoreElements()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Enumeration#nextElement()
	 */
	public Object nextElement()
	{
		throw new NoSuchElementException();
	}
}
