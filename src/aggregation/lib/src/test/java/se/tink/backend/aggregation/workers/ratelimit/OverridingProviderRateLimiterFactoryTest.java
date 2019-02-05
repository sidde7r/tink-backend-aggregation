package se.tink.backend.aggregation.workers.ratelimit;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Provider;

public class OverridingProviderRateLimiterFactoryTest {

    private static final String FALLBACK_PROVIDER = "banks.handelsbanken.v6.HandelsbankenV6Agent";
    private static final String OVERRIDE_PROVIDER = "fraud.CreditSafeAgent";
    private static final double OVERRIDE_RATE = 0.1;
    private static final double FALLBACK_RATE = 0.5;

    private static Provider createProvider(String name, String className, String market) {
        Provider p = new Provider();
        p.setName(name);
        p.setClassName(className);
        p.setMarket(market);
        return p;
    }

    private OverridingProviderRateLimiterFactory factory;

    @Before
    public void setUp() {
        factory = new OverridingProviderRateLimiterFactory(ImmutableMap.of(OVERRIDE_PROVIDER,
                0.1), new DefaultProviderRateLimiterFactory(0.5));
    }

    @Test
    public void testOverride() {
        Assert.assertEquals(OVERRIDE_RATE, factory.buildFor(OVERRIDE_PROVIDER).getRate(), 0.00001);
    }

    @Test
    public void testFallback() {
        Assert.assertEquals(FALLBACK_RATE, factory.buildFor(FALLBACK_PROVIDER).getRate(), 0.00001);
    }

}
