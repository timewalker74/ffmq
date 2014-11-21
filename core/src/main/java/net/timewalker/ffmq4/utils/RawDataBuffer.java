package net.timewalker.ffmq4.utils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * <p>A <b>RawDataBuffer</b> is a dynamically growing byte array.</p>
 * <p>
 * It implements {@link java.io.DataOutput} and {@link java.io.DataInput} 
 * to write or read data plus additional null-safe versions of the methods. 
 * </p>
 * <p>
 * This class does not implement any synchronization nor bound checking
 * for maximum performance.<br>
 * Note that the UTF read/write methods are not compatible with the JDK implementation
 * because they were modified to take advantage of the RawDataBuffer layout.
 * </p>
 */
public final class RawDataBuffer implements DataOutput, DataInput
{
    // --- Internal constants ------------------------------
    private static final byte NULL_VALUE = 0;
    private static final byte NOT_NULL_VALUE = 1;

    private static final byte TYPE_BOOLEAN   = 10;
    private static final byte TYPE_BYTE      = 11;
    private static final byte TYPE_SHORT     = 12;
    private static final byte TYPE_INT       = 13;
    private static final byte TYPE_LONG      = 14;
    private static final byte TYPE_FLOAT     = 15;
    private static final byte TYPE_DOUBLE    = 16;
    private static final byte TYPE_STRING    = 17;
    private static final byte TYPE_BYTEARRAY = 18;
    private static final byte TYPE_CHARACTER = 19;
    
    private static final byte UTF_TYPE_1 = 1;
    private static final byte UTF_TYPE_2 = 2;
    private static final byte UTF_TYPE_4 = 4;
    //------------------------------------------------------
    
    // Runtime
    private byte[] buf;   // Underlying byte array buffer
    private int capacity; // Buffer capacity
    private int size;     // Actual used size
    private int pos;      // Current read cursor
    
    /**
     * Constructor
     * @param initialCapacity initial buffer capacity
     */
    public RawDataBuffer( int initialCapacity )
    {
        this.buf = new byte[initialCapacity];
        this.capacity = initialCapacity;
    }
    
    /**
     * Constructor
     * @param initialBuffer the initial buffer to use internally
     */
    public RawDataBuffer( byte[] initialBuffer )
    {
        this.buf = initialBuffer;
        this.size = this.capacity = initialBuffer.length;
    }
    
    /**
     * Write a string or null value to the stream
     */
    public void writeNullableUTF(String str)
    {
        if (str == null)
            write(NULL_VALUE);
        else
        {
            write(NOT_NULL_VALUE);
            writeUTF(str);
        }
    }
    
    /**
     * Ensure that the buffer internal capacity is at least targetCapacity
     * @param targetCapacity the expected capacity
     */
    public final void ensureCapacity( int targetCapacity )
    {
        if (targetCapacity > capacity)
        {
            int newLength = Math.max(capacity << 1, targetCapacity);
            byte[] copy = new byte[newLength];
            System.arraycopy(buf, 0, copy, 0, capacity);
            buf = copy;
            capacity = newLength;
        }
    }
    
    /**
     * Set the buffer size (buffer is enlarged as necessary and padded with zeroes)
     * @param size
     */
    public void setSize( int size )
    {
        ensureCapacity(size);
        this.size = size;
    }
    
    /*
     * (non-Javadoc)
     * @see java.io.DataOutput#write(int)
     */
    @Override
	public void write(int b)
    {
        ensureCapacity(size+1);
        buf[size++] = (byte)b;
    }
    
    /*
     * (non-Javadoc)
     * @see java.io.DataOutput#write(byte[])
     */
    @Override
	public void write(byte[] data)
    {
        int newCount = size+data.length; 
        ensureCapacity(newCount);
        System.arraycopy(data, 0, buf, size, data.length);
        size = newCount;
    }
    
    /* (non-Javadoc)
     * @see java.io.DataOutput#writeBytes(java.lang.String)
     */
    @Override
	public void writeBytes(String s)
    {
    	int len = s.length();
    	ensureCapacity(size+len);
    	for (int i = 0 ; i < len ; i++)
    	    buf[pos++] = (byte)s.charAt(i);
    }
    
	/* (non-Javadoc)
	 * @see java.io.DataOutput#writeChars(java.lang.String)
	 */
	@Override
	public void writeChars(String s)
	{
		int len = s.length();
		ensureCapacity(size+len*2);
        for (int i = 0 ; i < len ; i++) {
            int v = s.charAt(i);
            buf[pos++] = (byte)((v >>> 8) & 0xFF); 
            buf[pos++] = (byte)((v >>> 0) & 0xFF); 
        }
	}
    
    /*
     * (non-Javadoc)
     * @see java.io.DataOutput#write(byte[], int, int)
     */
    @Override
	public void write(byte[] data,int offset,int len)
    {
        int newCount = size+len; 
        ensureCapacity(newCount);
        System.arraycopy(data, offset, buf, size, len);
        size = newCount;
    }
    
    /*
     * (non-Javadoc)
     * @see java.io.DataOutput#writeByte(int)
     */
    @Override
	public void writeByte(int b)
    {
        ensureCapacity(size+1);
        buf[size++] = (byte)b;
    }
    
    /*
     * (non-Javadoc)
     * @see java.io.DataOutput#writeBoolean(boolean)
     */
    @Override
	public void writeBoolean(boolean v)
    {
        write(v ? 1 : 0);
    }
    
    /**
     * Read a byte value at an arbitrary position in the buffer
     * @param position the target position
     */
    public byte readByte(int position)
    {
    	return buf[position];
    }
    
    /**
     * Write a byte value at an arbitrary position in the buffer
     * @param v the byte value
     * @param position the target position
     */
    public void writeByte(byte v,int position)
    {
    	buf[position] = v;
    }
    
    /*
     * (non-Javadoc)
     * @see java.io.DataOutput#writeShort(int)
     */
    @Override
	public void writeShort(int v)
    {
        ensureCapacity(size + 2);
        buf[size++] = (byte)((v >>>  8) & 0xFF);
        buf[size++] = (byte)((v >>>  0) & 0xFF);
    }
    
    /*
     * (non-Javadoc)
     * @see java.io.DataOutput#writeInt(int)
     */
    @Override
	public void writeInt(int v)
    {
        ensureCapacity(size + 4);
        buf[size++] = (byte)((v >>> 24) & 0xFF);
        buf[size++] = (byte)((v >>> 16) & 0xFF);
        buf[size++] = (byte)((v >>>  8) & 0xFF);
        buf[size++] = (byte)((v >>>  0) & 0xFF);
    }
    
    /**
     * Write a int value at an arbitrary position in the stream
     * @param v the int value
     * @param pos the target position
     */
    public void writeInt(int v,int pos)
    {
        buf[pos++] = (byte)((v >>> 24) & 0xFF);
        buf[pos++] = (byte)((v >>> 16) & 0xFF);
        buf[pos++] = (byte)((v >>>  8) & 0xFF);
        buf[pos]   = (byte)((v >>>  0) & 0xFF);
    }
    
    /*
     * (non-Javadoc)
     * @see java.io.DataOutput#writeLong(long)
     */
    @Override
	public void writeLong(long v)
    {
        ensureCapacity(size + 8);
        buf[size++] = (byte)(v >>> 56);
        buf[size++] = (byte)(v >>> 48);
        buf[size++] = (byte)(v >>> 40);
        buf[size++] = (byte)(v >>> 32);
        buf[size++] = (byte)(v >>> 24);
        buf[size++] = (byte)(v >>> 16);
        buf[size++] = (byte)(v >>>  8);
        buf[size++] = (byte)(v >>>  0);
    }
    
    /*
     * (non-Javadoc)
     * @see java.io.DataOutput#writeFloat(float)
     */
    @Override
	public void writeFloat(float v)
    {
        writeInt(Float.floatToIntBits(v));
    }
    
    /*
     * (non-Javadoc)
     * @see java.io.DataOutput#writeDouble(double)
     */
    @Override
	public void writeDouble(double v)
    {
        writeLong(Double.doubleToLongBits(v));
    }
    
    /*
     * (non-Javadoc)
     * @see java.io.DataOutput#writeChar(int)
     */
    @Override
	public void writeChar(int v)
    {
        ensureCapacity(size + 2);
        buf[size++] = (byte)((v >>>  8) & 0xFF);
        buf[size++] = (byte)((v >>>  0) & 0xFF);
    }
    
    /*
     * (non-Javadoc)
     * @see java.io.DataOutput#writeUTF(java.lang.String)
     */
    @Override
	public void writeUTF(String str)
    {
        int strlen = str.length();
        if (strlen < 255)
            writeUTF(str,UTF_TYPE_1);
        else
        if (strlen < 65535)
            writeUTF(str,UTF_TYPE_2);
        else
            writeUTF(str,UTF_TYPE_4);
    }
    
    private void writeUTF( String str , byte type )
    {       
        // We need at least type + strlen bytes (1 byte/char) + 1
        int strlen = str.length();
        int rawlen = size+type+strlen+1;
        ensureCapacity(rawlen);
        
        // Write UTF type prefix
        buf[size++] = type;
        
        // Write the string length
        switch (type)
        {
            case UTF_TYPE_1 :
                buf[size++] = (byte)((strlen >>>  0) & 0xFF);
                break;
            case UTF_TYPE_2 :
                buf[size++] = (byte)((strlen >>>  8) & 0xFF);
                buf[size++] = (byte)((strlen >>>  0) & 0xFF);
                break;
            case UTF_TYPE_4 :
                buf[size++] = (byte)((strlen >>> 24) & 0xFF);
                buf[size++] = (byte)((strlen >>> 16) & 0xFF);
                buf[size++] = (byte)((strlen >>>  8) & 0xFF);
                buf[size++] = (byte)((strlen >>>  0) & 0xFF);
                break;
            default:
            	throw new IllegalArgumentException("Invalid UTF type : "+type);   
        }

        // Write the string
        int c,i = 0;
        for (i = 0 ; i < strlen ; i++)
        {
            c = str.charAt(i);
            if (!((c >= 0x0001) && (c <= 0x007F)))
                break;
            buf[size++] = (byte)c;
        }
        // Are we done ?
        if (i == strlen)
            return;

        // Fallback to full encoding mode
        for ( ; i < strlen ; i++)
        {
            c = str.charAt(i);

            if ((c >= 0x0001) && (c <= 0x007F))
            {
                // One byte
                buf[size++] = (byte)c;
            }
            else if (c > 0x07FF)
            {
                rawlen += 3;
                ensureCapacity(rawlen);
                
                buf[size++] = (byte)(0xE0 | ((c >> 12) & 0x0F));
                buf[size++] = (byte)(0x80 | ((c >> 6) & 0x3F));
                buf[size++] = (byte)(0x80 | ((c >> 0) & 0x3F));
            }
            else
            {
                rawlen += 2;
                ensureCapacity(rawlen);
                
                buf[size++] = (byte)(0xC0 | ((c >> 6) & 0x1F));
                buf[size++] = (byte)(0x80 | ((c >> 0) & 0x3F));
            }
        }
    }
    
    /**
     * Write a nullable byte array to the stream
     */
    public void writeNullableByteArray( byte[] value )
    {
        if (value == null)
            write(NULL_VALUE);
        else
        {
            write(NOT_NULL_VALUE);
            writeInt(value.length);
            write(value);
        }
    }
    
    /**
     * Write a nullable byte array to the stream
     */
    public void writeByteArray( byte[] value )
    {
        writeInt(value.length);
        write(value);
    }
    
    /**
     * Write a generic type to the stream
     */
    public void writeGeneric( Object value )
    {
        if (value == null)
            write(NULL_VALUE);
        else
        {
            if (value instanceof String)
            {
                writeByte(TYPE_STRING);
                writeUTF((String)value);
            }
            else if (value instanceof Boolean)
            {
                writeByte(TYPE_BOOLEAN);
                writeBoolean(((Boolean)value).booleanValue());
            }
            else if (value instanceof Byte)
            {
                writeByte(TYPE_BYTE);
                writeByte(((Byte)value).byteValue());
            }
            else if (value instanceof Short)
            {
                writeByte(TYPE_SHORT);
                writeShort(((Short)value).shortValue());
            }
            else if (value instanceof Integer)
            {
                writeByte(TYPE_INT);
                writeInt(((Integer)value).intValue());
            }
            else if (value instanceof Long)
            {
                writeByte(TYPE_LONG);
                writeLong(((Long)value).longValue());
            }
            else if (value instanceof Float)
            {
                writeByte(TYPE_FLOAT);
                writeFloat(((Float)value).floatValue());
            }
            else if (value instanceof Double)
            {
                writeByte(TYPE_DOUBLE);
                writeDouble(((Double)value).doubleValue());
            }
            else if (value instanceof byte[])
            {
                writeByte(TYPE_BYTEARRAY);
                writeByteArray((byte[])value);
            }
            else if (value instanceof Character)
            {
                writeByte(TYPE_CHARACTER);
                writeChar(((Character)value).charValue());
            }
            else
                throw new IllegalArgumentException("Unsupported type : "+value.getClass().getName());
        }
    }
    
    /*
     * (non-Javadoc)
     * @see java.io.DataInput#readBoolean()
     */
    @Override
	public boolean readBoolean()
    {
        int ch = buf[pos++];
        return (ch != 0);
    }
    
    public void read(byte b[])
    {
        read(b, 0, b.length);
    }
    
    public void read(byte b[], int off, int len)
    {
        System.arraycopy(buf, pos, b, off, len);
        pos += len;
    }
    
    public byte[] readBytes( int len )
    {
    	byte[] data = new byte[len];
    	readFully(data);
    	return data;
    }
    
    /*
     * (non-Javadoc)
     * @see java.io.DataInput#readFully(byte[])
     */
    @Override
	public void readFully(byte b[])
    {
        readFully(b, 0, b.length);
    }
    
    /*
     * (non-Javadoc)
     * @see java.io.DataInput#readFully(byte[], int, int)
     */
    @Override
	public void readFully(byte b[], int off, int len)
    {
        System.arraycopy(buf, pos, b, off, len);
        pos += len;
    }
    
    /*
     * (non-Javadoc)
     * @see java.io.DataInput#readByte()
     */
    @Override
	public byte readByte()
    {
        return buf[pos++];
    }
    
    /* (non-Javadoc)
     * @see java.io.DataInput#readUnsignedByte()
     */
    @Override
	public int readUnsignedByte()
    {
    	return buf[pos++] & 0xff;
    }
    
    /* (non-Javadoc)
     * @see java.io.DataInput#readLine()
     */
    @Override
	public String readLine()
    {
    	throw new IllegalStateException("Unsupported operation (deprecated)");
    }
    
    /* (non-Javadoc)
     * @see java.io.DataInput#skipBytes(int)
     */
    @Override
	public int skipBytes(int n)
    {
    	int skippable = Math.min(n, size-pos);
    	pos += skippable;
    	return skippable;
    }
    
    /*
     * (non-Javadoc)
     * @see java.io.DataInput#readShort()
     */
    @Override
	public short readShort()
    {
        int ch1 = buf[pos++] & 0xff;
        int ch2 = buf[pos++] & 0xff;
        return (short)((ch1 << 8) + (ch2 << 0));
    }
    
    /* (non-Javadoc)
     * @see java.io.DataInput#readUnsignedShort()
     */
    @Override
	public int readUnsignedShort()
    {
    	int ch1 = buf[pos++] & 0xff;
        int ch2 = buf[pos++] & 0xff;
        return (ch1 << 8) + (ch2 << 0);
    }
    
    /*
     * (non-Javadoc)
     * @see java.io.DataInput#readInt()
     */
    @Override
	public int readInt()
    {
        int ch1 = buf[pos++] & 0xff;
        int ch2 = buf[pos++] & 0xff;
        int ch3 = buf[pos++] & 0xff;
        int ch4 = buf[pos++] & 0xff;
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }
    
    /*
     * (non-Javadoc)
     * @see java.io.DataInput#readLong()
     */
    @Override
	public long readLong()
    {
        return (((long)buf[pos++] << 56) +
                ((long)(buf[pos++] & 255) << 48) +
                ((long)(buf[pos++] & 255) << 40) +
                ((long)(buf[pos++] & 255) << 32) +
                ((long)(buf[pos++] & 255) << 24) +
                ((buf[pos++] & 255) << 16) +
                ((buf[pos++] & 255) <<  8) +
                ((buf[pos++] & 255) <<  0));
    }
    
    /*
     * (non-Javadoc)
     * @see java.io.DataInput#readFloat()
     */
    @Override
	public float readFloat()
    {
        return Float.intBitsToFloat(readInt());
    }
    
    /*
     * (non-Javadoc)
     * @see java.io.DataInput#readDouble()
     */
    @Override
	public double readDouble()
    {
        return Double.longBitsToDouble(readLong());
    }
    
    /*
     * (non-Javadoc)
     * @see java.io.DataInput#readChar()
     */
    @Override
	public char readChar()
    {
        int ch1 = buf[pos++] & 0xFF;
        int ch2 = buf[pos++] & 0xFF;
        return (char)((ch1 << 8) + (ch2 << 0));
    }
    
    /*
     * (non-Javadoc)
     * @see java.io.DataInput#readUTF()
     */
    @Override
	public String readUTF()
    {
        int type = buf[pos++] & 0xff; // Read UTF type prefix
        
        // Read UTF length
        int strlen;
        switch (type)
        {
            case UTF_TYPE_1 :
                strlen = buf[pos++] & 0xff;
                break;
            case UTF_TYPE_2 :
                int ch1 = buf[pos++] & 0xff;
                int ch2 = buf[pos++] & 0xff;
                strlen = (ch1 << 8) + (ch2 << 0);
                break;
            case UTF_TYPE_4 :
                ch1 = buf[pos++] & 0xff;
                ch2 = buf[pos++] & 0xff;
                int ch3 = buf[pos++] & 0xff;
                int ch4 = buf[pos++] & 0xff;
                strlen = ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
                break;
               default:
                   throw new IllegalArgumentException("Invalid UTF type : "+type);
        }
        
        char[] chars = new char[strlen];

        int c,char2,char3;
        int count = 0;
        while (count < strlen) {
            c = buf[pos] & 0xff;      
            if (c > 127) break;
            pos++;
            chars[count++]=(char)c;
        }

        while (count < strlen) {
            c = buf[pos++] & 0xff;
            switch (c >> 4) {
                case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                    /* 0xxxxxxx*/
                    chars[count++]=(char)c;
                    break;
                case 12: case 13:
                    /* 110x xxxx   10xx xxxx*/
                    char2 = buf[pos++];
                    chars[count++]=(char)(((c & 0x1F) << 6) | (char2 & 0x3F));  
                    break;
                case 14:
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
                    char2 = buf[pos++];
                    char3 = buf[pos++];
                    chars[count++]=(char)(((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
                    break;
                default:
                    /* 10xx xxxx,  1111 xxxx */
                    throw new IllegalStateException("Malformed input around byte " + count);
            }
        }

        return new String(chars);
    }
    
    /**
     * Read a string or null value from the stream
     */
    public String readNullableUTF()
    {
        byte isNull = readByte();
        if (isNull == NULL_VALUE)
            return null;
        
        return readUTF();
    }
    
    /**
     * Read a nullable byte array from the stream
     */
    public byte[] readNullableByteArray()
    {
        byte isNull = readByte();
        if (isNull == NULL_VALUE)
            return null;
        
        int arraySize = readInt();
        byte[] value = new byte[arraySize];
        readFully(value);
        return value;
    }
    
    /**
     * Read a nullable byte array from the stream
     */
    public byte[] readByteArray()
    {
        int arraySize = readInt();
        byte[] value = new byte[arraySize];
        readFully(value);
        return value;
    }
    
    /**
     * Read a generic type from the stream
     */
    public Object readGeneric()
    {
        byte type = readByte();
        switch (type)
        {
            case NULL_VALUE     : return null;
            case TYPE_STRING    : return readUTF();
            case TYPE_BOOLEAN   : return Boolean.valueOf(readBoolean());
            case TYPE_BYTE      : return Byte.valueOf(readByte());
            case TYPE_SHORT     : return Short.valueOf(readShort());
            case TYPE_INT       : return Integer.valueOf(readInt());
            case TYPE_LONG      : return Long.valueOf(readLong());
            case TYPE_FLOAT     : return new Float(readFloat());
            case TYPE_DOUBLE    : return new Double(readDouble());
            case TYPE_BYTEARRAY : return readByteArray();
            case TYPE_CHARACTER : return new Character(readChar());
            default:
                throw new IllegalArgumentException("Unsupported type : "+type);
        }                
    }
    
    public void clear() 
    {
        size = 0;
    }
    
    public void reset()
    {
        pos = 0;
    }

    public int size()
    {
        return size;
    }
    
    public int pos()
    {
        return pos;
    }
    
    public byte[] toByteArray() 
    {
        byte[] copy = new byte[size];
        System.arraycopy(buf, 0, copy, 0, size);
        return copy;
    }
    
    public byte[] toByteArray( int offset , int len ) 
    {
        byte[] copy = new byte[len];
        System.arraycopy(buf, offset, copy, 0, len);
        return copy;
    }
    
    public void writeTo( OutputStream out ) throws IOException
    {
        out.write(buf, 0, size);
    }
    
    public void putTo( ByteBuffer byteBuffer , int offset , int len )
    {
        byteBuffer.put(buf,offset,len);
    }

    public int readFrom(InputStream in , int offset, int len) throws IOException
    {
        ensureCapacity(offset+len);
        int readSize = in.read(buf, offset, len);
        if (readSize == -1)
            return -1;
        if (offset+readSize > size)
            size = offset+readSize;
        return readSize;
    }
    
    public void getFrom( ByteBuffer byteBuffer , int offset , int len )
    {
        ensureCapacity(offset+len);
        byteBuffer.get(buf,offset,len);
    }
    
    public void writeTo( RawDataBuffer otherBuffer )
    {
    	otherBuffer.write(buf,0,size);
    }
    
    /**
     * Create a copy of this buffer
     * @return a copy of this buffer
     */
    public RawDataBuffer copy()
    {
    	RawDataBuffer copy = new RawDataBuffer(capacity);
    	System.arraycopy(buf, 0, copy.buf, 0, capacity);
    	copy.size = this.size;
    	copy.pos = this.pos;
    	return copy;
    }
}
