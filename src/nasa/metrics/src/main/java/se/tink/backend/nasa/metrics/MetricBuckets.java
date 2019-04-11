package se.tink.backend.nasa.metrics;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.atomic.LongAdder;

/**
 * A fixed-bucket histogram used for metrics.
 *
 * <p>Optimized for many writes a few reads.
 *
 * <p>Calls must be thread-safe.
 */
public class MetricBuckets {
    // Borrowed from Prometheus' golang client w/ +0 to catch metrics that need heavy tuning.
    public static final ImmutableList<Double> STANDARD_BUCKETS =
            ImmutableList.of(0., .005, .01, .025, .05, .1, .25, .5, 1., 2.5, 5., 10.);
    public static final List<? extends Number> PERCENTAGE_BUCKETS =
            ImmutableList.of(0, 1, 2, 5, 10, 20, 30, 70, 80, 90, 95, 98, 99, 100);

    private final ImmutableSortedMap<Double, LongAdder> buckets;

    public MetricBuckets(List<? extends Number> limits) {
        ImmutableSortedMap.Builder<Double, LongAdder> builder = ImmutableSortedMap.naturalOrder();
        for (Number limit : limits) {
            builder.put(limit.doubleValue(), new LongAdder());
        }
        builder.put(Double.POSITIVE_INFINITY, new LongAdder());
        buckets = builder.build();
    }

    public void update(double value) {
        buckets.ceilingEntry(value).getValue().add(1);
    }

    public SortedSet<Double> getLimits() {
        return buckets.keySet();
    }

    public long getBucket(double limit) {
        Preconditions.checkArgument(getLimits().contains(limit), "`limit` is not a bucket.");

        long counter = 0;
        // Must reverse the map since `headMap` is non-inclusive, while `tailMap` is.
        for (Map.Entry<Double, LongAdder> bucket :
                buckets.descendingMap().tailMap(limit).entrySet()) {
            counter += bucket.getValue().sum();
        }

        return counter;
    }
}
