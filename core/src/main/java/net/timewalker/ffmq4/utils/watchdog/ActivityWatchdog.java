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
package net.timewalker.ffmq3.utils.watchdog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Daemon thread watching a list of active objects.</p>
 * <p>
 *  The watchdog will wake up every WAKE_UP_INTERVAL milliseconds
 *  and check the active objects last activity timestamps.
 *  If an active object has been inactive for too long, its associated 
 *  timeout callback is called.
 * </p>
 * <p>
 * The activity watchdog is a singleton.
 * </p>
 * @see ActiveObject
 */
public class ActivityWatchdog extends Thread
{
	private static final Log log = LogFactory.getLog(ActivityWatchdog.class);

	// Singleton instance
	private static ActivityWatchdog instance;
	
	/**
	 * Get the singleton instance
	 * @return the singleton instance
	 */
	public static synchronized ActivityWatchdog getInstance()
	{
		if (instance == null)
		{
			instance = new ActivityWatchdog();
			instance.start();
		}
		return instance;
	}
	
	//-------------------------------------------------------------
	
	// Attributes
	private static long WAKE_UP_INTERVAL = 2000;
	
	// Runtime
	private List<WeakReference<ActiveObject>> watchList = new ArrayList<>();
	private boolean stop;
	
	/**
	 * Constructor
	 */
	private ActivityWatchdog()
	{
		super("ActivityWatchdog");
		setDaemon(true);
		setPriority(MIN_PRIORITY);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public synchronized void run()
	{
		List<ActiveObject> inactiveList = new ArrayList<>();
		try
		{
			log.debug("Starting activity watchdog (wakeUpInterval="+WAKE_UP_INTERVAL+"ms)");
			while (!stop)
			{
				wait(WAKE_UP_INTERVAL);
				if (stop)
					return;
				
				// Check watchlist
				long now = System.currentTimeMillis();
				synchronized (watchList)
				{
					for (int i = 0; i < watchList.size(); i++)
					{
						WeakReference<ActiveObject> weakRef = watchList.get(i);
						ActiveObject obj = weakRef.get();
						if (obj == null)
						{
							// Object was garbage collected, remove and continue
							watchList.remove(i--);
							continue;
						}
					
						// Check object activity
						if ((now - obj.getLastActivity()) > obj.getTimeoutDelay())
						{
							log.trace("Marking object as inactive : "+obj);
							inactiveList.add(obj);
						}
					}
				}
				
				// Process inactive objects
				for (int n = 0; n < inactiveList.size(); n++)
				{
					ActiveObject obj = inactiveList.get(n);
					try
					{
						if (!obj.onActivityTimeout())
							inactiveList.remove(n--);
					}
					catch (Exception e)
					{
						log.error("Error notifying an inactive object",e);
					}
				}
				
				// Remove expired objects
				synchronized (watchList)
				{
					for(int n=0;n<inactiveList.size();n++)
					{
						ActiveObject obj = inactiveList.get(n);
						
						log.trace("Removing inactive object from watch list : "+obj);
						watchList.remove(obj);
					}
				}
		
				// Clear inactive object list
				inactiveList.clear();
			}
			log.debug("Activity watchdog stopped");
		}
		catch (Throwable e)
		{
			log.error("Activity watchdog failed",e);
		}
	}
	
	/**
	 * Ask the watchdog thread to stop
	 */
	public synchronized void pleaseStop()
	{
		stop = true;
		notify();
	}
	
	/**
	 * Register an active object to be monitored
	 * @param object the object to register
	 */
	public void register( ActiveObject object )
	{
		synchronized (watchList)
		{
			watchList.add(new WeakReference<>(object));
		}
	}
	
	/**
	 * Unregister a monitored active object
	 * @param object the object to unregister
	 */
	public void unregister( ActiveObject object )
	{
		synchronized (watchList)
		{
			for (int i = 0; i < watchList.size(); i++)
			{
				WeakReference<ActiveObject> weakRef = watchList.get(i);
				ActiveObject obj = weakRef.get();
				if (obj == null)
				{
					// Object was garbage collected, remove and continue
					watchList.remove(i--);
					continue;
				}
				
				if (obj == object)
				{
					// Found it !
					watchList.remove(i);
					break;
				}
			}
		}
	}
}
