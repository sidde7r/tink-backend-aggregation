package se.tink.backend.aggregation.workers.ratelimit;

import com.google.common.util.concurrent.RateLimiter;

public interface ProviderRateLimiterFactory {

    RateLimiter buildFor(String providerClassName);

}
