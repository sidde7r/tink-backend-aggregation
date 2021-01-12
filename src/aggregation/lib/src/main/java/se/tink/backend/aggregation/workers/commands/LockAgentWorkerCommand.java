package se.tink.backend.aggregation.workers.commands;

import java.util.concurrent.TimeUnit;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.events.IntegrationParameters;
import se.tink.backend.aggregation.events.LoginAgentEventProducer;
import se.tink.backend.aggregation.workers.concurrency.InterProcessSemaphoreMutexFactory;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto;
import se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult;
import se.tink.libraries.credentials.service.CredentialsRequest;

/*
   LockAgentWorkerCommand ensures exclusive access to the credentials in the current request
*/
public class LockAgentWorkerCommand extends AgentWorkerCommand {
    private static final Logger log = LoggerFactory.getLogger(LockAgentWorkerCommand.class);
    private static final String LOCK_FORMAT =
            "/locks/aggregation/LockAgentWorkerCommand/%s/%s"; // % (userId, credentialsId)

    private InterProcessLock lock;
    private AgentWorkerCommandContext context;
    private boolean hasAcquiredLock;
    private final String operation;
    private final InterProcessSemaphoreMutexFactory interProcessSemaphoreMutexFactory;

    private LoginAgentEventProducer loginAgentEventProducer;
    private Long startTime;

    public LockAgentWorkerCommand(
            AgentWorkerCommandContext context,
            String operation,
            InterProcessSemaphoreMutexFactory interProcessSemaphoreMutexFactory) {
        this.context = context;
        this.operation = operation;
        this.interProcessSemaphoreMutexFactory = interProcessSemaphoreMutexFactory;
    }

    public LockAgentWorkerCommand withLoginEvent(LoginAgentEventProducer loginAgentEventProducer) {
        this.loginAgentEventProducer = loginAgentEventProducer;
        this.startTime = System.nanoTime();
        return this;
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        CredentialsRequest request = context.getRequest();

        String userId = request.getUser().getId();
        String credentialsId = request.getCredentials().getId();

        lock =
                interProcessSemaphoreMutexFactory.createLock(
                        context.getCoordinationClient(),
                        String.format(LOCK_FORMAT, userId, credentialsId));

        hasAcquiredLock = lock.acquire(5, TimeUnit.SECONDS);

        log.info(
                "Lock(user: {} credentials: {}) is {} for operation: {}",
                userId,
                credentialsId,
                hasAcquiredLock ? "acquired" : "NOT acquired",
                operation);

        if (!hasAcquiredLock) {
            emitLockFailedEvent();
        }

        return hasAcquiredLock
                ? AgentWorkerCommandResult.CONTINUE
                : AgentWorkerCommandResult.REJECT;
    }

    @Override
    protected void doPostProcess() {
        try {
            // If we never executed the command
            if (lock == null) {
                return;
            }

            // If we didn't aquire this lock (e.g. there is a timeout of 4 minutes)
            if (!hasAcquiredLock) {
                return;
            }

            lock.release();
        } catch (Exception e) {
            log.error("Caught exception while releasing lock", e);
        }
    }

    private void emitLockFailedEvent() {
        if (loginAgentEventProducer != null) {
            long finishTime = System.nanoTime();
            long elapsedTime = finishTime - startTime;

            loginAgentEventProducer.sendLoginCompletedEvent(
                    IntegrationParameters.builder()
                            .providerName(context.getRequest().getCredentials().getProviderName())
                            .correlationId(context.getCorrelationId())
                            .appId(context.getAppId())
                            .clusterId(context.getClusterId())
                            .userId(context.getRequest().getCredentials().getUserId())
                            .build(),
                    LoginResult.COULD_NOT_LOCK_CREDENTIALS,
                    elapsedTime,
                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                            .UserInteractionInformation.AUTHENTICATED_WITHOUT_USER_INTERACTION);
        }
    }
}
