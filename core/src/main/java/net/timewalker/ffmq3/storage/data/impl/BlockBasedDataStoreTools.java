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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Comparator;

import net.timewalker.ffmq3.storage.data.DataStoreException;
import net.timewalker.ffmq3.storage.data.impl.journal.JournalFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * BlockFileDataStoreTools
 */
public class BlockBasedDataStoreTools
{
	private static final Log log = LogFactory.getLog(BlockBasedDataStoreTools.class);

	/**
     * Create the filesystem for a new store
     */
    public static void create( String baseName ,
                               File dataFolder ,
                               int blockCount ,
                               int blockSize ,
                               boolean forceSync ) throws DataStoreException
    {
        if (blockCount <= 0)
            throw new DataStoreException("Block count should be > 0");
        if (blockSize <= 0)
            throw new DataStoreException("Block size should be > 0");
        
        File atFile = new File(dataFolder,baseName+AbstractBlockBasedDataStore.ALLOCATION_TABLE_SUFFIX);
        File dataFile = new File(dataFolder,baseName+AbstractBlockBasedDataStore.DATA_FILE_SUFFIX);
        if (atFile.exists())
            throw new DataStoreException("Cannot create store filesystem : "+atFile.getAbsolutePath()+" already exists");
        if (dataFile.exists())
            throw new DataStoreException("Cannot create store filesystem : "+dataFile.getAbsolutePath()+" already exists");
        
        initAllocationTable(atFile,
                            blockCount,
                            blockSize,
                            forceSync);
        initDataFile(dataFile,
                     blockCount,
                     blockSize,
                     forceSync);
    }
    
    private static final byte[] EMPTY_BLOCK = { 0,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1 }; // one 0 byte and three -1 ints
    
    private static void initAllocationTable(File atFile, int blockCount, int blockSize, boolean forceSync) throws DataStoreException
    {
        log.debug("Creating allocation table (size=" + blockCount + ") ...");

        // Create the file
        try
        {
            FileOutputStream outFile = new FileOutputStream(atFile);
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(outFile));

            out.writeInt(blockCount); // Block count
            out.writeInt(blockSize); // Block size
            out.writeInt(-1); // First block index
            for (int n = 0 ; n < blockCount ; n++)
                out.write(EMPTY_BLOCK);
            out.flush();
            if (forceSync)
                outFile.getFD().sync();

            out.close();
        }
        catch (IOException e)
        {
            throw new DataStoreException("Cannot initialize allocation table " + atFile.getAbsolutePath(),e);
        }
    }

    private static void initDataFile(File dataFile, int blockCount, int blockSize, boolean forceSync) throws DataStoreException
    {
        log.debug("Creating an empty map file (size=" + blockCount + "x" + blockSize + ") ...");

        // Create an empty file
        try
        {
            RandomAccessFile dataFileMap = new RandomAccessFile(dataFile, "rw");
            dataFileMap.setLength((long)blockSize * blockCount);
            if (forceSync)
                dataFileMap.getFD().sync();
            dataFileMap.close();
        }
        catch (IOException e)
        {
            throw new DataStoreException("Cannot initialize map file " + dataFile.getAbsolutePath(),e);
        }
    }
    
	/**
     * Delete the filesystem of a store
     */
    public static void delete( String baseName ,
                               File dataFolder ,
                               boolean force ) throws DataStoreException
    {
    	File[] journalFiles = findJournalFiles(baseName, dataFolder);
    	if (journalFiles.length > 0)
    	{
    		if (force)
        	{
    			for (int i = 0; i < journalFiles.length; i++)
    			{
    	        	if (!journalFiles[i].delete())
    	            	throw new DataStoreException("Cannot delete file : "+journalFiles[i].getAbsolutePath());
    			}  
        	}
    		else
    			throw new DataStoreException("Journal file exist : "+journalFiles[0].getAbsolutePath());
    	}
          	
    	
        File atFile = new File(dataFolder,baseName+AbstractBlockBasedDataStore.ALLOCATION_TABLE_SUFFIX);
        if (atFile.exists())
            if (!atFile.delete())
            	throw new DataStoreException("Cannot delete file : "+atFile.getAbsolutePath());

        File dataFile = new File(dataFolder,baseName+AbstractBlockBasedDataStore.DATA_FILE_SUFFIX);
        if (dataFile.exists())
            if (!dataFile.delete())
            	throw new DataStoreException("Cannot delete file : "+dataFile.getAbsolutePath());
    }
    
    /**
     * Find existing journal files for a given base name
     * @param baseName
     * @param dataFolder
     * @return an array of journal files
     */
    public static File[] findJournalFiles( String baseName , File dataFolder )
    {
    	final String journalBase = baseName+JournalFile.SUFFIX;
    	File[] journalFiles = dataFolder.listFiles(new FileFilter() {
			/*
			 * (non-Javadoc)
			 * @see java.io.FileFilter#accept(java.io.File)
			 */
			@Override
			public boolean accept(File pathname)
			{
				if (!pathname.isFile())
					return false;
				
				return pathname.getName().startsWith(journalBase) &&
				       !pathname.getName().endsWith(JournalFile.RECYCLED_SUFFIX);
			}
		});
    	
    	// Sort them in ascending order
    	Arrays.sort(journalFiles, new Comparator<File>() {
			@Override
			public int compare(File f1, File f2)
			{			
				return f1.getName().compareTo(f2.getName());
			}
		});
    	
    	return journalFiles;
    }
    
    /**
     * Find recycled journal files for a given base name
     * @param baseName
     * @param dataFolder
     * @return an array of journal files
     */
    public static File[] findRecycledJournalFiles( String baseName , File dataFolder )
    {
    	final String journalBase = baseName+JournalFile.SUFFIX;
    	File[] recycledFiles = dataFolder.listFiles(new FileFilter() {
			/*
			 * (non-Javadoc)
			 * @see java.io.FileFilter#accept(java.io.File)
			 */
			@Override
			public boolean accept(File pathname)
			{
				if (!pathname.isFile())
					return false;
				
				return pathname.getName().startsWith(journalBase) &&
				       pathname.getName().endsWith(JournalFile.RECYCLED_SUFFIX);
			}
		});
    	
    	return recycledFiles;
    }
}
