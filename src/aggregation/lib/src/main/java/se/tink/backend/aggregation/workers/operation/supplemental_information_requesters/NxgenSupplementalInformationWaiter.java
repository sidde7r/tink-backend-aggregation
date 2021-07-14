package se.tink.backend.aggregation.workers.operation.supplemental_information_requesters;

import com.google.common.base.Stopwatch;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.controllers.SupplementalInformationController;
import se.tink.backend.aggregation.locks.BarrierName;
import se.tink.backend.aggregation.workers.operation.OperationStatus;
import se.tink.backend.aggregation.workers.operation.OperationStatusManager;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.metrics.registry.MetricRegistry;

public class NxgenSupplementalInformationWaiter implements SupplementalInformationWaiter {

    private static final Logger logger =
            LoggerFactory.getLogger(NxgenSupplementalInformationWaiter.class);

    private final MetricRegistry metricRegistry;
    private final CredentialsRequest request;
    private final CuratorFramework coordinationClient;
    private final String clusterId;
    private final String appId;
    private final SupplementalInformationController supplementalInformationController;
    private final OperationStatusManager operationStatusManager;

    /**
     * In waitForSupplementalInformation method, if the value for "waitFor" parameter is for example
     * 500 and WAITING_PERIOD_FOR_SUPPLEMENTAL_INFO_IN_SINGLE_ITERATION = 10 then we will wait for a
     * supplemental information for 500 seconds in total but in every 10 seconds we will stop
     * waiting, check if the flow that triggered this supplementalInformation wait is cancelled and
     * if not so and if there is no supplementalInformation received then we will set the barrier
     * again to wait for an additional 10 seconds.
     */
    private static final long WAITING_PERIOD_IN_SECONDS_FOR_SUPPLEMENTAL_INFO_IN_SINGLE_ITERATION =
            10;

    public NxgenSupplementalInformationWaiter(
            MetricRegistry metricRegistry,
            CredentialsRequest request,
            CuratorFramework coordinationClient,
            String clusterId,
            String appId,
            SupplementalInformationController supplementalInformationController,
            OperationStatusManager operationStatusManager) {
        this.metricRegistry = metricRegistry;
        this.request = request;
        this.coordinationClient = coordinationClient;
        this.clusterId = clusterId;
        this.appId = appId;
        this.supplementalInformationController = supplementalInformationController;
        this.operationStatusManager = operationStatusManager;
    }

    @Override
    public Optional<String> waitForSupplementalInformation(
            String mfaId, long waitFor, TimeUnit unit, String initiator) {
        DistributedBarrier lock =
                new DistributedBarrier(
                        coordinationClient,
                        BarrierName.build(BarrierName.Prefix.SUPPLEMENTAL_INFORMATION, mfaId));
        SupplementalInformationMetrics.inc(
                metricRegistry,
                SupplementalInformationMetrics.attempts,
                clusterId,
                initiator,
                getClass().getName());
        Stopwatch stopwatch = Stopwatch.createStarted();
        Stopwatch overheadTime = Stopwatch.createUnstarted();
        try {
            // Reset barrier.
            lock.removeBarrier();
            lock.setBarrier();
            overheadTime.start();
            triggerRollbackIfOperationIsCancelled(lock);
            overheadTime.stop();
            logger.info(
                    "Supplemental information request of key {} is waiting for {} {}",
                    mfaId,
                    waitFor,
                    unit);
            logger.info(
                    "[Supplemental Information] Credential Status: {}",
                    Optional.ofNullable(request.getCredentials())
                            .map(Credentials::getStatus)
                            .orElse(null));

            long numberOfMaxIterations =
                    (unit.toSeconds(waitFor)
                                    / WAITING_PERIOD_IN_SECONDS_FOR_SUPPLEMENTAL_INFO_IN_SINGLE_ITERATION)
                            + 1;
            long waitingPeriodInSecondsPerIteration =
                    Math.min(
                            unit.toSeconds(waitFor),
                            WAITING_PERIOD_IN_SECONDS_FOR_SUPPLEMENTAL_INFO_IN_SINGLE_ITERATION);
            for (int i = 0; i < numberOfMaxIterations; i++) {
                logger.debug("[waitForSupplementalInformation] Iteration {}", i);
                if (lock.waitOnBarrier(waitingPeriodInSecondsPerIteration, TimeUnit.SECONDS)) {
                    logger.debug(
                            "[waitForSupplementalInformation] passed the barrier without timeout");
                    String result =
                            supplementalInformationController.getSupplementalInformation(mfaId);

                    if (Objects.isNull(result) || Objects.equals(result, "null")) {
                        SupplementalInformationMetrics.inc(
                                metricRegistry,
                                SupplementalInformationMetrics.cancelled,
                                clusterId,
                                initiator,
                                getClass().getName());
                        logger.info(
                                "Supplemental information request was cancelled by client (returned null)");
                        return Optional.empty();
                    }

                    if ("".equals(result)) {
                        logger.info(
                                "Supplemental information response (empty!) has been received for provider: {}, from appid: {}",
                                request.getProvider().getName(),
                                appId);
                        SupplementalInformationMetrics.inc(
                                metricRegistry,
                                SupplementalInformationMetrics.finished_with_empty,
                                clusterId,
                                initiator,
                                getClass().getName());
                    } else {
                        if ("{}".equals(result)) {
                            logger.info(
                                    "Supplemental information response (empty map) has been received");
                        } else {
                            logger.info(
                                    "Supplemental information response (non-null &  non-empty) has been received");
                        }
                        SupplementalInformationMetrics.inc(
                                metricRegistry,
                                SupplementalInformationMetrics.finished,
                                clusterId,
                                initiator,
                                getClass().getName());
                    }

                    return Optional.of(result);
                }
                overheadTime.start();
                logger.debug(
                        "[waitForSupplementalInformation] timed-out while waiting for the barrier, timeout period is {} seconds",
                        waitingPeriodInSecondsPerIteration);
                triggerRollbackIfOperationIsCancelled(lock);
                overheadTime.stop();
            }

            /*
             We tried to get a supplemental information "numberOfMaxIterations" times
             but none of them worked so we ended up with time-out
            */
            logger.info("Supplemental information request timed out");
            SupplementalInformationMetrics.inc(
                    metricRegistry,
                    SupplementalInformationMetrics.timedOut,
                    clusterId,
                    initiator,
                    getClass().getName());
            // Did not get lock, release anyways and return.
            lock.removeBarrier();
        } catch (SupplementalInfoException e) {
            logger.debug(
                    "triggerRollbackIfOperationIsCancelled triggered a rollback by throwing exception");
            throw e;
        } catch (Exception e) {
            try {
                lock.removeBarrier();
            } catch (Exception ex) {
                logger.error("Exception while trying to remove barrier", e);
            }
            logger.error("Caught exception while waiting for supplemental information", e);
            SupplementalInformationMetrics.inc(
                    metricRegistry,
                    SupplementalInformationMetrics.error,
                    clusterId,
                    initiator,
                    getClass().getName());
        } finally {
            // Always clean up the supplemental information
            Credentials credentials = request.getCredentials();
            credentials.setSupplementalInformation(null);
            stopwatch.stop();
            if (overheadTime.isRunning()) {
                overheadTime.stop();
            }
            SupplementalInformationMetrics.observe(
                    metricRegistry,
                    SupplementalInformationMetrics.duration,
                    stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000,
                    initiator,
                    getClass().getName());
            SupplementalInformationMetrics.observe(
                    metricRegistry,
                    SupplementalInformationMetrics.overhead_duration,
                    overheadTime.elapsed(TimeUnit.MILLISECONDS) / 1000,
                    initiator,
                    getClass().getName());
        }
        logger.info("Supplemental information (empty) will be returned");
        return Optional.empty();
    }

    private void triggerRollbackIfOperationIsCancelled(DistributedBarrier lock) throws Exception {
        logger.debug("Checking status for operation with id {}", request.getOperationId());
        // TODO (AAP-1301): We will use operationId when the Payments team is ready
        OperationStatus operationStatus =
                operationStatusManager
                        .get(request.getCredentials().getId())
                        .orElseThrow(
                                () -> new IllegalStateException("Operation state does not exist!"));
        logger.debug(
                "Status for operation with id {} is {}", request.getOperationId(), operationStatus);
        if (OperationStatus.TRYING_TO_ABORT.equals(operationStatus)) {
            logger.debug(
                    "For operation with id {}, trying to remove barrier", request.getOperationId());
            lock.removeBarrier();
            logger.debug(
                    "For operation with id {}, barrier is removed, setting status to ABORTING",
                    request.getOperationId());
            // TODO (AAP-1301): We will use operationId when the Payments team is ready
            operationStatusManager.set(request.getCredentials().getId(), OperationStatus.ABORTING);
            logger.debug(
                    "For operation with id {}, status is set to ABORTING. Throwing ABORTED exception",
                    request.getOperationId());
            throw SupplementalInfoError.ABORTED.exception();
        }
        // just for defensive programming
        else if (!OperationStatus.STARTED.equals(operationStatus)) {
            logger.error(
                    "There is a problem, status must have been either TRYING_TO_ABORT or STARTED!");
            logger.debug(
                    "For operation with id {}, trying to remove barrier", request.getOperationId());
            lock.removeBarrier();
            logger.debug(
                    "For operation with id {}, barrier is removed, throwing IllegalStateException",
                    request.getOperationId());
            throw new IllegalStateException(
                    String.format(
                            "Operation status is %s, in waitForSupplementalInformation method which is not a valid state ",
                            operationStatus));
        }
    }
}
