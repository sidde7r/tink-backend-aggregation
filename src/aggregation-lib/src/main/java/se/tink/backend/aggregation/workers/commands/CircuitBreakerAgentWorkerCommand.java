package se.tink.backend.aggregation.workers.commands;

import com.google.common.base.Objects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerContext;
import se.tink.backend.aggregation.workers.concurrency.CircuitBreakerStatistics;
import se.tink.backend.aggregation.workers.ratelimit.CachingProviderRateLimiterFactory;
import se.tink.backend.aggregation.workers.ratelimit.DefaultProviderRateLimiterFactory;
import se.tink.backend.common.config.CircuitBreakerConfiguration;
import se.tink.backend.common.config.CircuitBreakerMode;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.common.utils.MetricsUtils;
import se.tink.backend.aggregation.rpc.Credentials;

/**
 * A command that makes sure to stop continuing an operation if it has failed too many times recently.
 * <p/>
 * This was introduced for the following reasons:
 * <ul>
 * <li>To avoid hammering a broken bank. In other words "stop kicking someone already on the ground". :) This is to
 * protect us from bringing down a bank.</li>
 * <li>To avoid filling up our thread pool if banks are timing. We should have timeouts for our HTTP clients and the
 * monitoring thread should kill something not making progress. That said, if we are putting a lot of traffic through
 * timeouts could still fill up the thread pool.</li>
 * </ul>
 * That is, operations that are high-throughput should probably use a circuit breaker.
 */
public class CircuitBreakerAgentWorkerCommand extends AgentWorkerCommand {
    private static final AggregationLogger log = new AggregationLogger(CircuitBreakerAgentWorkerCommand.class);
    private static final MetricId CIRCUIT_BROKEN_PROVIDERS = MetricId.newId("circuit_broken_providers");

    private CircuitBreakerAgentWorkerCommandState state;
    private AgentWorkerContext context;
    private boolean wasCircuitBreaked;

    public CircuitBreakerAgentWorkerCommand(AgentWorkerContext context, CircuitBreakerAgentWorkerCommandState state) {
        this.context = context;
        this.state = state;
    }

    public static class CircuitBreakerAgentWorkerCommandState {
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

        RateLimiter getRateLimiter(String providerName) {
            return cachingProviderRateLimiterFactory.buildFor(providerName);
        }

        void setRateLimiterRate(String providerName, int rateMultiplier) {
            cachingProviderRateLimiterFactory.buildFor(providerName).setRate(originalRate * rateMultiplier);
        }

        LoadingCache<Provider, CircuitBreakerStatistics> getCircuitBreakerStatistics() {
            return statisticsByProvider;
        }

        MetricRegistry getMetricRegistry() {
            return metricRegistry;
        }
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        Credentials credentials = context.getRequest().getCredentials();
        Provider provider = context.getRequest().getProvider();
        wasCircuitBreaked = false;

        final CircuitBreakerConfiguration circuitBreakerConfiguration = context.getServiceContext().getConfiguration()
                .getAggregationWorker().getCircuitBreaker();

        CircuitBreakerStatistics.CircuitBreakerStatus circuitBreakerStatus = state.getCircuitBreakerStatistics().get(
                provider).getStatus();

        if (Objects.equal(circuitBreakerConfiguration.getMode(), CircuitBreakerMode.DISABLED)) {
            return AgentWorkerCommandResult.CONTINUE;
        }

        state.getMetricRegistry().meter(MetricId.newId("circuit_breaker")
                .label("broken", String.valueOf(circuitBreakerStatus.isCircuitBroken()))
                .label("consecutive_operations", String.valueOf(circuitBreakerStatus.getConsecutiveOperationsCounter()))
                .label("provider_type", provider.getType().name().toLowerCase())
                .label("provider", MetricsUtils.cleanMetricName(provider.getName()))).inc();

        if (circuitBreakerStatus.isCircuitBroken()) {
            state.setRateLimiterRate(provider.getName(), circuitBreakerStatus.getRateLimitMultiplicationFactor());
            RateLimiter rateLimiter = state.getRateLimiter(provider.getName());

            // If we acquire a rate limiter the operation will continue as if the provider is not circuit broken.
            if (!rateLimiter.tryAcquire()) {
                if (Objects.equal(circuitBreakerConfiguration.getMode(), CircuitBreakerMode.ENABLED)) {
                    context.updateStatus(
                            CredentialsStatus.TEMPORARY_ERROR,
                            Catalog.format(
                                    context.getCatalog().getString("We are currently having technical issues with {0}. Please try again later."),
                                    provider.getDisplayName()),
                            false);
                    wasCircuitBreaked = true;
                    return AgentWorkerCommandResult.ABORT;
                } else if (Objects.equal(circuitBreakerConfiguration.getMode(), CircuitBreakerMode.TEST)) {
                    log.info(String.format(
                            "[EVALUATION MODE] Provider: %s, Multiplication factor: %s",
                            credentials.getProviderName(),
                            circuitBreakerStatus.getRateLimitMultiplicationFactor()));
                }
            }
        }

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() throws Exception {
        final CircuitBreakerStatistics circuitBreakerStatistics = state.getCircuitBreakerStatistics().get(
                context.getRequest().getProvider());

        // Register errors.

        if (!wasCircuitBreaked) {
            switch (context.getRequest().getCredentials().getStatus()) {
            case TEMPORARY_ERROR:
                circuitBreakerStatistics.registerError();
                break;
            default:
                circuitBreakerStatistics.registerSuccess();
                break;
            }
        }
    }
}
