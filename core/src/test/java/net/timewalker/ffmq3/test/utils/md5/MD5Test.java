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

package net.timewalker.ffmq3.test.utils.md5;

import net.timewalker.ffmq3.utils.StringTools;
import net.timewalker.ffmq3.utils.md5.MD5;
import junit.framework.TestCase;

/**
 * MD5Test
 */
public class MD5Test extends TestCase
{
	public void testMD5() throws Exception
	{
		MD5 md5 = new MD5();
		
		md5.update("foobarfoobarfoobarfoobarfoobarfoobarfoobarfoobarfoobarfoobar".getBytes());
		byte[] result = md5.digest();
		
		System.out.println(StringTools.asHex(result));
	}
}
