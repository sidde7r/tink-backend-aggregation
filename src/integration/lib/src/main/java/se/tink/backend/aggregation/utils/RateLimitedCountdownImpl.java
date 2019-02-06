package se.tink.backend.aggregation.utils;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.RateLimiter;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class RateLimitedCountdownImpl implements RateLimitedCountdown {
    private Integer counter;
    private final RateLimiter rateLimiter;

    @Inject
    public RateLimitedCountdownImpl(
            @Named("RateLimitedCountdownImpl.counts") Integer counts,
            @Named("RateLimitedCountdownImpl.rateLimiter") RateLimiter rateLimiter) {
        Preconditions.checkArgument(counts != null);
        Preconditions.checkArgument(counts > 0);
        Preconditions.checkArgument(rateLimiter != null);

        this.counter = counts;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean acquireIsMore() {
        rateLimiter.acquire();

        if (counter > 0) {
            counter--;
            return true;
        } else {
            return false;
        }
    }
}
