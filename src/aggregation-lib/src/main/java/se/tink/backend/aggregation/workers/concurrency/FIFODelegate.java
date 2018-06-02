package se.tink.backend.aggregation.workers.concurrency;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import se.tink.backend.common.concurrency.CappedBlockingQueue.Delegate;

/**
 * Always removes and evicts the oldest element.
 * 
 * @param <E>
 *            the type of element it holds.
 */
public class FIFODelegate<E> implements Delegate<E> {

    private final Deque<E> delegate = new LinkedList<>();

    @Override
    public void add(E e) {
        delegate.addLast(e);
    }

    @Override
    public E remove() throws NoSuchElementException {
        return delegate.removeFirst();
    }

    @Override
    public void evict() throws NoSuchElementException {
        remove();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public Iterator<E> iterator() {
        return delegate.iterator();
    }

    @Override
    public E peekNextEviction() {
        return delegate.getFirst();
    }

    @Override
    public E peekNextRemove() {
        return delegate.getFirst();
    }

}
