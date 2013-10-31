package net.timewalker.ffmq3.transport.tcp.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * TcpBufferedOutputStream
 */
public final class TcpBufferedOutputStream extends FilterOutputStream
{
    private byte[] buffer;
    private int capacity;
    private int size;
    
    /**
     * Constrauctor
     */
    public TcpBufferedOutputStream(OutputStream out,int capacity)
    {
        super(out);
        this.buffer = new byte[capacity];
        this.capacity = capacity;
    }
    
    /*
     * (non-Javadoc)
     * @see java.io.FilterOutputStream#write(int)
     */
    @Override
	public void write(int b) throws IOException
    {
        if ((capacity - size) < 1)
            flush();
        buffer[size++] = (byte)b;
    }
    
    /*
     * (non-Javadoc)
     * @see java.io.FilterOutputStream#write(byte[], int, int)
     */
    @Override
	public void write(byte data[], int offset, int len) throws IOException
    {
        if ((capacity - size) < len)
            flush();
        
        // Do we have enough space ?
        if (capacity >= len)
        {
            System.arraycopy(data, offset, buffer, size, len);
            size += len;
        }
        else
        {
            // Too big, write it directly ...
            out.write(data, offset, len);
        }
    }
 
    /*
     * (non-Javadoc)
     * @see java.io.FilterOutputStream#flush()
     */
    @Override
	public void flush() throws IOException
    {
        if (size > 0)
        {
            // Flush everything we have ...
            out.write(buffer,0,size);
            size = 0;
        }
    }
}
