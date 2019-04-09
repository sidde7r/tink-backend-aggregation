package se.tink.libraries.metrics;

import com.google.common.collect.ImmutableList;
import java.util.SortedSet;
import org.junit.Assert;
import org.junit.Test;

public class MetricBucketsTest {

    @Test
    public void testBucketer() {
        MetricBuckets buckets =
                new MetricBuckets(
                        ImmutableList.of(0, .005, .01, .025, .05, .1, .25, .5, 1, 2.5, 5, 10));
        buckets.update(0.05);

        // Expect that buckets le=0.05 and above have been marked (last one is +Inf)
        ImmutableList<Long> expected =
                ImmutableList.of(0L, 0L, 0L, 0L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L);
        Assert.assertEquals(
                ImmutableList.copyOf(expected), ImmutableList.copyOf(getBuckets(buckets)));

        buckets.update(100.0);

        expected = ImmutableList.of(0L, 0L, 0L, 0L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 2L);
        Assert.assertEquals(
                ImmutableList.copyOf(expected), ImmutableList.copyOf(getBuckets(buckets)));
    }

    private ImmutableList<Long> getBuckets(MetricBuckets buckets) {
        final SortedSet<Double> limits = buckets.getLimits();
        ImmutableList.Builder<Long> result = ImmutableList.builder();

        int i = 0;
        for (Double limit : limits) {
            result.add(buckets.getBucket(limit));
        }

        return result.build();
    }
}
