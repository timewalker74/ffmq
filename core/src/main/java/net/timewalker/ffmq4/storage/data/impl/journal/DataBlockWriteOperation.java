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


/**
 * DataBlockWriteOperation
 */
public final class DataBlockWriteOperation extends AbstractJournalWriteOperation
{
    // Attributes
    private int blockIndex;
    private byte[] blockData;
    
    /**
     * Constructor
     */
    public DataBlockWriteOperation( long transactionId , int blockIndex , long blockOffset , byte[] blockData )
    {
        super(transactionId,TYPE_DATA_BLOCK_WRITE,blockOffset);
        this.blockIndex = blockIndex;
        this.blockData = blockData;
    }
    
    /**
     * @return the blockIndex
     */
    public int getBlockIndex()
    {
        return blockIndex;
    }
    
    /**
     * @return the blockData
     */
    public byte[] getBlockData()
    {
        return blockData;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.storage.data.impl.journal.AbstractJournalOperation#size()
     */
    @Override
	public int size()
    {
    	return super.size() + 4 + blockData.length;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.storage.data.impl.journal.AbstractJournalOperation#writeTo(net.timewalker.ffmq4.storage.data.impl.journal.JournalFile)
     */
    @Override
	protected void writeTo(JournalFile journalFile) throws JournalException
    {
        super.writeTo(journalFile);
        journalFile.writeInt(blockData.length);
        journalFile.write(blockData);
    }
    
    protected int writeTo( RandomAccessFile dataRandomAccessFile ) throws JournalException
    {
    	try
    	{
    		// Make sure we do not conflict with another async operation
    		synchronized (dataRandomAccessFile)
			{
		    	dataRandomAccessFile.seek(offset);
		    	dataRandomAccessFile.write(blockData);
			}
    	}
    	catch (IOException e)
    	{
    		throw new JournalException("Cannot write to store data file",e);
    	}
    	
    	return blockData.length;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString()
    {
    	return super.toString()+" [DATA_BLOCK] blockDataSize="+blockData.length;
    }
}
