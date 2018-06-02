package se.tink.backend.common.concurrency;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SortedDelegateTest {
    private SortedDelegate<Integer, Integer> sortedDelegate;

    @Before
    public void setUp() throws Exception {
        sortedDelegate = new SortedDelegate<>(Ordering.explicit(0,1,2,3), t -> t/2);
        sortedDelegate.add(0);
        sortedDelegate.add(4);
        sortedDelegate.add(2);
        sortedDelegate.add(6);
        sortedDelegate.add(3);
        System.out.println("sortedDelegate = " + sortedDelegate);
    }

    @Test
    public void testSize() {
        Assert.assertEquals(5, sortedDelegate.size());
    }
    
    @Test
    public void testRemove() {
        testRemoveElement(0, 4);
        testRemoveElement(2, 3);
        testRemoveElement(3, 2);
        testRemoveElement(4, 1);
        testRemoveElement(6, 0);
    }

    private void testRemoveElement(int expectedValue, long expectedSize) {
        Assert.assertEquals(expectedValue, (long)sortedDelegate.peekNextRemove());
        Assert.assertEquals(expectedValue, (long)sortedDelegate.remove());
        Assert.assertEquals(expectedSize, sortedDelegate.size());
    }

    @Test
    public void testEvict() throws Exception {
        testEvictElement(6, 4);
        testEvictElement(4, 3);
        testEvictElement(3, 2);
        testEvictElement(2, 1);
        testEvictElement(0, 0);
    }

    private void testEvictElement(int expectedValue, int expectedSize) {
        Assert.assertEquals((long)expectedValue, (long)sortedDelegate.peekNextEviction());
        sortedDelegate.evict();
        Assert.assertEquals(expectedSize, sortedDelegate.size());
    }

    @Test
    public void testIterator() throws Exception {
        final ImmutableList<Integer> testData = ImmutableList.copyOf(sortedDelegate);
        Assert.assertEquals(ImmutableList.of(0,2,3,4,6), testData);
    }
}
