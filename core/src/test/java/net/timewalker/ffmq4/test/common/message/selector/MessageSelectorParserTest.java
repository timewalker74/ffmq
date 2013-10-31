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

package net.timewalker.ffmq3.test.common.message.selector;

import javax.jms.InvalidSelectorException;
import javax.jms.JMSException;
import javax.jms.Message;

import junit.framework.TestCase;
import net.timewalker.ffmq3.common.message.MessageSelector;
import net.timewalker.ffmq3.common.message.TextMessageImpl;
import net.timewalker.ffmq3.common.message.selector.MessageSelectorParser;
import net.timewalker.ffmq3.common.message.selector.expression.SelectorNode;

/**
 * MessageSelectorParserTest
 */
public class MessageSelectorParserTest extends TestCase
{
	private static final SelectorUseCase[] VALID_SELECTORS = {
		
		// White space handling
	    new SelectorUseCase("   ",false),
		new SelectorUseCase("iProp\t=\t1",true),
		new SelectorUseCase("iProp\n=\n1",true),
		new SelectorUseCase("iProp\r=\r1",true),
		
		// No whitespace
		new SelectorUseCase("(iProp=1)and(iProp < 2)",true),
		new SelectorUseCase("(iProp=1)and(iProp < 2)or(sProp='s')",true),
		
		// Missing property
		new SelectorUseCase("invalidProp is null",true),
		new SelectorUseCase("invalidProp is not null",false),
		new SelectorUseCase("invalidProp is true",false),
		new SelectorUseCase("INvalidProp > 2",false),
		
		// Types
		new SelectorUseCase("bProp",true),
		new SelectorUseCase("bProp=true",true),
		new SelectorUseCase("iProp=1",true),
		new SelectorUseCase("iProp2=2",true),
		new SelectorUseCase("lProp=3",true),
		new SelectorUseCase("fProp=1.2300000190734863",true),
		new SelectorUseCase("dProp=4.56",true),
		new SelectorUseCase("sProp='foobar'",true),
		
		// Invalid types
		new SelectorUseCase("sProp = 2",false),
		new SelectorUseCase("sProp > 2",false),
		new SelectorUseCase("sProp <> 2",true),
		
		// Comparators
		new SelectorUseCase("fProp<>1.2300000190734863",false),
		new SelectorUseCase("dProp<>4.56",false),
		new SelectorUseCase("fProp>1.2",true),
		new SelectorUseCase("dProp>4.01",true),
		new SelectorUseCase("fProp<2.5",true),
		new SelectorUseCase("dProp<4.57",true),
		new SelectorUseCase("fProp>=1.2",true),
		new SelectorUseCase("dProp>=4.01",true),
		new SelectorUseCase("fProp<=2.5",true),
		new SelectorUseCase("dProp<=4.57",true),
		new SelectorUseCase("sProp<>'foobar'",false),
		new SelectorUseCase("bProp<>true",false),
		new SelectorUseCase("iProp >= undefined",false),
		new SelectorUseCase("iProp <= undefined",false),
		new SelectorUseCase("undefined >= iProp",false),
        new SelectorUseCase("undefined <= iProp",false),
        new SelectorUseCase("iProp > undefined",false),
        new SelectorUseCase("iProp < undefined",false),
        new SelectorUseCase("undefined > iProp",false),
        new SelectorUseCase("undefined < iProp",false),
		new SelectorUseCase("iProp <= sProp",false),
		new SelectorUseCase("sProp <= iProp",false),
		new SelectorUseCase("iProp >= sProp",false),
        new SelectorUseCase("sProp >= iProp",false),
        new SelectorUseCase("iProp < sProp",false),
        new SelectorUseCase("sProp < iProp",false),
        new SelectorUseCase("iProp > sProp",false),
        new SelectorUseCase("sProp > iProp",false),
        
		// Arithmetic
		new SelectorUseCase("0 < -1*2/(0.6+3)+1",true),
		new SelectorUseCase("(dProp+2)*1.5 > 6.333",true),
		new SelectorUseCase("4.3-2.6>0",true),
		new SelectorUseCase("-1/-2.0 > 0",true),
		new SelectorUseCase("-(-1) = 1",true),
		new SelectorUseCase("2--1 = 3",true),
		new SelectorUseCase("1*2*3/4.0 = 1.5",true),
		new SelectorUseCase("24/2/3 = 4",true),
		new SelectorUseCase("null between 1 and 3",false),
		new SelectorUseCase("undefined between 1 and 3",false),
		new SelectorUseCase("2 between undefined and 3",false),
		new SelectorUseCase("2 between 1 and undefined",false),
		new SelectorUseCase("(1+2+3)=6",true),
		new SelectorUseCase("-undefined > 0",false),
		new SelectorUseCase("undefined <> 3",false),
		new SelectorUseCase("undefined/3 = 0",false),
		new SelectorUseCase("3/undefined = 0",false),
		new SelectorUseCase("undefined*3 = 0",false),
        new SelectorUseCase("3*undefined = 0",false),
        new SelectorUseCase("undefined+3 = 0",false),
        new SelectorUseCase("3+undefined = 0",false),
        new SelectorUseCase("undefined-3 = 0",false),
        new SelectorUseCase("3-undefined = 0",false),
        new SelectorUseCase("3/0=1",false),
        new SelectorUseCase("3/0.0=1",false),
        new SelectorUseCase("3>6",false),
        new SelectorUseCase("-fProp<0",true),
        
		// Explicit boolean constructs
		new SelectorUseCase("true or false",true),
		new SelectorUseCase("true",true),
		new SelectorUseCase("false",false),
		new SelectorUseCase("TRUE",true),
		new SelectorUseCase("FALSE",false),
		new SelectorUseCase("(true)",true),
		new SelectorUseCase("(false)",false),
		new SelectorUseCase("not true",false),
		new SelectorUseCase("not false",true),
		new SelectorUseCase("not bProp",false),
		new SelectorUseCase("not (not bProp)",true),
		
		// Boolean and nulls
		new SelectorUseCase("(true and undefined)  = false",false),
		new SelectorUseCase("(undefined and true)  = false",false),
		new SelectorUseCase("(false and undefined) = false",true),
		new SelectorUseCase("(undefined and false) = false",true),
		new SelectorUseCase("(true or undefined) = true",true),
		new SelectorUseCase("(undefined or true) = true",true),
		new SelectorUseCase("(false or undefined) = false",false),
		new SelectorUseCase("(undefined or false) = false",false),
		new SelectorUseCase("(not undefined) = false",false),
		
		// Quoting
		new SelectorUseCase("sProp <> 'foo''bar'",true),
		new SelectorUseCase("sProp like 'foo''bar'",false),
		new SelectorUseCase("sProp like 'foo''bar' escape ''''",true),
		
		// Special constructs
		new SelectorUseCase("bProp is true",true),
		new SelectorUseCase("bProp is false",false),
		new SelectorUseCase("bProp is not true",false),
		new SelectorUseCase("bProp is not false",true),
		new SelectorUseCase("bProp is null",false),
		new SelectorUseCase("bProp is not null",true),
		new SelectorUseCase("sProp not in ('aa','bb')",true),
		new SelectorUseCase("iProp not between 4 and 6",true),
		new SelectorUseCase("iProp not between -10 and -2",true),
		new SelectorUseCase("sProp not like 'foo%'",false),
		new SelectorUseCase("sProp IN ('fff','foobar')",true),
		new SelectorUseCase("iProp between 1 and 2",true),
		new SelectorUseCase("sProp like 'foo%'",true),
		new SelectorUseCase("(iProp between 1 and 2) is true",true),
		new SelectorUseCase("(iProp between 3-2 and 2*iProp2) is true",true),
		new SelectorUseCase("undefined like 'foo'",false),
		new SelectorUseCase("undefined in ('1','2')",false),

        // Operator precedence
		new SelectorUseCase("iProp = 3 and iProp2=5 or iProp=2",false),
		new SelectorUseCase("iProp = 2 or iProp = 3 and iProp2=5",false),
		new SelectorUseCase("iProp = 3 and (foo=5 or iProp=2)",false),
		new SelectorUseCase("(iProp = 2 or iProp = 3) and iProp2=5",false),
		new SelectorUseCase("iProp2=2 and sProp in ('1','2') or iProp2=3",false),

		// Headers
		new SelectorUseCase("JMSDeliveryMode='PERSISTENT'",false),
		new SelectorUseCase("JMSPriority=2",false),
		new SelectorUseCase("JMSMessageID='ID:FOO'",false),
		new SelectorUseCase("JMSTimestamp=12345",false),
		new SelectorUseCase("JMSCorrelationID='FOOBAR'",false),
		new SelectorUseCase("JMSType='TEST'",false),
		
		// Restricted headers
		new SelectorUseCase("JMSXFoo is null",true),
	    new SelectorUseCase("JMS_Foo is null",true)
    };
    
    private static final String[] INVALID_SELECTORS = {

        // Invalid syntax
        "1 = 3 2",
        "'a'",
        "1foo = 6",
        "+2 > 1",
        ".foo =3",
        "foo is 3",
        "foo is not 4",
        "foo in ('1',2,'3'",
        "1 + ( 3*2",
        "1 + ( 3*2 (",
        "1 + ( 3*2 foo",
        
    	// Invalid quoting
    	"color = 'foo'bar'",
    	"color = 'foo'''bar'",
    	"color like 'foobar' escape '''",
    	
    	// Special constructs
    	"iProp between 1 or 2",
    	"iProp not between 1 or 2",
    	"not between 1 and 2",
    	"between 1 and 2",
    	"2 not between",
    	"2 not between 1",
    	"sProp like",
    	"sProp like 1",
    	"sProp like 'foo' escape ''",
    	"sProp like 'foo' escape 'xx'",
    	"sProp not like",
    	"not like 'foo'",
    	"between 'foo'",
    	"test in",
    	"test in foo",
    	"test in ('1',foo 3)",
    	"test not in",
    	"in 1",
    	"not in 1",
    	"3 is null",
    	"3 is not null",
    	"foo like 'bar' escape",
    	"foo like 'bar' escape 3",
    	"3 like 'test'",
    	"3 in ('1','2')",
    	"sProp like 'test' escape '22'",
    	
    	// Restricted headers
    	"JMSExpiration=123",
    	"JMSFoo=123",
    	
    	// Invalid types
    	"true and 3",
    	"3 > true",
    	"3 + true",
    	
    	// Invalid arithmetic
    	"'foo'*3",
    	"-'a'",
    	"3 and true",
    	"3 > 'foo'",
    	"'foo' <= 4",
    	"'foo' between 1 and 2",
    	"2 between 1 and 'foo'",
    	"2 between 'foo' and 3",
    	"2 between",
    	"2 between 3",
    	"2 between 3 and"
    };
    
    private static final String[] FAIL_ON_EVAL_SELECTORS = {
        "false or iProp",
        "3/sProp = 1",
        "iProp like 'test'",
        "sProp like iProp",
        "sProp like undefined",
        "sProp in (undefined,'2')",
        "sProp in ('2',null)",
        "sProp in ('2',iProp)",
        "sProp in (1,2)",
        "undefined in (1,2)",
    };
    
    private Message getTestMessage() throws Exception
    {
    	Message msg = new TextMessageImpl();
    	msg.setStringProperty("sProp", "foobar");
    	msg.setIntProperty("iProp", 1);
    	msg.setIntProperty("iProp2", 2);
    	msg.setIntProperty("lProp", 3);
    	msg.setFloatProperty("fProp", 1.23f);
    	msg.setDoubleProperty("dProp", 4.56);
    	msg.setBooleanProperty("bProp", true);
    	return msg;
    }
    
    public void testValidParse() throws Exception
    {
    	System.out.println("-------------------------------------------------");
        for (int n = 0 ; n < VALID_SELECTORS.length; n++)
        {
            System.out.print("TESTING valid ["+VALID_SELECTORS[n]+"] : ");
            
            MessageSelector selector = new MessageSelector(VALID_SELECTORS[n].getSelector());
            System.out.println(selector);
            boolean matches = selector.matches(getTestMessage());
            assertEquals(VALID_SELECTORS[n].getSelector(),VALID_SELECTORS[n].shouldMatch(), matches);
            
            String render1 = selector.toString();
            selector = new MessageSelector(render1);
            matches = selector.matches(getTestMessage());
            assertEquals(VALID_SELECTORS[n].shouldMatch(), matches);
            
            String render2 = selector.toString();
            if (!render1.equals(render2))
            	fail("["+render1+"] <> ["+render2+"]");
        }
        System.out.println("-------------------------------------------------");
    }

    public void testInvalidParse() throws Exception
    {
    	System.out.println("-------------------------------------------------");
        for (int n = 0 ; n < INVALID_SELECTORS.length; n++)
        {
            System.out.print("TESTING invalid ["+INVALID_SELECTORS[n]+"] ");
            try
            {
            	SelectorNode node = new MessageSelectorParser(INVALID_SELECTORS[n]).parse();
            	System.out.println(node);
	            fail("Should have failed : "+INVALID_SELECTORS[n]);
            }
            catch (InvalidSelectorException e)
            {
            	System.out.println(e.getMessage());
            }
        }
        System.out.println("-------------------------------------------------");
    }
    
    public void testFailOnEval() throws Exception
    {
        System.out.println("-------------------------------------------------");
        for (int n = 0 ; n < FAIL_ON_EVAL_SELECTORS.length; n++)
        {
            System.out.print("TESTING fail on eval ["+FAIL_ON_EVAL_SELECTORS[n]+"] ");
            try
            {
                MessageSelector selector = new MessageSelector(FAIL_ON_EVAL_SELECTORS[n]);
                selector.matches(getTestMessage());
                
                fail("Should have failed : "+FAIL_ON_EVAL_SELECTORS[n]);
            }
            catch (JMSException e)
            {
                System.out.println(e.getMessage());
            }
        }
        System.out.println("-------------------------------------------------");
    }
    
    private static class SelectorUseCase
    {
    	private String selector;
    	private boolean shouldMatch;
    	
    	/**
		 * Constructor 
		 */
		public SelectorUseCase( String selector , boolean shouldMatch )
		{
			this.selector = selector;
			this.shouldMatch = shouldMatch;
		}
		
		/**
		 * @return the selector
		 */
		public String getSelector()
		{
			return selector;
		}
		
		public boolean shouldMatch()
		{
			return shouldMatch;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return selector;
		}
    }
}
