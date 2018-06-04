package se.tink.backend.common.utils;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;

import se.tink.backend.common.utils.Iterators;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class IteratorsTest {
    @Test
    public void testCounter() {
        Iterator<Long> counter = Iterators.counter();
        Long count = new Long(0);
        for (int i = 0; i < 50; i++) {
            Assert.assertTrue(counter.hasNext());
            Assert.assertEquals(count, counter.next());
            count++;
        }
    }

    @Test
    public void testCounterCustomStart() {
        int start = 5;
        Iterator<Long> counter = Iterators.counter(start);
        Long count = new Long(start);
        for (int i = 0; i < 50; i++) {
            Assert.assertTrue(counter.hasNext());
            Assert.assertEquals(count, counter.next());
            count++;
        }
    }

    @Test
    public void testCounterCustomStartAndStep() {
        int stepSize = 2;
        int start = 5;
        Iterator<Long> counter = Iterators.counter(start, stepSize);
        Long count = new Long(start);
        for (int i = 0; i < 50; i++) {
            Assert.assertTrue(counter.hasNext());
            Assert.assertEquals(count, counter.next());
            count += stepSize;
        }
    }

    @Test
    public void testNegativeRange() {
        int stepSize = -2;
        int start = 10;
        int stop = 5;
        Iterator<Long> counter = Iterators.range(start, stop, stepSize);

        Assert.assertTrue(counter.hasNext());
        Assert.assertEquals(new Long(10), counter.next());

        Assert.assertTrue(counter.hasNext());
        Assert.assertEquals(new Long(8), counter.next());

        Assert.assertTrue(counter.hasNext());
        Assert.assertEquals(new Long(6), counter.next());

        Assert.assertFalse(counter.hasNext());

        boolean exception = false;
        try {
            counter.next();
        } catch (NoSuchElementException e) {
            // Expected
            exception = true;
        }
        Assert.assertTrue(exception);
    }

    @Test
    public void testPositiveRange() {
        int stepSize = 2;
        int start = 7;
        int stop = 12;
        Iterator<Long> counter = Iterators.range(start, stop, stepSize);

        Assert.assertTrue(counter.hasNext());
        Assert.assertEquals(new Long(7), counter.next());

        Assert.assertTrue(counter.hasNext());
        Assert.assertEquals(new Long(9), counter.next());

        Assert.assertTrue(counter.hasNext());
        Assert.assertEquals(new Long(11), counter.next());

        Assert.assertFalse(counter.hasNext());

        boolean exception = false;
        try {
            counter.next();
        } catch (NoSuchElementException e) {
            // Expected
            exception = true;
        }
        Assert.assertTrue(exception);
    }

    @Test
    public void testUntilIteratorEmptyList() {
        List<Integer> all = Lists.newArrayList();
        Predicate<Integer> moreThanFive = input -> input > 5;
        Assert.assertEquals(Lists.newArrayList(), Lists.newArrayList(Iterators.until(all.iterator(), moreThanFive)));
    }

    @Test
    public void testUntilIteratorNoMatch() {
        List<Integer> all = Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Predicate<Integer> moreThanFiveHundred = input -> input > 500;
        Assert.assertEquals(Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
                Lists.newArrayList(Iterators.until(all.iterator(), moreThanFiveHundred)));
    }

    @Test
    public void testUntilIteratorPredicate() {
        List<Integer> all = Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Predicate<Integer> moreThanFive = input -> input > 5;
        Assert.assertEquals(Lists.newArrayList(1, 2, 3, 4, 5),
                Lists.newArrayList(Iterators.until(all.iterator(), moreThanFive)));
    }
}
