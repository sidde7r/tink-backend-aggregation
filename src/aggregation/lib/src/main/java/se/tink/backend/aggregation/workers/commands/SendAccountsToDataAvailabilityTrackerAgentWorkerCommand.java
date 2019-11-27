package se.tink.backend.aggregation.workers.commands;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerOperationMetricType;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.integration.agent_data_availability_tracker.client.AgentDataAvailabilityTrackerClient;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.metrics.core.MetricId;

public class SendAccountsToDataAvailabilityTrackerAgentWorkerCommand extends AgentWorkerCommand
        implements MetricsCommand {
    private static final Logger log =
            LoggerFactory.getLogger(SendAccountsToDataAvailabilityTrackerAgentWorkerCommand.class);

    private static final String METRIC_NAME = "data_availability_tracker_refresh";
    private static final String METRIC_ACTION = "send_refresh_data_to_data_availability_tracker";

    private final AgentWorkerCommandContext context;
    private final AgentWorkerCommandMetricState metrics;

    private final AgentDataAvailabilityTrackerClient agentDataAvailabilityTrackerClient;

    private final String agentName;
    private final String provider;
    private final String market;

    private final ImmutableSet<String> ENABLED_MARKETS =
            ImmutableSet.<String>builder().add("SE", "GB", "ES", "DK", "NO", "BE").build();

    public SendAccountsToDataAvailabilityTrackerAgentWorkerCommand(
            AgentWorkerCommandContext context,
            AgentWorkerCommandMetricState metrics,
            AgentDataAvailabilityTrackerClient agentDataAvailabilityTrackerClient) {
        this.context = context;
        this.metrics = metrics.init(this);
        this.agentDataAvailabilityTrackerClient = agentDataAvailabilityTrackerClient;

        CredentialsRequest request = context.getRequest();

        this.agentName = request.getProvider().getClassName();
        this.provider = request.getProvider().getName();
        this.market = request.getProvider().getMarket();
    }

    @Override
    public String getMetricName() {
        return METRIC_NAME;
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        metrics.start(AgentWorkerOperationMetricType.EXECUTE_COMMAND);
        try {
            MetricAction action =
                    metrics.buildAction(new MetricId.MetricLabels().add("action", METRIC_ACTION));
            try {

                if (!Strings.isNullOrEmpty(market)
                        && ENABLED_MARKETS.contains(market.toUpperCase())) {

                    context.getCachedAccountsWithFeatures()
                            .forEach(
                                    pair ->
                                            agentDataAvailabilityTrackerClient.sendAccount(
                                                    agentName,
                                                    provider,
                                                    market,
                                                    pair.first,
                                                    pair.second));

                    if (context.getCachedIdentityData() != null) {
                        agentDataAvailabilityTrackerClient.sendIdentityData(
                                agentName, provider, market, context.getAggregationIdentityData());
                    }

                    action.completed();
                } else {

                    action.cancelled();
                }

            } catch (Exception e) {
                action.failed();
                log.error("Failed sending refresh data to tracking service.", e);
            }
        } finally {
            metrics.stop();
        }

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() throws Exception {
        // Deliberately left empty.
    }

    @Override
    public List<MetricId.MetricLabels> getCommandTimerName(AgentWorkerOperationMetricType type) {
        MetricId.MetricLabels typeName =
                new MetricId.MetricLabels()
                        .add(
                                "class",
                                SendAccountsToDataAvailabilityTrackerAgentWorkerCommand.class
                                        .getSimpleName())
                        .add("command", type.getMetricName());

        return Lists.newArrayList(typeName);
    }
}
