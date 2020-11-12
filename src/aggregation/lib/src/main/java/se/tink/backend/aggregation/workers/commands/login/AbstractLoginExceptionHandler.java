package se.tink.backend.aggregation.workers.commands.login;

import com.google.common.base.Preconditions;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;

@RequiredArgsConstructor
@AllArgsConstructor
public abstract class AbstractLoginExceptionHandler implements LoginExceptionHandler {

    private final StatusUpdater statusUpdater;
    private final AgentWorkerCommandContext context;
    private CredentialsStatus credentialsStatus;

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
        Preconditions.checkNotNull(getCredentialsStatus(), "Credentials status has to be provided");
        if (exception instanceof AgentException) {
            statusUpdater.updateStatus(
                    getCredentialsStatus(),
                    context.getCatalog()
                            .getString((((AgentException) exception).getUserMessage())));
        } else {
            statusUpdater.updateStatus(getCredentialsStatus());
        }
    }

    protected CredentialsStatus getCredentialsStatus() {
        return credentialsStatus;
    }
}
