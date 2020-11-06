package se.tink.backend.aggregation.workers.commands.login;

import java.util.Optional;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;

public class BankIdLoginExceptionHandler extends AbstractLoginExceptionHandler {

    private final AgentLoginEventPublisherService agentLoginEventPublisherService;

    public BankIdLoginExceptionHandler(
            StatusUpdater statusUpdater,
            AgentWorkerCommandContext context,
            AgentLoginEventPublisherService agentLoginEventPublisherService) {
        super(statusUpdater, context, CredentialsStatus.UNCHANGED);
        this.agentLoginEventPublisherService = agentLoginEventPublisherService;
    }

    @Override
    public Optional<AgentWorkerCommandResult> handle(
            Exception exception, MetricActionIface metricAction) {
        if (exception instanceof BankIdException) {
            metricAction.cancelled();
            agentLoginEventPublisherService.publishLoginBankIdErrorEvent(
                    (BankIdException) exception);
            return Optional.of(AgentWorkerCommandResult.ABORT);
        }
        return Optional.empty();
    }
}
