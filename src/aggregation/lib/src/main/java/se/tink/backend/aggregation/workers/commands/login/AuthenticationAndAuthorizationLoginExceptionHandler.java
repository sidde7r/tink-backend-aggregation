package se.tink.backend.aggregation.workers.commands.login;

import java.util.Optional;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;

public class AuthenticationAndAuthorizationLoginExceptionHandler
        extends AbstractLoginExceptionHandler {

    public AuthenticationAndAuthorizationLoginExceptionHandler(
            StatusUpdater statusUpdater, AgentWorkerCommandContext context) {
        super(statusUpdater, context, CredentialsStatus.AUTHENTICATION_ERROR);
    }

    @Override
    protected Optional<AgentWorkerCommandResult> handle(
            Exception exception, MetricActionIface metricAction) {
        if (exception instanceof AuthenticationException
                || exception instanceof AuthorizationException) {
            metricAction.cancelled();
            return Optional.of(AgentWorkerCommandResult.ABORT);
        }
        return Optional.empty();
    }
}
