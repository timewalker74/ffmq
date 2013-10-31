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
package net.timewalker.ffmq4.storage.data.impl.journal;

import java.io.IOException;
import java.io.RandomAccessFile;

import net.timewalker.ffmq4.storage.data.impl.AbstractBlockBasedDataStore;


/**
 * StoreExtendOperation
 */
public final class StoreExtendOperation extends AbstractJournalOperation
{
    private static final byte[] EMPTY_ALLOCATION_BLOCK = new byte[] {
      0, 
      (byte)-1,(byte)-1,(byte)-1,(byte)-1,
      (byte)-1,(byte)-1,(byte)-1,(byte)-1,
      (byte)-1,(byte)-1,(byte)-1,(byte)-1
    };

    // Attributes
    private int blockSize;
    private int oldBlockCount;
    private int newBlockCount;
    
    /**
     * Constructor
     */
    public StoreExtendOperation( long transactionId , int blockSize , int oldBlockCount , int newBlockCount )
    {
        super(transactionId,TYPE_STORE_EXTEND);
        this.blockSize = blockSize;
        this.oldBlockCount = oldBlockCount;
        this.newBlockCount = newBlockCount;
    }

    /**
     * @return the blockSize
     */
    public int getBlockSize()
    {
        return blockSize;
    }
    
    /**
     * @return the oldBlockCount
     */
    public int getOldBlockCount()
    {
        return oldBlockCount;
    }
    
    /**
     * @return the newBlockCount
     */
    public int getNewBlockCount()
    {
        return newBlockCount;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.storage.data.impl.journal.AbstractJournalOperation#size()
     */
    @Override
	public int size()
    {
    	return super.size() + 4 + 4 + 4;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.storage.data.impl.journal.AbstractJournalOperation#writeTo(net.timewalker.ffmq4.storage.data.impl.journal.JournalFile)
     */
    @Override
	protected void writeTo(JournalFile journalFile) throws JournalException
    {
    	super.writeTo(journalFile);
    	journalFile.writeInt(blockSize);
    	journalFile.writeInt(oldBlockCount);
    	journalFile.writeInt(newBlockCount);
    }
    
    protected int extend( RandomAccessFile allocationTableRandomAccessFile , RandomAccessFile dataRandomAccessFile ) throws JournalException
    {
        try
        {
            // Update allocation table
            allocationTableRandomAccessFile.setLength(AbstractBlockBasedDataStore.AT_HEADER_SIZE+(long)newBlockCount*AbstractBlockBasedDataStore.AT_BLOCK_SIZE);
            allocationTableRandomAccessFile.seek(AbstractBlockBasedDataStore.AT_HEADER_SIZE+oldBlockCount*AbstractBlockBasedDataStore.AT_BLOCK_SIZE);
            for (int n = oldBlockCount ; n < newBlockCount ; n++)
                allocationTableRandomAccessFile.write(EMPTY_ALLOCATION_BLOCK);
            allocationTableRandomAccessFile.seek(AbstractBlockBasedDataStore.AT_HEADER_BLOCKCOUNT_OFFSET);
            allocationTableRandomAccessFile.writeInt(newBlockCount);
            
            // Update data file
    		synchronized (dataRandomAccessFile) // Make sure we do not conflict with another async operation
    		{
    			dataRandomAccessFile.setLength((long)blockSize*newBlockCount);
    		}
        }
        catch (IOException e)
        {
            throw new JournalException("Cannot write to store allocation table file",e);
        }
        
        return newBlockCount;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString()
    {
    	return super.toString()+" [STORE_EXTEND] blockSize="+blockSize+" oldBlockCount="+oldBlockCount+" newBlockCount="+newBlockCount;
    }
}
