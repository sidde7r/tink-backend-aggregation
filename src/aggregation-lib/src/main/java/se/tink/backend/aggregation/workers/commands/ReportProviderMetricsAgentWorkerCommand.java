package se.tink.backend.aggregation.workers.commands;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerContext;
import se.tink.backend.aggregation.workers.metrics.MeterCacheLoader;
import se.tink.backend.aggregation.workers.metrics.TimerCacheLoader;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;
import se.tink.backend.common.utils.MetricsUtils;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.log.AggregationLogger;

public class ReportProviderMetricsAgentWorkerCommand extends AgentWorkerCommand {
    public static class ReportProviderMetricsAgentWorkerCommandState {
        private LoadingCache<MetricId.MetricLabels, Counter> authenticationErrorMeters;
        private LoadingCache<MetricId.MetricLabels, Counter> executionsMeters;
        private LoadingCache<MetricId.MetricLabels, Timer> queuedTimers;
        private LoadingCache<MetricId.MetricLabels, Counter> temporaryErrorMeters;
        private LoadingCache<MetricId.MetricLabels, Timer> executionsTimers;
        private LoadingCache<MetricId.MetricLabels, Timer> globalExecutionsTimers;

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

    private static final AggregationLogger log = new AggregationLogger(ReportProviderMetricsAgentWorkerCommand.class);

    private String operationName;
    private ReportProviderMetricsAgentWorkerCommandState state;
    private AgentWorkerContext context;

    public ReportProviderMetricsAgentWorkerCommand(AgentWorkerContext context,
            String operationName, ReportProviderMetricsAgentWorkerCommandState state) {
        this.context = context;
        this.operationName = operationName;
        this.state = state;
    }

    private MetricId.MetricLabels constructProviderMetricLabels() {
        Provider provider = context.getRequest().getProvider();

        return new MetricId.MetricLabels()
                .add("provider_type", provider.getType().name().toLowerCase())
                .add("provider", MetricsUtils.cleanMetricName(provider.getName()))
                .add("operation", operationName);
    }

    private MetricId.MetricLabels constructCredentialsTypeMetricLabels() {
        Provider provider = context.getRequest().getProvider();

        return new MetricId.MetricLabels()
                .add("credential_type", provider.getCredentialsType().name().toLowerCase())
                .add("operation", operationName);
    }

    @Override
    public AgentWorkerCommandResult execute() {
        context.setTimeLeavingQueue(System.currentTimeMillis());

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() throws Exception {
        Credentials credentials = context.getRequest().getCredentials();

        // Update the error meters.
        MetricId.MetricLabels typeMetricName = constructCredentialsTypeMetricLabels();
        MetricId.MetricLabels providerMetricName = constructProviderMetricLabels();

        CredentialsStatus status = credentials.getStatus();
        if (status != null) {
            switch (status) {
            case TEMPORARY_ERROR:
                state.getTemporaryErrorMeters().get(providerMetricName).inc();

                break;
            case AUTHENTICATION_ERROR:
                state.getAuthenticationErrorMeters().get(providerMetricName).inc();

                break;
            case UPDATED:
            case UPDATING:
                long queuedTime = context.getTimeLeavingQueue() - context.getTimePutOnQueue();
                long executionTime = System.currentTimeMillis() - context.getTimeLeavingQueue();

                log.debug(
                        String.format(
                                "Reporting metrics on successful execution (provider=%s, operation=%s, queued=%sms, execution=%sms)"
                                , credentials.getProviderName(), operationName, queuedTime, executionTime));

                state.getQueuedTimers().get(providerMetricName).update(queuedTime, TimeUnit.MILLISECONDS);
                state.getExecutionsTimers().get(providerMetricName).update(executionTime, TimeUnit.MILLISECONDS);
                state.getGlobalExecutionsTimers().get(typeMetricName).update(executionTime, TimeUnit.MILLISECONDS);
                break;
            default:
                break;
            }
        }

        // Counts total executions finished. Useful when calculating relative errors from meters above.
        state.getExecutionsMeters().get(providerMetricName).inc();
    }
}
