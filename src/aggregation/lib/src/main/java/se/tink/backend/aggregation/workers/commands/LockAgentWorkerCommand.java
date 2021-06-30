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
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.metrics.types.timers.Timer;

/*
   LockAgentWorkerCommand ensures exclusive access to the credentials in the current request
*/
public class LockAgentWorkerCommand extends AgentWorkerCommand {
    private static final Logger log = LoggerFactory.getLogger(LockAgentWorkerCommand.class);
    private static final String LOCK_FORMAT =
            "/locks/aggregation/LockAgentWorkerCommand/%s/%s"; // % (userId, credentialsId)
    private static final MetricId LOCKING_SUCCESS_METRIC =
            MetricId.newId("aggregation_locking_credentials_id");
    private static final MetricId LOCKING_TIMING_METRIC =
            MetricId.newId("aggregation_locking_worker_command_seconds");
    public static final String PHASE = "phase";
    private static final MetricId CREATE_LOCK_TIME = LOCKING_TIMING_METRIC.label(PHASE, "create");
    private static final MetricId ACQUIRE_LOCK_TIME = LOCKING_TIMING_METRIC.label(PHASE, "acquire");
    private static final MetricId RUNNING_LOCK_TIME = LOCKING_TIMING_METRIC.label(PHASE, "running");
    private static final MetricId RELEASE_LOCK_TIME = LOCKING_TIMING_METRIC.label(PHASE, "release");

    private static final MetricId RELEASE_METRIC = MetricId.newId("aggregation_locking_release");
    private static final MetricId SUCCESS_RELEASE_METRIC = RELEASE_METRIC.label("success", "true");
    private static final MetricId FAILED_RELEASE_METRIC = RELEASE_METRIC.label("success", "false");

    private final AgentWorkerCommandContext context;
    private boolean hasAcquiredLock;
    private final String operation;
    private final InterProcessSemaphoreMutexFactory interProcessSemaphoreMutexFactory;
    private final MetricRegistry metricRegistry;

    private InterProcessLock lock;
    private LoginAgentEventProducer loginAgentEventProducer;
    private Long startTime;
    private Timer.Context running;

    public LockAgentWorkerCommand(
            AgentWorkerCommandContext context,
            String operation,
            InterProcessSemaphoreMutexFactory interProcessSemaphoreMutexFactory) {
        this.context = context;
        this.operation = operation;
        this.interProcessSemaphoreMutexFactory = interProcessSemaphoreMutexFactory;
        this.metricRegistry = context.getMetricRegistry();
        running = null;
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

        Timer.Context created = metricRegistry.timer(CREATE_LOCK_TIME).time();
        lock =
                interProcessSemaphoreMutexFactory.createLock(
                        context.getCoordinationClient(),
                        String.format(LOCK_FORMAT, userId, credentialsId));
        created.stop();

        Timer.Context acquire = metricRegistry.timer(ACQUIRE_LOCK_TIME).time();
        hasAcquiredLock = lock.acquire(5, TimeUnit.SECONDS);
        acquire.stop();

        running = metricRegistry.timer(RUNNING_LOCK_TIME).time();
        log.info(
                "Lock(user: {} credentials: {}) is {} for operation: {}",
                userId,
                credentialsId,
                hasAcquiredLock ? "acquired" : "NOT acquired",
                operation);

        metricRegistry
                .meter(
                        LOCKING_SUCCESS_METRIC
                                .label("acquired", hasAcquiredLock ? "true" : "false")
                                .label("operation", operation)
                                .label("cluster_id", context.getClusterId()))
                .inc();

        if (!hasAcquiredLock) {
            emitLockFailedEvent();
        }

        return hasAcquiredLock
                ? AgentWorkerCommandResult.CONTINUE
                : AgentWorkerCommandResult.REJECT;
    }

    @Override
    protected void doPostProcess() {
        if (running != null) {
            running.stop();
        }
        Timer.Context unlock = metricRegistry.timer(RELEASE_LOCK_TIME).time();
        try {
            // If we never executed the command
            if (lock == null) {
                return;
            }

            // If we didn't aquire this lock (e.g. there is a timeout of 4 minutes)
            if (!hasAcquiredLock) {
                return;
            }
            // We're about to unlock - ensure we can see how long we remained locked.
            lock.release();
            metricRegistry.meter(SUCCESS_RELEASE_METRIC).inc();
        } catch (Exception e) {
            metricRegistry.meter(FAILED_RELEASE_METRIC).inc();
            log.error("Caught exception while releasing lock", e);
        } finally {
            unlock.stop();
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
