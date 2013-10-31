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
import java.nio.channels.FileChannel;

import net.timewalker.ffmq3.storage.StorageSyncMethod;
import net.timewalker.ffmq3.storage.data.DataStoreException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JournalFile
 */
public final class JournalFile
{
	private static final Log log = LogFactory.getLog(JournalFile.class);
	
    public static final String SUFFIX = ".journal";
    public static final String RECYCLED_SUFFIX = ".recycled";
    
    // Attributes
    private String baseName;
    private int storageSyncMethod;
    
    // Runtime
    private File file;
    private FileChannel channel;
    private RandomAccessFile output;
    private byte[] writeBuffer;
    private int usedBufferSize;
    private long size;
    private long lastTransactionId;
    private boolean complete;
    
    /**
     * Constructor (new file)
     */
    public JournalFile( int index , String baseName , File dataFolder , long maxJournalSize , int writeBufferSize , int storageSyncMethod , boolean preAllocateFiles ) throws JournalException
    {
    	this.baseName = baseName;
    	this.storageSyncMethod = storageSyncMethod;
        initNewFile(index, baseName, dataFolder, maxJournalSize, writeBufferSize, preAllocateFiles);
    }
    
    /**
     * Constructor (recycled file)
     */
    public JournalFile( int index , String baseName , File dataFolder , File recycledFile , int writeBufferSize , int storageSyncMethod ) throws JournalException
    {
    	this.baseName = baseName;
    	this.storageSyncMethod = storageSyncMethod;
    	initFromRecycledFile(index, baseName, dataFolder, recycledFile, writeBufferSize);
    }
    
    private void initNewFile( int index , String baseName , File dataFolder , long maxJournalSize , int writeBufferSize , boolean preAllocateFiles ) throws JournalException
    {
        this.file = new File(dataFolder,baseName+SUFFIX+"."+index);
        if (file.exists())
        	throw new JournalException("Journal file already exists : "+file.getAbsolutePath());
        
        try
        {
            this.output = new RandomAccessFile(file,"rw");
            this.channel = output.getChannel();
            if (preAllocateFiles)
            	fillWithZeroes(maxJournalSize,writeBufferSize);
        }
        catch (IOException e)
        {
        	log.error("["+baseName+"] Cannot create store journal : "+file.getAbsolutePath(),e);
            throw new JournalException("Cannot create store journal : "+file.getAbsolutePath(),e);
        }
        
        this.writeBuffer = new byte[writeBufferSize];
    }
    
    private void initFromRecycledFile( int index , String baseName , File dataFolder , File recycledFile , int writeBufferSize ) throws JournalException
    {
        this.file = new File(dataFolder,baseName+SUFFIX+"."+index);
        if (file.exists())
        	throw new JournalException("Journal file already exists : "+file.getAbsolutePath());
        
        if (!recycledFile.renameTo(file))
        	throw new JournalException("Cannot rename recycled journal file "+recycledFile.getAbsolutePath()+" to "+file.getAbsolutePath());
        
        try
        {
            this.output = new RandomAccessFile(file,"rw");
            this.channel = output.getChannel();
        }
        catch (IOException e)
        {
        	log.error("["+baseName+"] Cannot initialize store journal : "+file.getAbsolutePath(),e);
            throw new JournalException("Cannot initialize store journal : "+file.getAbsolutePath(),e);
        }
        
        this.writeBuffer = new byte[writeBufferSize];
    }

    private void fillWithZeroes( long maxJournalSize , int writeBufferSize ) throws IOException
    {
    	// Fill journal file with maxJournalSize zeroes, using a chunk size of writeBufferSize
    	this.output.seek(0);
    	byte[] zeroes = new byte[writeBufferSize];
    	long total = 0;
    	while (total+writeBufferSize < maxJournalSize)
    	{
    		this.output.write(zeroes);
    		total += writeBufferSize;
    	}
    	if (total < maxJournalSize)
    		this.output.write(zeroes,0,(int)(maxJournalSize-total));
    	this.output.seek(0);
    }
    
    /**
     * Force file content sync to disk
     * @throws DataStoreException
     */
    protected void sync() throws JournalException
    {      
    	try
    	{	       
    		flushBuffer();
    		
	        switch (storageSyncMethod)
    		{
    			case StorageSyncMethod.FD_SYNC : output.getFD().sync(); break;
    			case StorageSyncMethod.CHANNEL_FORCE_NO_META : channel.force(false); break;
    			default:
    				throw new JournalException("Unsupported sync method : "+storageSyncMethod);
    		}
    	}
        catch (IOException e)
        {
        	log.error("["+baseName+"] Cannot create sync journal file : "+file.getAbsolutePath(),e);
            throw new JournalException("Could not sync journal file : "+file.getAbsolutePath(),e);
        }
    }

    protected void complete() throws JournalException
    {
    	sync();
    	complete = true;
    }
    
    private void flushBuffer() throws IOException
    {
    	if (usedBufferSize > 0)
    	{
    		output.write(writeBuffer,0,usedBufferSize);
    		usedBufferSize = 0;
    	}
    }
    
    public void write( byte[] data ) throws JournalException
    {
        try
        {
        	int len = data.length;
        	
        	if (len > writeBuffer.length)
        	{
        		// Too big, write it directly
        		flushBuffer();
        		output.write(data);
        	}
        	else
        	{
        		if (len > writeBuffer.length - usedBufferSize)
        		{
        			// Need to make some room first
        			flushBuffer();
        		}
        		
        		System.arraycopy(data, 0, writeBuffer, usedBufferSize, len);
        		usedBufferSize += len;
        	}
            
            size += data.length;
        }
        catch (IOException e)
        {
        	log.error("["+baseName+"] Cannot write to store journal : "+file.getAbsolutePath(),e);
            throw new JournalException("Cannot write to store journal : "+file.getAbsolutePath(),e);
        }
    }
    
    public void writeByte( int v ) throws JournalException
    {
        try
        {
        	if (usedBufferSize == writeBuffer.length)
    		{
    			// Need to make some room first
    			flushBuffer();
    		}
    		
        	writeBuffer[usedBufferSize++] = (byte)v;
            size++;
        }
        catch (IOException e)
        {
        	log.error("["+baseName+"] Cannot write to store journal : "+file.getAbsolutePath(),e);
            throw new JournalException("Cannot write to store journal : "+file.getAbsolutePath(),e);
        }
    }
    
    public void writeInt( int v ) throws JournalException
    {
        try
        {
        	if (writeBuffer.length - usedBufferSize < 4)
    		{
    			// Need to make some room first
    			flushBuffer();
    		}
        	
        	writeBuffer[usedBufferSize++] = (byte)((v >>> 24) & 0xFF);
        	writeBuffer[usedBufferSize++] = (byte)((v >>> 16) & 0xFF);
        	writeBuffer[usedBufferSize++] = (byte)((v >>>  8) & 0xFF);
        	writeBuffer[usedBufferSize++] = (byte)((v >>>  0) & 0xFF);

            size += 4;
        }
        catch (IOException e)
        {
        	log.error("["+baseName+"] Cannot write to store journal : "+file.getAbsolutePath(),e);
            throw new JournalException("Cannot write to store journal : "+file.getAbsolutePath(),e);
        }
    }
    
    public void writeLong( long v ) throws JournalException
    {
        try
        {
        	if (writeBuffer.length - usedBufferSize < 8)
    		{
    			// Need to make some room first
    			flushBuffer();
    		}
        	
        	writeBuffer[usedBufferSize++] = (byte)((v >>> 56) & 0xFF);
        	writeBuffer[usedBufferSize++] = (byte)((v >>> 48) & 0xFF);
        	writeBuffer[usedBufferSize++] = (byte)((v >>> 40) & 0xFF);
        	writeBuffer[usedBufferSize++] = (byte)((v >>> 32) & 0xFF);
        	writeBuffer[usedBufferSize++] = (byte)((v >>> 24) & 0xFF);
        	writeBuffer[usedBufferSize++] = (byte)((v >>> 16) & 0xFF);
        	writeBuffer[usedBufferSize++] = (byte)((v >>>  8) & 0xFF);
        	writeBuffer[usedBufferSize++] = (byte)((v >>>  0) & 0xFF);

            size += 8;
        }
        catch (IOException e)
        {
        	log.error("["+baseName+"] Cannot write to store journal : "+file.getAbsolutePath(),e);
            throw new JournalException("Cannot write to store journal : "+file.getAbsolutePath(),e);
        }
    }
    
    public long size()
    {
        return size;
    }
    
    /**
	 * @return the complete
	 */
	public boolean isComplete()
	{
		return complete;
	}
    
    /**
     * @param lastTransactionId the lastTransactionId to set
     */
    public void setLastTransactionId(long lastTransactionId)
    {
        this.lastTransactionId = lastTransactionId;
    }
    
    /**
     * @return the lastTransactionId
     */
    public long getLastTransactionId()
    {
        return lastTransactionId;
    }
    
    /**
     * Close and delete the journal file
     */
    public void closeAndDelete() throws JournalException
    {
    	close();
    	if (!file.delete())
    	    if (file.exists())
    	        throw new JournalException("Cannot delete journal file : "+file.getAbsolutePath());
    }
    
    /**
     * Close and recycle the journal file
     */
    public File closeAndRecycle() throws JournalException
    {
    	File recycledFile = new File(file.getAbsolutePath()+RECYCLED_SUFFIX);
    	if (!file.renameTo(recycledFile))
    		throw new JournalException("Cannot rename journal file "+file.getAbsolutePath()+" to "+recycledFile.getAbsolutePath());
    	
    	try
    	{
    		fillWithZeroes(recycledFile.length(), writeBuffer.length);
    	}
        catch (IOException e)
        {
            throw new JournalException("Cannot clear journal file : "+recycledFile.getAbsolutePath(),e);
        }
        
        sync();
        close();
        
        return recycledFile;
    }
    
    public void close() throws JournalException
    {
        try
        {
            output.close();
            channel.close();
        }
        catch (IOException e)
        {
        	log.error("["+baseName+"] Cannot close journal file : "+file.getAbsolutePath(),e);
            throw new JournalException("Cannot close journal file",e);
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString()
    {
        return file != null ? file.getAbsolutePath() : "?";
    }
}
