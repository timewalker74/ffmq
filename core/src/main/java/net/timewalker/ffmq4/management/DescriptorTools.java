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
package net.timewalker.ffmq4.management;

import java.io.File;
import java.io.FileFilter;

/**
 * DescriptorTools
 */
public final class DescriptorTools
{
    /**
     * Find all descriptor files with the given prefix in a target folder
     * @param definitionDir the target folder
     * @param prefix the descriptor filename prefix
     * @return an array of descriptor files
     */
    public static File[] getDescriptorFiles( File definitionDir , final String prefix , final String suffix )
    {
        return definitionDir.listFiles(new FileFilter() {
            /*
             * (non-Javadoc)
             * @see java.io.FileFilter#accept(java.io.File)
             */
            @Override
			public boolean accept(File pathname)
            {
                if (!pathname.isFile() || !pathname.canRead())
                    return false;
                
                String name = pathname.getName();
                return name.toLowerCase().endsWith(suffix) && name.startsWith(prefix);
            }
        });
    }
}
