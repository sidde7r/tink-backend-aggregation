package se.tink.backend.aggregation.workers.commands;

import com.google.common.collect.Lists;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.workers.commands.metrics.MetricsCommand;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.operation.type.AgentWorkerOperationMetricType;
import se.tink.libraries.metrics.core.MetricId;

public class SendAccountsToUpdateServiceAgentWorkerCommand extends AgentWorkerCommand
        implements MetricsCommand {
    private static final Logger log =
            LoggerFactory.getLogger(SendAccountsToUpdateServiceAgentWorkerCommand.class);

    private static final String METRIC_NAME = "agent_refresh";
    private static final String METRIC_ACTION = "send_accounts";

    private final AgentWorkerCommandContext context;
    private final AgentWorkerCommandMetricState metrics;

    public SendAccountsToUpdateServiceAgentWorkerCommand(
            AgentWorkerCommandContext context, AgentWorkerCommandMetricState metrics) {
        this.context = context;
        this.metrics = metrics.init(this);
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
                log.info("Sending accounts to UpdateService");

                context.sendAllCachedAccountsToUpdateService();

                action.completed();
            } catch (Exception e) {
                action.failed();
                log.warn("Couldn't send Accounts to UpdateService");

                throw e;
            }
        } finally {
            metrics.stop();
        }

        return AgentWorkerCommandResult.CONTINUE;
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
                                SendAccountsToUpdateServiceAgentWorkerCommand.class.getSimpleName())
                        .add("command", type.getMetricName());

        return Lists.newArrayList(typeName);
    }
}
