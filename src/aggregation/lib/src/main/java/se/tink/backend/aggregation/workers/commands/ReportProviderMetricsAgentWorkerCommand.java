package se.tink.backend.aggregation.workers.commands;

import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.agents.rpc.ProviderStatuses;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.workers.commands.state.ReportProviderMetricsAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.metrics.core.MetricId;

public class ReportProviderMetricsAgentWorkerCommand extends AgentWorkerCommand {
    private static final AggregationLogger log =
            new AggregationLogger(ReportProviderMetricsAgentWorkerCommand.class);

    private String operationName;
    private ReportProviderMetricsAgentWorkerCommandState state;
    private AgentWorkerCommandContext context;

    public ReportProviderMetricsAgentWorkerCommand(
            AgentWorkerCommandContext context,
            String operationName,
            ReportProviderMetricsAgentWorkerCommandState state) {
        this.context = context;
        this.operationName = operationName;
        this.state = state;
    }

    private MetricId.MetricLabels constructProviderMetricLabels() {
        Provider provider = context.getRequest().getProvider();

        return new MetricId.MetricLabels()
                .add("provider_type", provider.getType().name().toLowerCase())
                .add("provider", cleanMetricName(provider.getName()))
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

                    log.debug(
                            String.format(
                                    "Reporting metrics on successful execution (provider=%s, operation=%s, queued=%sms, execution=%sms)",
                                    credentials.getProviderName(),
                                    operationName,
                                    queuedTime,
                                    executionTime));

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
    }

    private static String cleanMetricName(String proposal) {
        return proposal.replace("'", "").replace("*", "").replace(")", "_").replace("(", "_");
    }
}
