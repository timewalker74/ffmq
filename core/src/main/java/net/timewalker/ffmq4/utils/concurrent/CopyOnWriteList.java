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
package net.timewalker.ffmq4.utils.concurrent;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

/**
 * CopyOnWriteList
 */
public class CopyOnWriteList<T> implements List<T>
{
	// Attributes
	private ReferenceAwareVector<T> internalList;
	
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
		this.internalList = new ReferenceAwareVector<>(initialCapacity);
		this.internalList.incRefCount();
	}
	
	/**
	 * Constructor (private)
	 */
	private CopyOnWriteList( ReferenceAwareVector<T> internalList )
	{
		this.internalList = internalList;
		this.internalList.incRefCount();
	}
	
	/*
	 * 	(non-Javadoc)
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	@Override
	public synchronized void add(int index, T element)
	{
		copyOnWrite();
		internalList.add(index, element);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.List#add(java.lang.Object)
	 */
	@Override
	public synchronized boolean add(T o)
	{
		copyOnWrite();
		return internalList.add(o);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	@Override
	public synchronized boolean addAll(Collection<? extends T> c)
	{
		copyOnWrite();
		return internalList.addAll(c);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	@Override
	public synchronized boolean addAll(int index, Collection<? extends T> c)
	{
		copyOnWrite();
		return internalList.addAll(index, c);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#clear()
	 */
	@Override
	public synchronized void clear()
	{
		copyOnWrite();
		internalList.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object o)
	{
		return internalList.contains(o);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(Collection<?> c)
	{
		return internalList.containsAll(c);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o)
	{
		return internalList.equals(o);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#get(int)
	 */
	@Override
	public T get(int index)
	{
		return internalList.get(index);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return internalList.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	@Override
	public int indexOf(Object o)
	{
		return internalList.indexOf(o);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#isEmpty()
	 */
	@Override
	public boolean isEmpty()
	{
		return internalList.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#iterator()
	 */
	@Override
	public Iterator<T> iterator()
	{
		throw new IllegalStateException("Unsupported operation");
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	@Override
	public int lastIndexOf(Object o)
	{
		return internalList.lastIndexOf(o);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#listIterator()
	 */
	@Override
	public ListIterator<T> listIterator()
	{
		throw new IllegalStateException("Unsupported operation");
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#listIterator(int)
	 */
	@Override
	public ListIterator<T> listIterator(int index)
	{
		return internalList.listIterator(index);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#remove(int)
	 */
	@Override
	public synchronized T remove(int index)
	{	
		copyOnWrite();
		return internalList.remove(index);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#remove(java.lang.Object)
	 */
	@Override
	public synchronized boolean remove(Object o)
	{
		copyOnWrite();
		return internalList.remove(o);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#removeAll(java.util.Collection)
	 */
	@Override
	public synchronized boolean removeAll(Collection<?> c)
	{
		copyOnWrite();
		return internalList.removeAll(c);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#retainAll(java.util.Collection)
	 */
	@Override
	public synchronized boolean retainAll(Collection<?> c)
	{
		copyOnWrite();
		return internalList.retainAll(c);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	@Override
	public synchronized T set(int index, T element)
	{
		copyOnWrite();
		return internalList.set(index, element);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#size()
	 */
	@Override
	public int size()
	{
		return internalList.size();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#subList(int, int)
	 */
	@Override
	public List<T> subList(int fromIndex, int toIndex)
	{
		throw new IllegalStateException("Unsupported operation");
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#toArray()
	 */
	@Override
	public Object[] toArray()
	{
		return internalList.toArray();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#toArray(java.lang.Object[])
	 */
	@Override
	public <O> O[] toArray(O[] a)
	{
		return internalList.toArray(a);
	}

	/**
	 * Create a copy-on-write copy of this list
	 * @return a copy-on-write copy of this list
	 */
	public synchronized CopyOnWriteList<T> fastCopy()
	{
		synchronized(internalList)
		{
			return new CopyOnWriteList<>(internalList);
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
				
				@SuppressWarnings("unchecked")
				ReferenceAwareVector<T> newList = (ReferenceAwareVector<T>)internalList.clone();
				newList.incRefCount();
				
				this.internalList = newList;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable
	{
		synchronized (internalList)
		{
			this.internalList.decRefCount();
		}
	}
	
	/**
	 * Get the number of lists sharing the same internal state as this one
	 * @return 
	 */
	public synchronized int getShareLevel()
	{
	    return internalList.getRefCount()-1;
	}
	
	//-------------------------------------------------------------------------------------
	
	/**
	 * ReferenceAwareVector
	 */
	private static class ReferenceAwareVector<T> extends Vector<T>
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
		@SuppressWarnings("unchecked")
		@Override
		public synchronized Object clone()
		{
		    Object copy = super.clone();
		    
		    ((ReferenceAwareVector<T>)copy).refCount = 0;
		    
		    return copy;
		}
		
		/* (non-Javadoc)
		 * @see java.util.Vector#equals(java.lang.Object)
		 */
		@Override
		public synchronized boolean equals(Object o)
		{
			// Same as parent
			return super.equals(o);
		}
		
		/* (non-Javadoc)
		 * @see java.util.Vector#hashCode()
		 */
		@Override
		public synchronized int hashCode()
		{
			// Same as parent
			return super.hashCode();
		}
	}
}
