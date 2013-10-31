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

import java.util.NoSuchElementException;

/**
 * FIFO queue of {@link AbstractJournalOperation} objects 
 */
public final class JournalQueue
{
    // Runtime
    private AbstractJournalOperation head;
    private AbstractJournalOperation tail;
    private int size;
    
    /**
     * Return the first operation in queue
	 * @return the first operation in queue
	 */
	public AbstractJournalOperation getFirst()
	{
		return head;
	}
    
    /**
     * Append an operation at the end of the queue
     * @param op a journal operation
     */
    public void addLast( AbstractJournalOperation op )
    {
        if (tail == null)
        {
            head = tail = op;
        }
        else
        {
            tail.setNext(op);
            tail = op;
        }
        
        op.setNext(null);
        size++;
    }
    
    /**
     * Remove the first available journal operation in queue
     * @return a journal operation
     * @throws NoSuchElementException if queue is empty
     */
    public AbstractJournalOperation removeFirst()
    {
        AbstractJournalOperation op = head;
        if (op == null)
            throw new NoSuchElementException();
        
        head = op.next();
        if (head == null)
            tail = null;
        op.setNext(null);
        size--;
        
        return op;
    }
    
    /**
     * Migrate all operations to the given target queue
     * @param otherQueue target journal queue
     */
    public void migrateTo( JournalQueue otherQueue )
    {
        if (head == null)
            return; // Empty
        
        if (otherQueue.head == null)
        {
            // Other queue was empty, copy everything
            otherQueue.head = head;
            otherQueue.tail = tail;
            otherQueue.size = size;
        }
        else
        {
            // Other queue is not empty, append operations
            otherQueue.tail.setNext(head);
            otherQueue.tail = tail;
            otherQueue.size += size;
        }
        
        // Clear this queue
        head = tail = null;
        size = 0;
    }
    
    /**
     * Get the queue size
     * @return number of operations in queue
     */
    public int size()
    {
        return size;
    }
}
