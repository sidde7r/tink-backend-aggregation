package se.tink.backend.aggregation.workers.ratelimit;

import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.RateLimiter;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingProviderRateLimiterFactory implements ProviderRateLimiterFactory {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private ProviderRateLimiterFactory delegate;

    public LoggingProviderRateLimiterFactory(ProviderRateLimiterFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public RateLimiter buildFor(String providerClassName) {
        RateLimiter rateLimiter = delegate.buildFor(providerClassName);

        logger.info(
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
