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
package net.timewalker.ffmq4.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * SerializationTools
 */
public final class SerializationTools
{
	/**
	 * Object to byte array
	 */
	public static byte[] toByteArray( Serializable object )
	{
	    try
	    {
            ByteArrayOutputStream buf = new ByteArrayOutputStream(1024);
            ObjectOutputStream objOut = new ObjectOutputStream(buf);
            objOut.writeObject(object);
            objOut.close();
            return buf.toByteArray();
	    }
	    catch (IOException e)
	    {
	        throw new IllegalArgumentException("Cannot serialize object : "+e.toString());
	    }
	}
	
	/**
	 * Byte array to object
	 */
	public static Serializable fromByteArray( byte[] data )
	{
		try
		{
	        ByteArrayInputStream buf = new ByteArrayInputStream(data);
	        ObjectInputStream objIn = new ObjectInputStream(buf);
	        Serializable response = (Serializable)objIn.readObject();
	        objIn.close();
	        return response;
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Cannot deserialize object : "+e.toString());
		}
	}
	
	public static void writeInt(int v,OutputStream out) throws IOException 
    {
        out.write((v >>> 24) & 0xFF);
        out.write((v >>> 16) & 0xFF);
        out.write((v >>>  8) & 0xFF);
        out.write((v >>>  0) & 0xFF);
    }
}
