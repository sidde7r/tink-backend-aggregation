package se.tink.backend.aggregation.utils;

import com.google.common.util.concurrent.RateLimiter;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RateLimitedCountdownImplTest {
    @Test
    public void acquireConsidersCounter() {
        RateLimitedCountdownImpl rateLimitedCountdown = new RateLimitedCountdownImpl(2, RateLimiter.create(999999));

        assertThat(rateLimitedCountdown.acquireIsMore()).isTrue(); // 2
        assertThat(rateLimitedCountdown.acquireIsMore()).isTrue(); // 1
        assertThat(rateLimitedCountdown.acquireIsMore()).isFalse(); // 0
        assertThat(rateLimitedCountdown.acquireIsMore()).isFalse(); // 0
    }

    @Test
    public void acquireUsesRateLimiter() {
        RateLimiter rateLimiterMock = mock(RateLimiter.class);
        when(rateLimiterMock.acquire()).thenReturn(0.0);
        RateLimitedCountdownImpl rateLimitedCountdown = new RateLimitedCountdownImpl(1, rateLimiterMock);

        rateLimitedCountdown.acquireIsMore();
        rateLimitedCountdown.acquireIsMore();
        rateLimitedCountdown.acquireIsMore();

        // Each call above should have called acquire on rateLimiter
        verify(rateLimiterMock, times(3)).acquire();
    }
}
