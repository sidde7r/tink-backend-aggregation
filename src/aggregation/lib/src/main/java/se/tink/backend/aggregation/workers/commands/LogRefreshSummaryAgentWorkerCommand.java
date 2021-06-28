package se.tink.backend.aggregation.workers.commands;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.summary.refresh.RefreshSummary;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;

@Slf4j
public class LogRefreshSummaryAgentWorkerCommand extends AgentWorkerCommand {

    private final AgentWorkerCommandContext context;

    public LogRefreshSummaryAgentWorkerCommand(AgentWorkerCommandContext context) {
        this.context = context;
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        RefreshSummary summary = context.getRefreshSummary();
        log.info("[REFRESH SUMMARY] isNull: {}", summary == null);
        if (summary != null) {
            log.info(summary.toJson());
        }

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() throws Exception {
        // Post processing not necessary
    }
}
