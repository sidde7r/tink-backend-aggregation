package se.tink.backend.aggregation.workers.commands.login;

import java.util.Optional;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;

public abstract class AbstractLoginExceptionHandler implements LoginExceptionHandler {

    private final StatusUpdater statusUpdater;
    private final AgentWorkerCommandContext context;
    private final CredentialsStatus credentialsStatus;

    public AbstractLoginExceptionHandler(
            StatusUpdater statusUpdater,
            AgentWorkerCommandContext context,
            CredentialsStatus credentialsStatus) {
        this.statusUpdater = statusUpdater;
        this.context = context;
        this.credentialsStatus = credentialsStatus;
    }

    @Override
    public Optional<AgentWorkerCommandResult> handleLoginException(
            Exception exception, MetricActionIface metricAction) {
        Optional<AgentWorkerCommandResult> result = handle(exception, metricAction);
        result.ifPresent((r) -> updateStatus(exception));
        return result;
    }

    protected abstract Optional<AgentWorkerCommandResult> handle(
            Exception exception, MetricActionIface metricAction);

    private void updateStatus(final Exception exception) {
        if (exception instanceof AgentException) {
            statusUpdater.updateStatus(
                    credentialsStatus,
                    context.getCatalog()
                            .getString((((AgentException) exception).getUserMessage())));
        } else {
            statusUpdater.updateStatus(credentialsStatus);
        }
    }
}
