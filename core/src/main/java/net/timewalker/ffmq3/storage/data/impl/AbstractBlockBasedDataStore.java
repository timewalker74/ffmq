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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import net.timewalker.ffmq3.management.destination.AbstractDestinationDescriptor;
import net.timewalker.ffmq3.storage.data.DataStoreException;
import net.timewalker.ffmq3.utils.ArrayTools;
import net.timewalker.ffmq3.utils.FastBitSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <b>Block-based data store with journal support</b><br>
 * <p>
 * Implements a mix between a block-based file system structure and a doubly-linked list.
 * </p>
 * <p>
 *  Disk layout of the allocation table :
 * </p>
 * <pre>
 *  blockCount               | 4 bytes (int)     | Number of blocks in store
 *  blockSize                | 4 bytes (int)     | Size of a storage block
 *  firstClusterIndex        | 4 bytes (int)     | Index of the first block
 *  allocationTable  (*blockCount allocation entries)
 *      flags                | 1 byte            | Flags indicating the block usage
 *      allocatedSize        | 4 bytes (int)     | Size of the actual data stored in this block
 *      previousClusterIndex | 4 bytes (int)     | Index of the previous block
 *      nextClusterIndex     | 4 bytes (int)     | Index of the next block
 * </pre>
 * <p>
 *  Disk layout of the data store :
 * </p>
 * <pre>
 *  blocks (*blockCount blocks of size blockSize)
 * </pre>
 * <p>
 *  <b>Computing a block offset from its index :</b><br>
 *  blockOffset = blockIndex*blockSize<br>
 * </p>
 */
public abstract class AbstractBlockBasedDataStore extends AbstractDataStore
{
    private static final Log log = LogFactory.getLog(AbstractBlockBasedDataStore.class);

    // Constants
    public static final String DATA_FILE_SUFFIX        = ".store";
    public static final String ALLOCATION_TABLE_SUFFIX = ".index";
    
    // Flags
    private static final byte FLAG_START_BLOCK = 1;
    private static final byte FLAG_END_BLOCK   = 2;
    
    // Offsets in the allocation table   
    public static final int AT_HEADER_SIZE                 = 4+4+4;
    public static final int AT_BLOCK_SIZE                  = 1+4+4+4;
    public static final int AT_HEADER_BLOCKCOUNT_OFFSET    = 0;
    protected static final int AT_HEADER_FIRSTBLOCK_OFFSET = 4+4;
    
    // Offsets inside an allocation block
    protected static final int AB_FLAGS_OFFSET             = 0;
    protected static final int AB_ALLOCSIZE_OFFSET         = 1;
    protected static final int AB_PREVBLOCK_OFFSET         = 1+4;
    protected static final int AB_NEXTBLOCK_OFFSET         = 1+4+4;

    // Setup
    protected AbstractDestinationDescriptor descriptor;
    
    // In-memory allocation table
    protected int blockSize;
    protected int blockCount;
    private int maxBlockCount;
    private int autoExtendAmount;
    protected byte[] flags;
    protected int[] allocatedSize;
    protected int[] nextBlock;
    protected int[] previousBlock;
    protected int firstBlock;

    // Filesystem 
    protected File allocationTableFile;
    protected File dataFile;
    protected RandomAccessFile allocationTableRandomAccessFile;
    protected RandomAccessFile dataRandomAccessFile;
    
    // Runtime only
    private int lastEmpty;
    private int size;
    private int blocksInUse;
    
    /**
     * Constructor
     */
    public AbstractBlockBasedDataStore( AbstractDestinationDescriptor descriptor )
    {
        this.descriptor = descriptor;
        this.maxBlockCount = descriptor.getMaxBlockCount();
        this.autoExtendAmount = descriptor.getAutoExtendAmount();
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.LinkedDataStore#init()
     */
    public final void init() throws DataStoreException
    {
        initFilesystem();
        loadAllocationTable();
    }

    protected void initFilesystem() throws DataStoreException
    {
        String baseName = descriptor.getName();
        File dataFolder = descriptor.getDataFolder();
        
        log.debug("["+baseName+"] Initializing store '"+baseName+"' filesystem");
        try
        {
            allocationTableFile = new File(dataFolder,baseName+ALLOCATION_TABLE_SUFFIX);
            if (!allocationTableFile.canRead())
                throw new DataStoreException("Cannot access store allocation table : "+allocationTableFile.getAbsolutePath());
            allocationTableRandomAccessFile = new RandomAccessFile(allocationTableFile,"rw");
            
            dataFile = new File(dataFolder,baseName+DATA_FILE_SUFFIX);
            if (!dataFile.canRead())
                throw new DataStoreException("Cannot access store data file : "+dataFile.getAbsolutePath());
            dataRandomAccessFile = new RandomAccessFile(dataFile,"rw");
        }
        catch (FileNotFoundException e)
        {
            throw new DataStoreException("Cannot access file : "+e.getMessage());
        }
    }
    
    private final void loadAllocationTable() throws DataStoreException
    {
        log.debug("["+descriptor.getName()+"] Loading allocation table "+allocationTableFile.getAbsolutePath());
        DataInputStream in = null;
        try
        {
            in = new DataInputStream(new BufferedInputStream(new FileInputStream(allocationTableFile),16384));
            
            this.blockCount = in.readInt();
            this.blockSize  = in.readInt();
            this.firstBlock = in.readInt();
            
            this.flags         = new byte[blockCount];
            this.allocatedSize = new int[blockCount];
            this.previousBlock = new int[blockCount];
            this.nextBlock     = new int[blockCount];
            this.blocksInUse   = 0;
            int msgCount = 0;
            for (int n = 0 ; n < blockCount ; n++)
            {
                flags[n]         = in.readByte();
                allocatedSize[n] = in.readInt();
                previousBlock[n] = in.readInt();
                nextBlock[n]     = in.readInt();
                
                if (allocatedSize[n] != -1)
                {
                	blocksInUse++;
                	
                	if ((flags[n] & FLAG_START_BLOCK) > 0)
                		msgCount++;
                }
            }
            this.locks = new FastBitSet(blockCount);
            this.size = msgCount;
            
            log.debug("["+descriptor.getName()+"] "+msgCount+" entries found");
        }
        catch (EOFException e)
        {
            throw new DataStoreException("Allocation table is truncated : "+allocationTableFile.getAbsolutePath(),e);
        }
        catch (IOException e)
        {
            throw new DataStoreException("Cannot initialize allocation table : "+allocationTableFile.getAbsolutePath(),e);
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    log.error("["+descriptor.getName()+"] Could not close file input stream",e);
                }
            }
        }
    }
    
    private int findEmpty() throws DataStoreException
    {
        int pos = lastEmpty; 
        for(int n=0;n<blockCount;n++)
        {
        	if (pos >= blockCount)
                pos = 0;
        	
            if (allocatedSize[pos] == -1)
            {
                lastEmpty = pos+1;
                return pos;
            }
            
            pos++;
        }
        throw new DataStoreException("Allocation table is full ("+blockCount+" blocks)");
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.impl.AbstractDataStore#checkHandle(int)
     */
    protected void checkHandle(int handle) throws DataStoreException
    {
        if (handle < 0 ||
            handle >= blockCount ||
            allocatedSize[handle] == -1 ||
            (flags[handle] & FLAG_START_BLOCK) == 0)
            throw new DataStoreException("Invalid handle : "+handle);
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.store.ChainedDataStore#first()
     */
    public final int first() throws DataStoreException
    {
        return firstBlock;
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.store.ChainedDataStore#next(int)
     */
    public final int next(int handle) throws DataStoreException
    {
        if (SAFE_MODE) checkHandle(handle);
        
        int current = nextBlock[handle];
        while (current != -1 && (flags[current] & FLAG_START_BLOCK) == 0)
            current = nextBlock[current];

        return current;
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.store.LinkedStore#previous(int)
     */
    public final int previous(int handle) throws DataStoreException
    {
        if (SAFE_MODE) checkHandle(handle);
        
        int current = previousBlock[handle];
        while (current != -1 && (flags[current] & FLAG_START_BLOCK) == 0)
            current = previousBlock[current];
        
        return current;
    }

    private int computeSize(int handle) throws DataStoreException
    {
    	int totalSize = 0;
        int current = handle;
        while (current != -1)
        {
            totalSize += allocatedSize[current];
            if ((flags[current] & FLAG_END_BLOCK) > 0)
                break;
            current = nextBlock[current];
        }
        if (current == -1)
            throw new DataStoreException("Can't find end block for "+handle);
        
        return totalSize;
    }
    
    private int computeBlocks(int handle) throws DataStoreException
    {
    	int totalBlocks = 0;
        int current = handle;
        while (current != -1)
        {
        	totalBlocks++;
            if ((flags[current] & FLAG_END_BLOCK) > 0)
                break;
            current = nextBlock[current];
        }
        if (current == -1)
            throw new DataStoreException("Can't find end block for "+handle);
        
        return totalBlocks;
    }
    
    public final byte[] retrieveHeader(int handle,int headerSize) throws DataStoreException
    {
    	if (SAFE_MODE) checkHandle(handle);
    	
    	// Read block from map file
    	byte[] data = new byte[headerSize];
    	readDataBlock(data,0,headerSize,handle);
    	return data;
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.store.ChainedDataStore#retrieve(int)
     */
    public final Object retrieve(int handle) throws DataStoreException
    {
        if (SAFE_MODE) checkHandle(handle);
        
        // Compute total size
        int totalSize = computeSize(handle);
        
        // Retrieve all blocks
        byte[] data = new byte[totalSize];
        int offset = 0;
        int current = handle;
        while (current != -1)
        {
            int blockLen = allocatedSize[current];
            
            // Read block from map file
            readDataBlock(data,offset,blockLen,current);
            offset += blockLen;
            
            if ((flags[current] & FLAG_END_BLOCK) > 0)
                break;
            current = nextBlock[current];
        }
        if (current == -1)
            throw new DataStoreException("Can't find end block for "+handle);
        
        return data;
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.store.ChainedDataStore#size()
     */
    public final int size()
    {
        return size;
    }

    private byte[] asByteArray( Object obj ) throws DataStoreException
    {
    	if (obj instanceof byte[])
    		return (byte[])obj;
    	throw new DataStoreException("Unsupported object type : "+obj.getClass().getName());
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.store.ChainedDataStore#delete(int)
     */
    public final int delete(int handle) throws DataStoreException
    {
        if (SAFE_MODE) checkHandle(handle);
        
        int previousHandle = previousBlock[handle];
          
        int current = handle;
        int nextHandle = -1;
        while (current != -1)
        {
            nextHandle = nextBlock[current];
            boolean isEndBlock = (flags[current] & FLAG_END_BLOCK) > 0;
            
            // Clear block entry
            flags[current] = 0;
            allocatedSize[current] = -1;
            previousBlock[current] = -1;
            nextBlock[current] = -1;
            locks.clear(current);
            
            // Update used blocks count
            blocksInUse--;
            
            writeAllocationBlock(current);
            
            if (isEndBlock)
                break;  
            current = nextHandle;
        }     
        // Reconnect chain
        if (nextHandle != -1)
        {
            previousBlock[nextHandle] = previousHandle;
            writeAllocationBlock(nextHandle);
        }
        if (previousHandle != -1)
        {
            nextBlock[previousHandle] = nextHandle;
            writeAllocationBlock(previousHandle);
        }

        // Update first block if necessary
        if (firstBlock == handle)
        {
            firstBlock = nextHandle;
            writeFirstBlock();
        }
        
        size--;
        
        flush();
        
        // Find previous object start from previous block handle
        while (previousHandle != -1 && (flags[previousHandle] & FLAG_START_BLOCK) == 0)
        	previousHandle = previousBlock[previousHandle];
        
        return previousHandle;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.LinkedDataStore#replace(int, java.lang.Object)
     */
    public final int replace(int handle, Object obj) throws DataStoreException
    {
    	if (SAFE_MODE)
    		checkHandle(handle);
    	
    	byte[] data = asByteArray(obj);
    	int previousHandle = previousBlock[handle];
    	
    	// Make sure we have enough space to do this
    	int originalBlocks = computeBlocks(handle);
    	int targetBlocks = (data.length/blockSize) + ((data.length % blockSize) > 0 ? 1 : 0);
    	if (targetBlocks > originalBlocks)
    	{
    		int requiredFreeBlocks = targetBlocks-originalBlocks;
    		if (requiredFreeBlocks+blocksInUse > blockCount)
    		{
        		log.error("["+descriptor.getName()+"] Not enough free blocks to update message ("+requiredFreeBlocks+" needed, "+(blockCount-blocksInUse)+" left), queue is full.");
        		return -1;
    		}
    	}

    	// Save locking status
    	boolean wasLocked = isLocked(handle);
    	
    	// Delete the old message
    	delete(handle);
    	
    	// Store the new version
    	int newHandle = store(obj, previousHandle);
    	
    	// Re-lock if necessary
    	if (wasLocked)
    		lock(newHandle);
    	
    	flush();
    	
    	return newHandle;
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.store.ChainedDataStore#store(byte[], int)
     */
    public final int store(Object obj, int previousHandle) throws DataStoreException
    {
    	byte[] data = asByteArray(obj);
    	
        int lastBlockOfPreviousEntry;
        int firstBlockOfNextEntry;
        
        // Find blocks around insertion point
        if (previousHandle != -1)
        {
            //if (SAFE_MODE) checkHandle(previousHandle); FIXME previousHandle may already be lastBlockOfPreviousEntry
        
            // Find end block
            lastBlockOfPreviousEntry = previousHandle;
            while (lastBlockOfPreviousEntry != -1 && (flags[lastBlockOfPreviousEntry] & FLAG_END_BLOCK) == 0)
                lastBlockOfPreviousEntry = nextBlock[lastBlockOfPreviousEntry];
            if (lastBlockOfPreviousEntry == -1)
                throw new DataStoreException("Can't find end block for "+previousHandle);
            
            // Get successor block
            firstBlockOfNextEntry = nextBlock[lastBlockOfPreviousEntry];
        }
        else
        {
            lastBlockOfPreviousEntry = previousHandle;
            firstBlockOfNextEntry = firstBlock;
        }
        
        // Store data in split blocks
        int newHandle = storeData(data,lastBlockOfPreviousEntry,firstBlockOfNextEntry);
        if (newHandle == -1)
        	return -1;
        
        // Update first block index if necessary
        // --> This should be done last for I/O failure reliability : 
        //      if something goes wrong before this, the firstBlock is still consistent
        if (previousHandle == -1)
        {
            firstBlock = newHandle;
            writeFirstBlock();
        }
        
        size++;

        flush();
        
        return newHandle;
    }
    
    private boolean autoExtendStore() throws DataStoreException
    {
        if (autoExtendAmount == 0)
            return false;
        
        int oldBlockCount = blockCount;
        int newBlockCount = Math.min(blockCount+autoExtendAmount,maxBlockCount);
        if (newBlockCount <= oldBlockCount)
            return false;
        
        log.debug("["+descriptor.getName()+"] Auto-extending store to "+newBlockCount+" blocks");
         
        // Extend memory structures
        this.flags         = ArrayTools.extend(flags, newBlockCount);
        this.allocatedSize = ArrayTools.extend(allocatedSize, newBlockCount);
        this.nextBlock     = ArrayTools.extend(nextBlock, newBlockCount);
        this.previousBlock = ArrayTools.extend(previousBlock, newBlockCount);
        for (int n = blockCount ; n < newBlockCount ; n++)
        {
            allocatedSize[n] = -1;
            previousBlock[n] = -1;
            nextBlock[n] = -1;
        }
        this.locks.ensureCapacity(newBlockCount);
        this.blockCount = newBlockCount;
        
        // Extend physical storage
        extendStoreFiles(oldBlockCount,newBlockCount);
        
        return true;
    }
    
    private int storeData(byte[] data,int previousBlockHandle,int nextBlockHandle) throws DataStoreException
    {
        int fullBlocks = data.length / blockSize;
        int remaining = data.length % blockSize;
        
        // Check that we have enough space before changing anything
        int requiredFreeBlocks = fullBlocks + (remaining > 0 ? 1 : 0);
        if ((blocksInUse + requiredFreeBlocks) > blockCount)
        {
            if ((blocksInUse + requiredFreeBlocks) > maxBlockCount)
            {
            	log.debug("["+descriptor.getName()+"] Not enough free blocks to store message ("+requiredFreeBlocks+" needed, "+(blockCount-blocksInUse)+" left), queue is full.");
            	return -1;
            }
            else
            {
                // Auto-extend store
                if (!autoExtendStore())
                    return -1;
            }
        }
        
        int newHandle = -1;
        int lastBlock = previousBlockHandle;
        int offset = 0;
        for (int i = 0 ; i < fullBlocks ; i++)
        {
        	boolean isStartBlock = (i == 0);
        	boolean isEndBlock = (remaining == 0 && i == fullBlocks-1);
        	
        	lastBlock = storeDataBlock(data,
                                   offset,
                                   blockSize,
                                   lastBlock,
                                   isStartBlock,
                                   isEndBlock);
            offset += blockSize;
            if (isStartBlock)
                newHandle = lastBlock;
        }
        if (remaining > 0)
        {
        	lastBlock = storeDataBlock(data,
                                   offset,
                                   remaining,
                                   lastBlock,
                                   fullBlocks == 0,
                                   true);
            if (newHandle == -1)
                newHandle = lastBlock;
        }
        
        // Connect end of sub-list to existing list
        if (nextBlockHandle != -1)
        {
            nextBlock[lastBlock] = nextBlockHandle;
            previousBlock[nextBlockHandle] = lastBlock;
            writeAllocationBlock(nextBlockHandle);
        }
        
        // Connect end of sub-list to existing list
        if (previousBlockHandle != -1)
        {            
            nextBlock[previousBlockHandle] = newHandle;
            writeAllocationBlock(previousBlockHandle);
        }
        
        // Write intermediate allocation blocks
        int current = newHandle;
        for (int i = 0 ; i < requiredFreeBlocks ; i++)
        {
        	 writeAllocationBlock(current);
        	 current = nextBlock[current];
        }
        
        // Update used blocks count
        blocksInUse += requiredFreeBlocks;

        return newHandle;
    }
    
    private int storeDataBlock(byte[] data,int offset,int len,int previousHandle,boolean startBlock,boolean endBlock) throws DataStoreException
    {
        int nextEmpty = findEmpty();
        
        byte flag = 0;
        if (startBlock) flag |= FLAG_START_BLOCK;
        if (endBlock)   flag |= FLAG_END_BLOCK;
        flags[nextEmpty] = flag;
        nextBlock[nextEmpty] = -1;
        previousBlock[nextEmpty] = previousHandle;
        allocatedSize[nextEmpty] = len;
        
        // Connect block to previous if available
        if (previousHandle != -1)
            nextBlock[previousHandle] = nextEmpty;
        
        // Write data to map file
       	writeDataBlock(data,offset,len,nextEmpty);
        
        return nextEmpty;
    }

    
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.store.ChainedDataStore#close()
     */
    public void close()
    {
    	// Close filesystem
        try
        {
            if (allocationTableRandomAccessFile != null)
                allocationTableRandomAccessFile.close();
        }
        catch (IOException e)
        {
            log.error("["+descriptor.getName()+"] Could not close allocation table file : "+e.toString());
        }
        try
        {
            if (dataRandomAccessFile != null)
                dataRandomAccessFile.close();
        }
        catch (IOException e)
        {
            log.error("["+descriptor.getName()+"] Could not close map file : "+e.toString());
        }
    }
	
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append("Allocation Table (size="+size+")\n");
        sb.append("------------------------------------\n");
        sb.append("first block index : ");
        sb.append(firstBlock);
        sb.append("\n");
        for (int n = 0 ; n < blockCount ; n++)
        {
            sb.append(n);
            sb.append(": ");
            if (allocatedSize[n] == -1)
                sb.append("(free)\n");
            else
            {
                sb.append(previousBlock[n]);
                sb.append("\t");
                sb.append(nextBlock[n]);
                sb.append("\t");
                sb.append(allocatedSize[n]);
                if ((flags[n] & FLAG_START_BLOCK) > 0)
                {
                    sb.append("\t");
                    sb.append("START");
                }
                if ((flags[n] & FLAG_END_BLOCK) > 0)
                {
                    sb.append("\t");
                    sb.append("END");
                }
                sb.append("\n");
            }
        }
        sb.append("------------------------------------\n");
        
        return sb.toString();
    }
    
    /**
     * Run an integrity check on the store files and fix them as necessary
     * @throws DataStoreException if the files could not be fixed
     */
    protected void integrityCheck() throws DataStoreException
    {
        try
        {
            //========================
            // 1 - Check files sizes
            //========================
            // -- Allocation table
            long atFileSize = allocationTableRandomAccessFile.length();
            if (atFileSize < AT_HEADER_SIZE+AT_BLOCK_SIZE) /* Should have at least one entry */
                throw new DataStoreException("Allocation table is truncated : "+allocationTableFile.getAbsolutePath());
            
            // Read some header fields
            FileInputStream inFile = new FileInputStream(allocationTableFile);
            DataInputStream in = new DataInputStream(new BufferedInputStream(inFile,16384));
            int blockCount = in.readInt();
            int blockSize  = in.readInt();
            int firstBlock = in.readInt();
            // Fix AT size
            long expectedATFileSize = AT_HEADER_SIZE+AT_BLOCK_SIZE*(long)blockCount;
            if (atFileSize != expectedATFileSize)
            {
                log.error("["+descriptor.getName()+"] Allocation table has an invalid size (actual:"+atFileSize+",expected:"+expectedATFileSize+"), fixing.");
                allocationTableRandomAccessFile.setLength(expectedATFileSize);
            }
            // Fix data size
            long dataFileSize = dataRandomAccessFile.length();
            long expectedDataFileSize = (long)blockSize*blockCount;
            if (dataFileSize != expectedDataFileSize)
            {
                log.error("["+descriptor.getName()+"] Data file has an invalid size (actual:"+dataFileSize+",expected:"+expectedDataFileSize+"), fixing.");
                dataRandomAccessFile.setLength(expectedDataFileSize);
            }
            
            //============================
            // 2 - Check allocation table
            //============================
            // Read the AT into memory
            byte[] flags        = new byte[blockCount];
            int[] allocatedSize = new int[blockCount];
            int[] previousBlock = new int[blockCount];
            int[] nextBlock     = new int[blockCount];
            int blocksInUse = 0;
            int msgCount = 0;
            for (int n = 0 ; n < blockCount ; n++)
            {
                flags[n]         = in.readByte();
                allocatedSize[n] = in.readInt();
                previousBlock[n] = in.readInt();
                nextBlock[n]     = in.readInt();
                if (allocatedSize[n] != -1)
                {
                    blocksInUse++;
                    if ((flags[n] & FLAG_START_BLOCK) > 0)
                        msgCount++;
                }
            }
            in.close();
            log.debug("["+descriptor.getName()+"] Blocks in use before fix : "+blocksInUse);
            log.debug("["+descriptor.getName()+"] Messages count before fix : "+msgCount);
            
            // Fix first block index
            boolean changed = false;
            if (firstBlock < -1 || firstBlock >= blockCount)
            {
                log.error("["+descriptor.getName()+"] Invalid allocation table first block index ("+firstBlock+"), guessing new one ...");
                firstBlock = guessFirstBlockIndex(blockCount, allocatedSize, nextBlock);
                log.debug("["+descriptor.getName()+"] Guessed first block index : "+firstBlock);
                changed = true;
            }
            
            // Recover table
            if (msgCount == 0)
            {
                if (firstBlock == -1)
                {
                    // Table is empty, cleanup dirty entries
                    changed = changed || cleanupEmptyBlocks(blockCount, flags, allocatedSize, previousBlock, nextBlock);
                }
                else
                {
                    log.error("["+descriptor.getName()+"] First block index should be -1, clearing ...");
                    firstBlock = -1;
                    changed = true;
                }
            }
            else
            {
                if (firstBlock == -1)
                {
                    log.error("["+descriptor.getName()+"] Invalid first block index, guessing value ...");
                    firstBlock = guessFirstBlockIndex(blockCount, allocatedSize, nextBlock);
                    log.debug("["+descriptor.getName()+"] Guessed first block index : "+firstBlock);
                    changed = true;
                }
     
                changed = changed || fixBlocks(blockCount, blockSize, firstBlock, flags, allocatedSize, previousBlock, nextBlock);
                changed = changed || cleanupEmptyBlocks(blockCount, flags, allocatedSize, previousBlock, nextBlock);
            }
            
            // Update the allocation file table
            if (changed)
            {
                // Re-compute size
                msgCount = 0;
                blocksInUse = 0;
                for (int n = 0 ; n < blockCount ; n++)
                {
                    if (allocatedSize[n] != -1) 
                    {
                        blocksInUse++;
                        if ((flags[n] & FLAG_START_BLOCK) > 0)
                            msgCount++;
                    }
                }
                log.debug("["+descriptor.getName()+"] Blocks in use after fix : "+blocksInUse);
                log.debug("["+descriptor.getName()+"] Messages count after fix : "+msgCount);
                
                log.debug("["+descriptor.getName()+"] Allocation table was altered, saving ...");
                allocationTableRandomAccessFile.seek(AT_HEADER_FIRSTBLOCK_OFFSET);
                allocationTableRandomAccessFile.writeInt(firstBlock);
                for (int n = 0 ; n < blockCount ; n++)
                {
                    byte[] allocationBlock = new byte[AT_BLOCK_SIZE];

                    // Regroup I/O to improve performance
                    allocationBlock[AB_FLAGS_OFFSET]       = flags[n];
                    allocationBlock[AB_ALLOCSIZE_OFFSET]   = (byte)((allocatedSize[n] >>> 24) & 0xFF);
                    allocationBlock[AB_ALLOCSIZE_OFFSET+1] = (byte)((allocatedSize[n] >>> 16) & 0xFF);
                    allocationBlock[AB_ALLOCSIZE_OFFSET+2] = (byte)((allocatedSize[n] >>>  8) & 0xFF);
                    allocationBlock[AB_ALLOCSIZE_OFFSET+3] = (byte)((allocatedSize[n] >>>  0) & 0xFF);
                    allocationBlock[AB_PREVBLOCK_OFFSET]   = (byte)((previousBlock[n] >>> 24) & 0xFF);
                    allocationBlock[AB_PREVBLOCK_OFFSET+1] = (byte)((previousBlock[n] >>> 16) & 0xFF);
                    allocationBlock[AB_PREVBLOCK_OFFSET+2] = (byte)((previousBlock[n] >>>  8) & 0xFF);
                    allocationBlock[AB_PREVBLOCK_OFFSET+3] = (byte)((previousBlock[n] >>>  0) & 0xFF);
                    allocationBlock[AB_NEXTBLOCK_OFFSET]   = (byte)((nextBlock[n] >>> 24) & 0xFF);
                    allocationBlock[AB_NEXTBLOCK_OFFSET+1] = (byte)((nextBlock[n] >>> 16) & 0xFF);
                    allocationBlock[AB_NEXTBLOCK_OFFSET+2] = (byte)((nextBlock[n] >>>  8) & 0xFF);
                    allocationBlock[AB_NEXTBLOCK_OFFSET+3] = (byte)((nextBlock[n] >>>  0) & 0xFF);
                    
                    allocationTableRandomAccessFile.seek(AT_HEADER_SIZE+n*AT_BLOCK_SIZE);
                    allocationTableRandomAccessFile.write(allocationBlock);
                }
                allocationTableRandomAccessFile.getFD().sync();
            }
            else
                log.debug("["+descriptor.getName()+"] Allocation table was not altered");
        }
        catch (IOException e)
        {
            throw new DataStoreException("Cannot check/fix store integrity : "+e);
        }
    }
    
    private int guessFirstBlockIndex( int blockCount , int[] allocatedSize , int[] nextBlock )
    {
        FastBitSet referenced = new FastBitSet(blockCount);
        
        // Flag all referenced blocks
        for (int n = 0 ; n < blockCount ; n++)
            if (allocatedSize[n] != -1 && nextBlock[n] != -1)
                referenced.set(nextBlock[n]);
        
        // Find candidate
        for (int n = 0 ; n < blockCount ; n++)
            if (allocatedSize[n] != -1 &&
                nextBlock[n] != -1 &&
                !referenced.get(n))
                return n;
        
        return -1;
    }
    
    private boolean fixBlocks( int blockCount , int blockSize , int firstBlock , byte[] flags , int[] allocatedSize , int[] previousBlock , int[] nextBlock )
    {
        boolean changed = false;
        FastBitSet fixedBlocks = new FastBitSet(blockCount);
        
        // Fix reverse links first
        int previous = -1;
        int current = firstBlock;
        while (current != -1)
        {
            if (previousBlock[current] != previous)
            {
                log.debug("["+descriptor.getName()+"] Fixing previous reference "+previousBlock[current]+" -> "+previous);
                previousBlock[current] = previous;
                changed = true;
            }
            fixedBlocks.set(current);
            previous = current;
            current = nextBlock[current];
        }
        
        // Look for lost blocks and fix allocated sizes
        for (int n = 0 ; n < blockCount ; n++)
        {
            if (allocatedSize[n] != -1 && !fixedBlocks.get(n))
            {
                log.warn("["+descriptor.getName()+"] Lost block found : "+n);
                allocatedSize[n] = -1;
                changed = true;
            }
            if (fixedBlocks.get(n) && (allocatedSize[n] <= 0 || allocatedSize[n]>blockSize))
            {
                log.warn("["+descriptor.getName()+"] Block has an invalid size ("+allocatedSize[n]+"), replacing by "+blockSize);
                allocatedSize[n] = blockSize;
                changed = true;
            }
        }

        // Fix flags
        boolean startExpected = true;
        previous = -1;
        current = firstBlock;
        while (current != -1)
        {
            if (startExpected)
            {
                if ((flags[current] & FLAG_START_BLOCK) == 0)
                {
                    log.warn("["+descriptor.getName()+"] Missing start block flag, adding to block "+current);
                    flags[current] |= FLAG_START_BLOCK;
                    changed = true;
                }
                if ((flags[current] & FLAG_END_BLOCK) == 0)
                    startExpected = false;
            }
            else
            {
                if ((flags[current] & FLAG_START_BLOCK) > 0)
                {
                    log.warn("["+descriptor.getName()+"] Missing end block flag, adding to previous block : "+previous);
                    flags[previous] |= FLAG_END_BLOCK;
                    changed = true;
                }
                if ((flags[current] & FLAG_END_BLOCK) > 0)
                    startExpected = true;
            }

            previous = current;
            current = nextBlock[current];
        }
        // Fix end of chain if necessary
        if (!startExpected)
        {
            log.warn("["+descriptor.getName()+"] Missing end block flag, adding to last block : "+previous);
            flags[previous] |= FLAG_END_BLOCK;
            changed = true;
        }
        
        return changed;
    }
    
    private boolean cleanupEmptyBlocks( int blockCount , byte[] flags , int[] allocatedSize , int[] previousBlock, int[] nextBlock )
    {
        boolean changed = false;
        for (int n = 0 ; n < blockCount ; n++)
        {
            if (allocatedSize[n] == -1 && (
                flags[n] != 0 ||
                nextBlock[n] != -1 ||
                previousBlock[n] != -1))
            {
                flags[n] = 0;
                nextBlock[n] = -1;
                previousBlock[n] = -1;
                changed = true;
            }
        }
        return changed;
    }
    
    public final int getBlockSize()
    {
        return blockSize;
    }
    
    /**
     * Write the first block index to disk
     * @throws DataStoreException
     */
    protected abstract void writeFirstBlock() throws DataStoreException;
    
    /**
     * Write an allocation block to disk
     * @throws DataStoreException
     */
    protected abstract void writeAllocationBlock( int blockIndex ) throws DataStoreException;
    
    /**
     * Write a data block to disk
     * @throws DataStoreException
     */
    protected abstract void writeDataBlock(byte[] data, int offset, int len, int blockHandle) throws DataStoreException;

    /**
     * Read a data block to disk
     * @throws DataStoreException
     */
    protected abstract void readDataBlock(byte[] data, int offset, int len, int blockHandle) throws DataStoreException;

    /**
     * Extend the store files to newBlockCount
     * @throws DataStoreException
     */
    protected abstract void extendStoreFiles( int oldBlockCount , int newBlockCount ) throws DataStoreException;
    
    /**
     * Flush internal buffers
     * @throws DataStoreException
     */
    protected abstract void flush() throws DataStoreException;

    /**
     * Serialize allocation block at index blockIndex
     * @param blockIndex the block index
     * @return the serialized allocation block
     */
    protected final byte[] serializeAllocationBlock( int blockIndex )
    {
    	byte[] allocationBlock = new byte[AT_BLOCK_SIZE];

        // Regroup I/O to improve performance
        allocationBlock[AB_FLAGS_OFFSET]       = flags[blockIndex];
        allocationBlock[AB_ALLOCSIZE_OFFSET]   = (byte)((allocatedSize[blockIndex] >>> 24) & 0xFF);
        allocationBlock[AB_ALLOCSIZE_OFFSET+1] = (byte)((allocatedSize[blockIndex] >>> 16) & 0xFF);
        allocationBlock[AB_ALLOCSIZE_OFFSET+2] = (byte)((allocatedSize[blockIndex] >>>  8) & 0xFF);
        allocationBlock[AB_ALLOCSIZE_OFFSET+3] = (byte)((allocatedSize[blockIndex] >>>  0) & 0xFF);
        allocationBlock[AB_PREVBLOCK_OFFSET]   = (byte)((previousBlock[blockIndex] >>> 24) & 0xFF);
        allocationBlock[AB_PREVBLOCK_OFFSET+1] = (byte)((previousBlock[blockIndex] >>> 16) & 0xFF);
        allocationBlock[AB_PREVBLOCK_OFFSET+2] = (byte)((previousBlock[blockIndex] >>>  8) & 0xFF);
        allocationBlock[AB_PREVBLOCK_OFFSET+3] = (byte)((previousBlock[blockIndex] >>>  0) & 0xFF);
        allocationBlock[AB_NEXTBLOCK_OFFSET]   = (byte)((nextBlock[blockIndex] >>> 24) & 0xFF);
        allocationBlock[AB_NEXTBLOCK_OFFSET+1] = (byte)((nextBlock[blockIndex] >>> 16) & 0xFF);
        allocationBlock[AB_NEXTBLOCK_OFFSET+2] = (byte)((nextBlock[blockIndex] >>>  8) & 0xFF);
        allocationBlock[AB_NEXTBLOCK_OFFSET+3] = (byte)((nextBlock[blockIndex] >>>  0) & 0xFF);
        
        return allocationBlock;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.data.LinkedDataStore#getStoreUsage()
     */
    public final int getStoreUsage()
    {
    	long ratio = blockCount > 0 ? (long)blocksInUse*100/blockCount : 0;
    	return (int)ratio;
    }
}
