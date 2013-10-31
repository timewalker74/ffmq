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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.Set;

import javax.jms.JMSException;

import net.timewalker.ffmq4.FFMQException;

/**
 * Settings
 * <p>This a type-enabled wrapper for a Properties object</p>
 */
public final class Settings
{
    private Properties settings = new Properties();
    
    /**
     * Constructor
     */
    public Settings()
    {
        // Nothing
    }
    
    /**
     * Constructor
     */
    public Settings( Properties settings )
    {
        this.settings.putAll(settings);
    }

    public void readFrom( File settingsFile ) throws JMSException
    {
        FileInputStream in = null;
        try
        {
            in = new FileInputStream(settingsFile);
            settings.load(in);
            in.close();
        }
        catch (Exception e)
        {
            throw new FFMQException("Cannot load settings from "+settingsFile.getAbsolutePath()+" : "+e,"FS_ERROR");
        }
        finally
        {
            try
            {
                if (in != null)
                    in.close();
            }
            catch (Exception e)
            {
                throw new FFMQException("Cannot close settings file "+settingsFile.getAbsolutePath()+" : "+e,"FS_ERROR");
            }
        }
    }
    
    public void writeTo( File settingsFile , String title ) throws JMSException
    {
        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream(settingsFile);
            settings.store(out, title);
            out.close();
        }
        catch (Exception e)
        {
            throw new FFMQException("Cannot save settings to "+settingsFile.getAbsolutePath()+" : "+e,"FS_ERROR");
        }
        finally
        {
            try
            {
                if (out != null)
                    out.close();
            }
            catch (Exception e)
            {
                throw new FFMQException("Cannot close settings file "+settingsFile.getAbsolutePath()+" : "+e,"FS_ERROR");
            }
        }
    }
    
    /**
     * Get a property by name
     * @param key the property name
     */
    public String getStringProperty( String key )
    {
        return getStringProperty(key,null,true);
    }
    
    /**
     * Get a property by name
     * @param key the property name
     */
    public String getStringProperty( String key , String defaultValue )
    {
        return getStringProperty(key,defaultValue,true);
    }
    
    /**
     * Get a property by name
     * @param key the property name
     */
    public String getStringProperty( String key , String defaultValue , boolean replaceSystemProperties )
    {
        String value = settings.getProperty(key,defaultValue);
        if (replaceSystemProperties)
            value = SystemTools.replaceSystemProperties(value);
        return value != null ? value.trim() : null;
    }
    
    /**
     * Get a property by name
     * @param key the property name
     */
    public int getIntProperty( String key , int defaultValue )
    {
        String value = SystemTools.replaceSystemProperties(settings.getProperty(key));
        if (value == null)
            return defaultValue;
        try
        {
            return Integer.parseInt(value);
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }
    
    /**
     * Get a property by name
     * @param key the property name
     */
    public long getLongProperty( String key , long defaultValue )
    {
        String value = SystemTools.replaceSystemProperties(settings.getProperty(key));
        if (value == null)
            return defaultValue;
        try
        {
            return Long.parseLong(value);
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }
    
    /**
     * Get a property by name
     * @param key the property name
     */
    public boolean getBooleanProperty( String key , boolean defaultValue )
    {
        String value = SystemTools.replaceSystemProperties(settings.getProperty(key));
        if (value == null)
            return defaultValue;
        return Boolean.valueOf(value.trim()).booleanValue();
    }
    
    /**
     * Set a string property by name
     * @param key the property name
     */
    public void setStringProperty( String key , String value )
    {
        settings.setProperty(key, value);
    }
    
    /**
     * Set a string property by name
     * @param key the property name
     */
    public void setIntProperty( String key , int value )
    {
        settings.setProperty(key, String.valueOf(value));
    }
    
    /**
     * Set a string property by name
     * @param key the property name
     */
    public void setLongProperty( String key , long value )
    {
        settings.setProperty(key, String.valueOf(value));
    }
    
    /**
     * Set a string property by name
     * @param key the property name
     */
    public void setBooleanProperty( String key , boolean value )
    {
        settings.setProperty(key, String.valueOf(value));
    }
    
    /**
     * Remove a property from this settings object
     * @param key the property name
     * @return the value of the removed property or null if not found 
     */
    public String removeProperty( String key )
    {
        return (String)settings.remove(key);
    }
    
    /**
     * Utility method to access the settings object as a properties object
     * @return the underlying properties object
     */
    public Properties asProperties()
    {
        return settings;
    }
    
    /**
     * Return a set of the settings keys
     * @return a set of the settings keys
     */
    public Set<Object> keySet()
    {
        return settings.keySet();
    }
}
