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

import java.io.File;
import java.io.IOException;

import net.timewalker.ffmq3.management.destination.AbstractDestinationDescriptor;
import net.timewalker.ffmq3.storage.data.DataStoreException;
import net.timewalker.ffmq3.storage.data.impl.journal.BlockBasedDataStoreJournal;
import net.timewalker.ffmq3.storage.data.impl.journal.DirtyBlockTable;
import net.timewalker.ffmq3.storage.data.impl.journal.JournalRecovery;
import net.timewalker.ffmq3.utils.async.AsyncTaskManager;
import net.timewalker.ffmq3.utils.concurrent.SynchronizationBarrier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JournalingBlockBasedDataStore
 */
public final class JournalingBlockBasedDataStore extends AbstractBlockBasedDataStore
{
	private static final Log log = LogFactory.getLog(JournalingBlockBasedDataStore.class);
	
	// Journal related
	private AsyncTaskManager asyncTaskManager;
	private BlockBasedDataStoreJournal journal;
	private DirtyBlockTable dirtyBlockTable = new DirtyBlockTable();
	private boolean keepJournalFiles = System.getProperty("ffmq.dataStore.keepJournalFiles", "false").equals("true");
	
	/**
	 * Constructor
	 */
	public JournalingBlockBasedDataStore( AbstractDestinationDescriptor descriptor , AsyncTaskManager asyncTaskManager )
	{
		super(descriptor);
		this.asyncTaskManager = asyncTaskManager;
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.storage.data.impl.AbstractBlockBasedDataStore#initFilesystem()
	 */
	@Override
	protected void initFilesystem() throws DataStoreException
	{
		super.initFilesystem();

		String baseName = descriptor.getName();
        File dataFolder = descriptor.getDataFolder();
		
		// Delete old recycled files
		File[] oldRecycledFiles = BlockBasedDataStoreTools.findRecycledJournalFiles(baseName, dataFolder);
		for(int i=0;i<oldRecycledFiles.length;i++)
			if (!oldRecycledFiles[i].delete())
				throw new DataStoreException("Cannot delete recycled journal file : "+oldRecycledFiles[i].getAbsolutePath());
		
    	// Check for remaining journal files ...
    	File[] journalFiles = BlockBasedDataStoreTools.findJournalFiles(baseName, dataFolder);
    	if (journalFiles.length > 0)
    	{
    		// Recovery
    		JournalRecovery recovery = new JournalRecovery(baseName,journalFiles, allocationTableRandomAccessFile, dataRandomAccessFile);
    		int newBlockCount = recovery.recover();
    		
    		// Update block count if necessary
    		if (newBlockCount != -1)
    		    this.blockCount = newBlockCount;
    		
    		if (!keepJournalFiles)
    		{
	    		for (int i = 0; i < journalFiles.length; i++)
				{
	    			if (!journalFiles[i].delete())
	    				throw new DataStoreException("Cannot delete journal file : "+journalFiles[i].getAbsolutePath());
				}
    		}
    		
    		// Integrity check
    		log.warn("["+baseName+"] Forcing integrity check after journal recovery ...");
    		integrityCheck();
    		log.warn("["+baseName+"] Check complete.");
    	}
    	
    	// Create new journal
    	this.journal = 
    		new BlockBasedDataStoreJournal(baseName, 
    		                               descriptor.getJournalFolder(),
    			                           descriptor.getMaxJournalSize(),
    			                           descriptor.getMaxWriteBatchSize(),
    			                           descriptor.getMaxUnflushedJournalSize(),
    			                           descriptor.getMaxUncommittedStoreSize(),
    			                           descriptor.getJournalOutputBuffer(),
    			                           descriptor.getStorageSyncMethod(),
    			                           descriptor.isPreAllocateFiles(),
    			                           allocationTableRandomAccessFile, 
    			                           dataRandomAccessFile, 
    			                           dirtyBlockTable,
    			                           asyncTaskManager);
    }
	
	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.storage.data.impl.AbstractBlockBasedDataStore#writeFirstBlock()
	 */
    @Override
	protected void writeFirstBlock() throws DataStoreException
    {
    	journal.writeMetaData(AT_HEADER_FIRSTBLOCK_OFFSET, firstBlock);
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.impl.AbstractBlockBasedDataStore#writeAllocationBlock(int)
     */
    @Override
	protected void writeAllocationBlock( int blockIndex ) throws DataStoreException
    {
    	byte[] allocationBlock = serializeAllocationBlock(blockIndex);
        journal.writeMetaDataBlock(AT_HEADER_SIZE+blockIndex*AT_BLOCK_SIZE, allocationBlock);
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.impl.AbstractBlockBasedDataStore#writeDataBlock(byte[], int, int, int)
     */
    @Override
	protected void writeDataBlock(byte[] data, int offset, int len, int blockHandle) throws DataStoreException
    {
    	byte[] blockData = new byte[blockSize];
    	System.arraycopy(data, offset, blockData, 0, len);
    	
    	dirtyBlockTable.markDirty(blockHandle, blockData);    	
    	journal.writeDataBlock(blockHandle, (long)blockHandle*blockSize, blockData);
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.impl.AbstractBlockBasedDataStore#readDataBlock(byte[], int, int, int)
     */
    @Override
	protected void readDataBlock(byte[] data, int offset, int len, int blockHandle) throws DataStoreException
    {
    	byte[] dirtyBlock = dirtyBlockTable.get(blockHandle);
    	if (dirtyBlock != null)
    	{
    		// Copy data from memory-cached dirty block
    		System.arraycopy(dirtyBlock, 0, data, offset, len);
    	}
    	else
    	{
    		try
    		{
    			long dataOffset = (long)blockHandle*blockSize;
    			
	    		// Make sure we do not conflict with another async operation
	    		synchronized (dataRandomAccessFile)
				{
			        dataRandomAccessFile.seek(dataOffset);
			        if (dataRandomAccessFile.read(data,offset,len) != len)
			            throw new DataStoreException("Cannot read "+len+" bytes from store file");
				}
    		}
    		catch (DataStoreException e)
    		{
    		    throw e;
    		}
            catch (IOException e)
            {
                throw new DataStoreException("Could not read data block "+blockHandle,e);
            }
    	}
    }
	
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.impl.AbstractBlockBasedDataStore#extendStoreFiles(int, int)
     */
    @Override
	protected void extendStoreFiles(int oldBlockCount, int newBlockCount) throws DataStoreException
    {
        journal.extendStore(blockSize,oldBlockCount,newBlockCount);
    }
    
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.storage.data.impl.AbstractBlockBasedDataStore#flush()
	 */
	@Override
	protected void flush() throws DataStoreException
	{
		journal.flush();
	}
	
	/* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.DataStore#commit(net.timewalker.ffmq3.utils.concurrent.SynchronizationBarrier)
     */
    @Override
	public void commitChanges(SynchronizationBarrier barrier) throws DataStoreException
    {
    	journal.commit(barrier);
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.DataStore#commit()
     */
    @Override
	public void commitChanges() throws DataStoreException
    {
    	try
    	{
	    	SynchronizationBarrier barrier = new SynchronizationBarrier();
	    	journal.commit(barrier);
	    	barrier.waitFor();
    	}
    	catch (InterruptedException e)
    	{
    		throw new DataStoreException("Wait for commit barrier was interrupted");
    	}
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.impl.AbstractBlockBasedDataStore#close()
     */
    @Override
	public void close()
    {
    	// Close journal first
    	try
    	{
    		commitChanges();
    		journal.close();
    	}
    	catch (DataStoreException e)
    	{
    		log.error("["+descriptor.getName()+"] Could not properly close store journal",e);
    	}
    	
    	super.close();
    }
}
