package net.timewalker.ffmq4.test.storage.data.impl;

import net.timewalker.ffmq4.storage.data.impl.InMemoryLinkedDataStore;
import junit.framework.TestCase;

/**
 * InMemoryLinkedObjectStoreTest
 */
public class InMemoryLinkedObjectStoreTest extends TestCase
{
    private static final int MSG_COUNT = 1000; 
    
    public void testStore() throws Exception
    {
        InMemoryLinkedDataStore store = new InMemoryLinkedDataStore("test",16,10000);
        
        assertEquals(0, store.size());
        
        int previous = -1;
        for (int n = 0 ; n < MSG_COUNT ; n++)
        {
            previous = store.store("test_"+n, previous);
            if (previous == -1)
                throw new IllegalStateException("No space left !");
        }
        assertEquals(MSG_COUNT, store.size());
        store.commitChanges();
        
        int count = 0;
        int current = store.first();
        while (current != -1)
        {
            /*Object data = */store.retrieve(current);
            //System.out.println(data);
            current = store.next(current);
            count++;
        }
        assertEquals(MSG_COUNT, count);
        
        // Delete half entries
        count = 0;
        current = store.first();
        while (current != -1)
        {
            int next = store.next(current);
            if (next != -1)
                next = store.next(next);
            
            store.delete(current);
            count++;
            
            current = next;
        }
        assertEquals(MSG_COUNT-count, store.size());
        
        int pos = -1;
        for(int n=0;n<count;n++)
        {
            previous = store.store("other_test_"+n, pos);
            if (previous == -1)
                throw new IllegalStateException("No space left !");
            
            pos = store.next(previous);
            if (pos != -1)
                pos = store.next(pos);
        }
        store.commitChanges();
        assertEquals(MSG_COUNT, store.size());
        
        //System.out.println(store);
        
        count = 0;
        current = store.first();
        while (current != -1)
        {
            /*Object data = */store.retrieve(current);
            //System.out.println(data);
            current = store.next(current);
            count++;
        }
        assertEquals(MSG_COUNT, count);
 
        store.close();
    }
}
