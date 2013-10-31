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

package net.timewalker.ffmq3;

import java.util.Properties;

import net.timewalker.ffmq3.utils.Settings;

/**
 * FFMQJMXConsoleLauncher
 */
public final class FFMQJMXConsoleLauncher
{
	/**
	 * Main
	 * @param args command-line arguments
	 */
	public static void main(String[] args)
	{
		try
	    {
	        if (args.length == 0)
	            printUsage();
	        else
	        {
	            Settings settings = parseCommandLine(args);
	            if (settings != null)
	            	new FFMQJMXConsole(settings,
	            			           System.in,
	            			           System.out,
	            			           System.err).run();
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
	    System.out.println("  -host <hostname or address> : the target server host");
	    System.out.println("  -port <portNumber> : the target server JMX port (default: 10003)");
	    System.out.println("  -command <commandName> : the command to run");
	    System.out.println("  -interactive : enable interactive mode");
	    System.out.println();
	}
	
	private static Settings parseCommandLine(String[] args)
	{
	    Properties props = new Properties();

	    // Override parameters
	    for (int i = 0 ; i < args.length ; i++)
	    {
	    	if (args[i].equals("--help") || args[i].equals("-help") || args[i].equals("-h"))
	        {
	    		printUsage();
	    		return null;
	        }
	    	else
	        if (args[i].equals("-host"))
	        {
	            if (++i == args.length)
	                throw new IllegalArgumentException("Missing value after parameter "+args[i]);
	            props.setProperty(FFMQJMXConsoleSettings.SERVER_HOST,args[i]);
	        }
	        else if (args[i].equals("-port"))
	        {
	            if (++i == args.length)
	                throw new IllegalArgumentException("Missing value after parameter "+args[i]);
	            props.setProperty(FFMQJMXConsoleSettings.SERVER_PORT,args[i]);
	        }
	        else if (args[i].equals("-interactive"))
	        {
	            props.setProperty(FFMQJMXConsoleSettings.INTERACTIVE,"true");
	        }
	        else if (args[i].equals("-command"))
	        {
	            if (++i == args.length)
	                throw new IllegalArgumentException("Missing value after parameter "+args[i]);
	            props.setProperty(FFMQJMXConsoleSettings.COMMAND,args[i]);
	        }
	        else
	            throw new IllegalArgumentException("Unknown command-line option : "+args[i]);
	    }
	    
	    return new Settings(props);
	}
}
