package se.tink.backend.aggregation.workers.commands;

import com.google.common.collect.Lists;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.DeprecatedRefreshExecutor;
import se.tink.backend.aggregation.agents.RefreshExecutorUtils;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerOperationMetricType;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.metrics.RefreshMetricNameFactory;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.metrics.MetricId;

public class RefreshItemAgentWorkerCommand extends AgentWorkerCommand implements MetricsCommand {
    private static final Logger log = LoggerFactory.getLogger(RefreshItemAgentWorkerCommand.class);

    private static final String METRIC_NAME = "agent_refresh";
    private static final String METRIC_ACTION = "refresh";


    private final AgentWorkerCommandContext context;
    private final RefreshableItem item;
    private final AgentWorkerCommandMetricState metrics;

    public RefreshItemAgentWorkerCommand(AgentWorkerCommandContext context, RefreshableItem item,
            AgentWorkerCommandMetricState metrics) {
        this.context = context;
        this.item = item;
        this.metrics = metrics.init(this);
    }

    public RefreshableItem getRefreshableItem() {
        return item;
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
                log.info("Refreshing item: {}", item.name());

                Agent agent = context.getAgent();
                if (agent instanceof DeprecatedRefreshExecutor) {
                    ((DeprecatedRefreshExecutor) agent).refresh();
                } else {
                    RefreshExecutorUtils.executeSegregatedRefresher(agent, item, context);
                }
                action.completed();
            } catch(BankServiceException e) {
                    // The way frontend works now the message will not be displayed to the user.
                    context.updateStatus(CredentialsStatus.UNCHANGED, context.getCatalog().getString(e.getUserMessage()));
                    action.unavailable();
                    return AgentWorkerCommandResult.ABORT;
            } catch (Exception e) {
                action.failed();
                log.warn("Couldn't refresh RefreshableItem({})", item);

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
                .add("class", RefreshItemAgentWorkerCommand.class.getSimpleName())
                .add("item", RefreshMetricNameFactory.createCleanName(item))
                .add("command", type.getMetricName());

        return Lists.newArrayList(typeName);
    }
}
