package se.tink.backend.aggregation.workers.commands.login;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;

public class BankServiceLoginExceptionHandler extends AbstractLoginExceptionHandler {

    private static final Logger LOG =
            LoggerFactory.getLogger(BankServiceLoginExceptionHandler.class);

    private final AgentLoginEventPublisherService agentLoginEventPublisherService;

    public BankServiceLoginExceptionHandler(
            StatusUpdater statusUpdater,
            AgentWorkerCommandContext context,
            AgentLoginEventPublisherService agentLoginEventPublisherService) {
        super(statusUpdater, context, CredentialsStatus.TEMPORARY_ERROR);
        this.agentLoginEventPublisherService = agentLoginEventPublisherService;
    }

    @Override
    protected Optional<AgentWorkerCommandResult> handle(
            Exception exception, MetricActionIface metricAction) {
        if (exception instanceof BankServiceException) {
            metricAction.unavailable();
            BankServiceException bankServiceException = (BankServiceException) exception;
            LOG.info(
                    "Bank service exception: {}",
                    bankServiceException.getMessage(),
                    bankServiceException);
            agentLoginEventPublisherService.publishLoginBankServiceErrorEvent(bankServiceException);
            return Optional.of(AgentWorkerCommandResult.ABORT);
        }
        return Optional.empty();
    }
}
