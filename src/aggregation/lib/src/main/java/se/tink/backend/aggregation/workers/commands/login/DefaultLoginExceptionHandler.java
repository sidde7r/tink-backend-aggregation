package se.tink.backend.aggregation.workers.commands.login;

import java.util.Optional;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;

public class DefaultLoginExceptionHandler extends AbstractLoginExceptionHandler {

    public DefaultLoginExceptionHandler(
            StatusUpdater statusUpdater, AgentWorkerCommandContext context) {
        super(statusUpdater, context, CredentialsStatus.TEMPORARY_ERROR);
    }

    @Override
    protected Optional<AgentWorkerCommandResult> handle(
            Exception exception, MetricActionIface metricAction) {
        metricAction.failed();
        return Optional.of(AgentWorkerCommandResult.ABORT);
    }
}
