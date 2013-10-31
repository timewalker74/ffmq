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
package net.timewalker.ffmq4.utils.md5;

/**
 * <p>
 *  Modifed version of Fast MD5 for Java 2.7.1 (from http://www.twmacinta.com/myjava/fast_md5.php)
 *  Modifications from the original source :
 * 	 - Removed native code support
 *   - Stripped some code
 *   - Removed wacky synchronization
 *   - Reformatted code to match Java and FFMQ standards
 * </p>
 * 
 * This Java class has been derived from the RSA Data Security, Inc. MD5 
 * Message-Digest Algorithm and its reference implementation.
 * 
 * @author Santeri Paavolainen <sjpaavol@cc.helsinki.fi>
 * @author Timothy W Macinta (twm@alum.mit.edu) (optimizations and bug fixes)
 */
public final class MD5
{
	/**
	 * MD5 state
	 **/
	private MD5State state = new MD5State();

	private static final byte[] PADDING = { 
		(byte) 0x80, 0, 0, 0, 0, 0, 0, 0, 0, 
		 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
		 0, 0, 0, 0, 0, 0, 0 };

	/**
	 * Reset MD5 internal state
	 */
	public void reset()
	{
		state = new MD5State();
	}

	/**
	 * Class constructor
	 **/
	public MD5()
	{
		super();
	}

	private void decode(byte buffer[], int shift, int[] out)
	{
		/*
		 * len += shift; for (int i = 0; shift < len; i++, shift += 4) { out[i]
		 * = ((int) (buffer[shift] & 0xff)) | (((int) (buffer[shift + 1] &
		 * 0xff)) << 8) | (((int) (buffer[shift + 2] & 0xff)) << 16) | (((int)
		 * buffer[shift + 3]) << 24); }
		 */

		// unrolled loop (original loop shown above)
		out[0]  = ((buffer[shift     ] & 0xff)) | (((buffer[shift +  1] & 0xff)) << 8) | (((buffer[shift +  2] & 0xff)) << 16) | ((buffer[shift +  3]) << 24);
		out[1]  = ((buffer[shift +  4] & 0xff)) | (((buffer[shift +  5] & 0xff)) << 8) | (((buffer[shift +  6] & 0xff)) << 16) | ((buffer[shift +  7]) << 24);
		out[2]  = ((buffer[shift +  8] & 0xff)) | (((buffer[shift +  9] & 0xff)) << 8) | (((buffer[shift + 10] & 0xff)) << 16) | ((buffer[shift + 11]) << 24);
		out[3]  = ((buffer[shift + 12] & 0xff)) | (((buffer[shift + 13] & 0xff)) << 8) | (((buffer[shift + 14] & 0xff)) << 16) | ((buffer[shift + 15]) << 24);
		out[4]  = ((buffer[shift + 16] & 0xff)) | (((buffer[shift + 17] & 0xff)) << 8) | (((buffer[shift + 18] & 0xff)) << 16) | ((buffer[shift + 19]) << 24);
		out[5]  = ((buffer[shift + 20] & 0xff)) | (((buffer[shift + 21] & 0xff)) << 8) | (((buffer[shift + 22] & 0xff)) << 16) | ((buffer[shift + 23]) << 24);
		out[6]  = ((buffer[shift + 24] & 0xff)) | (((buffer[shift + 25] & 0xff)) << 8) | (((buffer[shift + 26] & 0xff)) << 16) | ((buffer[shift + 27]) << 24);
		out[7]  = ((buffer[shift + 28] & 0xff)) | (((buffer[shift + 29] & 0xff)) << 8) | (((buffer[shift + 30] & 0xff)) << 16) | ((buffer[shift + 31]) << 24);
		out[8]  = ((buffer[shift + 32] & 0xff)) | (((buffer[shift + 33] & 0xff)) << 8) | (((buffer[shift + 34] & 0xff)) << 16) | ((buffer[shift + 35]) << 24);
		out[9]  = ((buffer[shift + 36] & 0xff)) | (((buffer[shift + 37] & 0xff)) << 8) | (((buffer[shift + 38] & 0xff)) << 16) | ((buffer[shift + 39]) << 24);
		out[10] = ((buffer[shift + 40] & 0xff)) | (((buffer[shift + 41] & 0xff)) << 8) | (((buffer[shift + 42] & 0xff)) << 16) | ((buffer[shift + 43]) << 24);
		out[11] = ((buffer[shift + 44] & 0xff)) | (((buffer[shift + 45] & 0xff)) << 8) | (((buffer[shift + 46] & 0xff)) << 16) | ((buffer[shift + 47]) << 24);
		out[12] = ((buffer[shift + 48] & 0xff)) | (((buffer[shift + 49] & 0xff)) << 8) | (((buffer[shift + 50] & 0xff)) << 16) | ((buffer[shift + 51]) << 24);
		out[13] = ((buffer[shift + 52] & 0xff)) | (((buffer[shift + 53] & 0xff)) << 8) | (((buffer[shift + 54] & 0xff)) << 16) | ((buffer[shift + 55]) << 24);
		out[14] = ((buffer[shift + 56] & 0xff)) | (((buffer[shift + 57] & 0xff)) << 8) | (((buffer[shift + 58] & 0xff)) << 16) | ((buffer[shift + 59]) << 24);
		out[15] = ((buffer[shift + 60] & 0xff)) | (((buffer[shift + 61] & 0xff)) << 8) | (((buffer[shift + 62] & 0xff)) << 16) | ((buffer[shift + 63]) << 24);
	}

	private void transform(MD5State state, byte buffer[], int shift, int[] decode_buf)
	{
		int a = state.state[0], b = state.state[1], c = state.state[2], d = state.state[3], x[] = decode_buf;

		decode(buffer, shift, decode_buf);

		/* Round 1 */
		a += ((b & c) | (~b & d)) + x[0] + 0xd76aa478; /* 1 */
		a = ((a << 7) | (a >>> 25)) + b;
		d += ((a & b) | (~a & c)) + x[1] + 0xe8c7b756; /* 2 */
		d = ((d << 12) | (d >>> 20)) + a;
		c += ((d & a) | (~d & b)) + x[2] + 0x242070db; /* 3 */
		c = ((c << 17) | (c >>> 15)) + d;
		b += ((c & d) | (~c & a)) + x[3] + 0xc1bdceee; /* 4 */
		b = ((b << 22) | (b >>> 10)) + c;

		a += ((b & c) | (~b & d)) + x[4] + 0xf57c0faf; /* 5 */
		a = ((a << 7) | (a >>> 25)) + b;
		d += ((a & b) | (~a & c)) + x[5] + 0x4787c62a; /* 6 */
		d = ((d << 12) | (d >>> 20)) + a;
		c += ((d & a) | (~d & b)) + x[6] + 0xa8304613; /* 7 */
		c = ((c << 17) | (c >>> 15)) + d;
		b += ((c & d) | (~c & a)) + x[7] + 0xfd469501; /* 8 */
		b = ((b << 22) | (b >>> 10)) + c;

		a += ((b & c) | (~b & d)) + x[8] + 0x698098d8; /* 9 */
		a = ((a << 7) | (a >>> 25)) + b;
		d += ((a & b) | (~a & c)) + x[9] + 0x8b44f7af; /* 10 */
		d = ((d << 12) | (d >>> 20)) + a;
		c += ((d & a) | (~d & b)) + x[10] + 0xffff5bb1; /* 11 */
		c = ((c << 17) | (c >>> 15)) + d;
		b += ((c & d) | (~c & a)) + x[11] + 0x895cd7be; /* 12 */
		b = ((b << 22) | (b >>> 10)) + c;

		a += ((b & c) | (~b & d)) + x[12] + 0x6b901122; /* 13 */
		a = ((a << 7) | (a >>> 25)) + b;
		d += ((a & b) | (~a & c)) + x[13] + 0xfd987193; /* 14 */
		d = ((d << 12) | (d >>> 20)) + a;
		c += ((d & a) | (~d & b)) + x[14] + 0xa679438e; /* 15 */
		c = ((c << 17) | (c >>> 15)) + d;
		b += ((c & d) | (~c & a)) + x[15] + 0x49b40821; /* 16 */
		b = ((b << 22) | (b >>> 10)) + c;

		/* Round 2 */
		a += ((b & d) | (c & ~d)) + x[1] + 0xf61e2562; /* 17 */
		a = ((a << 5) | (a >>> 27)) + b;
		d += ((a & c) | (b & ~c)) + x[6] + 0xc040b340; /* 18 */
		d = ((d << 9) | (d >>> 23)) + a;
		c += ((d & b) | (a & ~b)) + x[11] + 0x265e5a51; /* 19 */
		c = ((c << 14) | (c >>> 18)) + d;
		b += ((c & a) | (d & ~a)) + x[0] + 0xe9b6c7aa; /* 20 */
		b = ((b << 20) | (b >>> 12)) + c;

		a += ((b & d) | (c & ~d)) + x[5] + 0xd62f105d; /* 21 */
		a = ((a << 5) | (a >>> 27)) + b;
		d += ((a & c) | (b & ~c)) + x[10] + 0x02441453; /* 22 */
		d = ((d << 9) | (d >>> 23)) + a;
		c += ((d & b) | (a & ~b)) + x[15] + 0xd8a1e681; /* 23 */
		c = ((c << 14) | (c >>> 18)) + d;
		b += ((c & a) | (d & ~a)) + x[4] + 0xe7d3fbc8; /* 24 */
		b = ((b << 20) | (b >>> 12)) + c;

		a += ((b & d) | (c & ~d)) + x[9] + 0x21e1cde6; /* 25 */
		a = ((a << 5) | (a >>> 27)) + b;
		d += ((a & c) | (b & ~c)) + x[14] + 0xc33707d6; /* 26 */
		d = ((d << 9) | (d >>> 23)) + a;
		c += ((d & b) | (a & ~b)) + x[3] + 0xf4d50d87; /* 27 */
		c = ((c << 14) | (c >>> 18)) + d;
		b += ((c & a) | (d & ~a)) + x[8] + 0x455a14ed; /* 28 */
		b = ((b << 20) | (b >>> 12)) + c;

		a += ((b & d) | (c & ~d)) + x[13] + 0xa9e3e905; /* 29 */
		a = ((a << 5) | (a >>> 27)) + b;
		d += ((a & c) | (b & ~c)) + x[2] + 0xfcefa3f8; /* 30 */
		d = ((d << 9) | (d >>> 23)) + a;
		c += ((d & b) | (a & ~b)) + x[7] + 0x676f02d9; /* 31 */
		c = ((c << 14) | (c >>> 18)) + d;
		b += ((c & a) | (d & ~a)) + x[12] + 0x8d2a4c8a; /* 32 */
		b = ((b << 20) | (b >>> 12)) + c;

		/* Round 3 */
		a += (b ^ c ^ d) + x[5] + 0xfffa3942; /* 33 */
		a = ((a << 4) | (a >>> 28)) + b;
		d += (a ^ b ^ c) + x[8] + 0x8771f681; /* 34 */
		d = ((d << 11) | (d >>> 21)) + a;
		c += (d ^ a ^ b) + x[11] + 0x6d9d6122; /* 35 */
		c = ((c << 16) | (c >>> 16)) + d;
		b += (c ^ d ^ a) + x[14] + 0xfde5380c; /* 36 */
		b = ((b << 23) | (b >>> 9)) + c;

		a += (b ^ c ^ d) + x[1] + 0xa4beea44; /* 37 */
		a = ((a << 4) | (a >>> 28)) + b;
		d += (a ^ b ^ c) + x[4] + 0x4bdecfa9; /* 38 */
		d = ((d << 11) | (d >>> 21)) + a;
		c += (d ^ a ^ b) + x[7] + 0xf6bb4b60; /* 39 */
		c = ((c << 16) | (c >>> 16)) + d;
		b += (c ^ d ^ a) + x[10] + 0xbebfbc70; /* 40 */
		b = ((b << 23) | (b >>> 9)) + c;

		a += (b ^ c ^ d) + x[13] + 0x289b7ec6; /* 41 */
		a = ((a << 4) | (a >>> 28)) + b;
		d += (a ^ b ^ c) + x[0] + 0xeaa127fa; /* 42 */
		d = ((d << 11) | (d >>> 21)) + a;
		c += (d ^ a ^ b) + x[3] + 0xd4ef3085; /* 43 */
		c = ((c << 16) | (c >>> 16)) + d;
		b += (c ^ d ^ a) + x[6] + 0x04881d05; /* 44 */
		b = ((b << 23) | (b >>> 9)) + c;

		a += (b ^ c ^ d) + x[9] + 0xd9d4d039; /* 33 */
		a = ((a << 4) | (a >>> 28)) + b;
		d += (a ^ b ^ c) + x[12] + 0xe6db99e5; /* 34 */
		d = ((d << 11) | (d >>> 21)) + a;
		c += (d ^ a ^ b) + x[15] + 0x1fa27cf8; /* 35 */
		c = ((c << 16) | (c >>> 16)) + d;
		b += (c ^ d ^ a) + x[2] + 0xc4ac5665; /* 36 */
		b = ((b << 23) | (b >>> 9)) + c;

		/* Round 4 */
		a += (c ^ (b | ~d)) + x[0] + 0xf4292244; /* 49 */
		a = ((a << 6) | (a >>> 26)) + b;
		d += (b ^ (a | ~c)) + x[7] + 0x432aff97; /* 50 */
		d = ((d << 10) | (d >>> 22)) + a;
		c += (a ^ (d | ~b)) + x[14] + 0xab9423a7; /* 51 */
		c = ((c << 15) | (c >>> 17)) + d;
		b += (d ^ (c | ~a)) + x[5] + 0xfc93a039; /* 52 */
		b = ((b << 21) | (b >>> 11)) + c;

		a += (c ^ (b | ~d)) + x[12] + 0x655b59c3; /* 53 */
		a = ((a << 6) | (a >>> 26)) + b;
		d += (b ^ (a | ~c)) + x[3] + 0x8f0ccc92; /* 54 */
		d = ((d << 10) | (d >>> 22)) + a;
		c += (a ^ (d | ~b)) + x[10] + 0xffeff47d; /* 55 */
		c = ((c << 15) | (c >>> 17)) + d;
		b += (d ^ (c | ~a)) + x[1] + 0x85845dd1; /* 56 */
		b = ((b << 21) | (b >>> 11)) + c;

		a += (c ^ (b | ~d)) + x[8] + 0x6fa87e4f; /* 57 */
		a = ((a << 6) | (a >>> 26)) + b;
		d += (b ^ (a | ~c)) + x[15] + 0xfe2ce6e0; /* 58 */
		d = ((d << 10) | (d >>> 22)) + a;
		c += (a ^ (d | ~b)) + x[6] + 0xa3014314; /* 59 */
		c = ((c << 15) | (c >>> 17)) + d;
		b += (d ^ (c | ~a)) + x[13] + 0x4e0811a1; /* 60 */
		b = ((b << 21) | (b >>> 11)) + c;

		a += (c ^ (b | ~d)) + x[4] + 0xf7537e82; /* 61 */
		a = ((a << 6) | (a >>> 26)) + b;
		d += (b ^ (a | ~c)) + x[11] + 0xbd3af235; /* 62 */
		d = ((d << 10) | (d >>> 22)) + a;
		c += (a ^ (d | ~b)) + x[2] + 0x2ad7d2bb; /* 63 */
		c = ((c << 15) | (c >>> 17)) + d;
		b += (d ^ (c | ~a)) + x[9] + 0xeb86d391; /* 64 */
		b = ((b << 21) | (b >>> 11)) + c;

		state.state[0] += a;
		state.state[1] += b;
		state.state[2] += c;
		state.state[3] += d;
	}

	/**
	 * Updates hash with the bytebuffer given (using at maximum length bytes from that buffer)
	 * 
	 * @param stat Which state is updated
	 * @param buffer Array of bytes to be hashed
	 * @param offset Offset to buffer array
	 * @param length Use at maximum `length' bytes (absolute maximum is buffer.length)
	 */
	public void update(byte buffer[], int offset, int length)
	{
		int index, partlen, i, start;

		/* Length can be told to be shorter, but not inter */
		if ((length - offset) > buffer.length)
			length = buffer.length - offset;

		/* compute number of bytes mod 64 */
		index = (int) (state.count & 0x3f);
		state.count += length;

		partlen = 64 - index;

		if (length >= partlen)
		{
			int[] decode_buf = new int[16];
			if (partlen == 64)
			{
				partlen = 0;
			}
			else
			{
				for (i = 0; i < partlen; i++)
					state.buffer[i + index] = buffer[i + offset];
				transform(state, state.buffer, 0, decode_buf);
			}
			
			for (i = partlen; (i + 63) < length; i += 64)
			{
				transform(state, buffer, i + offset, decode_buf);
			}

			index = 0;
		}
		else
			i = 0;

		/* buffer remaining input */
		if (i < length)
		{
			start = i;
			for (; i < length; i++)
				state.buffer[index + i - start] = buffer[i + offset];
		}
	}

	/**
	 * Updates hash with given array of bytes
	 * @param buffer Array of bytes to use for updating the hash
	 */
	public void update(byte buffer[])
	{
		update(buffer, 0, buffer.length);
	}

	private byte[] encode(int input[], int len)
	{
		int i, j;
		byte[] out = new byte[len];
		for (i = j = 0; j < len; i++, j += 4)
		{
			out[j]     = (byte)  (input[i]         & 0xff);
			out[j + 1] = (byte) ((input[i] >>> 8)  & 0xff);
			out[j + 2] = (byte) ((input[i] >>> 16) & 0xff);
			out[j + 3] = (byte) ((input[i] >>> 24) & 0xff);
		}

		return out;
	}

	/**
	 * Returns array of bytes (16 bytes) representing hash as of the current
	 * state of this object. Note: getting a hash does not invalidate the hash
	 * object, it only creates a copy of the real state which is finalized.
	 * 
	 * @return Array of 16 bytes, the hash of all updated bytes
	 */
	public byte[] digest()
	{
		byte bits[];
		int index, padlen;

		int[] count_ints = { (int) (state.count << 3), (int) (state.count >> 29) };
		bits = encode(count_ints, 8);

		index = (int) (state.count & 0x3f);
		padlen = (index < 56) ? (56 - index) : (120 - index);

		update(PADDING, 0, padlen);
		update(bits, 0, 8);

		return encode(state.state, 16);
	}

//	/**
//	 * @return true iff the first 16 bytes of both hash1 and hash2 are equal;
//	 *         both hash1 and hash2 are null; or either hash array is less than
//	 *         16 bytes in length and their lengths and all of their bytes are
//	 *         equal.
//	 **/
//	public static boolean hashesEqual(byte[] hash1, byte[] hash2)
//	{
//		if (hash1 == null)
//			return hash2 == null;
//		if (hash2 == null)
//			return false;
//		
//		int targ = 16;
//		if (hash1.length < 16)
//		{
//			if (hash2.length != hash1.length)
//				return false;
//			
//			targ = hash1.length;
//		}
//		else if (hash2.length < 16)
//		{
//			return false;
//		}
//		
//		for (int i = 0; i < targ; i++)
//			if (hash1[i] != hash2[i])
//				return false;
//		
//		return true;
//	}
	
	//-------------------------------------------------------------------------------
	
	private static class MD5State 
	{
		/**
		 * 128-bit state
		 */
		protected int state[];

		/**
		 * 64-bit character count
		 */
		protected long count;

		/**
		 * 64-byte buffer (512 bits) for storing to-be-hashed characters
		 */
		protected byte buffer[];

		/**
		 * Constructor
		 */
		public MD5State()
		{
			buffer = new byte[64];
			state = new int[] {
				0x67452301,
				0xefcdab89,
				0x98badcfe,
				0x10325476
			};
		}
	}
}
