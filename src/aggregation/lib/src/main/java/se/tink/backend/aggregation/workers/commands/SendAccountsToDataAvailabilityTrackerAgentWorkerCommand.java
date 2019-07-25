package se.tink.backend.aggregation.workers.commands;

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
import se.tink.backend.integration.agent_data_availability_tracker.client.AgentDataAvailabilityTrackerClientFactory;
import se.tink.backend.integration.agent_data_availability_tracker.client.AgentDataAvailabilityTrackerConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.metrics.MetricId;

public class SendAccountsToDataAvailabilityTrackerAgentWorkerCommand extends AgentWorkerCommand
        implements MetricsCommand {
    private static final Logger log =
            LoggerFactory.getLogger(SendAccountsToDataAvailabilityTrackerAgentWorkerCommand.class);

    /*
     *  Temporary limitation to prevent client running on all providers.
     */
    private static final String TEST_MARKET = "SE";

    private static final String METRIC_NAME = "agent_refresh";
    private static final String METRIC_ACTION = "send_accounts_to_data_availability_tracker";

    private final AgentWorkerCommandContext context;
    private final AgentWorkerCommandMetricState metrics;

    private final AgentDataAvailabilityTrackerClient agentDataAvailabilityTrackerClient;

    private final String agentName;

    public SendAccountsToDataAvailabilityTrackerAgentWorkerCommand(
            AgentWorkerCommandContext context, AgentWorkerCommandMetricState metrics) {
        this.context = context;
        this.metrics = metrics.init(this);

        AgentDataAvailabilityTrackerConfiguration configuration =
                context.getAgentsServiceConfiguration()
                        .getAgentDataAvailabilityTrackerConfiguration();

        CredentialsRequest request = context.getRequest();

        this.agentName = request.getProvider().getClassName();

        boolean forceMockClient = !TEST_MARKET.equalsIgnoreCase(request.getProvider().getMarket());

        this.agentDataAvailabilityTrackerClient =
                AgentDataAvailabilityTrackerClientFactory.getInstance()
                        .getClient(configuration, forceMockClient);
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

                agentDataAvailabilityTrackerClient.beginStream();
                context.getCachedAccountsWithFeatures()
                        .forEach(
                                pair ->
                                        agentDataAvailabilityTrackerClient.sendAccount(
                                                agentName, pair.first, pair.second));
                agentDataAvailabilityTrackerClient.endStreamBlocking();

                if (agentDataAvailabilityTrackerClient.isMockClient()) {

                    action.cancelled();
                } else {
                    action.completed();
                }

            } catch (Exception e) {
                action.failed();
                log.error("Failed sending accounts to tracking service.", e);
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
