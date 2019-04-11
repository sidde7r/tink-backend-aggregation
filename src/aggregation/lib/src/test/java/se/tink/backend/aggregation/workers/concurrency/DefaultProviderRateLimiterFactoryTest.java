package se.tink.backend.aggregation.workers.concurrency;

import com.google.common.util.concurrent.RateLimiter;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.workers.ratelimit.DefaultProviderRateLimiterFactory;

public class DefaultProviderRateLimiterFactoryTest {

    private static final String TEST_PROVIDER = "banks.handelsbanken.v6.HandelsbankenV6Agent";

    @Test
    public void testToShowHowGuavaRateLimiterWorks() {

        // Number is how many permits per second -- not the number of seconds inbetween permits.
        RateLimiter rateLimiter = RateLimiter.create(100);

        long start = System.currentTimeMillis();
        int i = 0;
        while (i < 10) {
            rateLimiter.acquire();
            i++;
        }
        long duration = System.currentTimeMillis() - start;
        System.out.println("Took " + duration + "ms");

        // should be around 90ms
    }

    @Test
    public void testBasicBuild() {
        double testRate = 0.4;
        DefaultProviderRateLimiterFactory factory = new DefaultProviderRateLimiterFactory(0.4);
        Assert.assertEquals(testRate, factory.buildFor(TEST_PROVIDER).getRate(), 0.00001);
    }

    @Test
    public void testNullInput() {
        double testRate = 0.2;
        DefaultProviderRateLimiterFactory factory = new DefaultProviderRateLimiterFactory(0.2);
        Assert.assertEquals(testRate, factory.buildFor(null).getRate(), 0.00001);
    }
}
