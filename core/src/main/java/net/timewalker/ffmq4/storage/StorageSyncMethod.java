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

package net.timewalker.ffmq4.storage;

/**
 * StorageSyncMethod
 */
public final class StorageSyncMethod
{
	public static final int FD_SYNC = 1;
	public static final int CHANNEL_FORCE_NO_META = 2;
	
	public static final int[] ALL = {
		FD_SYNC, CHANNEL_FORCE_NO_META
	};
	
	public static boolean isValid( int method )
	{
		for (int i = 0; i < ALL.length; i++)
			if (ALL[i] == method)
				return true;
		return false;
	}
}
