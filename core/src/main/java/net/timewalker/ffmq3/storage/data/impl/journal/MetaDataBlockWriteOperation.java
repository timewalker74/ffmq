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
package net.timewalker.ffmq3.storage.data.impl.journal;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * MetaDataBlockWriteOperation
 */
public final class MetaDataBlockWriteOperation extends AbstractMetaDataWriteOperation
{
    // Attributes
    private byte[] metaData;
    
    /**
     * Constructor
     */
    public MetaDataBlockWriteOperation( long transactionId , long metaDataOffset , byte[] metaData )
    {
        super(transactionId,TYPE_META_DATA_BLOCK_WRITE,metaDataOffset);
        this.metaData = metaData;
    }
    
    /**
     * @return the metaData
     */
    public byte[] getMetaData()
    {
        return metaData;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.impl.journal.AbstractJournalOperation#size()
     */
    public int size()
    {
    	return super.size() + 4 + metaData.length;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.impl.journal.AbstractJournalOperation#writeTo(net.timewalker.ffmq3.storage.data.impl.journal.JournalFile)
     */
    protected void writeTo(JournalFile journalFile) throws JournalException
    {
        super.writeTo(journalFile);
        journalFile.writeInt(metaData.length);
        journalFile.write(metaData);
    }
    
    protected int writeTo( RandomAccessFile allocationTableRandomAccessFile ) throws JournalException
    {
    	try
    	{
    		allocationTableRandomAccessFile.seek(offset);
	    	allocationTableRandomAccessFile.write(metaData);
    	}
    	catch (IOException e)
    	{
    		throw new JournalException("Cannot write to store allocation table file",e);
    	}
    	
    	return metaData.length;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
    	return super.toString()+" [METADATA_BLOCK] metaDataSize="+metaData.length;
    }
}
