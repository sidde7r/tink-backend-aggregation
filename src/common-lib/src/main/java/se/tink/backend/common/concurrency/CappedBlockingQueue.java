package se.tink.backend.common.concurrency;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import se.tink.backend.utils.LogUtils;

/**
 * Wraps a {@link BlockingQueue}, but adds upper queue size limit. Useful for for example a
 * {@link PriorityBlockingQueue} which doesn't support an upper limit.
 *
 * @param <E>
 */
public class CappedBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E> {

    private static final LogUtils log = new LogUtils(CappedBlockingQueue.class);

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Condition notEmpty = lock.writeLock().newCondition();
    private final Condition notFull = lock.writeLock().newCondition();
    private final EvictionCallback<E> evictionCallback;

    /**
     * A simple interface for delegation to a custom backing collection. It handles queue behaviour; item priority and
     * eviction policy.
     * <p>
     * Must not have to be thread safe.
     *
     * @param <E>
     */
    public interface Delegate<E> extends Iterable<E> {
        void add(E e);

        E remove() throws NoSuchElementException;

        E peekNextRemove();

        void evict() throws NoSuchElementException;

        E peekNextEviction() throws NoSuchElementException;

        int size();
    }

    /**
     * Actual container for the queue. Smallest (first) element has highest priority. We poll from that end. We prune the last element.
     */
    private final Delegate<E> delegate; // Not instantiating using Sets#newTreeSet because it
    // requires E to be Comparable, which is not the case of
    // BlockingQueue<Runnable>, which a ThreadPoolExecutor's
    // constructor takes.
    private final int maxSize;
    private Predicate<E> isEvictable;

    private CappedBlockingQueue(Delegate<E> delegate, int maxSize, Predicate<E> isEvictable,
            EvictionCallback<E> evictionCallback) {
        Preconditions.checkArgument(maxSize > 0, "maxSize must be positive.");
        Preconditions.checkState(delegate.size() == 0,
                "Delegate must be empty on initialization to properly assert its capacity.");

        this.maxSize = maxSize;
        this.delegate = delegate;
        this.isEvictable = isEvictable;
        this.evictionCallback = evictionCallback;
    }

    public interface EvictionCallback<E> {
        void evicted(E e);
    }

    public static class NilEvictionCallback<E> implements EvictionCallback<E> {

        @Override
        public void evicted(E e) {
            // Deliberately left empty.
        }
    }

    public static class Builder<E> {

        private Optional<Delegate<E>> delegate = Optional.empty();
        private Predicate<E> isEvictable = Predicates.alwaysTrue();
        private EvictionCallback<E> evictionCallback = new NilEvictionCallback<>();

        /**
         * Use {@link CappedBlockingQueue#builder()}.
         */
        private Builder() {
        }

        /**
         * Set which elements can be evicted. Note that only the least prioritized elements will be considered for
         * eviction.
         *
         * @param isEvictable a predicate that checks if an element is eligible for eviction.
         * @return this for chainability.
         */
        public Builder<E> setIsEvictable(Predicate<E> isEvictable) {
            this.isEvictable = isEvictable;
            return this;
        }

        public Builder<E> setEvictionCallback(EvictionCallback<E> evictionCallback) {
            this.evictionCallback = Preconditions.checkNotNull(evictionCallback);
            return this;
        }

        public CappedBlockingQueue<E> build(int maxSize, Delegate<E> delegate) {
            return new CappedBlockingQueue<>(delegate, maxSize, isEvictable, evictionCallback);
        }

    }

    public static <E> Builder<E> builder() {
        return new Builder<E>();
    }

    @Override
    public E poll() {
        lock.writeLock().lock();
        try {
            return delegate.size() > 0 ? delegate.remove() : null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public E peek() {
        lock.readLock().lock();
        try {
            return delegate.size() > 0 ? delegate.peekNextRemove() : null;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Removed least prioritized elements.
     * <p>
     * Expected to be executed within a synchronized block/lock.
     */
    private void makeRoomIfNecessaryAndPossible() {
        while (delegate.size() >= maxSize && this.isEvictable.apply(delegate.peekNextEviction())) {
            final E toEvict = delegate.peekNextEviction();
            delegate.evict();
            evictionCallback.evicted(toEvict);
            if (log.isTraceEnabled()) {
                log.trace(String.format("Evicting element because %d >= %d", delegate.size(), maxSize));
            }
        }
    }

    @Override
    public boolean offer(E e) {
        lock.writeLock().lock();
        try {

            // Try to make room if necessary.
            makeRoomIfNecessaryAndPossible();

            if (delegate.size() < maxSize) {
                // Do we have room?

                delegate.add(e);
                notEmpty.signal();
                return true; // Yes.
            } else {
                return false; // No.
            }

        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void put(E e) throws InterruptedException {
        lock.writeLock().lock();
        try {
            do {

                // Try to make room if necessary.
                makeRoomIfNecessaryAndPossible();

                if (delegate.size() < maxSize) {
                    // Do we have room?

                    // Yes, we do:
                    delegate.add(e);
                    notEmpty.signal(); // Let all consumers know there's now an element available.
                    return;
                } else {
                    // No, no room.

                    if (isEvictable.apply(e)) {
                        // Can we ignore this insert?
                        evictionCallback.evicted(e);
                        if (log.isTraceEnabled()) {
                            log.trace(String.format("(put) Ignoring insert because %d >= %d.", delegate.size(),
                                    maxSize));
                        }
                        return; // Yes.
                    } else {
                        notFull.await(); // No; wait until we have room.
                    }
                }

            } while (true);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        long beforeAwait = System.currentTimeMillis();
        if (!lock.writeLock().tryLock(timeout, unit)) { // Try to get a write lock within the given timeout.
            return false;
        }
        try {
            do {

                // Try to make room if necessary.
                makeRoomIfNecessaryAndPossible();

                if (delegate.size() < maxSize) {
                    // Do we have room?

                    // Yes, we do.
                    delegate.add(e);
                    notEmpty.signal(); // Let all consumers know there's now an element available.
                    return true;
                } else {
                    // No room right now.

                    if (isEvictable.apply(e)) {
                        // Can we ignore this insert?
                        evictionCallback.evicted(e);
                        if (log.isTraceEnabled()) {
                            log.trace(String.format("(offer) Ignoring insert because %d >= %d.", delegate.size(),
                                    maxSize));
                        }
                        return false; // Yes.
                    } else {
                        // No; wait until there is room or until we time out.
                        if (!notFull
                                .await(
                                        timeout - unit.convert(System.currentTimeMillis() - beforeAwait,
                                                TimeUnit.MILLISECONDS),
                                        unit)) {
                            return false;
                        }
                    }
                }

            } while (true);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public E take() throws InterruptedException {
        lock.writeLock().lock();
        try {

            // Wait forever until there is an element available.
            while (delegate.size() == 0) {
                notEmpty.await();
            }

            // Now there's an element available. Let's grab it.
            E e = delegate.remove();

            notFull.signal(); // - "Hey, other producer threads, there might now a slot available in the queue."

            return e;

        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        final long beforeAwait = System.currentTimeMillis();
        if (!lock.writeLock().tryLock(timeout, unit)) { // Try to get a write lock within the given timeout.
            return null;
        }
        try {

            // Wait forever until there is an element available.
            while (delegate.size() == 0) {
                if (!notEmpty.await(
                        timeout - unit.convert(System.currentTimeMillis() - beforeAwait, TimeUnit.MILLISECONDS),
                        unit)) {
                    return null;
                }
            }

            // Now there's an element available. Let's grab it. Should be non-blocking since we know delegate isn't
            // empty.
            E e = delegate.remove();
            Preconditions
                    .checkState(
                            e != null,
                            "The delegate wasn't empty according to us. However, it told us it was. Possible non-wrapped access or improper delegate implementation.");

            notFull.signal(); // - "Hey, other producer threads, there might now a slot available in the queue."

            return e;

        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public int remainingCapacity() {
        lock.readLock().lock();
        try {
            return maxSize - delegate.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        lock.writeLock().lock();
        try {
            int added = 0;
            while (delegate.size() > 0) {
                c.add(delegate.remove());
                added++;
            }
            notFull.signalAll();
            return added;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public int drainTo(final Collection<? super E> c, final int maxElements) {
        lock.writeLock().lock();
        try {

            int added = 0;
            while (delegate.size() > 0 && added < maxElements) {
                c.add(delegate.remove());
                added++;
            }
            notFull.signalAll();
            return added;

        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Returns an {@link UnmodifiableIterator}.
     */
    @Override
    public Iterator<E> iterator() {
        lock.readLock().lock();
        try {
            return Iterators.unmodifiableIterator(Lists.newArrayList(delegate.iterator()).iterator());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int size() {
        lock.readLock().lock();
        try {
            return delegate.size();
        } finally {
            lock.readLock().unlock();
        }
    }

}
