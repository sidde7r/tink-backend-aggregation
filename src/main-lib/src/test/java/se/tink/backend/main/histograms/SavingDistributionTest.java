package se.tink.backend.main.histograms;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;

public class SavingDistributionTest {
    @Test
    public void binarySearchPresent() {
        List<SavingDistribution.Bucket> buckets = Lists.newArrayList(
                createBucket(0, 0.1, 0.1, 0),
                createBucket(0.1, 0.2, 0.12, 0.1),
                createBucket(0.2, 0.3, 0.08, 0.13),
                createBucket(0.3, 0.4, 0.19, 0.21),
                createBucket(0.4, 0.5, 0.03, 0.4)
        );

        Optional<SavingDistribution.Bucket> bucket = new SavingDistribution(buckets)
                .binarySearch(BigDecimal.valueOf(0.45));

        Assert.assertTrue(bucket.isPresent());
        Assert.assertEquals(Range.closedOpen(BigDecimal.valueOf(0.4), BigDecimal.valueOf(0.5)),
                bucket.get().getAmountRange());
        Assert.assertEquals(BigDecimal.valueOf(0.03), bucket.get().getFrequency());
        Assert.assertEquals(BigDecimal.valueOf(0.4), bucket.get().getPercentile());
    }

    @Test
    public void binarySearchNotPresent() {
        List<SavingDistribution.Bucket> buckets = Lists.newArrayList(
                createBucket(0, 0.1, 0.1, 0),
                createBucket(0.1, 0.2, 0.12, 0.1),
                createBucket(0.2, 0.3, 0.08, 0.13),
                createBucket(0.4, 0.5, 0.03, 0.4)
        );

        Optional<SavingDistribution.Bucket> bucket = new SavingDistribution(buckets)
                .binarySearch(BigDecimal.valueOf(0.35));

        Assert.assertFalse(bucket.isPresent());
    }

    private SavingDistribution.Bucket createBucket(double lowerEndpoint, double upperEndpoint,
            double frequency, double percentile) {
        return new SavingDistribution.Bucket(BigDecimal.valueOf(lowerEndpoint),
                BigDecimal.valueOf(upperEndpoint),
                BigDecimal.valueOf(frequency), BigDecimal.valueOf(percentile));
    }

}
