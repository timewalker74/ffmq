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
package net.timewalker.ffmq3.utils.concurrent;

/**
 * SynchronizationPoint
 */
public class SynchronizationPoint
{
    private boolean reached;
    
    /**
     * Test if the synchronization point was reached
     */
    public synchronized boolean isReached()
    {
        return reached;
    }
    
    /**
     * Wait for the synchronization point to be reached.
     * Returns immediately if the sync. point is already reached.
     */
    public synchronized void waitFor() throws InterruptedException
    {
        while (!reached)
            wait();
    }
    
    /**
     * Indicate that the synchronization point is reached, 
     * waking up threads waiting for it.
     */
    public synchronized void reach()
    {
        if (reached)
            return;
            
        reached = true;
        notifyAll();
    }
    
    /**
     * Reset the synchronization point status
     */
    public synchronized void reset()
    {
        reached = false;
    }
}
