package se.tink.backend.aggregation.workers.commands.login;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;

public class AuthorizationLoginExceptionHandler extends AbstractLoginExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final AgentLoginEventPublisherService agentLoginEventPublisherService;

    public AuthorizationLoginExceptionHandler(
            StatusUpdater statusUpdater,
            AgentWorkerCommandContext context,
            AgentLoginEventPublisherService agentLoginEventPublisherService) {
        super(statusUpdater, context, CredentialsStatus.AUTHENTICATION_ERROR);
        this.agentLoginEventPublisherService = agentLoginEventPublisherService;
    }

    @Override
    protected Optional<AgentWorkerCommandResult> handle(
            Exception exception, MetricActionIface metricAction) {
        if (exception instanceof AuthorizationException) {
            metricAction.cancelled();
            logger.info("Authorization Error", exception);
            agentLoginEventPublisherService.publishLoginAuthorizationErrorEvent(
                    (AuthorizationException) exception);
            return Optional.of(AgentWorkerCommandResult.ABORT);
        }
        return Optional.empty();
    }
}
