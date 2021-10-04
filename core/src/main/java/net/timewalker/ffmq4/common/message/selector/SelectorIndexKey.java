/* 
 * ===================================================================
 * This document and/or file is OVERKIZ property. All information
 * it contains is strictly confidential. This document and/or file
 * shall not be used, reproduced or passed on in any way, in full
 * or in part without OVERKIZ prior written approval.
 * All rights reserved.
 * ===================================================================
 */
package net.timewalker.ffmq4.common.message.selector;

import java.util.Arrays;

/**
 * SelectorIndexKey
 */
public final class SelectorIndexKey
{
	private String headerName;
	private Object[] values;
	
	/**
	 * Constructor
	 * @param headerName
	 * @param value
	 */
	public SelectorIndexKey(String headerName, Object value)
	{
		super();
		this.headerName = headerName;
		this.values = new Object[] { value };
	}
	
	/**
	 * Constructor
	 * @param headerName
	 * @param values
	 */
	public SelectorIndexKey(String headerName, Object[] values)
	{
		super();
		this.headerName = headerName;
		this.values = values;
	}

	/**
	 * @return the headerName
	 */
	public String getHeaderName()
	{
		return headerName;
	}
	
	/**
	 * @return the value
	 */
	public Object[] getValues()
	{
		return values;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "SelectorIndexKey [headerName=" + headerName + ", values=" + Arrays.toString(values) + "]";
	}
}
