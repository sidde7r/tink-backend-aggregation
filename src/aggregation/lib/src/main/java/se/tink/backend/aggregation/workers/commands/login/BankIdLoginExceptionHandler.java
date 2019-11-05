package se.tink.backend.aggregation.workers.commands.login;

import java.util.Optional;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;

public class BankIdLoginExceptionHandler extends AbstractLoginExceptionHandler {

    public BankIdLoginExceptionHandler(
            StatusUpdater statusUpdater, AgentWorkerCommandContext context) {
        super(statusUpdater, context, CredentialsStatus.UNCHANGED);
    }

    @Override
    public Optional<AgentWorkerCommandResult> handle(
            Exception exception, MetricActionIface metricAction) {
        if (exception instanceof BankIdException) {
            metricAction.cancelled();
            return Optional.of(AgentWorkerCommandResult.ABORT);
        }
        return Optional.empty();
    }
}
