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

package net.timewalker.ffmq3.storage.data.impl;

import java.io.IOException;

import net.timewalker.ffmq3.management.destination.definition.QueueDefinition;
import net.timewalker.ffmq3.storage.data.DataStoreException;
import net.timewalker.ffmq3.utils.concurrent.SynchronizationBarrier;

/**
 * BlockBasedDataStore
 */
public final class BlockBasedDataStore extends AbstractBlockBasedDataStore
{
	/**
     * Constructor
     */
    public BlockBasedDataStore( QueueDefinition queueDef )
    {
		super(queueDef);
	}
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.impl.AbstractBlockBasedDataStore#initFilesystem()
     */
    @Override
	protected void initFilesystem() throws DataStoreException
    {
        super.initFilesystem();
        
        // Integrity check
        integrityCheck();
    }
    
    /*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.storage.data.impl.AbstractBlockBasedDataStore#writeFirstBlock()
	 */
    @Override
	protected void writeFirstBlock() throws DataStoreException
    {
    	try
    	{
	    	allocationTableRandomAccessFile.seek(AT_HEADER_FIRSTBLOCK_OFFSET);
	        allocationTableRandomAccessFile.writeInt(firstBlock);
    	}
    	catch (IOException e)
    	{
    		throw new DataStoreException("Cannot write to allocation table file : "+allocationTableFile.getAbsolutePath(),e);
    	}
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.impl.AbstractBlockBasedDataStore#writeAllocationBlock(int)
     */
    @Override
	protected void writeAllocationBlock( int blockIndex ) throws DataStoreException
    {
    	byte[] allocationBlock = serializeAllocationBlock(blockIndex);
        try
        {
	        allocationTableRandomAccessFile.seek(AT_HEADER_SIZE+blockIndex*AT_BLOCK_SIZE);
	        allocationTableRandomAccessFile.write(allocationBlock);
        }
    	catch (IOException e)
    	{
    		throw new DataStoreException("Cannot write to allocation table file : "+allocationTableFile.getAbsolutePath(),e);
    	}
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.impl.AbstractBlockBasedDataStore#writeDataBlock(byte[], int, int, int)
     */
    @Override
	protected void writeDataBlock(byte[] data, int offset, int len, int blockHandle) throws DataStoreException
    {
    	try
        {
	        dataRandomAccessFile.seek((long)blockHandle*blockSize);
	        dataRandomAccessFile.write(data,offset,len);
        }
    	catch (IOException e)
    	{
    		throw new DataStoreException("Cannot write data block "+blockHandle+" : "+dataFile.getAbsolutePath(),e);
    	}
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.impl.AbstractBlockBasedDataStore#readDataBlock(byte[], int, int, int)
     */
    @Override
	protected void readDataBlock(byte[] data, int offset, int len, int blockHandle) throws DataStoreException
    {
		try
		{
	        dataRandomAccessFile.seek((long)blockHandle*blockSize);
	        if (dataRandomAccessFile.read(data,offset,len) != len)
	            throw new DataStoreException("Cannot read "+len+" bytes from store file");
		}
		catch (DataStoreException e)
		{
		    throw e;
		}
        catch (IOException e)
        {
            throw new DataStoreException("Could not read data block "+blockHandle+" : "+dataFile.getAbsolutePath(),e);
        }
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.impl.AbstractBlockBasedDataStore#extendStoreFiles(int, int)
     */
    @Override
	protected void extendStoreFiles(int oldBlockCount, int newBlockCount) throws DataStoreException
    {
        try
        {
            // Update allocation table
            allocationTableRandomAccessFile.setLength(AT_HEADER_SIZE+(long)newBlockCount*AT_BLOCK_SIZE);
            for (int n = oldBlockCount ; n < newBlockCount ; n++)
                writeAllocationBlock(n);
            allocationTableRandomAccessFile.seek(AT_HEADER_BLOCKCOUNT_OFFSET);
            allocationTableRandomAccessFile.writeInt(newBlockCount);
            
            // Update data file
            dataRandomAccessFile.setLength((long)blockSize*newBlockCount);
        }
        catch (IOException e)
        {
            throw new DataStoreException("Could not extends store to "+newBlockCount+" blocks : "+dataFile.getAbsolutePath(),e);
        }
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.DataStore#commitChanges()
     */
    @Override
	public void commitChanges() throws DataStoreException
    {
    	// Ignore
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.DataStore#commitChanges(net.timewalker.ffmq3.utils.concurrent.SynchronizationBarrier)
     */
    @Override
	public void commitChanges(SynchronizationBarrier barrier) throws DataStoreException
    {
    	// Ignore
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.impl.AbstractBlockBasedDataStore#flush()
     */
    @Override
	protected void flush() throws DataStoreException
    {
    	// Nothing to do
    }
}
