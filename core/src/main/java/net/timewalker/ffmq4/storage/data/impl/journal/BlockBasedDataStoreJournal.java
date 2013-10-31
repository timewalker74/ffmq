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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.jms.JMSException;

import net.timewalker.ffmq3.storage.StorageSyncMethod;
import net.timewalker.ffmq3.storage.data.DataStoreException;
import net.timewalker.ffmq3.storage.data.impl.AbstractBlockBasedDataStore;
import net.timewalker.ffmq3.utils.async.AsyncTask;
import net.timewalker.ffmq3.utils.async.AsyncTaskManager;
import net.timewalker.ffmq3.utils.concurrent.SynchronizationBarrier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * BlockBasedDataStoreJournal
 */
public final class BlockBasedDataStoreJournal
{
    private static final Log log = LogFactory.getLog(BlockBasedDataStoreJournal.class);
    
    private static final int METADATA_FORWARD_SCAN_HORIZON = 10;
    
    // Global
    private String baseName;
    private File dataFolder;
    private AsyncTaskManager asyncTaskManager;
    
    // Store files
    private RandomAccessFile allocationTableRandomAccessFile;
    private RandomAccessFile dataRandomAccessFile;
    
    // Dirty blocks table
    private DirtyBlockTable dirtyBlockTable;
    
    // Settings
    private long maxJournalSize;
    private int maxWriteBatchSize;
    private int maxUnflushedJournalSize;
    private int maxUncommittedStoreSize;
    private int journalOutputBuffer;
    private int storageSyncMethod;
    private boolean preAllocateFiles;
    
    // Journal files management
    private LinkedList<JournalFile> journalFiles = new LinkedList<>();
    private JournalFile currentJournalFile;
    private int nextJournalFileIndex = 1;
    private long currentTransactionId = 1;
    private LinkedList<File> recycledJournalFiles = new LinkedList<>();
    
    // Journal write queues
    private JournalQueue journalWriteQueue = new JournalQueue();
    private JournalQueue journalProcessingQueue = new JournalQueue();
    private JournalQueue uncommittedJournalQueue = new JournalQueue();
    private List<SynchronizationBarrier> pendingBarriers = new ArrayList<>();
    private int unflushedJournalSize;
    private int writtenJournalOperations;
    private boolean flushingJournal;
    
    // Store write queues
    private JournalQueue storeWriteQueue = new JournalQueue();
    private JournalQueue storeProcessingQueue = new JournalQueue();
    private long lastStoreTransactionId;
    private int uncommittedStoreSize;
    private boolean flushingStore;
    
    // Async targets
    private FlushJournalAsyncTask flushJournalAsyncTask = new FlushJournalAsyncTask();
    private FlushStoreAsyncTask flushStoreAsyncTask = new FlushStoreAsyncTask();
    
    // Runtime
    private boolean traceEnabled;
    private boolean failing;
    private boolean closing;
    private volatile int totalPendingOperations; // same synchronization scope as journalWriteQueue
    private boolean keepJournalFiles = System.getProperty("ffmq.dataStore.keepJournalFiles", "false").equals("true"); // For testing purposes

    /**
     * Constructor
     */
    public BlockBasedDataStoreJournal( String baseName , 
    		                           File dataFolder ,
    		                           long maxJournalSize ,
    		                           int maxWriteBatchSize ,
    		                           int maxUnflushedJournalSize ,
    		                           int maxUncommittedStoreSize ,
    		                           int journalOutputBuffer ,
    		                           int storageSyncMethod ,
    		                           boolean preAllocateFiles ,
    		                           RandomAccessFile allocationTableRandomAccessFile ,
    		                           RandomAccessFile dataRandomAccessFile ,
    		                           DirtyBlockTable dirtyBlockTable ,
    		                           AsyncTaskManager asyncTaskManager )
    {
    	this.baseName = baseName;
    	this.dataFolder = dataFolder;
    	this.maxJournalSize = maxJournalSize;
    	this.maxWriteBatchSize = maxWriteBatchSize;
    	this.maxUnflushedJournalSize = maxUnflushedJournalSize;
    	this.maxUncommittedStoreSize = maxUncommittedStoreSize;
    	this.journalOutputBuffer = journalOutputBuffer;
    	this.storageSyncMethod = storageSyncMethod;
    	this.preAllocateFiles = preAllocateFiles;
    	this.allocationTableRandomAccessFile = allocationTableRandomAccessFile;
    	this.dataRandomAccessFile = dataRandomAccessFile;
        this.asyncTaskManager = asyncTaskManager;
        this.dirtyBlockTable = dirtyBlockTable;
        this.traceEnabled = log.isTraceEnabled();
    }
    
    private JournalFile createNewJournalFile() throws JournalException
    {	
    	// Look for a recycled file first
    	File recycledFile = null;
    	synchronized (recycledJournalFiles)
		{
    		if (recycledJournalFiles.size() > 0)
    			recycledFile = recycledJournalFiles.removeFirst();
		}
    	
    	JournalFile journalFile;
    	if (recycledFile == null)
    	{
    		// Create a new one
	        journalFile = new JournalFile(nextJournalFileIndex++, 
	                                      baseName,
	                                      dataFolder,
	                                      maxJournalSize,
	                                      journalOutputBuffer,
	                                      storageSyncMethod,
	                                      preAllocateFiles);
	        log.debug("["+baseName+"] Created a new journal file : "+journalFile);
    	}
    	else
    	{
    		// Recycle the old file
    		journalFile = new JournalFile(nextJournalFileIndex++, 
                    baseName,
                    dataFolder,
                    recycledFile,
                    journalOutputBuffer,
                    storageSyncMethod);
    		log.debug("["+baseName+"] Created a recycled journal file : "+journalFile);
    	}
     
        synchronized (journalFiles)
		{
        	journalFiles.addLast(journalFile);	
		}
        
        return journalFile;
    }
        
    /**
     * Write a data block (asynchronous)
     */
    public void writeDataBlock( int blockIndex , long blockOffset , byte[] blockData ) throws JournalException
    {
    	// Refuse any further operation in case of previous failure
    	if (failing)
    		throw new JournalException("Store journal is failing");
    	
        synchronized (journalWriteQueue)
        {
        	if (traceEnabled)
        		log.trace("["+baseName+"] #"+currentTransactionId+" Queueing data block write : ["+blockIndex+"] offset="+blockOffset+" size="+blockData.length+" / queueSize="+(journalWriteQueue.size()+1));
        	
        	DataBlockWriteOperation op = 
        		new DataBlockWriteOperation(currentTransactionId,
		        							blockIndex, 
		    		                        blockOffset, 
		    		                        blockData);
            journalWriteQueue.addLast(op);
            unflushedJournalSize += op.size();
            totalPendingOperations++;
        }
    }

    /**
     * Write some metadata block (asynchronous)
     */
    public void writeMetaDataBlock( long metaDataOffset , byte[] metaData ) throws JournalException
    {
    	// Refuse any further operation in case of previous failure
    	if (failing)
    		throw new JournalException("Store journal is failing");
    	
        synchronized (journalWriteQueue)
        {
        	if (traceEnabled)
        		log.trace("["+baseName+"] #"+currentTransactionId+" Queueing metadata block write : offset="+metaDataOffset+" size="+metaData.length+" / queueSize="+(journalWriteQueue.size()+1));
        	
        	MetaDataBlockWriteOperation op = 
        		new MetaDataBlockWriteOperation(currentTransactionId,
							                    metaDataOffset, 
							                    metaData);
            journalWriteQueue.addLast(op);
            unflushedJournalSize += op.size();
            totalPendingOperations++;
        }
    }
    
    /**
     * Write some metadata value (asynchronous)
     */
    public void writeMetaData( long metaDataOffset , int metaData ) throws JournalException
    {
    	// Refuse any further operation in case of previous failure
    	if (failing)
    		throw new JournalException("Store journal is failing");
    	
        synchronized (journalWriteQueue)
        {
        	if (traceEnabled)
        		log.trace("["+baseName+"] #"+currentTransactionId+" Queueing metadata write : offset="+metaDataOffset+" metaData="+metaData+" / queueSize="+(journalWriteQueue.size()+1));
        	
        	MetaDataWriteOperation op = new MetaDataWriteOperation(currentTransactionId,
												                   metaDataOffset, 
												                   metaData);
        	unflushedJournalSize += op.size();
            journalWriteQueue.addLast(op);
            totalPendingOperations++;
        }
    }

    /**
     * Write some store extend operation (asynchronous)
     */
    public void extendStore( int blockSize , int oldBlockCount , int newBlockCount ) throws JournalException
    {
        // Refuse any further operation in case of previous failure
        if (failing)
            throw new JournalException("Store journal is failing");
        
        synchronized (journalWriteQueue)
        {
            if (traceEnabled)
                log.trace("["+baseName+"] #"+currentTransactionId+" Queueing store extend : blockSize="+blockSize+" blockCount="+oldBlockCount+" -> "+newBlockCount+" / queueSize="+(journalWriteQueue.size()+1));
            
            StoreExtendOperation op = new StoreExtendOperation(currentTransactionId,blockSize,oldBlockCount,newBlockCount);

            unflushedJournalSize += op.size();
            journalWriteQueue.addLast(op);
            totalPendingOperations++;
        }
    }
    
    /**
     * Flush the journal write queue (asynchronous)
     */
    public void flush() throws JournalException
    {
    	if (failing)
    		throw new JournalException("Store journal is failing");
    	
    	boolean newFlushRequired = false;
    	synchronized (journalWriteQueue)
        {
    		if (unflushedJournalSize > maxUnflushedJournalSize)
    		{
    			newFlushRequired = !flushingJournal;
    			if (newFlushRequired)
    				flushingJournal = true;
    			unflushedJournalSize = 0;
    		}
        }
    	
    	// Flush journal to store (asynchronous)
    	if (newFlushRequired)
        {
        	try
        	{
        		asyncTaskManager.execute(flushJournalAsyncTask);
        	}
        	catch (JMSException e)
        	{
        		throw new JournalException("Cannot flush journal asynchronously : "+e);
        	}
        }
    }
    
    /**
     * Commit the journal (asynchronous)
     */
    public void commit( SynchronizationBarrier barrier ) throws JournalException
    {
    	// Refuse any further operation in case of previous failure
    	if (failing)
    		throw new JournalException("Store journal is failing");
    	
    	// Register as a barrier participant
    	if (barrier != null)
    		barrier.addParty();
        
        boolean newFlushRequired;
        synchronized (journalWriteQueue)
        {
        	writeCommit(barrier);
        	
        	newFlushRequired = !flushingJournal;
        	if (newFlushRequired)
        		flushingJournal = true;
        	unflushedJournalSize = 0;
        }
        
        if (newFlushRequired)
        {
	        // Flush journal to store (asynchronous)
	        try
	        {
	        	asyncTaskManager.execute(flushJournalAsyncTask);
	        }
	    	catch (JMSException e)
	    	{
	    		throw new JournalException("Cannot flush journal asynchronously : "+e);
	    	}
        }
    }
    
    private void writeCommit( SynchronizationBarrier barrier )
    {
    	if (traceEnabled)
    		log.trace("["+baseName+"] #"+currentTransactionId+" Queueing transaction commit -------------------");
    	
        journalWriteQueue.addLast(new CommitOperation(currentTransactionId,barrier));
        totalPendingOperations++;
        
        // Move to next transaction
        currentTransactionId++;
    }
    
    protected void flushJournal()
    {
    	try
    	{
            while (!failing)
            {
                synchronized (journalWriteQueue)
                {
                    if (journalWriteQueue.size() == 0)
                    {
                    	flushingJournal = false;
                        break; // Nothing more for now, exit
                    }
                    
                    // Move up to maxWriteBatchSize items to an intermediate queue 
                    int count = 0;
                    while (journalWriteQueue.size() > 0 && count < maxWriteBatchSize)
                    {
                        journalProcessingQueue.addLast(journalWriteQueue.removeFirst());
                        count++;
                    }
                }

                if (traceEnabled)
            		log.trace("["+baseName+"] [Journal] Flushing "+journalProcessingQueue.size()+" operations");
                
                // Lazy creation of the journal file
                if (currentJournalFile == null)
                	currentJournalFile = createNewJournalFile();
                
                // Process intermediate queue
                boolean commitRequired = false;
                int commitCount = 0;
                int prunedOperations = 0;
                while (journalProcessingQueue.size() > 0)
                {
                	AbstractJournalOperation op = journalProcessingQueue.getFirst();
                	
                    // Tuning : if write is superseeded by another write in the same transaction group, skip it
                    if (op instanceof AbstractMetaDataWriteOperation)
                    {
                    	//System.out.println(op.getClass().getSimpleName()+" "+((AbstractJournalWriteOperation)op).getOffset());
                    	if (isMetaDataSuperseeded((AbstractMetaDataWriteOperation)op))
                    	{
                    		// Drop operation
                    		journalProcessingQueue.removeFirst();
                    		prunedOperations++;
                        	continue;
                    	}
                    }
                 
                    // Remove from queue
                    journalProcessingQueue.removeFirst();

                    // If it's a commit operation we got more things to do
                    if (op instanceof CommitOperation)
                    {
                        CommitOperation commitOp = (CommitOperation)op;

                        // Update operations count for this transaction
                        commitOp.setOperationsCount(writtenJournalOperations);
                        writtenJournalOperations = 0;
                        
                        // Append to current journal file
                        op.writeTo(currentJournalFile);
                        
                        commitCount++;
                        
                        // Register the commit barrier for later use
                        if (commitOp.getBarrier() != null)
                        	pendingBarriers.add(commitOp.getBarrier());
                        
                        // Transaction boundary : check if we should rotate the journal file
                        if (rotateJournal())
                        {
                        	onJournalCommit();
                        	commitRequired = false; // The old journal file was synced on close
                        }
                        else
                        {
                        	// Tuning : do not commit if the transaction was empty
                        	if (commitOp.getOperationsCount() > 0)
                        		commitRequired = true;
                        }
                    }
                    else
                    {
                    	// Append to current journal file
                        op.writeTo(currentJournalFile);
                        writtenJournalOperations++;
                    	
                    	// Retain operations until next commit
                    	uncommittedJournalQueue.addLast(op);
                    }
                }
                
                // Should we commit ?
                if (commitRequired)
                {
                	if (traceEnabled)
                		log.trace("["+baseName+"] [Journal] Syncing ("+pendingBarriers.size()+" barrier(s))");
                	                	
                    // Sync journal
                	syncJournal();

                	// Post-process
                    onJournalCommit();
                }
                
                // Reach barriers
                if (pendingBarriers.size() > 0)
                {
                    for (int i = 0 ; i < pendingBarriers.size() ; i++)
                    {
                        SynchronizationBarrier barrier = pendingBarriers.get(i);
                        barrier.reach();
                    }
                    pendingBarriers.clear();
                }
                
                // Decrement global operations counter of completed commits
                if (commitCount+prunedOperations > 0)
                {
	                synchronized (journalWriteQueue)
	                {
	                	totalPendingOperations -= commitCount + prunedOperations;
	                	if (closing && totalPendingOperations == 0)
	                	    journalWriteQueue.notifyAll();
	                }
                }
            }
    	}
    	catch (DataStoreException e)
    	{
    		notifyFailure(e);
    	}
    }
    
    private boolean isMetaDataSuperseeded( AbstractMetaDataWriteOperation baseOp )
    {
    	AbstractJournalOperation current = baseOp.next();
    	int count = 0;
    	while (current != null && count < METADATA_FORWARD_SCAN_HORIZON)
    	{
    		if (current instanceof CommitOperation)
    			return false; // Stop at transaction boundary
    			
    		if (current instanceof AbstractMetaDataWriteOperation)
    		{
    			AbstractMetaDataWriteOperation writeOp = (AbstractMetaDataWriteOperation)current;
    			if (writeOp.getOffset() == baseOp.getOffset())
    				return true;
    			count++;
    		}
    		
    		current = current.next();
    	}
    	return false; // Not found
    }
    
    private void onJournalCommit() throws JournalException
    {
    	// Move completed journal operations to store write queue
    	boolean newFlushRequired;
        synchronized (storeWriteQueue)
        {
            uncommittedJournalQueue.migrateTo(storeWriteQueue);
            
            newFlushRequired = storeWriteQueue.size() > 0 && !flushingStore;
            if (newFlushRequired)
            	flushingStore = true;
        }
        
        if (newFlushRequired)
        {
        	// Schedule asynchronous store flush
	        try
	        {
	        	asyncTaskManager.execute(flushStoreAsyncTask);
	        }
	    	catch (JMSException e)
	    	{
	    		throw new JournalException("Cannot flush store asynchronously : "+e);
	    	}
        }
    }
    
    private void notifyFailure( Exception e )
    {
    	failing = true;
		log.fatal("["+baseName+"] Data store failure",e);
    }
    
    protected void flushStore()
    {
    	try
        {
    		while (!failing)
            {
                synchronized (storeWriteQueue)
                {
                    if (storeWriteQueue.size() == 0)
                    {
                    	flushingStore = false;
                        break; // Nothing more for now, exit
                    }
                    
                    // Move up to maxWriteBatchSize items to an intermediate queue 
                    int count = 0;
                    while (storeWriteQueue.size() > 0 && count < maxWriteBatchSize)
                    {
                        storeProcessingQueue.addLast(storeWriteQueue.removeFirst());
                        count++;
                    }
                }
                
                int flushCount = storeProcessingQueue.size();
                if (traceEnabled)
            		log.trace("["+baseName+"] [Store] Flushing "+flushCount+" operations");
                
                // Process intermediate queue
                while (storeProcessingQueue.size() > 0)
                {
                    AbstractJournalOperation op = storeProcessingQueue.removeFirst();
                    
                    if (op instanceof DataBlockWriteOperation)
                    {
                    	DataBlockWriteOperation blockOp = (DataBlockWriteOperation)op;
                    	uncommittedStoreSize += blockOp.writeTo(dataRandomAccessFile);
                       	
                    	// Update dirty block table
                       	dirtyBlockTable.blockFlushed(blockOp.getBlockIndex());
                    }
                    else
                	if (op instanceof MetaDataWriteOperation)
                    {
                		uncommittedStoreSize += ((MetaDataWriteOperation)op).writeTo(allocationTableRandomAccessFile);
                    }
                    else
                	if (op instanceof MetaDataBlockWriteOperation)
                    {
                		uncommittedStoreSize += ((MetaDataBlockWriteOperation)op).writeTo(allocationTableRandomAccessFile);
                    }
                	else
                    if (op instanceof StoreExtendOperation)
                    {
                        StoreExtendOperation extendOp = (StoreExtendOperation)op;
                        extendOp.extend(allocationTableRandomAccessFile, dataRandomAccessFile);
                        uncommittedStoreSize += (long)(extendOp.getNewBlockCount()-extendOp.getOldBlockCount())*AbstractBlockBasedDataStore.AT_BLOCK_SIZE;
                    }
                	else
                		throw new IllegalArgumentException("Unexpected journal operation : "+op);
                    	
                    lastStoreTransactionId = op.getTransactionId();
                }
                
                // Should we force commit ?
                if (uncommittedStoreSize > maxUncommittedStoreSize)
                {
                	uncommittedStoreSize = 0;

                	if (traceEnabled)
                		log.trace("["+baseName+"] [Store] Committing store (lastTrsId="+lastStoreTransactionId+")");
                		
                	// Sync the store
                	syncStore();
                	
                	// Recycle unused journal files
                	recycleUnusedJournalFiles();
                }
                
                // Notify waiting threads that the I/O queue is empty
                synchronized (journalWriteQueue)
                {
                	totalPendingOperations -= flushCount;
                	if (closing && totalPendingOperations == 0)
                        journalWriteQueue.notifyAll();
                }
            }
        }
    	catch (DataStoreException e)
        {
            notifyFailure(e);
        }
    }
    
    private void syncJournal() throws JournalException
    {
    	if (currentJournalFile != null)
    		currentJournalFile.sync();    	
    }
    
    private boolean rotateJournal() throws JournalException
    {
    	synchronized (journalFiles)
		{
	    	// If the journal file is full, mark it as 'complete' and create a new one
	        if (currentJournalFile.size() > maxJournalSize)
	        {
	        	log.debug("["+baseName+"] Rotating journal : "+currentJournalFile);
	        	
	        	currentJournalFile.complete();
	        	currentJournalFile = createNewJournalFile();
	        	return true;
	        }
		}
    	
    	return false;
    }
    
    private void syncStore() throws JournalException
    {
    	syncStoreFile(allocationTableRandomAccessFile);
    	synchronized (dataRandomAccessFile)
		{
    		syncStoreFile(dataRandomAccessFile);
		}
    }
    
    private void syncStoreFile( RandomAccessFile storeFile ) throws JournalException
    {
    	try
    	{
    		switch (storageSyncMethod)
    		{
    			case StorageSyncMethod.FD_SYNC : storeFile.getFD().sync(); break;
    			case StorageSyncMethod.CHANNEL_FORCE_NO_META : storeFile.getChannel().force(false); break;
    			default:
    				throw new JournalException("Unsupported sync method : "+storageSyncMethod);
    		}
    	}
    	catch (IOException e)
        {
    		log.error("["+baseName+"] Could not sync store file",e);
            throw new JournalException("Could not sync store file");
        }
    }
    
    private void recycleUnusedJournalFiles() throws JournalException
    {
    	LinkedList<JournalFile> unusedJournalFiles = null;
    	
    	// Look for unused journal files
    	synchronized (journalFiles)
		{
	    	while (journalFiles.size() > 0)
	    	{
	    		JournalFile journalFile = journalFiles.getFirst();
	    		if (journalFile.isComplete() && journalFile.getLastTransactionId() < lastStoreTransactionId)
	    		{
	    			if (unusedJournalFiles == null)
	    				unusedJournalFiles = new LinkedList<>();
	    			unusedJournalFiles.addLast(journalFile);
	    			
	    			journalFiles.removeFirst(); // Remove from list
	    		}
	    		else
	    			break;
	    	}
		}
    	
    	// Recycle unused journal files
    	if (unusedJournalFiles != null)
    	{
    		while (!unusedJournalFiles.isEmpty())
	    	{
    			JournalFile journalFile = unusedJournalFiles.removeFirst();
    			
    			if (keepJournalFiles)
    				journalFile.close();
    			else
    			{
    				log.debug("["+baseName+"] Recycling unused journal file : "+journalFile);
    				File recycledFile = journalFile.closeAndRecycle();
    				synchronized (recycledJournalFiles)
					{
    					recycledJournalFiles.addLast(recycledFile);
					}
    			}
	    	}
    	}
    }
    
    public void close() throws JournalException
    {
    	if (failing)
    		return;
    	
    	log.debug("["+baseName+"] Waiting for async operations to complete ...");
    	closing = true;
    	boolean complete = false;
    	while (true)
    	{
    		synchronized (journalWriteQueue)
            {
	    		if (totalPendingOperations == 0)
	    		{
	    			complete = true;
	    			break;
	    		}
	    		else
	    		{
	    		    // Passive wait, someone will wake us up when totalPendingOperations reaches 0
	    		    try
	    		    {
	    		        journalWriteQueue.wait();
	    		    }
	    		    catch (InterruptedException e)
	    		    {
	    		        log.error("["+baseName+"] Wait for async operations completion was interrupted",e);
	    		        break;
	    		    }
	    		}
            }    		
    	}
    	if (!complete)
    		throw new JournalException("Timeout or interrupt waiting for async tasks completion.");
    	
    	// Sync everything to disk
    	syncJournal();
    	syncStore();
    	
    	// Destroy journal files
    	destroyJournalFiles();
    }
    
    private void destroyJournalFiles() throws JournalException
    {
    	log.debug("["+baseName+"] Destroying recycled journal files ...");
    	while (recycledJournalFiles.size() > 0)
    	{
    		File recycledFile = recycledJournalFiles.removeFirst();
    		recycledFile.delete();
    	}
    	
    	log.debug("["+baseName+"] Destroying remaining journal files ...");
    	while (journalFiles.size() > 0)
    	{
    		JournalFile journalFile = journalFiles.removeFirst();
    		
    		if (keepJournalFiles)
    			journalFile.close();
    		else
    			journalFile.closeAndDelete();
    	}
    }
    
    //-------------------------------------------------------------------------------------
    //     Stub classes to interface with the disk I/O asynchronous task manager
    //-------------------------------------------------------------------------------------
    
    private class FlushJournalAsyncTask implements AsyncTask
    {
        /**
         * Constructor
         */
        public FlushJournalAsyncTask()
        {
            super();
        }
        
        /* (non-Javadoc)
         * @see net.timewalker.ffmq3.utils.async.AsyncTask#execute()
         */
        @Override
		public void execute()
        {
        	flushJournal();
        }
        
        /* (non-Javadoc)
         * @see net.timewalker.ffmq3.utils.async.AsyncTask#isMergeable()
         */
        @Override
		public boolean isMergeable()
        {
            return false;
        }
    }
    
    private class FlushStoreAsyncTask implements AsyncTask
    {
        /**
         * Constructor
         */
        public FlushStoreAsyncTask()
        {
            super();
        }
        
        /* (non-Javadoc)
         * @see net.timewalker.ffmq3.utils.async.AsyncTask#execute()
         */
        @Override
		public void execute()
        {
        	flushStore();
        }
        
        /* (non-Javadoc)
         * @see net.timewalker.ffmq3.utils.async.AsyncTask#isMergeable()
         */
        @Override
		public boolean isMergeable()
        {
            return false;
        }
    }
}
