package net.timewalker.ffmq3.test.utils.concurrent;

import junit.framework.TestCase;
import net.timewalker.ffmq3.utils.concurrent.CopyOnWriteList;

/**
 * CopyOnWriteListTest
 */
public class CopyOnWriteListTest extends TestCase
{
    protected boolean failed;
    
    public void testFirstGen()
    {
        CopyOnWriteList cowList = new CopyOnWriteList();
        
        assertEquals(0, cowList.getShareLevel());
        cowList.add("v1");
        cowList.add("v2");
        cowList.add("v3");
        assertEquals(0, cowList.getShareLevel());
        assertEquals(3, cowList.size());
        
        CopyOnWriteList child1 = cowList.fastCopy();
        CopyOnWriteList child2 = cowList.fastCopy();
        CopyOnWriteList child3 = cowList.fastCopy();
        assertEquals(3, cowList.getShareLevel());
        
        child1.add("v4");
        assertEquals(2, cowList.getShareLevel());
        assertEquals(3, cowList.size());
        assertEquals(0, child1.getShareLevel());
        assertEquals(2, child2.getShareLevel());
        assertEquals(2, child3.getShareLevel());
        assertEquals(4, child1.size());
        assertEquals(3, child2.size());
        assertEquals(3, child3.size());
    }
    
    public void testMultipleGen()
    {
        CopyOnWriteList cowList = new CopyOnWriteList();
        
        assertEquals(0, cowList.getShareLevel());
        cowList.add("v1");
        cowList.add("v2");
        cowList.add("v3");
        assertEquals(0, cowList.getShareLevel());
        assertEquals(3, cowList.size());
        
        CopyOnWriteList child1 = cowList.fastCopy();
        CopyOnWriteList child2 = child1.fastCopy();
        CopyOnWriteList child3 = child2.fastCopy();
        assertEquals(3, cowList.getShareLevel());
        
        child3.add("v4");
        assertEquals(2, cowList.getShareLevel());
        assertEquals(3, cowList.size());
        assertEquals(2, child1.getShareLevel());
        assertEquals(2, child2.getShareLevel());
        assertEquals(0, child3.getShareLevel());
        assertEquals(3, child1.size());
        assertEquals(3, child2.size());
        assertEquals(4, child3.size());
    }
    
    public void testConcurrency() throws Exception
    {
        final CopyOnWriteList cowList = new CopyOnWriteList();
        cowList.add("v1");
        cowList.add("v2");
        cowList.add("v3");
        assertEquals(0, cowList.getShareLevel());
        assertEquals(3, cowList.size());

        Thread[] threads = new Thread[10];
        for(int n=0;n<threads.length;n++)
            threads[n] = new Thread() {
            /*
             * (non-Javadoc)
             * @see java.lang.Thread#run()
             */
            public void run() {
                try
                {
                    for (int i = 0 ; i < 100000 ; i++)
                    {
                        CopyOnWriteList copy = cowList.fastCopy();
                        int size = copy.size();
                        for (int j = 0 ; j < size ; j++)
                            copy.get(j);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    failed = true;
                }
            }
        };
        for(int n=0;n<threads.length;n++)
            threads[n].start();
        
        for(int n=0;n<100000;n++)
        {
            cowList.add("v4");
            cowList.remove(3);
        }
        
        for(int n=0;n<threads.length;n++)
            threads[n].join();
        
        assertFalse(failed);
    }
}
