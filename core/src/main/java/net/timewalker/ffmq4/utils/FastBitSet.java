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

import java.util.BitSet;


/**
 * <p>Simplistic version of the jdk {@link BitSet}</p>
 * No bound-checking, internal capacity must be extended explicitely.
 */
public final class FastBitSet
{
    /*
     * BitSets are packed into arrays of "words."  Currently a word is
     * a long, which consists of 64 bits, requiring 6 address bits.
     * The choice of word size is determined purely by performance concerns.
     */
    private final static int  ADDRESS_BITS_PER_WORD = 6;
    private final static int  BITS_PER_WORD         = 1 << ADDRESS_BITS_PER_WORD;

    // Storage
    private long[] words;

    /**
     * Creates a bit set of size nbits
     * @param nbits the initial size of the bit set.
     */
    public FastBitSet(int nbits)
    {
        words = new long[wordIndex(nbits - 1) + 1];
    }
    
    /**
     * Given a bit index, return word index containing it.
     */
    private static int wordIndex(int bitIndex)
    {
        return bitIndex >> ADDRESS_BITS_PER_WORD;
    }

    /**
     * Ensures that the BitSet can hold enough bits.
     * @param requiredBits the required number of bits.
     */
    public void ensureCapacity(int requiredBits)
    {
        int requiredWords = wordIndex(requiredBits - 1) + 1;
        if (words.length < requiredWords)
        {
            long[] newWords = new long[requiredWords];
            System.arraycopy(words, 0, newWords, 0, words.length);
            words = newWords;
        }
    }

    /**
     * Sets the bit at the specified index to the complement of its
     * current value.
     * @param   bitIndex the index of the bit to flip
     * @return true if the bit was set to, false if it was unset
     */
    public boolean flip(int bitIndex)
    {
        int wordIndex = wordIndex(bitIndex);
        words[wordIndex] ^= (1L << bitIndex);
        
        return ((words[wordIndex] & (1L << bitIndex)) != 0);
    }

    /**
     * Sets the bit at the specified index to <code>true</code>.
     * @param     bitIndex   a bit index.
     */
    public void set(int bitIndex)
    {
        int wordIndex = wordIndex(bitIndex);
        words[wordIndex] |= (1L << bitIndex); // Restores invariants
    }

    /**
     * Sets the bit specified by the index to <code>false</code>.
     * @param     bitIndex   the index of the bit to be cleared.
     */
    public void clear(int bitIndex)
    {
        int wordIndex = wordIndex(bitIndex);
        words[wordIndex] &= ~(1L << bitIndex);
    }

    /**
     * Sets all of the bits in this BitSet to <code>false</code>.
     */
    public void clear()
    {
        for (int i = 0 ; i < words.length ; i++)
            words[i] = 0;
    }

    /**
     * Returns the value of the bit with the specified index. The value
     * is <code>true</code> if the bit with the index <code>bitIndex</code>
     * is currently set in this <code>BitSet</code>; otherwise, the result
     * is <code>false</code>.
     * @param     bitIndex   the bit index.
     * @return    the value of the bit with the specified index.
     */
    public boolean get(int bitIndex)
    {
        int wordIndex = wordIndex(bitIndex);
        return ((words[wordIndex] & (1L << bitIndex)) != 0);
    }

    /**
     * Returns the number of bits of space actually in use by this
     * <code>BitSet</code> to represent bit values.
     * The maximum element in the set is the size - 1st element.
     * @return  the number of bits currently in this bit set.
     */
    public int size()
    {
        return words.length * BITS_PER_WORD;
    }
}
