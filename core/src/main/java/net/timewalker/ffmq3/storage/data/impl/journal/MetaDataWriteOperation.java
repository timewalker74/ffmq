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
 * MetaDataWriteOperation
 */
public final class MetaDataWriteOperation extends AbstractMetaDataWriteOperation
{
    // Attributes
    private int metaData;
    
    /**
     * Constructor
     */
    public MetaDataWriteOperation( long transactionId , long metaDataOffset , int metaData )
    {
        super(transactionId,TYPE_META_DATA_WRITE,metaDataOffset);
        this.metaData = metaData;
    }
    
    /**
     * @return the metaData
     */
    public int getMetaData()
    {
        return metaData;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.impl.journal.AbstractJournalOperation#size()
     */
    public int size()
    {
    	return super.size() + 4;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.impl.journal.AbstractJournalOperation#writeTo(net.timewalker.ffmq3.storage.data.impl.journal.JournalFile)
     */
    protected void writeTo(JournalFile journalFile) throws JournalException
    {
        super.writeTo(journalFile);
        journalFile.writeInt(metaData);
    }
    
    protected int writeTo( RandomAccessFile allocationTableRandomAccessFile ) throws JournalException
    {
    	try
    	{
    		allocationTableRandomAccessFile.seek(offset);
	    	allocationTableRandomAccessFile.writeInt(metaData);
    	}
    	catch (IOException e)
    	{
    		throw new JournalException("Cannot write to store allocation table file",e);
    	}
    	
    	return 4; // sizeof(int)
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
    	return super.toString()+" [META_DATA] metaData="+metaData;
    }
}
