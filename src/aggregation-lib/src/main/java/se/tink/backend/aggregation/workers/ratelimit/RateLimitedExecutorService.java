package se.tink.backend.aggregation.workers.ratelimit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.dropwizard.lifecycle.Managed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import se.tink.backend.agents.rpc.Provider;
import se.tink.libraries.concurrency.ListenableThreadPoolExecutor;
import se.tink.libraries.concurrency.NamedRunnable;
import se.tink.libraries.metrics.utils.MetricsUtils;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public class RateLimitedExecutorService implements Managed {

    private static final AggregationLogger log = new AggregationLogger(RateLimitedExecutorService.class);
    private final MetricRegistry metricRegistry;

    private LoadingCache<Provider, RateLimitedExecutorProxy> rateLimitedRefreshInformationRequestExecutorByProvider;
    private ListenableThreadPoolExecutor<Runnable> executorService;
    private final AtomicReference<ProviderRateLimiterFactory> rateLimiterFactory;
    private int maxQueuedItems;

    public RateLimitedExecutorService(ListenableThreadPoolExecutor<Runnable> executorService,
            MetricRegistry metricRegistry, int maxQueuedItems) {
        this.executorService = executorService;
        this.metricRegistry = metricRegistry;
        this.maxQueuedItems = maxQueuedItems;

        this.rateLimiterFactory = new AtomicReference<ProviderRateLimiterFactory>(
                new CachingProviderRateLimiterFactory(new LoggingProviderRateLimiterFactory(
                        new OverridingProviderRateLimiterFactory(ImmutableMap.of(
                                "fraud.CreditSafeAgent", 0.2,
                                "abnamro.ics.IcsAgent", 8.
                        ), new DefaultProviderRateLimiterFactory(0.5)))));

        log.info(String.format("Rate limiter factory on initialization: %s", rateLimiterFactory));
    }

    private LoadingCache<Provider, RateLimitedExecutorProxy> buildRateLimittedProxyCache(
            final AtomicReference<ProviderRateLimiterFactory> providerRateLimiterFactory) {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(96, TimeUnit.HOURS) // Must be longer than the time it takes to process the
                // ratelimitter queue for a single provider. That is, if we queue
                // up all CreditSafe refreshes in a single batch, we must be sure
                // that it finishes all those batches in X hours.
                .removalListener((RemovalListener<Provider, RateLimitedExecutorProxy>) notification -> {
                    RateLimitedExecutorProxy executor = notification.getValue();

                    if (!MoreExecutors.shutdownAndAwaitTermination(executor, 2, TimeUnit.MINUTES)) {
                        log.error(String.format(
                                "Could gracefully shut down RateLimitedExecutorProxy for provider '%s'.",
                                notification.getKey().getName()));
                    }
                }).build(new CacheLoader<Provider, RateLimitedExecutorProxy>() {
                    @Override
                    public RateLimitedExecutorProxy load(final Provider provider) throws Exception {
                        MetricId.MetricLabels labels = new MetricId.MetricLabels()
                                .add("provider_type", provider.getType().name().toLowerCase())
                                .add("provider", MetricsUtils.cleanMetricName(provider.getName()));

                        return new RateLimitedExecutorProxy(
                                () -> RateLimitedExecutorProxy.RateLimiters.from(providerRateLimiterFactory
                                        .get().buildFor(provider.getClassName())),
                                executorService,
                                new ThreadFactoryBuilder()
                                        .setNameFormat(provider.getName() + "-rate-limiter-%d")
                                        .build(),
                                metricRegistry,
                                labels,
                                maxQueuedItems
                        );
                    }
                });
    }

    public void execute(final NamedRunnable namedRunnable, final Provider provider) throws Exception {
        final RateLimitedExecutorProxy executorProxy = rateLimitedRefreshInformationRequestExecutorByProvider.get(
                provider);
        executorProxy.execute(namedRunnable);
    }

    @Override
    public void start() throws Exception {
        rateLimitedRefreshInformationRequestExecutorByProvider = buildRateLimittedProxyCache(
                rateLimiterFactory);
    }

    @Override
    public void stop() throws Exception {
        if (rateLimitedRefreshInformationRequestExecutorByProvider != null) {
            rateLimitedRefreshInformationRequestExecutorByProvider.invalidateAll();
            rateLimitedRefreshInformationRequestExecutorByProvider.cleanUp(); // just in case
        }
    }

    public void setRateLimiterFactory(ProviderRateLimiterFactory rateLimiterFactory) {
        ProviderRateLimiterFactory loggedFactory = new LoggingProviderRateLimiterFactory(rateLimiterFactory);

        // We must wrap all these in CachingProviderRateLimiterFactory so that each call to a factory with the same
        // provider returns the same RateLimiter instance.
        ProviderRateLimiterFactory cachedFactor = new CachingProviderRateLimiterFactory(loggedFactory);

        ProviderRateLimiterFactory oldFactory = this.rateLimiterFactory.getAndSet(cachedFactor);

        log.info(String.format("Old provider rate limiter factory: %s", oldFactory));
        log.info(String.format("New provider rate limiter factory: %s", cachedFactor));
    }
}
