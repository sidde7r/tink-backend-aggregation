package se.tink.backend.aggregation.workers.ratelimit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.RateLimiter;
import java.util.Map;

public class OverridingProviderRateLimiterFactory implements ProviderRateLimiterFactory {

    private final ProviderRateLimiterFactory fallback;
    private final ImmutableMap<String, Double> rateByClassname;

    /**
     * @param rateByClassname a map from provider classname to permits per second.
     * @param fallback        the fallback providers rate limiter factory if no matches were found in the
     *                        rateByClassname.
     */
    public OverridingProviderRateLimiterFactory(Map<String, Double> rateByClassname,
            ProviderRateLimiterFactory fallback) {
        this.rateByClassname = ImmutableMap.copyOf(Preconditions.checkNotNull(rateByClassname));
        this.fallback = Preconditions.checkNotNull(fallback);
    }

    @Override
    public RateLimiter buildFor(String providerClassName) {

        String key = providerClassName;
        if (key != null && rateByClassname.containsKey(key)) {
            return RateLimiter.create(rateByClassname.get(key));
        }

        return fallback.buildFor(providerClassName);

    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("rateByClassname", rateByClassname).add("fallback", fallback)
                .toString();
    }

}
