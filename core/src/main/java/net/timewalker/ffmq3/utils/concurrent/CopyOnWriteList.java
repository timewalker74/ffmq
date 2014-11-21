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
package net.timewalker.ffmq3.utils.concurrent;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

/**
 * CopyOnWriteList
 */
public class CopyOnWriteList implements List
{
	// Attributes
	private ReferenceAwareVector internalList;
	
	/**
	 * Constructor
	 */
	public CopyOnWriteList()
	{
		this(10);
	}
	
	/**
	 * Constructor
	 */
	public CopyOnWriteList( int initialCapacity )
	{
		this.internalList = new ReferenceAwareVector(initialCapacity);
		this.internalList.incRefCount();
	}
	
	/**
	 * Constructor (private)
	 */
	private CopyOnWriteList( ReferenceAwareVector internalList )
	{
		this.internalList = internalList;
		this.internalList.incRefCount();
	}
	
	/*
	 * 	(non-Javadoc)
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	public synchronized void add(int index, Object element)
	{
		copyOnWrite();
		internalList.add(index, element);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.List#add(java.lang.Object)
	 */
	public synchronized boolean add(Object o)
	{
		copyOnWrite();
		return internalList.add(o);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	public synchronized boolean addAll(Collection c)
	{
		copyOnWrite();
		return internalList.addAll(c);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	public synchronized boolean addAll(int index, Collection c)
	{
		copyOnWrite();
		return internalList.addAll(index, c);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#clear()
	 */
	public synchronized void clear()
	{
		copyOnWrite();
		internalList.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#contains(java.lang.Object)
	 */
	public boolean contains(Object o)
	{
		return internalList.contains(o);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection c)
	{
		return internalList.containsAll(c);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o)
	{
		return internalList.equals(o);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#get(int)
	 */
	public Object get(int index)
	{
		return internalList.get(index);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		return internalList.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	public int indexOf(Object o)
	{
		return internalList.indexOf(o);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#isEmpty()
	 */
	public boolean isEmpty()
	{
		return internalList.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#iterator()
	 */
	public Iterator iterator()
	{
		throw new IllegalStateException("Unsupported operation");
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object o)
	{
		return internalList.lastIndexOf(o);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#listIterator()
	 */
	public ListIterator listIterator()
	{
		throw new IllegalStateException("Unsupported operation");
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#listIterator(int)
	 */
	public ListIterator listIterator(int index)
	{
		return internalList.listIterator(index);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#remove(int)
	 */
	public synchronized Object remove(int index)
	{	
		copyOnWrite();
		return internalList.remove(index);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#remove(java.lang.Object)
	 */
	public synchronized boolean remove(Object o)
	{
		copyOnWrite();
		return internalList.remove(o);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#removeAll(java.util.Collection)
	 */
	public synchronized boolean removeAll(Collection c)
	{
		copyOnWrite();
		return internalList.removeAll(c);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#retainAll(java.util.Collection)
	 */
	public synchronized boolean retainAll(Collection c)
	{
		copyOnWrite();
		return internalList.retainAll(c);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	public synchronized Object set(int index, Object element)
	{
		copyOnWrite();
		return internalList.set(index, element);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#size()
	 */
	public int size()
	{
		return internalList.size();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#subList(int, int)
	 */
	public List subList(int fromIndex, int toIndex)
	{
		throw new IllegalStateException("Unsupported operation");
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#toArray()
	 */
	public Object[] toArray()
	{
		return internalList.toArray();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#toArray(java.lang.Object[])
	 */
	public Object[] toArray(Object[] a)
	{
		return internalList.toArray(a);
	}

	/**
	 * Create a copy-on-write copy of this list
	 * @return a copy-on-write copy of this list
	 */
	public synchronized CopyOnWriteList fastCopy()
	{
		synchronized(internalList)
		{
			return new CopyOnWriteList(internalList);
		}
	}
	
	private void copyOnWrite()
	{
		synchronized (internalList)
		{
			// Only copy-on-write if we have given away the internal array
			// otherwise we can freely modify things.
			if (internalList.getRefCount() > 1)
			{
				this.internalList.decRefCount();
				
				ReferenceAwareVector newList = (ReferenceAwareVector)internalList.clone();
				newList.incRefCount();
				
				this.internalList = newList;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable
	{
		synchronized (internalList)
		{
			this.internalList.decRefCount();
		}
	}
	
	/**
	 * Get the number of lists sharing the same internal state as this one
	 * @return the number of lists sharing the same internal state as this one
	 */
	public synchronized int getShareLevel()
	{
	    return internalList.getRefCount()-1;
	}
	
	//-------------------------------------------------------------------------------------
	
	/**
	 * ReferenceAwareVector
	 */
	private static class ReferenceAwareVector extends Vector
	{
		private static final long serialVersionUID = 1L;
		
		// Attribute
		private int refCount;

		/**
		 * Constructor
		 * @param initialCapacity
		 */
		public ReferenceAwareVector(int initialCapacity)
		{
			super(initialCapacity);
		}
		
		public int getRefCount()
		{
			return refCount;
		}

		public void incRefCount()
		{
			refCount++;
		}

		public void decRefCount()
		{
			refCount--;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.Vector#clone()
		 */
		public synchronized Object clone()
		{
		    Object copy = super.clone();
		    
		    ((ReferenceAwareVector)copy).refCount = 0;
		    
		    return copy;
		}
		
		/* (non-Javadoc)
		 * @see java.util.Vector#equals(java.lang.Object)
		 */
		public synchronized boolean equals(Object o)
		{
			// Same as parent
			return super.equals(o);
		}
		
		/* (non-Javadoc)
		 * @see java.util.Vector#hashCode()
		 */
		public synchronized int hashCode()
		{
			// Same as parent
			return super.hashCode();
		}
	}
}
