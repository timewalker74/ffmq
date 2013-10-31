package net.timewalker.ffmq4.transport.tcp.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * TcpBufferedInputStream
 */
public final class TcpBufferedInputStream extends FilterInputStream
{
    private byte[] buffer;
    private int capacity;
    private int size;
    private int pos;
    
    /**
     * Constructor
     */
    public TcpBufferedInputStream( InputStream in , int capacity )
    {
        super(in);
        this.buffer = new byte[capacity];
        this.capacity = capacity;
    }
    
    private void readMore() throws IOException
    {
        size = 0;
        pos = 0;
        int amount = in.read(buffer,0,capacity);
        if (amount > 0)
            size += amount;
    }
    
    /*
     * (non-Javadoc)
     * @see java.io.FilterInputStream#read()
     */
    @Override
	public int read() throws IOException
    {
        if (pos >= size)
        {
            // try to get more ...
            readMore();
            if (pos >= size)
                return -1; // Still nothing
        }
        return buffer[pos++] & 0xFF;
    }
    
    /*
     * (non-Javadoc)
     * @see java.io.FilterInputStream#read(byte[], int, int)
     */
    @Override
	public int read(byte[] b, int off, int len) throws IOException
    {
        if (len == 0)
            return 0;
        
        int totalRead = 0;
        do
        {
            int amount = readBufferedOrAvailable(b,off+totalRead,len-totalRead);
            if (amount <= 0)
                return (totalRead == 0) ? amount : totalRead;
            totalRead += amount;
            
            // Did we get enough ?
            if (totalRead >= len)
                break;
        }
        while (in.available() > 0);
        
        return totalRead;
    }
    
    private int readBufferedOrAvailable(byte[] b, int off, int len) throws IOException
    {
        int maxAmount = size - pos;
        if (maxAmount <= 0)
        {
            // If it's too big for us, read it directly ...
            if (len >= capacity)
                return in.read(b,off,len);
            
            readMore();
            maxAmount = size - pos;
            if (maxAmount <= 0)
                return -1; // Nothing available ...
        }
        
        int amount = (maxAmount > len) ? len : maxAmount;
        System.arraycopy(buffer, pos, b, off, amount);
        pos += amount;
        
        return amount;
    }
    
    /*
     * (non-Javadoc)
     * @see java.io.FilterInputStream#skip(long)
     */
    @Override
	public long skip(long n) throws IOException
    {
        if (n<=0)
            return 0;
        
        // How much can we skip ?
        long skippable = size - pos;
        if (skippable <= 0)
            return super.skip(n);
        
        long skipped = (skippable >= n) ? n : skippable;
        pos += skipped;
        return skipped;
    }
    
    /*
     * (non-Javadoc)
     * @see java.io.FilterInputStream#available()
     */
    @Override
	public int available() throws IOException
    {
        return super.available()+(size-pos);
    }

    /*
     * (non-Javadoc)
     * @see java.io.FilterInputStream#markSupported()
     */
    @Override
	public boolean markSupported()
    {
        return false;
    }
}
