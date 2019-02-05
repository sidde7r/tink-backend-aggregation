package se.tink.backend.aggregation.workers.commands;

import com.google.common.collect.Lists;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.AgentWorkerOperationMetricType;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.libraries.metrics.MetricId;

public class SendAccountsToUpdateServiceAgentWorkerCommand extends AgentWorkerCommand implements MetricsCommand {
    private static final Logger log = LoggerFactory.getLogger(SendAccountsToUpdateServiceAgentWorkerCommand.class);

    private static final String METRIC_NAME = "agent_refresh";
    private static final String METRIC_ACTION = "send_accounts";

    private final AgentWorkerCommandContext context;
    private final AgentWorkerCommandMetricState metrics;

    public SendAccountsToUpdateServiceAgentWorkerCommand(AgentWorkerCommandContext context,
            AgentWorkerCommandMetricState metrics) {
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
            );
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
    public void postProcess() throws Exception {
        // Deliberately left empty.
    }

    @Override
    public List<MetricId.MetricLabels> getCommandTimerName(AgentWorkerOperationMetricType type) {
        MetricId.MetricLabels typeName = new MetricId.MetricLabels()
                .add("class", SendAccountsToUpdateServiceAgentWorkerCommand.class.getSimpleName())
                .add("command", type.getMetricName());

        return Lists.newArrayList(typeName);
    }
}
