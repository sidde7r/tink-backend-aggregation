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

public class SendAccountsHoldersToUpdateServiceAgentWorkerCommand extends AgentWorkerCommand
        implements MetricsCommand {
    private static final Logger log =
            LoggerFactory.getLogger(SendAccountsHoldersToUpdateServiceAgentWorkerCommand.class);

    private static final String METRIC_NAME = "agent_refresh";
    static final String METRIC_ACTION = "send_accounts_holders";

    private final AgentWorkerCommandContext context;
    private final AgentWorkerCommandMetricState metrics;

    public SendAccountsHoldersToUpdateServiceAgentWorkerCommand(
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
        MetricAction action = null;
        try {
            action = metrics.buildAction(new MetricId.MetricLabels().add("action", METRIC_ACTION));

            log.info("Sending accounts holders to UpdateService");

            context.sendAllCachedAccountsHoldersToUpdateService();

            action.completed();
        } catch (Exception e) {
            if (action != null) {
                action.failed();
            }
            // don't fail refresh if account holder information is not updated
            log.warn("Couldn't send accounts holders to UpdateService", e);

        } finally {
            metrics.stop();
        }

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() throws Exception {
        // left empty on purpose
    }

    @Override
    public List<MetricId.MetricLabels> getCommandTimerName(AgentWorkerOperationMetricType type) {
        MetricId.MetricLabels typeName =
                new MetricId.MetricLabels()
                        .add(
                                "class",
                                SendAccountsHoldersToUpdateServiceAgentWorkerCommand.class
                                        .getSimpleName())
                        .add("command", type.getMetricName());

        return Lists.newArrayList(typeName);
    }
}
