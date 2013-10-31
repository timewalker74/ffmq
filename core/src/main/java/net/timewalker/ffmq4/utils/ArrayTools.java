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

/**
 * ArrayTools
 */
public final class ArrayTools
{
	/**
	 * Copy a byte array
	 * @param array the original array
	 * @return an array copy
	 */
	public static byte[] copy( byte[] array )
	{
		byte[] result = new byte[array.length];
		System.arraycopy(array, 0, result, 0, array.length);
		return result;
	}
	
	public static byte[] extend( byte[] array , int newSize )
	{
	    byte[] result = new byte[newSize];
        System.arraycopy(array, 0, result, 0, Math.min(array.length, newSize));
        return result;
	}
	
	public static int[] extend( int[] array , int newSize )
    {
	    int[] result = new int[newSize];
        System.arraycopy(array, 0, result, 0, Math.min(array.length, newSize));
        return result;
    }
}
