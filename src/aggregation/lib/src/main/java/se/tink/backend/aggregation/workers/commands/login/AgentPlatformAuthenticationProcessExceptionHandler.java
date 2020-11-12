package se.tink.backend.aggregation.workers.commands.login;

import java.util.Optional;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.AgentPlatformAuthenticationProcessException;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;

public class AgentPlatformAuthenticationProcessExceptionHandler
        extends AbstractLoginExceptionHandler {

    private final AgentLoginEventPublisherService agentLoginEventPublisherService;
    private CredentialsStatus credentialsStatus;

    public AgentPlatformAuthenticationProcessExceptionHandler(
            StatusUpdater statusUpdater,
            AgentWorkerCommandContext context,
            AgentLoginEventPublisherService agentLoginEventPublisherService) {
        super(statusUpdater, context);
        this.agentLoginEventPublisherService = agentLoginEventPublisherService;
    }

    @Override
    protected Optional<AgentWorkerCommandResult> handle(
            Exception exception, MetricActionIface metricAction) {
        if (exception instanceof AgentPlatformAuthenticationProcessException) {
            AgentPlatformAuthenticationProcessException
                    agentPlatformAuthenticationProcessException =
                            (AgentPlatformAuthenticationProcessException) exception;
            credentialsStatus =
                    agentPlatformAuthenticationProcessException
                            .getSourceAgentPlatformError()
                            .accept(
                                    new AgentPlatformAuthenticationProcessExceptionBankApiErrorVisitor(
                                            metricAction, agentLoginEventPublisherService));
            return Optional.of(AgentWorkerCommandResult.ABORT);
        }
        return Optional.empty();
    }

    @Override
    protected CredentialsStatus getCredentialsStatus() {
        return credentialsStatus;
    }
}
