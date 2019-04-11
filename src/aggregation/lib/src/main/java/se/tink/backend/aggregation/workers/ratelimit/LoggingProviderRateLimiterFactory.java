package se.tink.backend.aggregation.workers.ratelimit;

import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.RateLimiter;
import se.tink.backend.aggregation.log.AggregationLogger;

public class LoggingProviderRateLimiterFactory implements ProviderRateLimiterFactory {

    private static final AggregationLogger log =
            new AggregationLogger(LoggingProviderRateLimiterFactory.class);
    private ProviderRateLimiterFactory delegate;

    public LoggingProviderRateLimiterFactory(ProviderRateLimiterFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public RateLimiter buildFor(String providerClassName) {
        RateLimiter rateLimiter = delegate.buildFor(providerClassName);

        log.info(
                String.format(
                        "Creating RateLimiter for %s, with rate: %s per second",
                        providerClassName == null ? "null" : providerClassName,
                        rateLimiter.getRate()));

        return rateLimiter;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("delegate", delegate).toString();
    }
}
