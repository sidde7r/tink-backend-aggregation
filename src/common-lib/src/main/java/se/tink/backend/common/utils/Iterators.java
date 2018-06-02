package se.tink.backend.common.utils;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.PeekingIterator;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Iterators {
    private static class StepIterator extends UnmodifiableIterator<Long> {

        private long current;
        private final long step;

        public StepIterator(long start, long step) {
            Preconditions.checkArgument(step != 0, "Step must not be zero.");

            this.current = start;
            this.step = step;
        }

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public Long next() {
            final long result = current;
            current += step;
            return result;
        }

    }

    private static class UntilIterator<T> implements Iterator<T> {

        private PeekingIterator<T> iterator;
        private Predicate<T> shouldStopPredicate;

        public UntilIterator(Iterator<T> iterator, Predicate<T> shouldStopPredicate) {
            this.iterator = com.google.common.collect.Iterators.peekingIterator(iterator);
            this.shouldStopPredicate = shouldStopPredicate;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext() && !shouldStopPredicate.apply(iterator.peek());
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return iterator.next();
        }

        @Override
        public void remove() {
            iterator.remove();
        }

    }

    /**
     * An iterator over all integer values, starting from 0 stepping with 1.
     * 
     * @return an iterator.
     */
    public static Iterator<Long> counter() {
        return counter(0);
    }

    /**
     * An iterator over all integer values, starting from start stepping with 1.
     * 
     * @return an iterator.
     */
    public static Iterator<Long> counter(long start) {
        return counter(start, 1);
    }

    /**
     * An iterator over all integer values, starting from 0 stepping with step.
     * 
     * @return an iterator.
     */
    public static Iterator<Long> counter(long start, long step) {
        return new StepIterator(start, step);
    }

    /**
     * An integer iterator that iterates over a range of integers.
     * 
     * @param start
     *            first value of the iterator.
     * @param limit
     *            for positive step, the upper limit. For negative step, the lower limit.
     * @param step
     *            the step size. Can be negative.
     * @return
     */
    public static Iterator<Long> range(long start, final long limit, final long step) {
        if (step > 0) {
            Preconditions.checkArgument(start <= limit);
        } else {
            Preconditions.checkArgument(start >= limit);
        }

        return until(new StepIterator(start, step), input -> {
            if (step > 0) {
                return input >= limit;
            } else {
                return input <= limit;
            }
        });
    }

    /**
     * Iterate until a predicate is true. Does _not_ include the element on which the predicate is true.
     * 
     * @param iterator
     *            the source iterator.
     * @param stopPredicate
     *            the predicate whether we've reached the end of an iterator.
     * @return a capped iterator based on a predicate.
     */
    public static <T> Iterator<T> until(Iterator<T> iterator, Predicate<T> shouldStopPredicate) {
        return new UntilIterator<>(iterator, shouldStopPredicate);
    }
}
