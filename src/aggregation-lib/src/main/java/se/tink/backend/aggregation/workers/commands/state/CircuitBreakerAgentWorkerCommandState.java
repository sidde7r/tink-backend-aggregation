package se.tink.backend.aggregation.workers.commands.state;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.aggregation.workers.concurrency.CircuitBreakerStatistics;
import se.tink.backend.aggregation.workers.ratelimit.CachingProviderRateLimiterFactory;
import se.tink.backend.aggregation.workers.ratelimit.DefaultProviderRateLimiterFactory;
import se.tink.backend.common.config.CircuitBreakerConfiguration;
import se.tink.libraries.metrics.MetricRegistry;

public class CircuitBreakerAgentWorkerCommandState {
    private final CachingProviderRateLimiterFactory cachingProviderRateLimiterFactory;
    private final double originalRate;
    private final MetricRegistry metricRegistry;
    private LoadingCache<Provider, CircuitBreakerStatistics> statisticsByProvider;

    public CircuitBreakerAgentWorkerCommandState(CircuitBreakerConfiguration configuration,
                                                 MetricRegistry metricRegistry) {
        this.originalRate = configuration.getRateLimitRate();
        this.metricRegistry = metricRegistry;
        this.cachingProviderRateLimiterFactory = new CachingProviderRateLimiterFactory(
                new DefaultProviderRateLimiterFactory(originalRate));
        statisticsByProvider = CacheBuilder.newBuilder()
                .build(
                        new CacheLoader<Provider, CircuitBreakerStatistics>() {
                            @Override
                            public CircuitBreakerStatistics load(Provider provider) throws Exception {
                                return new CircuitBreakerStatistics(
                                        configuration.getResetInterval(),
                                        configuration.getResetIntervalTimeUnit(),
                                        configuration.getRateLimitMultiplicationFactors(),
                                        configuration.getFailRatioThreshold(),
                                        configuration.getCircuitBreakerThreshold(),
                                        configuration.getBreakCircuitBreakerThreshold(),
                                        metricRegistry,
                                        provider.getName(),
                                        provider.getClassName());
                            }
                        });
    }

    public  RateLimiter getRateLimiter(String providerName) {
        return cachingProviderRateLimiterFactory.buildFor(providerName);
    }

    public void setRateLimiterRate(String providerName, int rateMultiplier) {
        cachingProviderRateLimiterFactory.buildFor(providerName).setRate(originalRate * rateMultiplier);
    }

    public LoadingCache<Provider, CircuitBreakerStatistics> getCircuitBreakerStatistics() {
        return statisticsByProvider;
    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }
}
