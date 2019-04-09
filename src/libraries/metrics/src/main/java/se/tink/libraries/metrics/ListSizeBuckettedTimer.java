package se.tink.libraries.metrics;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Calls a delegate {@link Callable < List >} and instruments the time it took to call, bucketed by
 * the size of the returned list. Useful to investigate the advantages of partitioning the data in
 * various ways.
 */
public class ListSizeBuckettedTimer {
    private static final TimeUnit PRECISION = TimeUnit.NANOSECONDS;

    private final ImmutableSortedMap<Integer, Timer> itemBuckets;

    public ListSizeBuckettedTimer(
            MetricRegistry registry,
            MetricId metricId,
            List<? extends Number> timeBuckets,
            List<Integer> itemBuckets) {
        final ImmutableSortedMap.Builder<Integer, Timer> builder =
                ImmutableSortedMap.naturalOrder();
        for (Integer itemBucket : itemBuckets) {
            builder.put(
                    itemBucket,
                    registry.timer(metricId.label("items_le", itemBucket.toString()), timeBuckets));
        }
        builder.put(Integer.MAX_VALUE, registry.timer(metricId.label("items_le", "+Inf")));
        this.itemBuckets = builder.build();
    }

    public <V> Callable<List<V>> decorate(final Callable<List<V>> delegate) {
        return () -> {
            final Stopwatch watch = Stopwatch.createStarted();
            List<V> items = delegate.call();
            watch.stop();

            register(items.size(), watch);

            return items;
        };
    }

    public <M extends Multimap<K, V>, K, V> Callable<M> decorateMultimap(
            final Callable<M> delegate) {
        return () -> {
            final Stopwatch watch = Stopwatch.createStarted();
            M items = delegate.call();
            watch.stop();

            register(items.size(), watch);

            return items;
        };
    }

    private void register(int n, Stopwatch watch) {
        final long timeElapsed = watch.elapsed(PRECISION);

        // Important head map is inclusive.
        for (Timer timer : itemBuckets.headMap(n, true).values()) {
            timer.update(timeElapsed, PRECISION);
        }
    }
}
