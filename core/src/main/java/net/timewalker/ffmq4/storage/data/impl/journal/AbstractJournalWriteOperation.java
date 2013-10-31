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

/**
 * AbstractJournalWriteOperation
 */
public abstract class AbstractJournalWriteOperation extends AbstractJournalOperation
{
	// Attributes
	protected long offset;
	
	/**
     * Constructor
     */
    public AbstractJournalWriteOperation( long transactionId , byte type , long offset )
    {
    	super(transactionId, type);
    	this.offset = offset;
    }
    
    /**
	 * @return the offset
	 */
	public long getOffset()
	{
		return offset;
	}
	
	/* (non-Javadoc)
     * @see net.timewalker.ffmq4.storage.data.impl.journal.AbstractJournalOperation#writeTo(net.timewalker.ffmq4.storage.data.impl.journal.JournalFile)
     */
    @Override
	protected void writeTo(JournalFile journalFile) throws JournalException
    {
        super.writeTo(journalFile);
        journalFile.writeLong(offset);
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.storage.data.impl.journal.AbstractJournalOperation#size()
     */
    @Override
	public int size()
    {
    	return super.size() + 8;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString()
    {
    	return super.toString()+" offset="+offset;
    }
}
