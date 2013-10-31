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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JournalRecovery
 */
public final class JournalRecovery
{
	private static final Log log = LogFactory.getLog(JournalRecovery.class);
	
	// Attributes
	private String baseName;
	private File[] journalFiles;
	private RandomAccessFile allocationTableRandomAccessFile;
	private RandomAccessFile dataRandomAccessFile ;
    
	/**
	 * Constructor
	 */
	public JournalRecovery( String baseName ,
			                File[] journalFiles ,
			                RandomAccessFile allocationTableRandomAccessFile ,
                            RandomAccessFile dataRandomAccessFile )
	{
		this.baseName = baseName;
		this.journalFiles = journalFiles;
		this.allocationTableRandomAccessFile = allocationTableRandomAccessFile;
		this.dataRandomAccessFile = dataRandomAccessFile;
	}
	
	/**
	 * Start the recovery process
	 * @return the new block count of the store, or -1 if unchanged
	 * @throws JournalException
	 */
	public int recover() throws JournalException
	{
	    int newBlockCount = -1;
	    
		log.warn("["+baseName+"] Recovery required for data store : found "+journalFiles.length+" journal file(s)");
		for (int i = 0; i < journalFiles.length; i++)
		    newBlockCount = recoverFromJournalFile(journalFiles[i]);
		
		return newBlockCount;
	}
	
	private int recoverFromJournalFile( File journalFile ) throws JournalException
	{
		log.debug("["+baseName+"] Processing "+journalFile.getAbsolutePath());
		
		DataInputStream in;
		try
		{
			// Create a buffered data input stream from file
			in = new DataInputStream(new BufferedInputStream(new FileInputStream(journalFile)));
		}
		catch (IOException e)
		{
			throw new JournalException("Cannot open journal file : "+journalFile.getAbsolutePath(),e);
		}
		
		int replayedOperations = 0;
		int replayedTransactions = 0;
		long currentTransactionId = -1;
		int newBlockCount = -1;
		LinkedList transactionQueue = new LinkedList();
		try
		{
			AbstractJournalOperation op;
			while ((op = readJournalOperation(in)) != null)
			{				
				// Check transaction id
				if (currentTransactionId == -1)
					currentTransactionId = op.getTransactionId();
				else
					if (currentTransactionId != op.getTransactionId())
						throw new IllegalStateException("Transaction id inconsistency : "+currentTransactionId+" -> "+op.getTransactionId());
				
				if (op instanceof CommitOperation)
				{
					// Check transaction size
					int opCount = ((CommitOperation)op).getOperationsCount();
					if (transactionQueue.size() != opCount)
					{
						throw new IllegalStateException("Transaction size mismatch (expected "+opCount+", got "+transactionQueue.size()+")");
					}
					else
					{
						// Everything looks fine, proceed ...
						log.trace("["+baseName+"] Replaying transaction #"+currentTransactionId+" ("+transactionQueue.size()+" operation(s))");
						replayedOperations += transactionQueue.size();
						replayedTransactions++;
						newBlockCount = applyOperations(transactionQueue);
						currentTransactionId = -1;
					}
				}
				else
					transactionQueue.addLast(op);
			}
			
			if (transactionQueue.size() > 0)
			{
				op = (AbstractJournalOperation)transactionQueue.removeFirst();
				log.warn("["+baseName+"] Dropping incomplete transaction : #"+op.getTransactionId());
			}
			
			syncStore();
			
			log.warn("["+baseName+"] Recovery complete. (Replayed "+replayedTransactions+" transaction(s) and "+replayedOperations+" operation(s))");
		}
		finally
		{
			try
			{
				in.close();
			}
			catch (IOException e)
			{
				throw new JournalException("Cannot close journal file : "+journalFile.getAbsolutePath(),e);
			}
		}
		
		return newBlockCount;
	}
	
	private int applyOperations( LinkedList transactionQueue ) throws JournalException
	{
	    int newBlockCount = -1;
		while (transactionQueue.size() > 0)
		{
			AbstractJournalOperation op = (AbstractJournalOperation)transactionQueue.removeFirst();
			
			if (op instanceof MetaDataWriteOperation)
			{
				((MetaDataWriteOperation)op).writeTo(allocationTableRandomAccessFile);
			}
			else
			if (op instanceof MetaDataBlockWriteOperation)
			{
				((MetaDataBlockWriteOperation)op).writeTo(allocationTableRandomAccessFile);
			}
			else
			if (op instanceof DataBlockWriteOperation)
			{
				((DataBlockWriteOperation)op).writeTo(dataRandomAccessFile);
			}
			else
            if (op instanceof StoreExtendOperation)
            {
                newBlockCount = ((StoreExtendOperation)op).extend(allocationTableRandomAccessFile,dataRandomAccessFile);
            }
			else
				throw new IllegalArgumentException("Unexpected journal operation : "+op);
		}
		
		return newBlockCount;
	}
	
	private void syncStore() throws JournalException
    {
    	try
    	{
	    	allocationTableRandomAccessFile.getFD().sync();
    	}
    	catch (IOException e)
        {
    		log.error("["+baseName+"] Could not sync store allocation table file",e);
            throw new JournalException("Could not sync store allocation table file");
        }
    	try
    	{
	    	dataRandomAccessFile.getFD().sync();
    	}
    	catch (IOException e)
        {
    		log.error("["+baseName+"] Could not sync store data file",e);
    		throw new JournalException("Could not sync store data file");
        }
    }
	
	//-------------------------------------------------------------------------
	
	public static AbstractJournalOperation readJournalOperation( DataInputStream in )
	{
		try
		{
			int operationType = in.read();
			if (operationType == -1)
				return null; // EOF
	
			switch (operationType)
			{
				case 0 : // Padding
					return null;
				
				case AbstractJournalOperation.TYPE_META_DATA_WRITE :
					return readMetaDataWriteOperation(in);
					
				case AbstractJournalOperation.TYPE_META_DATA_BLOCK_WRITE :
					return readMetaDataBlockWriteOperation(in);
					
				case AbstractJournalOperation.TYPE_DATA_BLOCK_WRITE :
					return readDataBlockWriteOperation(in);
					
				case AbstractJournalOperation.TYPE_STORE_EXTEND :
                    return readStoreExtendOperation(in);
					
				case AbstractJournalOperation.TYPE_COMMIT :
					return readCommitOperation(in);
					
				default:
					throw new IllegalArgumentException("Invalid operation type : "+operationType);
			}
		}
		catch (Exception e)
		{
			log.error("Corrupted or truncated journal operation, skipping.",e);
			return null;
		}
	}
	
	private static MetaDataWriteOperation readMetaDataWriteOperation( DataInputStream in ) throws IOException
	{
		long transactionId = in.readLong();
		long metaDataOffset = in.readLong();
		int metaData = in.readInt();
		
		return new MetaDataWriteOperation(transactionId, metaDataOffset, metaData);
	}
	
	private static MetaDataBlockWriteOperation readMetaDataBlockWriteOperation( DataInputStream in ) throws IOException
	{
		long transactionId = in.readLong();
		long metaDataOffset = in.readLong();
		int len = in.readInt();
		byte[] metaData = new byte[len];
		in.readFully(metaData);
		
		return new MetaDataBlockWriteOperation(transactionId, metaDataOffset, metaData);
	}
	
	private static DataBlockWriteOperation readDataBlockWriteOperation( DataInputStream in ) throws IOException
	{
		long transactionId = in.readLong();
		long blockOffset = in.readLong();
		int len = in.readInt();
		byte[] dataBlock = new byte[len];
		in.readFully(dataBlock);
		
		return new DataBlockWriteOperation(transactionId, -1, blockOffset, dataBlock);
	}

	private static StoreExtendOperation readStoreExtendOperation( DataInputStream in ) throws IOException
    {
        long transactionId = in.readLong();
        int blockSize = in.readInt();
        int oldBlockCount = in.readInt();
        int newBlockCount = in.readInt();
        
        return new StoreExtendOperation(transactionId, blockSize, oldBlockCount, newBlockCount);
    }
	
	private static CommitOperation readCommitOperation( DataInputStream in ) throws IOException
	{
		long transactionId = in.readLong();
		int operationsCount = in.readInt();
		
		return new CommitOperation(transactionId, operationsCount, null);
	}
}
