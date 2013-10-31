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

import net.timewalker.ffmq3.utils.concurrent.SynchronizationBarrier;

/**
 * CommitOperation
 */
public final class CommitOperation extends AbstractJournalOperation
{
    // Attributes
	private int operationsCount;
    private SynchronizationBarrier barrier;
    
    /**
     * Constructor
     */
    public CommitOperation( long transactionId , SynchronizationBarrier barrier )
    {
        super(transactionId,TYPE_COMMIT);
        this.barrier = barrier;
    }
    
    /**
     * Constructor
     */
    public CommitOperation( long transactionId , int operationsCount , SynchronizationBarrier barrier )
    {
        super(transactionId,TYPE_COMMIT);
        this.operationsCount = operationsCount;
        this.barrier = barrier;
    }

    /**
	 * @return the operationsCount
	 */
	public int getOperationsCount()
	{
		return operationsCount;
	}
	
	/**
	 * @param operationsCount the operationsCount to set
	 */
	public void setOperationsCount(int operationsCount)
	{
		this.operationsCount = operationsCount;
	}
    
    /**
     * @return the barrier
     */
    public SynchronizationBarrier getBarrier()
    {
        return barrier;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.impl.journal.AbstractJournalOperation#size()
     */
    @Override
	public int size()
    {
    	return super.size() + 4;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.impl.journal.AbstractJournalOperation#writeTo(net.timewalker.ffmq3.storage.data.impl.journal.JournalFile)
     */
    @Override
	protected void writeTo(JournalFile journalFile) throws JournalException
    {
    	super.writeTo(journalFile);
    	journalFile.writeInt(operationsCount);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString()
    {
    	return super.toString()+" [COMMIT] operationsCount="+operationsCount;
    }
}
