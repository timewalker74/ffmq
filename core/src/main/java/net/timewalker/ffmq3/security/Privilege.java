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
package net.timewalker.ffmq3.security;

import java.util.HashSet;
import java.util.Set;

import net.timewalker.ffmq3.utils.StringTools;

/**
 * Privilege
 */
public class Privilege 
{
	private String resourcePattern;
	private Set<String> actions = new HashSet<>();
	
	/**
	 * Constructor
	 */
	public Privilege()
	{
		// Nothing
	}

    public void setResourcePattern(String resourcePattern)
    {
        this.resourcePattern = resourcePattern;
    }

    public void setActions( String actionsList )
    {
        String[] actionNames = StringTools.split(actionsList, ',');
        for (int i = 0 ; i < actionNames.length ; i++)
            actions.add(actionNames[i].trim());
    }
        
    /**
	 * Check if this privilege matches the given resource/action couple
	 * @param resourceName the resource name
	 * @param action the action name
	 * @return true if the privilege matches
	 */
	public boolean matches( String resourceName , String action )
	{
		// Match rsource name first
		if (!StringTools.matches(resourceName, resourcePattern))
			return false;
		
		// Check action
		return actions.contains(action);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
	public String toString()
    {
        return StringTools.join(actions,",")+" on "+resourcePattern;
    }
}
