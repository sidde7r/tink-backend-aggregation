package se.tink.backend.aggregation.workers.ratelimit;

import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.RateLimiter;

public class DefaultProviderRateLimiterFactory implements ProviderRateLimiterFactory {

    private double rate;

    public DefaultProviderRateLimiterFactory(double rate) {
        this.rate = rate;
    }

    @Override
    public RateLimiter buildFor(String providerClassName) {
        return RateLimiter.create(rate);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("rate", rate).toString();
    }
}
