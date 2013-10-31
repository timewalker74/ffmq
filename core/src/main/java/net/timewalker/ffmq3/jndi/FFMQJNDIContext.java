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
package net.timewalker.ffmq3.jndi;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.CompoundName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import javax.naming.OperationNotSupportedException;

import net.timewalker.ffmq3.common.destination.QueueRef;
import net.timewalker.ffmq3.common.destination.TopicRef;

/**
 * <p>Implementation of a JNDI {@link Context}, providing lookup for FFMQ connection factories.</p>
 */
public final class FFMQJNDIContext implements Context
{
    private Hashtable<String,Object>   env;
    protected Hashtable<String,Object> bindings   = new Hashtable<>();
    private static final NameParser flatParser = new FlatNameParser();

    /**
     * Constructor
     */
    public FFMQJNDIContext(Hashtable<?,?> environment)
    {
        // Copy environment
        if (environment != null)
            env = (Hashtable<String,Object>)environment.clone();
        else
            env = new Hashtable<>();
    }

    /**
     * Constructor
     */
    protected FFMQJNDIContext(Hashtable<String,Object> environment,Hashtable<String,Object> bindings)
    {
        this.env = (Hashtable<String,Object>)environment.clone();
        this.bindings = bindings;
    }

    /*
     * (non-Javadoc)
     * @see javax.naming.Context#lookup(java.lang.String)
     */
    @Override
	public Object lookup(String name) throws NamingException
    {
        return lookup(new CompositeName(name));
    }

    /*
     * (non-Javadoc)
     * @see javax.naming.Context#lookup(javax.naming.Name)
     */
    @Override
	public Object lookup(Name name) throws NamingException
    {
        if (name.isEmpty())
            return new FFMQJNDIContext(env, bindings);

        // Extract components that belong to this namespace
        String nm = name.toString();

        // Find object in internal hash table
        Object answer = bindings.get(nm);
        if (answer == null)
        {
        	// Dynamic queue or topic lookup
        	if (nm.startsWith("queue/") && nm.length() > 6)
        		return new QueueRef(nm.substring(nm.indexOf('/')+1));
        	if (nm.startsWith("topic/") && nm.length() > 6)
        		return new TopicRef(nm.substring(nm.indexOf('/')+1));
        	
            throw new NameNotFoundException(name + " not found");
        }

        return answer;
    }

    /*
     * (non-Javadoc)
     * @see javax.naming.Context#bind(java.lang.String, java.lang.Object)
     */
    @Override
	public void bind(String name, Object obj) throws NamingException
    {
        bind(new CompositeName(name), obj);
    }

    /*
     * (non-Javadoc)
     * @see javax.naming.Context#bind(javax.naming.Name, java.lang.Object)
     */
    @Override
	public void bind(Name name, Object obj) throws NamingException
    {
        if (name.isEmpty())
            throw new InvalidNameException("Cannot bind empty name");

        // Extract components that belong to this namespace
        String nm = name.toString();

        // Find object in internal hash table
        if (bindings.get(nm) != null)
        {
            throw new NameAlreadyBoundException("Use rebind to override");
        }

        // Add object to internal hash table
        bindings.put(nm, obj);
    }

    /*
     * (non-Javadoc)
     * @see javax.naming.Context#rebind(java.lang.String, java.lang.Object)
     */
    @Override
	public void rebind(String name, Object obj) throws NamingException
    {
        rebind(new CompositeName(name), obj);
    }

    /*
     * (non-Javadoc)
     * @see javax.naming.Context#rebind(javax.naming.Name, java.lang.Object)
     */
    @Override
	public void rebind(Name name, Object obj) throws NamingException
    {
        if (name.isEmpty())
            throw new InvalidNameException("Cannot bind empty name");

        // Extract components that belong to this namespace
        String nm = name.toString();

        // Add object to internal hash table
        bindings.put(nm, obj);
    }

    /*
     * (non-Javadoc)
     * @see javax.naming.Context#unbind(java.lang.String)
     */
    @Override
	public void unbind(String name) throws NamingException
    {
        unbind(new CompositeName(name));
    }

    /*
     * (non-Javadoc)
     * @see javax.naming.Context#unbind(javax.naming.Name)
     */
    @Override
	public void unbind(Name name) throws NamingException
    {
        if (name.isEmpty())
            throw new InvalidNameException("Cannot unbind empty name");

        // Extract components that belong to this namespace
        String nm = name.toString();

        // Remove object from internal hash table
        bindings.remove(nm);
    }
    
    /*
     * (non-Javadoc)
     * @see javax.naming.Context#rename(java.lang.String, java.lang.String)
     */
    @Override
	public void rename(String oldname, String newname) throws NamingException
    {
        rename(new CompositeName(oldname), new CompositeName(newname));
    }

    /*
     * (non-Javadoc)
     * @see javax.naming.Context#rename(javax.naming.Name, javax.naming.Name)
     */
    @Override
	public void rename(Name oldname, Name newname) throws NamingException
    {
        if (oldname.isEmpty() || newname.isEmpty())
            throw new InvalidNameException("Cannot rename empty name");

        // Extract components that belong to this namespace
        String oldnm = oldname.toString();
        String newnm = newname.toString();

        // Check if new name exists
        if (bindings.get(newnm) != null)
            throw new NameAlreadyBoundException(newname.toString() + " is already bound");

        // Check if old name is bound
        Object oldBinding = bindings.remove(oldnm);
        if (oldBinding == null)
            throw new NameNotFoundException(oldname.toString() + " not bound");

        bindings.put(newnm, oldBinding);
    }

    /*
     * (non-Javadoc)
     * @see javax.naming.Context#list(java.lang.String)
     */
    @Override
	public NamingEnumeration<NameClassPair> list(String name) throws NamingException
    {
        return list(new CompositeName(name));
    }

    /*
     * (non-Javadoc)
     * @see javax.naming.Context#list(javax.naming.Name)
     */
    @Override
	public NamingEnumeration<NameClassPair> list(Name name) throws NamingException
    {
        if (name.isEmpty())
            return new ListOfNames(bindings.keys());

        Object target = lookup(name);
        if (target instanceof Context)
        {
            try
            {
                return ((Context)target).list("");
            }
            finally
            {
                ((Context)target).close();
            }
        }
        throw new NotContextException(name + " cannot be listed");
    }

    /*
     * (non-Javadoc)
     * @see javax.naming.Context#listBindings(java.lang.String)
     */
    @Override
	public NamingEnumeration<Binding> listBindings(String name) throws NamingException
    {
        return listBindings(new CompositeName(name));
    }

    /*
     * (non-Javadoc)
     * @see javax.naming.Context#listBindings(javax.naming.Name)
     */
    @Override
	public NamingEnumeration<Binding> listBindings(Name name) throws NamingException
    {
        if (name.isEmpty())
            return new ListOfBindings(bindings.keys());

        Object target = lookup(name);
        if (target instanceof Context)
        {
            try
            {
                return ((Context)target).listBindings("");
            }
            finally
            {
                ((Context)target).close();
            }
        }
        throw new NotContextException(name + " cannot be listed");
    }

    /*
     * (non-Javadoc)
     * @see javax.naming.Context#destroySubcontext(java.lang.String)
     */
    @Override
	public void destroySubcontext(String name) throws NamingException
    {
        destroySubcontext(new CompositeName(name));
    }

    /*
     * (non-Javadoc)
     * @see javax.naming.Context#destroySubcontext(javax.naming.Name)
     */
    @Override
	public void destroySubcontext(Name name) throws NamingException
    {
        throw new OperationNotSupportedException("This context does not support subcontexts");
    }

    /*
     * (non-Javadoc)
     * @see javax.naming.Context#createSubcontext(java.lang.String)
     */
    @Override
	public Context createSubcontext(String name) throws NamingException
    {
        return createSubcontext(new CompositeName(name));
    }

    /*
     * (non-Javadoc)
     * @see javax.naming.Context#createSubcontext(javax.naming.Name)
     */
    @Override
	public Context createSubcontext(Name name) throws NamingException
    {
        throw new OperationNotSupportedException("This context does not support subcontexts");
    }

    /*
     * (non-Javadoc)
     * @see javax.naming.Context#lookupLink(java.lang.String)
     */
    @Override
	public Object lookupLink(String name) throws NamingException
    {
        return lookupLink(new CompositeName(name));
    }

    /*
     * (non-Javadoc)
     * @see javax.naming.Context#lookupLink(javax.naming.Name)
     */
    @Override
	public Object lookupLink(Name name) throws NamingException
    {
        return lookup(name);
    }

    /*
     * (non-Javadoc)
     * @see javax.naming.Context#getNameParser(java.lang.String)
     */
    @Override
	public NameParser getNameParser(String name) throws NamingException
    {
        return getNameParser(new CompositeName(name));
    }

    /*
     * (non-Javadoc)
     * @see javax.naming.Context#getNameParser(javax.naming.Name)
     */
    @Override
	public NameParser getNameParser(Name name) throws NamingException
    {
        // Do lookup to verify name exists
        Object obj = lookup(name);
        if (obj instanceof Context)
        {
            ((Context)obj).close();
        }
        return flatParser;
    }

    /*
     * (non-Javadoc)
     * @see javax.naming.Context#composeName(java.lang.String, java.lang.String)
     */
    @Override
	public String composeName(String name, String prefix) throws NamingException
    {
        Name result = composeName(new CompositeName(name), new CompositeName(prefix));
        return result.toString();
    }

    /*
     * (non-Javadoc)
     * @see javax.naming.Context#composeName(javax.naming.Name, javax.naming.Name)
     */
    @Override
	public Name composeName(Name name, Name prefix) throws NamingException
    {
        Name result = (Name)(prefix.clone());
        result.addAll(name);
        return result;
    }

    /*
     * (non-Javadoc)
     * @see javax.naming.Context#addToEnvironment(java.lang.String, java.lang.Object)
     */
    @Override
	public Object addToEnvironment(String propName, Object propVal)
    {
        return env.put(propName, propVal);
    }

    /*
     * (non-Javadoc)
     * @see javax.naming.Context#removeFromEnvironment(java.lang.String)
     */
    @Override
	public Object removeFromEnvironment(String propName)
    {
        return env.remove(propName);
    }

    /*
     * (non-Javadoc)
     * @see javax.naming.Context#getEnvironment()
     */
    @Override
	public Hashtable<?,?> getEnvironment()
    {
        return (Hashtable<?,?>)env.clone();
    }

    /*
     * (non-Javadoc)
     * @see javax.naming.Context#getNameInNamespace()
     */
    @Override
	public String getNameInNamespace()
    {
        return "";
    }

    /*
     * (non-Javadoc)
     * @see javax.naming.Context#close()
     */
    @Override
	public void close()
    {
        // Nothing to do
    }

    //--------------------------------------------------------------------------------------
    
    // Class for enumerating name/class pairs
    private class ListOfNames implements NamingEnumeration<NameClassPair>
    {
        protected Enumeration<String> names;

        /**
         * Constructor
         */
        public ListOfNames(Enumeration<String> names)
        {
            this.names = names;
        }

        /*
         * (non-Javadoc)
         * @see java.util.Enumeration#hasMoreElements()
         */
        @Override
		public boolean hasMoreElements()
        {
            return hasMore();
        }

        /*
         * (non-Javadoc)
         * @see javax.naming.NamingEnumeration#hasMore()
         */
        @Override
		public boolean hasMore()
        {
            return names.hasMoreElements();
        }

        /*
         * (non-Javadoc)
         * @see javax.naming.NamingEnumeration#next()
         */
        @Override
		public NameClassPair next()
        {
            String name = names.nextElement();
            String className = bindings.get(name).getClass().getName();
            return new NameClassPair(name, className);
        }

        /*
         * (non-Javadoc)
         * @see java.util.Enumeration#nextElement()
         */
        @Override
		public NameClassPair nextElement()
        {
            return next();
        }

        /*
         * (non-Javadoc)
         * @see javax.naming.NamingEnumeration#close()
         */
        @Override
		public void close()
        {
            // Nothing
        }
    }

    // Class for enumerating bindings
    private class ListOfBindings implements NamingEnumeration<Binding>
    {
    	protected Enumeration<String> names;
    	
        /**
         * Constructor
         */
        public ListOfBindings(Enumeration<String> names)
        {
        	this.names = names;
        }

        /*
         * (non-Javadoc)
         * @see java.util.Enumeration#hasMoreElements()
         */
        @Override
		public boolean hasMoreElements()
        {
            return hasMore();
        }

        /*
         * (non-Javadoc)
         * @see javax.naming.NamingEnumeration#hasMore()
         */
        @Override
		public boolean hasMore()
        {
            return names.hasMoreElements();
        }
        
        /*
         * (non-Javadoc)
         * @see net.timewalker.ffmq3.common.jndi.FFMQJNDIContext.ListOfNames#next()
         */
        @Override
		public Binding next()
        {
            String name = names.nextElement();
            return new Binding(name, bindings.get(name));
        }
        
        /*
         * (non-Javadoc)
         * @see java.util.Enumeration#nextElement()
         */
        @Override
		public Binding nextElement()
        {
            return next();
        }
        
        /*
         * (non-Javadoc)
         * @see javax.naming.NamingEnumeration#close()
         */
        @Override
		public void close()
        {
            // Nothing
        }
    }

    private static class FlatNameParser implements NameParser
    {
        private static final Properties syntax = new Properties();
        static
        {
            syntax.put("jndi.syntax.direction", "flat");
            syntax.put("jndi.syntax.ignorecase", "false");
        }
        
		/**
		 * Constructor
		 */
        public FlatNameParser()
		{
			super();
		}
        
        /*
         * (non-Javadoc)
         * @see javax.naming.NameParser#parse(java.lang.String)
         */
        @Override
		public Name parse(String name) throws NamingException
        {
            return new CompoundName(name, syntax);
        }
    }
}
