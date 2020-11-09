package se.tink.backend.aggregation.workers.commands.login;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;

public class DefaultLoginExceptionHandler extends AbstractLoginExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(DefaultLoginExceptionHandler.class);

    private final AgentLoginEventPublisherService agentLoginEventPublisherService;

    public DefaultLoginExceptionHandler(
            StatusUpdater statusUpdater,
            AgentWorkerCommandContext context,
            AgentLoginEventPublisherService agentLoginEventPublisherService) {
        super(statusUpdater, context, CredentialsStatus.TEMPORARY_ERROR);
        this.agentLoginEventPublisherService = agentLoginEventPublisherService;
    }

    @Override
    protected Optional<AgentWorkerCommandResult> handle(
            Exception exception, MetricActionIface metricAction) {
        log.error(exception.getMessage(), exception);
        metricAction.failed();
        agentLoginEventPublisherService.publishLoginErrorUnknown();
        return Optional.of(AgentWorkerCommandResult.ABORT);
    }
}
