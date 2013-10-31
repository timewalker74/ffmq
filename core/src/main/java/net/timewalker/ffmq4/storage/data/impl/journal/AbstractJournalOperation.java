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

/**
 * Base class for a Journal operation
 */
public abstract class AbstractJournalOperation
{
    public static final byte TYPE_DATA_BLOCK_WRITE      = 1;
    public static final byte TYPE_META_DATA_WRITE       = 2;
    public static final byte TYPE_META_DATA_BLOCK_WRITE = 3;
    public static final byte TYPE_STORE_EXTEND          = 4;
    public static final byte TYPE_COMMIT                = 5;
    
    // Attributes
    private long transactionId;
    private byte type;
    
    // Runtime
    private AbstractJournalOperation next; // for operation queueing, see JournalQueue
    
    /**
     * Constructor
     */
    public AbstractJournalOperation( long transactionId , byte type )
    {
    	this.transactionId = transactionId;
        this.type = type;
    }
    
    /**
	 * @return the transactionId
	 */
	public long getTransactionId()
	{
		return transactionId;
	}
    
    /**
     * @return the type
     */
    public final byte getType()
    {
        return type;
    }
    
    /**
     * The storage size for this operation
     */
    public int size()
    {
    	return 9; // sizeof(byte) + sizeof(long), see writeTo()
    }
    
    /**
     * Write the operation to the given journal file
     * @param journalFile teh journal file
     */
    protected void writeTo( JournalFile journalFile ) throws JournalException
    {
    	journalFile.writeByte(type);
    	journalFile.writeLong(transactionId); 
    }
    
    public AbstractJournalOperation next()
    {
        return next;
    }

    public void setNext(AbstractJournalOperation next)
    {
        this.next = next;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString()
    {
    	return "#"+transactionId;
    }
}
