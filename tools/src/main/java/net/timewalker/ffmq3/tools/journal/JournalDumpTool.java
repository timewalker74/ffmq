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

package net.timewalker.ffmq3.tools.journal;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import net.timewalker.ffmq3.storage.data.impl.journal.AbstractJournalOperation;
import net.timewalker.ffmq3.storage.data.impl.journal.CommitOperation;
import net.timewalker.ffmq3.storage.data.impl.journal.JournalException;
import net.timewalker.ffmq3.storage.data.impl.journal.JournalRecovery;

/**
 * JournalDumpTool
 */
public class JournalDumpTool
{
	public static void main(String[] args) throws Exception
	{
		if (args.length != 1)
			throw new IllegalArgumentException("Expected one parameter : <journalFile>");
		
		File journalFile = new File(args[0]);
		
		System.out.println("---------------------------------------------------------------------------------------");
		System.out.println("Dump of journal file : "+journalFile.getAbsolutePath());
		System.out.println("---------------------------------------------------------------------------------------");
		
		DataInputStream in;
		try
		{
			// Create a buffered data input stream from file
			in = new DataInputStream(new BufferedInputStream(new FileInputStream(journalFile)));
		}
		catch (IOException e)
		{
			throw new JournalException("Cannot open journal file : "+journalFile.getAbsolutePath(),e);
		}
		
		try
		{
			AbstractJournalOperation op;
			while ((op = JournalRecovery.readJournalOperation(in)) != null)
			{				
				System.out.println(op);
				if (op instanceof CommitOperation)
					System.out.println("----------------------------------------------------------------------");
			}
		}
		finally
		{
			try
			{
				in.close();
			}
			catch (IOException e)
			{
				throw new JournalException("Cannot close journal file : "+journalFile.getAbsolutePath(),e);
			}
		}
	}
}
