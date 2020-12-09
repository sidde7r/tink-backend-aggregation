package se.tink.backend.aggregation.workers.commands;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.events.DataTrackerEventProducer;
import se.tink.backend.aggregation.workers.commands.metrics.MetricsCommand;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.operation.type.AgentWorkerOperationMetricType;
import se.tink.backend.integration.agent_data_availability_tracker.client.AgentDataAvailabilityTrackerClient;
import se.tink.backend.integration.agent_data_availability_tracker.serialization.AccountTrackingSerializer;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.pair.Pair;

public class SendAccountsToDataAvailabilityTrackerAgentWorkerCommand extends AgentWorkerCommand
        implements MetricsCommand {
    private static final Logger log =
            LoggerFactory.getLogger(SendAccountsToDataAvailabilityTrackerAgentWorkerCommand.class);

    private static final String METRIC_NAME = "data_availability_tracker_refresh";
    private static final String METRIC_ACTION = "send_refresh_data_to_data_availability_tracker";

    private final AgentWorkerCommandContext context;
    private final AgentWorkerCommandMetricState metrics;

    private final AgentDataAvailabilityTrackerClient agentDataAvailabilityTrackerClient;
    private final DataTrackerEventProducer dataTrackerEventProducer;

    private final String agentName;
    private final String provider;
    private final String market;

    public SendAccountsToDataAvailabilityTrackerAgentWorkerCommand(
            AgentWorkerCommandContext context,
            AgentWorkerCommandMetricState metrics,
            AgentDataAvailabilityTrackerClient agentDataAvailabilityTrackerClient,
            DataTrackerEventProducer dataTrackerEventProducer) {
        this.context = context;
        this.metrics = metrics.init(this);
        this.agentDataAvailabilityTrackerClient = agentDataAvailabilityTrackerClient;
        this.dataTrackerEventProducer = dataTrackerEventProducer;
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
    protected AgentWorkerCommandResult doExecute() throws Exception {
        metrics.start(AgentWorkerOperationMetricType.EXECUTE_COMMAND);
        try {
            MetricAction action =
                    metrics.buildAction(new MetricId.MetricLabels().add("action", METRIC_ACTION));
            try {

                if (!Strings.isNullOrEmpty(market)) {

                    context.getCachedAccountsWithFeatures()
                            .forEach(pair -> processForDataTracker(pair.first, pair.second));

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

    private void processForDataTracker(final Account account, final AccountFeatures features) {
        agentDataAvailabilityTrackerClient.sendAccount(
                agentName, provider, market, account, features);

        AccountTrackingSerializer serializer =
                agentDataAvailabilityTrackerClient.serializeAccount(account, features);

        List<Pair<String, Boolean>> eventData = new ArrayList<>();

        serializer
                .buildList()
                .forEach(
                        entry -> {
                            if (entry.getName().endsWith(".identifiers")) {
                                eventData.add(
                                        new Pair<String, Boolean>(
                                                entry.getName()
                                                        + "."
                                                        + entry.getValue().replace("-", ""),
                                                true));
                            } else {
                                eventData.add(
                                        new Pair<String, Boolean>(
                                                entry.getName(),
                                                !entry.getValue().equalsIgnoreCase("null")));
                            }
                        });

        dataTrackerEventProducer.sendDataTrackerEvent(
                context.getRequest().getCredentials().getProviderName(),
                context.getCorrelationId(),
                eventData,
                context.getAppId(),
                context.getClusterId(),
                context.getRequest().getCredentials().getUserId());
    }

    @Override
    protected void doPostProcess() throws Exception {
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
