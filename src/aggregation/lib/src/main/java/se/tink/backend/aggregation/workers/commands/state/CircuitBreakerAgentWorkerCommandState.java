package se.tink.backend.aggregation.workers.commands.state;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import javax.inject.Inject;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.CircuitBreakerConfiguration;
import se.tink.backend.aggregation.workers.concurrency.CircuitBreakerStatistics;
import se.tink.backend.aggregation.workers.ratelimit.CachingProviderRateLimiterFactory;
import se.tink.backend.aggregation.workers.ratelimit.DefaultProviderRateLimiterFactory;
import se.tink.libraries.metrics.MetricRegistry;

public class CircuitBreakerAgentWorkerCommandState {
    private final CachingProviderRateLimiterFactory cachingProviderRateLimiterFactory;
    private final double originalRate;
    private final MetricRegistry metricRegistry;
    private LoadingCache<Provider, CircuitBreakerStatistics> statisticsByProvider;

    @Inject
    public CircuitBreakerAgentWorkerCommandState(
            AgentsServiceConfiguration agentsServiceConfiguration, MetricRegistry metricRegistry) {
        CircuitBreakerConfiguration configuration =
                agentsServiceConfiguration.getAggregationWorker().getCircuitBreaker();
        this.originalRate = configuration.getRateLimitRate();
        this.metricRegistry = metricRegistry;
        this.cachingProviderRateLimiterFactory =
                new CachingProviderRateLimiterFactory(
                        new DefaultProviderRateLimiterFactory(originalRate));
        statisticsByProvider =
                CacheBuilder.newBuilder()
                        .build(
                                new CacheLoader<Provider, CircuitBreakerStatistics>() {
                                    @Override
                                    public CircuitBreakerStatistics load(Provider provider)
                                            throws Exception {
                                        return new CircuitBreakerStatistics(
                                                configuration.getResetInterval(),
                                                configuration.getResetIntervalTimeUnit(),
                                                configuration.getRateLimitMultiplicationFactors(),
                                                configuration.getFailRatioThreshold(),
                                                configuration.getCircuitBreakerThreshold(),
                                                configuration.getBreakCircuitBreakerThreshold(),
                                                metricRegistry,
                                                provider.getName(),
                                                provider.getClassName(),
                                                provider.getMarket());
                                    }
                                });
    }

    public RateLimiter getRateLimiter(String providerName) {
        return cachingProviderRateLimiterFactory.buildFor(providerName);
    }

    public void setRateLimiterRate(String providerName, int rateMultiplier) {
        cachingProviderRateLimiterFactory
                .buildFor(providerName)
                .setRate(originalRate * rateMultiplier);
    }

    public LoadingCache<Provider, CircuitBreakerStatistics> getCircuitBreakerStatistics() {
        return statisticsByProvider;
    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }
}
