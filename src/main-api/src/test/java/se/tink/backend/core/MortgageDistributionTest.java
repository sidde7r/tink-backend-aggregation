package se.tink.backend.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import java.math.BigDecimal;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class MortgageDistributionTest {

    @Test
    public void fillGapsCheckOrder() {
        List<MortgageDistribution.Bucket> buckets = Lists.newArrayList(
                new MortgageDistribution.Bucket(BigDecimal.valueOf(0), BigDecimal.valueOf(0.1),
                        BigDecimal.valueOf(123)),
                new MortgageDistribution.Bucket(BigDecimal.valueOf(0.5), BigDecimal.valueOf(0.6),
                        BigDecimal.valueOf(101)),
                new MortgageDistribution.Bucket(BigDecimal.valueOf(0.7), BigDecimal.valueOf(0.8),
                        BigDecimal.valueOf(12)));
        List<MortgageDistribution.Bucket> result = MortgageDistribution
                .fillGaps(buckets, BigDecimal.valueOf(0.1), BigDecimal.valueOf(10));

        Assert.assertEquals(8, result.size());
        Assert.assertEquals(Range.closedOpen(BigDecimal.ZERO, BigDecimal.valueOf(0.1)), result.get(0).getRange());
        Assert.assertEquals(Range.closedOpen(BigDecimal.valueOf(0.7), BigDecimal.valueOf(0.8)),
                result.get(7).getRange());
        for (int i = 1; i < result.size(); i++) {
            Assert.assertTrue(
                    result.get(i - 1).getRange().upperEndpoint().compareTo(result.get(i).getRange().lowerEndpoint())
                            == 0);
        }
    }
}
