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
package net.timewalker.ffmq3.common.message.selector;

import java.util.ArrayList;
import java.util.List;

import javax.jms.InvalidSelectorException;

/**
 * <p>
 *  Implementation of a JMS message selector expression tokenizer.
 * </p>
 */
public final class MessageSelectorTokenizer
{
    private static final String[] SYMBOL_DELIMITERS = {
       "=", "<>", "<=", ">=", "<", ">",
       "+","-","/","*",
       "(",")",",",
    };
    
    // Attributes
    private List<String> tokens = new ArrayList<>();
    private int tokenizerOffset;
    
    /**
     * Constructor
     */
    public MessageSelectorTokenizer( String messageSelector ) throws InvalidSelectorException
    {
        tokenize(messageSelector);
    }
    
    private String getSymbolDelimiter( String text , int offset )
    {
        for (int n = 0 ; n < SYMBOL_DELIMITERS.length ; n++)
            if (text.startsWith(SYMBOL_DELIMITERS[n],offset))
                return SYMBOL_DELIMITERS[n];
        return null;
    }
    
    private void tokenize( String text ) throws InvalidSelectorException
    {
        boolean inQuotes = false;
        int textLen = text.length();
        int pos = 0;
        String delimiter;
        StringBuffer buf = new StringBuffer();
        
        while (pos < textLen)
        {
            char c = text.charAt(pos);
            if (inQuotes)
            {
                if (c=='\'')
                {
                	buf.append('\'');
                	
                	if (pos < textLen-1 && text.charAt(pos+1) == '\'')
                	{
                		buf.append('\'');
                		pos++;
                	}
                	else
                    {
                		// Closing quote
                        inQuotes = false;
                        addAndCompress(buf.toString(),tokens);
                        buf.setLength(0);
                    }
                }
                else
                    buf.append(c);
                pos++;
            }
            else
            {
                if (c=='\'')
                {
                    buf.append('\'');
                    inQuotes = true;
                    pos++;
                }
                else
                    if (Character.isWhitespace(c))
                    {
                        if (buf.length() > 0)
                        {
                            addAndCompress(buf.toString(),tokens);
                            buf.setLength(0);
                        }
                        pos++;
                    }
                    else
                    if ((delimiter = getSymbolDelimiter(text,pos)) != null)
                    {
                        if (buf.length() > 0)
                        {
                            addAndCompress(buf.toString(),tokens);
                            buf.setLength(0);
                        }
                        addAndCompress(delimiter,tokens);
                        pos += delimiter.length();
                    }
                    else
                    {
                        buf.append(c);
                        pos++;
                    }
            }
        }
        if (inQuotes)
            throw new InvalidSelectorException("Expression contains an unclosed quote");
        if (buf.length() > 0)
        {            
            addAndCompress(buf.toString(),tokens);
            buf.setLength(0);
        }
    }
    
    private void addAndCompress( String token , List<String> tokensList ) throws InvalidSelectorException
    {
        if (tokensList.isEmpty())
        {
            tokensList.add(token);
            return;
        }
        
        // Catches thoses special constructs and recompress them as one token:
        //   is true
        //   is false
        //   is not true
        //   is not false
        //   is null
        //   is not null
        //   not in
        //   not between
        //   not like
                
        String previous = tokensList.get(tokensList.size()-1);
        if (previous.equalsIgnoreCase("is"))
        {
        	if (token.equalsIgnoreCase("true") || 
                token.equalsIgnoreCase("false") ||
                token.equalsIgnoreCase("null") ||
                token.equalsIgnoreCase("not"))
        	{
        		tokensList.set(tokensList.size()-1, "is "+token);
        	}
        	else
        		throw new InvalidSelectorException("Unexpected token after 'is' operator : "+token);
        }
        else
    	if (previous.equalsIgnoreCase("is not"))
        {
    		if (token.equalsIgnoreCase("true") || 
                token.equalsIgnoreCase("false") ||
                token.equalsIgnoreCase("null"))
        	{
        		tokensList.set(tokensList.size()-1, "is not "+token);
        	}
        	else
        		throw new InvalidSelectorException("Unexpected token after 'is not' operator : "+token);
        }
    	else
    	if (previous.equalsIgnoreCase("not"))
        {
    		if (token.equalsIgnoreCase("in") || 
	            token.equalsIgnoreCase("between") ||
	            token.equalsIgnoreCase("like") ||
	            token.equalsIgnoreCase("true") || 
                token.equalsIgnoreCase("false"))
	        {
                tokensList.set(tokensList.size()-1, "not "+token);
	        }
    		else
    			tokensList.add(token);
        		//throw new InvalidSelectorException("Unexpected token after 'not' operator : "+token);
        }
    	else
    		tokensList.add(token);
    }
    
    /**
     * Check if the tokenizer has more tokens
     */
    public boolean hasMoreTokens()
    {
        return tokenizerOffset < tokens.size();
    }
    
    /**
     * Get the next available token
     */
    public String nextToken()
    {
        if (hasMoreTokens())
            return tokens.get(tokenizerOffset++);
        return null;
    }
}
