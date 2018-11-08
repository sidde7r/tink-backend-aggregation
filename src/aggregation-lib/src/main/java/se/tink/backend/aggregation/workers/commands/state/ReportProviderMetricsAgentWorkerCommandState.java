package se.tink.backend.aggregation.workers.commands.state;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import javax.inject.Inject;
import se.tink.backend.aggregation.workers.metrics.MeterCacheLoader;
import se.tink.backend.aggregation.workers.metrics.TimerCacheLoader;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;

public class ReportProviderMetricsAgentWorkerCommandState {
    private LoadingCache<MetricId.MetricLabels, Counter> authenticationErrorMeters;
    private LoadingCache<MetricId.MetricLabels, Counter> executionsMeters;
    private LoadingCache<MetricId.MetricLabels, Timer> queuedTimers;
    private LoadingCache<MetricId.MetricLabels, Counter> temporaryErrorMeters;
    private LoadingCache<MetricId.MetricLabels, Timer> executionsTimers;
    private LoadingCache<MetricId.MetricLabels, Timer> globalExecutionsTimers;

    @Inject
    public ReportProviderMetricsAgentWorkerCommandState(MetricRegistry metricRegistry) {
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();

        executionsMeters = cacheBuilder.build(new MeterCacheLoader(metricRegistry, "executions"));
        temporaryErrorMeters = cacheBuilder.build(new MeterCacheLoader(metricRegistry, "temporary_errors"));
        authenticationErrorMeters = cacheBuilder
                .build(new MeterCacheLoader(metricRegistry, "authentication_errors"));

        queuedTimers = cacheBuilder.build(new TimerCacheLoader(metricRegistry, "queue_duration"));
        executionsTimers = cacheBuilder.build(new TimerCacheLoader(metricRegistry, "executions_duration"));
        globalExecutionsTimers = cacheBuilder
                .build(new TimerCacheLoader(metricRegistry, "executions_global_duration"));
    }

    public LoadingCache<MetricId.MetricLabels, Counter> getAuthenticationErrorMeters() {
        return authenticationErrorMeters;
    }

    public LoadingCache<MetricId.MetricLabels, Counter> getExecutionsMeters() {
        return executionsMeters;
    }

    public LoadingCache<MetricId.MetricLabels, Timer> getQueuedTimers() {
        return queuedTimers;
    }

    public LoadingCache<MetricId.MetricLabels, Counter> getTemporaryErrorMeters() {
        return temporaryErrorMeters;
    }

    public LoadingCache<MetricId.MetricLabels, Timer> getExecutionsTimers() {
        return executionsTimers;
    }

    public LoadingCache<MetricId.MetricLabels, Timer> getGlobalExecutionsTimers() {
        return globalExecutionsTimers;
    }
}
