package se.tink.backend.aggregation.workers.concurrency;

import java.util.NoSuchElementException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FIFODelegateTest {

    private FIFODelegate<Integer> ascendingIntegers;
    private FIFODelegate<Integer> emptyCollection;

    @Before
    public void setUp() {
        emptyCollection = new FIFODelegate<Integer>();

        ascendingIntegers = new FIFODelegate<Integer>();
        ascendingIntegers.add(0);
        ascendingIntegers.add(1);
        ascendingIntegers.add(2);

        Assert.assertEquals(3, ascendingIntegers.size());
    }

    @Test
    public void testRemoval() {
        for (Integer expected : new int[] {
                0, 1, 2
        }) {
            Integer nextToRemove = ascendingIntegers.peekNextRemove();
            Assert.assertEquals(expected, nextToRemove);
            Assert.assertEquals(3 - expected, ascendingIntegers.size());

            Integer removed = ascendingIntegers.remove();
            Assert.assertEquals(expected, removed);
            Assert.assertEquals(3 - expected - 1, ascendingIntegers.size());
        }
    }

    @Test
    public void testEviction() {
        for (Integer expected : new int[] {
                0, 1, 2
        }) {
            Integer nextToRemove = ascendingIntegers.peekNextEviction();
            Assert.assertEquals(expected, nextToRemove);
            Assert.assertEquals(3 - expected, ascendingIntegers.size());

            ascendingIntegers.evict();
            Assert.assertEquals(3 - expected - 1, ascendingIntegers.size());

            for (Integer element : ascendingIntegers) {
                // Make sure the correct element was evicted.

                Assert.assertNotEquals(nextToRemove, element);
            }
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void testPeekingOnNextEvictionOnEmptyFails() {
        emptyCollection.peekNextEviction();
    }

    @Test(expected = NoSuchElementException.class)
    public void testPeekingOnNextRemovalOnEmptyFails() {
        emptyCollection.peekNextRemove();
    }

    @Test(expected = NoSuchElementException.class)
    public void testRemovingOnEmptyFails() {
        emptyCollection.remove();
    }

    @Test(expected = NoSuchElementException.class)
    public void testEvictingOnEmptyFails() {
        emptyCollection.evict();
    }

}
