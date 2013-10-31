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

import net.timewalker.ffmq3.common.message.selector.expression.ConditionalExpression;
import net.timewalker.ffmq3.common.message.selector.expression.Identifier;
import net.timewalker.ffmq3.common.message.selector.expression.SelectorNode;
import net.timewalker.ffmq3.common.message.selector.expression.literal.BooleanLiteral;
import net.timewalker.ffmq3.common.message.selector.expression.literal.NullLiteral;
import net.timewalker.ffmq3.common.message.selector.expression.literal.NumericLiteral;
import net.timewalker.ffmq3.common.message.selector.expression.literal.StringLiteral;
import net.timewalker.ffmq3.common.message.selector.expression.literal.StringLiteralList;
import net.timewalker.ffmq3.common.message.selector.expression.operator.AndOperator;
import net.timewalker.ffmq3.common.message.selector.expression.operator.BetweenOperator;
import net.timewalker.ffmq3.common.message.selector.expression.operator.DivideOperator;
import net.timewalker.ffmq3.common.message.selector.expression.operator.EqualsOperator;
import net.timewalker.ffmq3.common.message.selector.expression.operator.GreaterThanOperator;
import net.timewalker.ffmq3.common.message.selector.expression.operator.GreaterThanOrEqualsOperator;
import net.timewalker.ffmq3.common.message.selector.expression.operator.InOperator;
import net.timewalker.ffmq3.common.message.selector.expression.operator.IsNotNullOperator;
import net.timewalker.ffmq3.common.message.selector.expression.operator.IsNullOperator;
import net.timewalker.ffmq3.common.message.selector.expression.operator.LessThanOperator;
import net.timewalker.ffmq3.common.message.selector.expression.operator.LessThanOrEqualsOperator;
import net.timewalker.ffmq3.common.message.selector.expression.operator.LikeOperator;
import net.timewalker.ffmq3.common.message.selector.expression.operator.MinusOperator;
import net.timewalker.ffmq3.common.message.selector.expression.operator.MultiplyOperator;
import net.timewalker.ffmq3.common.message.selector.expression.operator.NotBetweenOperator;
import net.timewalker.ffmq3.common.message.selector.expression.operator.NotEqualsOperator;
import net.timewalker.ffmq3.common.message.selector.expression.operator.NotInOperator;
import net.timewalker.ffmq3.common.message.selector.expression.operator.NotLikeOperator;
import net.timewalker.ffmq3.common.message.selector.expression.operator.NotOperator;
import net.timewalker.ffmq3.common.message.selector.expression.operator.OrOperator;
import net.timewalker.ffmq3.common.message.selector.expression.operator.SubstractOperator;
import net.timewalker.ffmq3.common.message.selector.expression.operator.SumOperator;
import net.timewalker.ffmq3.common.message.selector.expression.utils.StringUtils;

/**
 * <p>
 *  Implementation of a JMS message selector parser.
 *  The parser first uses a {@link MessageSelectorTokenizer} to tokenize the message selector
 *  expression, then creates a node tree from the token stream.
 * </p>
 */
public final class MessageSelectorParser
{    
    private static final String[] COMPARISON_OPERATORS = {
        "=","<>","<",">","<=",">="
    };
    
    private static final String[] SPECIAL_OPERATORS = {
        "in","not in","like","not like","between",
        "not between","is null","is not null",
        "is true","is false","is not true","is not false"
    };
    
    private MessageSelectorTokenizer tokenizer;
    private String currentToken;
    
    /**
     * Constructor
     */
    public MessageSelectorParser( String messageSelector ) throws InvalidSelectorException
    {
        this.tokenizer = new MessageSelectorTokenizer(messageSelector);
        readNextToken();
    }

    /**
     * Update the current token
     */
    private void readNextToken()
    {
        currentToken = tokenizer.nextToken();
    }
    
    /**
     * Test if all tokens were consumed
     */
    private boolean isEndOfExpression()
    {
        return currentToken == null;
    }
    
    /**
     * Parse the given message selector expression into a selector node tree
     */
    public SelectorNode parse() throws InvalidSelectorException
    {
        if (isEndOfExpression())
            return null;
        SelectorNode expr = parseExpression();
        if (!isEndOfExpression())
            throw new InvalidSelectorException("Unexpected token : "+currentToken);
        if (!(expr instanceof ConditionalExpression))    
            throw new InvalidSelectorException("Selector expression is not a boolean expression");
        
        return expr;
    }
    
    private boolean isLiteral( String token )
    {
        if (token.charAt(0) == '\'') // String literal
            return true;
        
        if (Character.isDigit(token.charAt(0))) // Number
            return true;
        
        return false;
    }
    
    private boolean isComparisonOperator( String token )
    {
        for (int n = 0 ; n < COMPARISON_OPERATORS.length ; n++)
            if (COMPARISON_OPERATORS[n].equals(token))
                return true;
        return false;
    }
    
    private boolean isSpecialOperator( String token )
    {
        for (int n = 0 ; n < SPECIAL_OPERATORS.length ; n++)
            if (SPECIAL_OPERATORS[n].equalsIgnoreCase(token))
                return true;
        return false;
    }
    
    private boolean isIdentifier( String token )
    {
        if (!Character.isJavaIdentifierStart(token.charAt(0)))
            return false;
        
        for (int n = 1 ; n < token.length() ; n++)
            if (!Character.isJavaIdentifierPart(token.charAt(n)))
                return false;
        
        // Reserved keywords
        if (token.equalsIgnoreCase("NULL") ||
            token.equalsIgnoreCase("TRUE") ||
            token.equalsIgnoreCase("FALSE") ||
            token.equalsIgnoreCase("NOT") ||
            token.equalsIgnoreCase("AND") ||
            token.equalsIgnoreCase("OR") ||
            token.equalsIgnoreCase("BETWEEN") ||
            token.equalsIgnoreCase("LIKE") ||
            token.equalsIgnoreCase("IN") ||
            token.equalsIgnoreCase("IS") ||
            token.equalsIgnoreCase("ESCAPE"))
            return false;
        
        return true;
    }
    
    private SelectorNode parseExpression() throws InvalidSelectorException
    {
        return parseOrExpression();
    }
    
    private SelectorNode parseSpecialConstructs() throws InvalidSelectorException
    {
        SelectorNode lNode = parseComparison();
        
        if (isSpecialOperator(currentToken))
        {
        	if (currentToken.equalsIgnoreCase("is null"))
            {
                readNextToken(); // skip operator
                return new IsNullOperator(lNode);
            }
            if (currentToken.equalsIgnoreCase("is not null"))
            {
                readNextToken(); // skip operator
                return new IsNotNullOperator(lNode);
            }
            if (currentToken.equalsIgnoreCase("is true"))
            {
                readNextToken(); // skip operator
                return new EqualsOperator(lNode,new BooleanLiteral(Boolean.TRUE));
            }
            if (currentToken.equalsIgnoreCase("is false"))
            {
                readNextToken(); // skip operator
                return new EqualsOperator(lNode,new BooleanLiteral(Boolean.FALSE));
            }
            if (currentToken.equalsIgnoreCase("is not true"))
            {
                readNextToken(); // skip operator
                return new EqualsOperator(lNode,new BooleanLiteral(Boolean.FALSE));
            }
            if (currentToken.equalsIgnoreCase("is not false"))
            {
                readNextToken(); // skip operator
                return new EqualsOperator(lNode,new BooleanLiteral(Boolean.TRUE));
            }
            if (currentToken.equalsIgnoreCase("in"))
            {
                readNextToken(); // skip operator
                return new InOperator(lNode, parseListConstruct());
            }
            if (currentToken.equalsIgnoreCase("not in"))
            {
                readNextToken(); // skip operator
                return new NotInOperator(lNode, parseListConstruct());
            }
            if (currentToken.equalsIgnoreCase("like"))
            {
            	readNextToken(); // skip operator            	
                return new LikeOperator(lNode, parsePatternExpression(), parseEscapeExpression());
            }
            if (currentToken.equalsIgnoreCase("not like"))
            {
                readNextToken(); // skip operator
                return new NotLikeOperator(lNode, parsePatternExpression(), parseEscapeExpression());
            }
            if (currentToken.equalsIgnoreCase("between"))
            {
                readNextToken(); // skip 'between'
                SelectorNode lowerBound = parseAdditiveExpression();
                if (isEndOfExpression())
                    throw new InvalidSelectorException("Unexpected end of expression");
                if (!currentToken.equalsIgnoreCase("and"))
                    throw new InvalidSelectorException("Expected an AND operator after lower bound in BETWEEN construct");
                readNextToken(); // Skip 'and'
                SelectorNode upperBound = parseAdditiveExpression();
                
                return new BetweenOperator(lNode, lowerBound, upperBound);
            }
            if (currentToken.equalsIgnoreCase("not between"))
            {
                readNextToken(); // skip 'between'
                SelectorNode lowerBound = parseAdditiveExpression();
                if (isEndOfExpression())
                    throw new InvalidSelectorException("Unexpected end of expression");
                if (!currentToken.equalsIgnoreCase("and"))
                    throw new InvalidSelectorException("Expected an AND operator after lower bound in BETWEEN construct");
                readNextToken(); // Skip 'and'
                SelectorNode upperBound = parseAdditiveExpression();
                
                return new NotBetweenOperator(lNode, lowerBound, upperBound);
            }
        }
        
        return lNode;
    }
    
    private SelectorNode parseListConstruct() throws InvalidSelectorException
    {
        if (isEndOfExpression()) 
            throw new InvalidSelectorException("Unexpected end of expression");

        if (!currentToken.equals("("))
            throw new InvalidSelectorException("Expected an open parenthesis after IN operator");
        readNextToken(); // Skip (
        
        List items = new ArrayList();
        while (!isEndOfExpression() && !currentToken.equals(")"))
        {
            SelectorNode item = parseBaseExpression();
            items.add(item);
            
            if (isEndOfExpression()) 
                throw new InvalidSelectorException("Unexpected end of expression");
            if (currentToken.equals(","))
            {
                readNextToken(); // Skip ,
                continue;
            }
            else
            if (currentToken.equals(")"))
            { 
                readNextToken(); // Skip )
                break;
            }
            else
                throw new InvalidSelectorException("Unexpected token in list : "+currentToken);
        }
        
        SelectorNode[] itemList = (SelectorNode[])items.toArray(new SelectorNode[items.size()]);
        return new StringLiteralList(itemList);
    }
    
    private SelectorNode parsePatternExpression() throws InvalidSelectorException
    {
        if (isEndOfExpression())
        	throw new InvalidSelectorException("Expected pattern operand after LIKE operator");

        SelectorNode patternNode = parseBaseExpression();
        if (!(patternNode instanceof StringLiteral))
        	throw new InvalidSelectorException("pattern operand of LIKE operator should be a string literal");

        return patternNode;
    }
    
    private SelectorNode parseEscapeExpression() throws InvalidSelectorException
    {
        if (!isEndOfExpression() && currentToken.equalsIgnoreCase("escape"))
        {
            readNextToken(); // skip escape
            SelectorNode escapeNode = parseBaseExpression();
            if (!(escapeNode instanceof StringLiteral))
            	throw new InvalidSelectorException("escape operand of LIKE operator should be a string literal");
            String value = (String)((StringLiteral)escapeNode).getValue();
            if (value.length() != 1)
            	throw new InvalidSelectorException("escape operand of LIKE operator must contain one and only one character");
            
            return escapeNode;
        }
        return null;
    }
    
    private SelectorNode parseAndExpression() throws InvalidSelectorException
	{
    	SelectorNode lNode = parseSpecialConstructs();

		while (!isEndOfExpression() && currentToken.equalsIgnoreCase("and"))
		{
			readNextToken(); // skip 'and'
			lNode = new AndOperator(lNode, parseSpecialConstructs());
		}

		return lNode;
	}

	private SelectorNode parseOrExpression() throws InvalidSelectorException
	{
		SelectorNode lNode = parseAndExpression();

		while (!isEndOfExpression() && currentToken.equalsIgnoreCase("or"))
		{
			readNextToken(); // skip 'or'
			lNode = new OrOperator(lNode, parseAndExpression());
		}

		return lNode;
	}
    
    private SelectorNode parseComparison() throws InvalidSelectorException
    {
        SelectorNode lNode = parseAdditiveExpression();

        if (!isEndOfExpression() && isComparisonOperator(currentToken))
        {
            if (currentToken.equals("="))
            {
                readNextToken(); // skip comparison operator
                return new EqualsOperator(lNode, parseAdditiveExpression());
            }
            if (currentToken.equals("<>"))
            {
                readNextToken(); // skip comparison operator
                return new NotEqualsOperator(lNode, parseAdditiveExpression());
            }
            if (currentToken.equals("<"))
            {
                readNextToken(); // skip comparison operator
                return new LessThanOperator(lNode, parseAdditiveExpression());
            }
            if (currentToken.equals(">"))
            {
                readNextToken(); // skip comparison operator
                return new GreaterThanOperator(lNode, parseAdditiveExpression());
            }
            if (currentToken.equals("<="))
            {
                readNextToken(); // skip comparison operator
                return new LessThanOrEqualsOperator(lNode, parseAdditiveExpression());
            }
            if (currentToken.equals(">="))
            {
                readNextToken(); // skip comparison operator
                return new GreaterThanOrEqualsOperator(lNode, parseAdditiveExpression());
            }
        }
        
        return lNode;
    }
    
    private SelectorNode parseAdditiveExpression() throws InvalidSelectorException
    {
        SelectorNode lNode = parseMultiplicativeExpression();

        while (!isEndOfExpression())
        {
            if (currentToken.equals("+"))
            {
                readNextToken(); // skip '+'
                lNode = new SumOperator(lNode, parseMultiplicativeExpression());
            }
            else if (currentToken.equals("-"))
            {
                readNextToken(); // skip '-'
                lNode = new SubstractOperator(lNode, parseMultiplicativeExpression());
            }
            else break;
        }

        return lNode;
    }
    
    private SelectorNode parseMultiplicativeExpression() throws InvalidSelectorException
    {
        SelectorNode lNode = parseUnaryExpression();

        while (!isEndOfExpression())
        {
            if (currentToken.equals("*"))
            {
                readNextToken(); // skip '*'
                lNode = new MultiplyOperator(lNode, parseUnaryExpression());
            }
            else if (currentToken.equals("/"))
            {
                readNextToken(); // skip '/'
                lNode = new DivideOperator(lNode, parseUnaryExpression());
            }
            else break;
        }

        return lNode;
    }
    
    private SelectorNode parseUnaryExpression() throws InvalidSelectorException
    {
        if (isEndOfExpression()) 
            throw new InvalidSelectorException("Unexpected end of expression");

        if (currentToken.equals("-"))
        {
            readNextToken(); // skip '-'
            return new MinusOperator(parseBaseExpression());
        }
        else if (currentToken.equalsIgnoreCase("not"))
        {
            readNextToken(); // skip 'not'
            return new NotOperator(parseBaseExpression());
        }

        return parseBaseExpression();
    }
    
    private SelectorNode parseBaseExpression() throws InvalidSelectorException
    {
        if (isEndOfExpression()) 
            throw new InvalidSelectorException("Unexpected end of expression");

        if (currentToken.equals("("))
            return parseGroupExpression();
        
        if (isLiteral(currentToken))
        {
            if (currentToken.startsWith("'"))
                return parseStringConstant();
            else
                return parseNumericConstant();
        }
        
        // TRUE,FALSE and NULL keywords
        if (currentToken.equalsIgnoreCase("true"))
        {
            readNextToken(); // skip 'true'
            return new BooleanLiteral(Boolean.TRUE);
        }
        if (currentToken.equalsIgnoreCase("false"))
        {
            readNextToken(); // skip 'false'
            return new BooleanLiteral(Boolean.FALSE);
        }
        if (currentToken.equalsIgnoreCase("null"))
        {
            readNextToken(); // skip 'null'
            return new NullLiteral();
        }
        if (currentToken.equalsIgnoreCase("not true"))
        {
            readNextToken(); // skip 'not true'
            return new BooleanLiteral(Boolean.FALSE);
        }
        if (currentToken.equalsIgnoreCase("not false"))
        {
            readNextToken(); // skip 'not false'
            return new BooleanLiteral(Boolean.TRUE);
        }
        
        if (isIdentifier(currentToken))
            return parseIdentifier();

        throw new InvalidSelectorException("Unexpected token : "+currentToken);
    }
    
    private SelectorNode parseGroupExpression() throws InvalidSelectorException
    {
        readNextToken(); // skip '('
        SelectorNode lExpression = parseExpression();
        
        if (isEndOfExpression())
            throw new InvalidSelectorException("Unexpected end of sub-expression");
        if (!currentToken.equals(")"))
            throw new InvalidSelectorException("Unexpected extra token at end of sub-expression : "+currentToken);
        readNextToken(); // skip ')'
        
        return lExpression;
    }

    private SelectorNode parseIdentifier() throws InvalidSelectorException
    {
        String lName = currentToken;
        
        // Check headers restrictions
        if (lName.startsWith("JMS"))
        {
        	if (!lName.startsWith("JMSX") &&
                !lName.startsWith("JMS_"))
        	{
	        	if (!(lName.equals("JMSDeliveryMode") ||
	        		  lName.equals("JMSPriority") ||
	        		  lName.equals("JMSMessageID") ||
	        		  lName.equals("JMSTimestamp") ||
	        		  lName.equals("JMSCorrelationID") ||
	        		  lName.equals("JMSType")))
	        		throw new InvalidSelectorException("Header property "+lName+" cannot be used in a message selector");
        	}
        }
        
        readNextToken();
        return new Identifier(lName);
    }

    private SelectorNode parseStringConstant()
    {
        String lValue = currentToken.substring(1,currentToken.length()-1); // strip starting and ending quote
        lValue = StringUtils.replaceDoubleSingleQuotes(lValue); // replace quotes inside string
        readNextToken(); // skip string constant
        return new StringLiteral(lValue);
    }

    private SelectorNode parseNumericConstant() throws InvalidSelectorException
    {
        String value = currentToken;
        readNextToken(); // skip numeric constant
        return new NumericLiteral(value);
    }
}
