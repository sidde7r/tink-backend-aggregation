package se.tink.backend.aggregation.workers.commands;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.workers.commands.metrics.MetricsCommand;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.operation.type.AgentWorkerOperationMetricType;
import se.tink.backend.aggregation.workers.refresh.ProcessableItem;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.metrics.core.MetricId;

public class SendDataForProcessingAgentWorkerCommand extends AgentWorkerCommand
        implements MetricsCommand {
    private static final Logger log =
            LoggerFactory.getLogger(SendDataForProcessingAgentWorkerCommand.class);
    private static final String METRIC_NAME = "send_data_to_system";

    private final AgentWorkerCommandContext context;
    private final AgentWorkerCommandMetricState metrics;
    private final List<ProcessableItem> processableItems;

    public SendDataForProcessingAgentWorkerCommand(
            AgentWorkerCommandContext context,
            AgentWorkerCommandMetricState metrics,
            Set<ProcessableItem> processableItems) {
        this.context = context;
        this.metrics = metrics.init(this);
        this.processableItems = ProcessableItem.sort(processableItems);
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() throws Exception {
        Credentials credentials = context.getRequest().getCredentials();

        if (credentials.getStatus() != CredentialsStatus.UPDATING) {
            log.warn("Credentials status does not warrant processing: {}", credentials.getStatus());
            return;
        }

        metrics.start(AgentWorkerOperationMetricType.POST_PROCESS_COMMAND);
        try {
            for (ProcessableItem processableItem : processableItems) {
                MetricAction action =
                        metrics.buildAction(
                                new MetricId.MetricLabels()
                                        .add("action", "process")
                                        .add("item", processableItem.name()));
                try {
                    log.info("Processing item: {}", processableItem.name());

                    switch (processableItem) {
                        case ACCOUNTS:
                            context.processAccounts();
                            break;
                        case TRANSACTIONS:
                            context.processTransactions();
                            break;
                        case EINVOICES:
                            context.processEinvoices();
                            break;
                        case TRANSFER_DESTINATIONS:
                            context.processTransferDestinationPatterns();
                            break;
                        default:
                            throw new IllegalArgumentException(
                                    "Not implemented! Developer should take action");
                    }
                    action.completed();
                } catch (RuntimeException e) {
                    action.failed();
                    log.warn("Couldn't process ProcessableItem({})", processableItem);

                    throw e;
                }
            }

        } finally {
            metrics.stop();
        }
    }

    @Override
    public String getMetricName() {
        return METRIC_NAME;
    }

    @Override
    public List<MetricId.MetricLabels> getCommandTimerName(AgentWorkerOperationMetricType type) {
        MetricId.MetricLabels typeName =
                new MetricId.MetricLabels()
                        .add("class", SendDataForProcessingAgentWorkerCommand.class.getSimpleName())
                        .add("command", type.getMetricName());

        return Lists.newArrayList(typeName);
    }
}
