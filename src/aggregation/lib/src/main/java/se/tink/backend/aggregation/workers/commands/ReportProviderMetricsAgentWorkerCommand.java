package se.tink.backend.aggregation.workers.commands;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.agents.rpc.ProviderStatuses;
import se.tink.backend.aggregation.workers.agent_metrics.AgentWorkerMetricReporter;
import se.tink.backend.aggregation.workers.commands.state.ReportProviderMetricsAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.metrics.core.MetricId;

public class ReportProviderMetricsAgentWorkerCommand extends AgentWorkerCommand {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private String operationName;
    private ReportProviderMetricsAgentWorkerCommandState state;
    private AgentWorkerMetricReporter metricReporter;
    private AgentWorkerCommandContext context;

    public ReportProviderMetricsAgentWorkerCommand(
            AgentWorkerCommandContext context,
            String operationName,
            ReportProviderMetricsAgentWorkerCommandState state,
            AgentWorkerMetricReporter metricReporter) {
        this.context = context;
        this.operationName = operationName;
        this.state = state;
        this.metricReporter = metricReporter;
    }

    private MetricId.MetricLabels constructProviderMetricLabels() {
        Provider provider = context.getRequest().getProvider();

        return new MetricId.MetricLabels()
                .add("provider_type", provider.getType().name().toLowerCase())
                .add("provider", provider.getName())
                .add("market", provider.getMarket())
                .add("operation", operationName);
    }

    private MetricId.MetricLabels constructCredentialsTypeMetricLabels() {
        Provider provider = context.getRequest().getProvider();

        return new MetricId.MetricLabels()
                .add("credential_type", provider.getCredentialsType().name().toLowerCase())
                .add("operation", operationName);
    }

    @Override
    protected AgentWorkerCommandResult doExecute() {
        context.setTimeLeavingQueue(System.currentTimeMillis());

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() throws Exception {
        Credentials credentials = context.getRequest().getCredentials();

        // Update the error meters.
        MetricId.MetricLabels typeMetricName = constructCredentialsTypeMetricLabels();
        MetricId.MetricLabels providerMetricName = constructProviderMetricLabels();

        ProviderStatuses providerStatus = context.getRequest().getProvider().getStatus();
        CredentialsStatus status = credentials.getStatus();
        if (status != null) {
            switch (status) {
                case TEMPORARY_ERROR:

                    // Do not log fail metric if provider is disabled.
                    if (providerStatus == ProviderStatuses.TEMPORARY_DISABLED
                            || providerStatus == ProviderStatuses.DISABLED) {

                        break;
                    }

                    state.getTemporaryErrorMeters().get(providerMetricName).inc();
                    break;
                case AUTHENTICATION_ERROR:
                    state.getAuthenticationErrorMeters().get(providerMetricName).inc();

                    break;
                case UPDATED:
                case UPDATING:
                    long queuedTime = context.getTimeLeavingQueue() - context.getTimePutOnQueue();
                    long executionTime = System.currentTimeMillis() - context.getTimeLeavingQueue();

                    logger.debug(
                            "Reporting metrics on successful execution (provider={}, operation={}, queued={}ms, execution={}ms)",
                            credentials.getProviderName(),
                            operationName,
                            queuedTime,
                            executionTime);

                    state.getQueuedTimers()
                            .get(providerMetricName)
                            .update(queuedTime, TimeUnit.MILLISECONDS);
                    state.getExecutionsTimers()
                            .get(providerMetricName)
                            .update(executionTime, TimeUnit.MILLISECONDS);
                    state.getGlobalExecutionsTimers()
                            .get(typeMetricName)
                            .update(executionTime, TimeUnit.MILLISECONDS);
                    break;
                default:
                    break;
            }
        }

        // Counts total executions finished. Useful when calculating relative errors from meters
        // above.
        state.getExecutionsMeters().get(providerMetricName).inc();

        // Updates aggregated metrics - it's here since we want it to
        // trace the same amount of calls as the above solution
        try {
            metricReporter.observe(context, operationName);
        } catch (Exception e) {
            logger.warn("Could not update RealTimeBankMetrics", e);
        }
    }
}
