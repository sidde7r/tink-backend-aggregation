package se.tink.backend.common.concurrency;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import se.tink.backend.common.concurrency.CappedBlockingQueue.Delegate;

public class SortedDelegate<K, E> implements Delegate<E> {

    private final Function<E, K> keyFunction;
    private TreeMap<K, LinkedList<E>> delegate;

    public SortedDelegate(Comparator<K> comparator, Function<E, K> keyFunction) {
        this.delegate = Maps.newTreeMap(comparator);
        this.keyFunction = keyFunction;
    }

    @Override
    public void add(E e) {
        K bucket = keyFunction.apply(e);

        LinkedList<E> container = delegate.get(bucket);
        if (container == null) {
            container = Lists.newLinkedList();
            delegate.put(bucket, container);
        }

        container.add(e);
    }

    @Override
    public E remove() throws NoSuchElementException {
        if (delegate.size() == 0) {
            throw new NoSuchElementException();
        }
        final Iterator<Map.Entry<K, LinkedList<E>>> iterator = delegate.entrySet().iterator();
        final LinkedList<E> container = iterator.next().getValue();
        E element = container.getFirst();
        container.removeFirst();

        if (container.size() == 0) {
            iterator.remove();
        }

        return element;
    }

    @Override
    public int size() {
        return delegate.values().stream().mapToInt(LinkedList::size).sum();
    }

    @Override
    public Iterator<E> iterator() {
        // Could have done a `delegate.values().stream().flatMap(LinkedList::stream).iterator();` below, but it
        // doesn't support `Iterator#remove` method.
        return Iterators.concat(Iterators.transform(delegate.values().iterator(), v -> v.iterator()));
    }

    @Override
    public void evict() throws NoSuchElementException {
        if (delegate.size() == 0) {
            throw new NoSuchElementException();
        }
        final Iterator<Map.Entry<K, LinkedList<E>>> iterator = delegate.descendingMap().entrySet().iterator();
        final LinkedList<E> container = iterator.next().getValue();
        container.removeLast();
        if (container.size() == 0) {
            iterator.remove();
        }
    }

    @Override
    public E peekNextRemove() {
        return delegate.entrySet().iterator().next().getValue().getFirst();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("delegate", delegate).toString();
    }

    @Override
    public E peekNextEviction() {
        return delegate.descendingMap().entrySet().iterator().next().getValue().getLast();
    }

}
