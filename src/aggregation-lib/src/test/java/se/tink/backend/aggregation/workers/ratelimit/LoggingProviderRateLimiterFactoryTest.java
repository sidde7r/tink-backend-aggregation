package se.tink.backend.aggregation.workers.ratelimit;

import com.google.common.util.concurrent.RateLimiter;
import org.junit.Test;
import org.mockito.Mockito;

public class LoggingProviderRateLimiterFactoryTest {

    private static final String TEST_PROVIDER = "banks.handelsbanken.v6.HandelsbankenV6Agent";

    @Test
    public void testWeDelegate() {

        // Given

        ProviderRateLimiterFactory mockedDelegate = Mockito.mock(ProviderRateLimiterFactory.class);
        Mockito.when(mockedDelegate.buildFor(TEST_PROVIDER)).thenReturn(RateLimiter.create(0.8));
        
        LoggingProviderRateLimiterFactory factory = new LoggingProviderRateLimiterFactory(mockedDelegate);

        // When

        factory.buildFor(TEST_PROVIDER);

        // Then

        Mockito.verify(mockedDelegate).buildFor(TEST_PROVIDER);
    }

}
