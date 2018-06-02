package se.tink.backend.common.concurrency;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import se.tink.libraries.metrics.Gauge;
import se.tink.libraries.metrics.IncrementDecrementGauge;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

/**
 * Wraps a BlockingQueue and instruments the items it holds by grouping them into labels. Instrumentation is
 * published as gauges through Dropwizard.
 */
public class ElementMonitoredQueue<T> extends AbstractQueue<T> implements BlockingQueue<T> {

    @VisibleForTesting
    static final MetricId BASE_NAME_STUB = MetricId.newId("element_monitored_queue");

    private final MetricRegistry registry;
    private final MetricId baseName;

    public static class PrioritizedRunnableLabelExtractor implements Function<PrioritizedRunnable, String> {

        public static final String HIGH_PRIORITY_METRIC_NAME = "highPrio";
        public static final String LOW_PRIORITY_METRIC_NAME = "lowPrio";
        public static final String OTHER_PRIORITY_METRIC_NAME = "otherPrio";

        @Override
        public String apply(PrioritizedRunnable element) {
            switch (element.priority) {
            case PrioritizedRunnable.HIGH_PRIORITY:
                return HIGH_PRIORITY_METRIC_NAME;
            case PrioritizedRunnable.LOW_PRIORITY:
                return LOW_PRIORITY_METRIC_NAME;
            default:
                return OTHER_PRIORITY_METRIC_NAME;
            }
        }
    }

    private Function<T, String> labelExtractor;
    private BlockingQueue<T> delegate;
    private ConcurrentMap<String, Gauge> gauges = Maps.newConcurrentMap();
    private ConcurrentMap<String, LongAdder> values = Maps.newConcurrentMap();

    public ElementMonitoredQueue(BlockingQueue<T> delegate, Function<T, String> labelExtractor, MetricRegistry registry,
            String name, MetricId.MetricLabels labels) {

        this.delegate = Preconditions.checkNotNull(delegate);
        this.labelExtractor = labelExtractor;
        this.registry = registry;
        this.baseName = BASE_NAME_STUB.label("name", name).label(labels);
    }

    private T decrement(T r) {
        if (r == null) {
            return null;
        }

        getAtomicInteger(r).decrement();
        return r;
    }

    @Override
    public int drainTo(Collection<? super T> c) {
        return drainTo(c, Integer.MAX_VALUE);
    }

    @Override
    public int drainTo(Collection<? super T> c, int maxElements) {
        Collection<T> tmp = Lists.newArrayList();
        int res = delegate.drainTo(tmp);
        for (T e : tmp) {
            decrement(e);
        }
        c.addAll(tmp);
        return res;
    }

    private IncrementDecrementGauge getAtomicInteger(T r) {
        String label = labelExtractor.apply(r);
        return registry.incrementDecrementGauge(baseName.label("priority", label));
    }

    private T increment(T r) {
        if (r == null) {
            return null;
        }

        getAtomicInteger(r).increment();
        return r;
    }

    @Override
    public Iterator<T> iterator() {
        final Iterator<T> delegateIterator = delegate.iterator();
        return new Iterator<T>() {

            private T lastElement = null;

            @Override
            public boolean hasNext() {
                return delegateIterator.hasNext();
            }

            @Override
            public T next() {
                lastElement = delegateIterator.next();
                return lastElement;
            }

            @Override
            public void remove() {
                delegateIterator.remove(); // Important we remove before decrementing as this might throw exception.
                decrement(lastElement);
                lastElement = null;
            }

        };
    }

    @Override
    public boolean offer(T e) {
        if (delegate.offer(e)) {
            increment(e);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean offer(T e, long timeout, TimeUnit unit) throws InterruptedException {
        if (delegate.offer(e, timeout, unit)) {
            increment(e);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public T peek() {
        return delegate.peek();
    }

    @Override
    public T poll() {
        return decrement(delegate.poll());
    }

    @Override
    public T poll(long timeout, TimeUnit unit) throws InterruptedException {
        return decrement(delegate.poll(timeout, unit));
    }

    @Override
    public void put(T e) throws InterruptedException {
        delegate.put(e);
        increment(e);
    }

    @Override
    public int remainingCapacity() {
        return delegate.remainingCapacity();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public T take() throws InterruptedException {
        return decrement(delegate.take());
    }

}
