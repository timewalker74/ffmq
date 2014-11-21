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
package net.timewalker.ffmq4.management.destination;

import java.io.File;

import javax.jms.JMSException;

import net.timewalker.ffmq4.management.InvalidDescriptorException;
import net.timewalker.ffmq4.management.destination.definition.AbstractDestinationDefinition;
import net.timewalker.ffmq4.storage.StorageSyncMethod;
import net.timewalker.ffmq4.utils.Settings;
import net.timewalker.ffmq4.utils.StringTools;
import net.timewalker.ffmq4.utils.SystemTools;
import net.timewalker.ffmq4.utils.descriptor.AbstractSettingsBasedDescriptor;

/**
 * <p>Base implementation for a destination descriptor.</p>
 */
public abstract class AbstractDestinationDescriptor extends AbstractSettingsBasedDescriptor implements DestinationDescriptorMBean
{
    // Attributes
    protected String name;    
    protected int initialBlockCount;
    protected int maxBlockCount;
    protected int autoExtendAmount;
    protected int blockSize = 4096;
    protected String rawDataFolder;
    protected File dataFolder;
    protected int maxNonPersistentMessages;
    protected boolean useJournal;
    protected String rawJournalFolder;
    protected File journalFolder;
    protected long maxJournalSize = 1024*1024*32 /* 32 MB */;
    protected int maxWriteBatchSize = 1000;
    protected int maxUnflushedJournalSize = 1024*1024*4 /* 4 MB */;
    protected int maxUncommittedStoreSize = 1024*1024*16 /* 16 MB */;
    protected int journalOutputBuffer = 16384 /* 16 KB */;
    protected boolean temporary;
    protected int storageSyncMethod = StorageSyncMethod.CHANNEL_FORCE_NO_META;
    protected boolean preAllocateFiles;
    protected boolean overflowToPersistent;
    
    /**
     * Constructor
     */
    public AbstractDestinationDescriptor()
    {
        super();
    }
    
    /**
     * Constructor
     */
    public AbstractDestinationDescriptor( Settings settings )
    {
        super(settings);
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.management.Descriptor#initFromSettings(net.timewalker.ffmq4.utils.Settings)
     */
    @Override
	protected void initFromSettings( Settings settings )
    {
        this.name = settings.getStringProperty("name");
        this.initialBlockCount = settings.getIntProperty("persistentStore.initialBlockCount",0);
        this.maxBlockCount = settings.getIntProperty("persistentStore.maxBlockCount",0);
        this.autoExtendAmount = settings.getIntProperty("persistentStore.autoExtendAmount",0);
        this.blockSize = settings.getIntProperty("persistentStore.blockSize",4096);
        this.rawDataFolder = settings.getStringProperty("persistentStore.dataFolder","${FFMQ_BASE}/data",false);
        this.dataFolder = new File(SystemTools.replaceSystemProperties(rawDataFolder));
        this.maxNonPersistentMessages = settings.getIntProperty("memoryStore.maxMessages",0);
        this.useJournal = settings.getBooleanProperty("persistentStore.useJournal",true);
        this.rawJournalFolder = settings.getStringProperty("persistentStore.journal.dataFolder",rawDataFolder,false);
        this.journalFolder = new File(SystemTools.replaceSystemProperties(rawJournalFolder));
        this.maxJournalSize = settings.getLongProperty("persistentStore.journal.maxFileSize", 1024*1024*32 /* 32 MB */);
        this.maxWriteBatchSize = settings.getIntProperty("persistentStore.journal.maxWriteBatchSize", 1000);
        this.maxUnflushedJournalSize = settings.getIntProperty("persistentStore.journal.maxUnflushedJournalSize", 1024*1024*4 /* 4 MB */);
        this.maxUncommittedStoreSize = settings.getIntProperty("persistentStore.journal.maxUncommittedStoreSize", 1024*1024*16 /* 16 MB */);
        this.journalOutputBuffer = settings.getIntProperty("persistentStore.journal.outputBufferSize", 16384 /* 16 KB */);
        this.temporary = settings.getBooleanProperty("temporary",false);
        this.storageSyncMethod = settings.getIntProperty("persistentStore.syncMethod", StorageSyncMethod.CHANNEL_FORCE_NO_META);
        this.preAllocateFiles = settings.getBooleanProperty("persistentStore.journal.preAllocateFiles",false);
        this.overflowToPersistent = settings.getBooleanProperty("memoryStore.overflowToPersistent",false);
    }
    
    protected void copyAttributesTo( AbstractDestinationDefinition target )
    {
        // Do not copy 'name' and 'temporary' attributes
        target.rawDataFolder = rawDataFolder;
        target.dataFolder = dataFolder;
        target.initialBlockCount = initialBlockCount;
        target.maxBlockCount = maxBlockCount;
        target.autoExtendAmount = autoExtendAmount;
        target.blockSize = blockSize;
        target.maxNonPersistentMessages = maxNonPersistentMessages;
        target.useJournal = useJournal;
        target.rawJournalFolder = rawJournalFolder;
        target.journalFolder = journalFolder;
        target.maxJournalSize = maxJournalSize;
        target.maxWriteBatchSize = maxWriteBatchSize;
        target.maxUnflushedJournalSize = maxUnflushedJournalSize;
        target.maxUncommittedStoreSize = maxUncommittedStoreSize;
        target.journalOutputBuffer = journalOutputBuffer;
        target.storageSyncMethod = storageSyncMethod;
        target.preAllocateFiles = preAllocateFiles;
        target.overflowToPersistent = overflowToPersistent;
    }
    
    /**
     * Serialize the definition to settings
     */
    public final Settings asSettings()
    {
        Settings settings = new Settings();
        fillSettings(settings);
        return settings;
    }

    /**
     * Append nodes to the XML definition
     */
    protected void fillSettings( Settings settings )
    {
        settings.setStringProperty("name", name);
        settings.setIntProperty("persistentStore.initialBlockCount", initialBlockCount);
        settings.setIntProperty("persistentStore.maxBlockCount", maxBlockCount);
        settings.setIntProperty("persistentStore.autoExtendAmount", autoExtendAmount);
        settings.setIntProperty("persistentStore.blockSize", blockSize);
        if (rawDataFolder != null)
        	settings.setStringProperty("persistentStore.dataFolder", rawDataFolder);
        settings.setIntProperty("memoryStore.maxMessages", maxNonPersistentMessages);
        settings.setBooleanProperty("persistentStore.useJournal", useJournal);
        if (rawJournalFolder != null)
        	settings.setStringProperty("persistentStore.journal.dataFolder", rawJournalFolder);
        settings.setLongProperty("persistentStore.journal.maxFileSize", maxJournalSize);
        settings.setIntProperty("persistentStore.journal.maxWriteBatchSize", maxWriteBatchSize);
        settings.setIntProperty("persistentStore.journal.maxUnflushedJournalSize", maxUnflushedJournalSize);
        settings.setIntProperty("persistentStore.journal.maxUncommittedStoreSize", maxUncommittedStoreSize);
        settings.setIntProperty("persistentStore.journal.outputBufferSize", journalOutputBuffer);
        settings.setBooleanProperty("persistentStore.journal.preAllocateFiles", preAllocateFiles);
        settings.setIntProperty("persistentStore.syncMethod", storageSyncMethod);
        settings.setBooleanProperty("temporary", temporary);
        settings.setBooleanProperty("memoryStore.overflowToPersistent", overflowToPersistent);
    }
    
    @Override
	public String getName()
    {
        return name;
    }

    @Override
	public boolean isTemporary()
    {
        return temporary;
    }

    public File getDataFolder()
    {
        return dataFolder;
    }

    @Override
	public int getMaxNonPersistentMessages()
    {
        return maxNonPersistentMessages;
    }

    /**
	 * @return useJournal
	 */
	@Override
	public boolean isUseJournal()
	{
		return useJournal;
	}
	
	@Override
	public int getInitialBlockCount()
    {
        return initialBlockCount;
    }

    @Override
	public int getBlockSize()
    {
        return blockSize;
    }

    /**
     * @param initialBlockCount the initial blockCount to set
     */
    public void setInitialBlockCount(int initialBlockCount)
    {
        this.initialBlockCount = initialBlockCount;
    }
    
    /**
     * @param maxBlockCount the maxBlockCount to set
     */
    public void setMaxBlockCount(int maxBlockCount)
    {
        this.maxBlockCount = maxBlockCount;
    }
    
    /**
     * @return the maxBlockCount
     */
    @Override
	public int getMaxBlockCount()
    {
        return maxBlockCount;
    }
    
    /**
     * @param autoExtendAmount the autoExtendAmount to set
     */
    public void setAutoExtendAmount(int autoExtendAmount)
    {
        this.autoExtendAmount = autoExtendAmount;
    }
    
    /**
     * @return the autoExtendAmount
     */
    @Override
	public int getAutoExtendAmount()
    {
        return autoExtendAmount;
    }
    
    /**
     * @param blockSize the blockSize to set
     */
    public void setBlockSize(int blockSize)
    {
        this.blockSize = blockSize;
    }
    
    /**
     * @return the rawDataFolder
     */
    public String getRawDataFolder()
    {
        return rawDataFolder;
    }
    
    /**
     * @param rawDataFolder the rawDataFolder to set
     */
    public void setRawDataFolder(String rawDataFolder)
    {
        this.rawDataFolder = rawDataFolder;
    }
    
    /**
     * @param dataFolder the dataFolder to set
     */
    public void setDataFolder(File dataFolder)
    {
        this.dataFolder = dataFolder;
        
        // Update raw folder path too
        if (dataFolder != null)
        	rawDataFolder = dataFolder.getAbsolutePath();
        else
        	rawDataFolder = null;
    }
    
    /**
     * @param useJournal the useJournal to set
     */
    public void setUseJournal(boolean useJournal)
    {
        this.useJournal = useJournal;
    }
    
    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * @param maxNonPersistentMessages the maxNonPersistentMessages to set
     */
    public void setMaxNonPersistentMessages(int maxNonPersistentMessages)
    {
        this.maxNonPersistentMessages = maxNonPersistentMessages;
    }
    
    /**
     * @param temporary the temporary to set
     */
    public void setTemporary(boolean temporary)
    {
        this.temporary = temporary;
    }
    
    /**
     * @return the rawJournalFolder
     */
    public String getRawJournalFolder()
    {
        return rawJournalFolder;
    }

    /**
     * @param rawJournalFolder the rawJournalFolder to set
     */
    public void setRawJournalFolder(String rawJournalFolder)
    {
        this.rawJournalFolder = rawJournalFolder;
    }

    /**
     * @return the journalFolder
     */
    public File getJournalFolder()
    {
        return journalFolder;
    }

    /**
     * @param journalFolder the journalFolder to set
     */
    public void setJournalFolder(File journalFolder)
    {
        this.journalFolder = journalFolder;
        
        // Update raw folder path too
        if (journalFolder != null)
        	rawJournalFolder = journalFolder.getAbsolutePath();
        else
        	rawJournalFolder = null;
    }

    /**
     * @return the maxJournalSize
     */
    @Override
	public long getMaxJournalSize()
    {
        return maxJournalSize;
    }

    /**
     * @param maxJournalSize the maxJournalSize to set
     */
    public void setMaxJournalSize(long maxJournalSize)
    {
        this.maxJournalSize = maxJournalSize;
    }

    /**
     * @return the maxWriteBatchSize
     */
    @Override
	public int getMaxWriteBatchSize()
    {
        return maxWriteBatchSize;
    }

    /**
     * @param maxWriteBatchSize the maxWriteBatchSize to set
     */
    public void setMaxWriteBatchSize(int maxWriteBatchSize)
    {
        this.maxWriteBatchSize = maxWriteBatchSize;
    }

    /**
     * @return the maxUnflushedJournalSize
     */
    @Override
	public int getMaxUnflushedJournalSize()
    {
        return maxUnflushedJournalSize;
    }

    /**
     * @param maxUnflushedJournalSize the maxUnflushedJournalSize to set
     */
    public void setMaxUnflushedJournalSize(int maxUnflushedJournalSize)
    {
        this.maxUnflushedJournalSize = maxUnflushedJournalSize;
    }

    /**
     * @return the maxUncommittedStoreSize
     */
    @Override
	public int getMaxUncommittedStoreSize()
    {
        return maxUncommittedStoreSize;
    }

    /**
     * @param maxUncommittedStoreSize the maxUncommittedStoreSize to set
     */
    public void setMaxUncommittedStoreSize(int maxUncommittedStoreSize)
    {
        this.maxUncommittedStoreSize = maxUncommittedStoreSize;
    }

    /**
     * @return the journalOutputBuffer
     */
    @Override
	public int getJournalOutputBuffer()
    {
        return journalOutputBuffer;
    }

    /**
     * @param journalOutputBuffer the journalOutputBuffer to set
     */
    public void setJournalOutputBuffer(int journalOutputBuffer)
    {
        this.journalOutputBuffer = journalOutputBuffer;
    }

    /**
	 * @return the storageSyncMethod
	 */
	@Override
	public int getStorageSyncMethod()
	{
		return storageSyncMethod;
	}
	
	/**
	 * @param storageSyncMethod the syncMethod to set
	 */
	public void setStorageSyncMethod(int storageSyncMethod)
	{
		this.storageSyncMethod = storageSyncMethod;
	}
	
	/**
	 * @return the preAllocateFiles
	 */
	@Override
	public boolean isPreAllocateFiles()
	{
		return preAllocateFiles;
	}
	
	/**
	 * @param preAllocateFiles the preAllocateFiles to set
	 */
	public void setPreAllocateFiles(boolean preAllocateFiles)
	{
		this.preAllocateFiles = preAllocateFiles;
	}
    
	/**
	 * @return the overflowToPersistent
	 */
	public boolean isOverflowToPersistent()
	{
		return overflowToPersistent;
	}
	
	/**
	 * @param overflowToPersistent the overflowToPersistent to set
	 */
	public void setOverflowToPersistent(boolean overflowToPersistent)
	{
		this.overflowToPersistent = overflowToPersistent;
	}
	
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.utils.Checkable#check()
     */
    @Override
	public void check() throws JMSException
    {
        if (StringTools.isEmpty(name))
            throw new InvalidDescriptorException("Missing descriptor property : name");

        checkMinValue(maxNonPersistentMessages, 0, "maximum non persistent messages");
        checkMinValue(initialBlockCount,        0, "initial block count");
        checkMinValue(maxBlockCount,            0, "maximum block count");

        if (maxBlockCount < initialBlockCount)
            throw new InvalidDescriptorException("Maximum block count should be greater or equal than initial block count");
        
        if (maxBlockCount > 0)
        {
            if (rawDataFolder == null || StringTools.isEmpty(rawDataFolder))
                throw new InvalidDescriptorException("Missing destination raw data folder");
            if (dataFolder == null || StringTools.isEmpty(dataFolder.getName()))
                throw new InvalidDescriptorException("Missing destination data folder");
            if (!dataFolder.isDirectory())
            	throw new InvalidDescriptorException("Invalid data folder : "+dataFolder.getAbsolutePath());
            
            checkMinValue(blockSize,1024,"block size");
            if (initialBlockCount != maxBlockCount)
                checkMinValue(autoExtendAmount,1,"auto extend amount");

            if (useJournal)
            {
            	if (!journalFolder.isDirectory())
                	throw new InvalidDescriptorException("Invalid journal folder : "+journalFolder.getAbsolutePath());
            	
                checkMinValue(maxJournalSize,1024,"maximum journal size");
                checkMinValue(maxWriteBatchSize,1,"maximum write batch size");
                checkMinValue(journalOutputBuffer,1024,"journal output buffer size");
                checkMinValue(maxUnflushedJournalSize,1024,"maximum unflushed journal size");
                checkMinValue(maxUncommittedStoreSize,1024,"maximum uncommitted store size");
            }
        }

        if (initialBlockCount == 0 && maxNonPersistentMessages == 0)
            throw new InvalidDescriptorException("Destination cannot store any message !");
        
        if (!StorageSyncMethod.isValid(storageSyncMethod))
        	throw new InvalidDescriptorException("Invalid storage sync method : "+storageSyncMethod);
    }
    
    private void checkMinValue( long value , long min , String name ) throws InvalidDescriptorException
    {
        if (value < min)
            throw new InvalidDescriptorException("Missing or invalid value : "+name+" ("+value+"), should be >= "+min);
    }
    
    public boolean hasPersistentStore()
    {
    	return maxBlockCount > 0;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString()
    {
    	StringBuilder sb = new StringBuilder();
        
        sb.append("[");
        sb.append(name);
        sb.append("] initialBlockCount=");
        sb.append(initialBlockCount);
        sb.append(" maxBlockCount=");
        sb.append(maxBlockCount);
        sb.append(" autoExtendAmount=");
        sb.append(autoExtendAmount);
        sb.append(" blockSize=");
        sb.append(blockSize);
        sb.append(" dataFolder=");
        sb.append(rawDataFolder);
        sb.append(" maxNonPersistentMessages=");
        sb.append(maxNonPersistentMessages);
        sb.append(" useJournal=");
        sb.append(useJournal);
        sb.append(" syncMethod=");
        sb.append(storageSyncMethod);
        sb.append(" preAllocationFiles=");
        sb.append(preAllocateFiles);
        sb.append(" overflowToPersistent=");
        sb.append(overflowToPersistent);
        sb.append(" temporary=");
        sb.append(temporary);
        
        return sb.toString();
    }
}
