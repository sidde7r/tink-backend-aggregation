package se.tink.backend.aggregation.workers.commands;

import com.google.common.collect.Lists;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerContext;
import se.tink.backend.aggregation.workers.AgentWorkerOperationMetricType;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.refresh.ProcessableItem;
import se.tink.libraries.metrics.MetricId;

public class ProcessItemAgentWorkerCommand extends AgentWorkerCommand implements MetricsCommand {
    private static final Logger log = LoggerFactory.getLogger(ProcessItemAgentWorkerCommand.class);
    private static final MetricId.MetricLabels GLOBAL_METRIC_STUB = new MetricId.MetricLabels()
            .add("credential_type", "global");

    private static final String METRIC_NAME = "agent_refresh";
    private static final String METRIC_ACTION = "process";

    private final AgentWorkerContext context;
    private final ProcessableItem item;
    private final AgentWorkerCommandMetricState metrics;

    public ProcessItemAgentWorkerCommand(AgentWorkerContext context, ProcessableItem item,
            AgentWorkerCommandMetricState metrics) {
        this.item = item;
        this.context = context;
        this.metrics = metrics.init(this);
    }

    @Override
    public String getMetricName() {
        return METRIC_NAME;
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        metrics.start(AgentWorkerOperationMetricType.EXECUTE_COMMAND);
        try {
            MetricAction action = metrics.buildAction(
                    new MetricId.MetricLabels()
                            .add("action", METRIC_ACTION)
                            .add("item", item.name())
            );
            try {
                action.start();
                log.info("Processing item: {}", item.name());

                switch (item) {
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
                    throw new IllegalArgumentException("Not implemented! Developer should take action");
                }
                action.completed();
            } catch (Exception e) {
                action.failed();
                log.warn("Couldn't process ProcessableItem({})", item);

                throw e;
            } finally {
                action.stop();
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

        MetricId.MetricLabels globalName = GLOBAL_METRIC_STUB
                .add("class", ProcessItemAgentWorkerCommand.class.getSimpleName())
                .add("item", item.asMetricValue())
                .add("command", type.getMetricName());

        MetricId.MetricLabels typeName = new MetricId.MetricLabels()
                .add("class", ProcessItemAgentWorkerCommand.class.getSimpleName())
                .add("credential_type", context.getRequest().getCredentials().getType().name().toLowerCase())
                .add("item", item.asMetricValue())
                .add("command", type.getMetricName());

        return Lists.newArrayList(globalName, typeName);
    }
}
