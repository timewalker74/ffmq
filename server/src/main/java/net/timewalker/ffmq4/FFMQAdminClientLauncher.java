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
package net.timewalker.ffmq4;

import java.io.File;

import net.timewalker.ffmq4.logging.Log4JTools;
import net.timewalker.ffmq4.utils.Settings;
import net.timewalker.ffmq4.utils.StringTools;

/**
 * FFMQAdminClientLauncher
 */
public final class FFMQAdminClientLauncher
{
    private static final String DEFAULT_ADMIN_CLIENT_CONF_FILE = "../conf/ffmq-admin-client.properties";
    
    /**
     * Main
     */
    public static void main(String[] args)
    {
        try
        {
            if (args.length == 0)
                printUsage();
            else
            {
                Settings globalSettings = new Settings();
                Settings paramSettings = new Settings();
                parseCommandLine(args,globalSettings,paramSettings);
                
                new FFMQAdminClient(globalSettings,paramSettings,System.out,System.err).run();
            }
            
            System.exit(0);
        }
        catch (IllegalArgumentException e)
        {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            System.exit(-2);
        }
    }
    
    private static void printUsage()
    {
        System.out.println(" Command-line parameters");
        System.out.println("-------------------------");
        System.out.println("  -command <command>      : the command to execute ("+StringTools.join(FFMQAdminConstants.ADM_COMMAND_ALL, ",")+")");
        System.out.println("  -conf <propertiesFile>  : path to a properties file (optional)");
        System.out.println();
        System.out.println("    All other variables should be passed as variable=value");
        System.out.println();
        System.out.println("    Examples:");
        System.out.println();
        System.out.println("      # Create queue FOO with a non-persistent message capacity of 1000");
        System.out.println("      ffmq-admin-client -command "+FFMQAdminConstants.ADM_COMMAND_CREATE_QUEUE+" name=FOO memoryStore.maxMessages=1000");
        System.out.println();
        System.out.println("      # Delete topic BAR");
        System.out.println("      ffmq-admin-client -command "+FFMQAdminConstants.ADM_COMMAND_DELETE_TOPIC+" name=BAR");
    }
    
    private static void parseCommandLine( String[] args , Settings globalSettings , Settings paramSettings ) throws Exception
    {
        String confFilePath = DEFAULT_ADMIN_CLIENT_CONF_FILE;
        
        // First pass to get the config file name
        for (int i = 0 ; i < args.length ; i++)
        {
            if (args[i].equals("-conf"))
            {
                i++;
                if (i == args.length)
                    throw new IllegalArgumentException("Missing value after parameter "+args[i]);
                confFilePath = args[i];
            }
        }

        // Read main config file
        File confFile = new File(confFilePath);
        if (!confFile.canRead())
            throw new IllegalArgumentException("Cannot access config file : "+confFile.getAbsolutePath());
        globalSettings.readFrom(confFile);
        
        // Configure log4j logger
        Log4JTools.initializeLog4J(globalSettings.asProperties());
        
        // Override parameters
        for (int i = 0 ; i < args.length ; i++)
        {
            if (args[i].equals("-conf"))
            {
                if (i+1 == args.length)
                    throw new IllegalArgumentException("Missing value after parameter "+args[i]);
                i++;
                // Already handled above
            }
            else if (args[i].equals("-command"))
            {
                if (i+1 == args.length)
                    throw new IllegalArgumentException("Missing value after parameter "+args[i]);
                globalSettings.setStringProperty(FFMQAdminClientSettings.ADM_COMMAND,args[++i]);
            }
            else
            {
                int sepIdx = args[i].indexOf('=');
                if (sepIdx == -1)
                    throw new IllegalArgumentException("Invalid command-line parameter : "+args[i]);
                
                String paramName = args[i].substring(0,sepIdx).trim();
                String paramValue = args[i].substring(sepIdx+1).trim();
                if (paramName.length() == 0 || paramValue.length() == 0)
                    throw new IllegalArgumentException("Invalid command-line parameter : "+args[i]);
                
                paramSettings.setStringProperty(paramName, paramValue);
            }
        }
    }
}
