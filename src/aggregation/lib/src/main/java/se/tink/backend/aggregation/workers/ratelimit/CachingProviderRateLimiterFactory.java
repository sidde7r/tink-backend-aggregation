package se.tink.backend.aggregation.workers.ratelimit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import java.util.concurrent.ConcurrentMap;

public class CachingProviderRateLimiterFactory implements ProviderRateLimiterFactory {

    private final ConcurrentMap<String, RateLimiter> cache = Maps.newConcurrentMap();
    private final ProviderRateLimiterFactory delegate;

    public CachingProviderRateLimiterFactory(ProviderRateLimiterFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public RateLimiter buildFor(String providerClassName) {
        String key = providerClassName;

        Preconditions.checkNotNull(key, "Provider class name cannot be null.");

        if (!cache.containsKey(key)) {
            // Acceptable race condition since we do putIfAbsent below.
            cache.putIfAbsent(key, delegate.buildFor(providerClassName));
        }
        return cache.get(key);
    }

    @Override
    public String toString() {
        // Deliberately not printing the cache content here.
        return MoreObjects.toStringHelper(this).add("delegate", delegate).toString();
    }
}
