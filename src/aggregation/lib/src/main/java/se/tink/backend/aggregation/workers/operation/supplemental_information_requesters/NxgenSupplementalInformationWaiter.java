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
import se.tink.backend.aggregation.workers.operation.RequestStatus;
import se.tink.backend.aggregation.workers.operation.RequestStatusManager;
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
    private final RequestStatusManager requestStatusManager;

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
            RequestStatusManager requestStatusManager) {
        this.metricRegistry = metricRegistry;
        this.request = request;
        this.coordinationClient = coordinationClient;
        this.clusterId = clusterId;
        this.appId = appId;
        this.supplementalInformationController = supplementalInformationController;
        this.requestStatusManager = requestStatusManager;
    }

    @Override
    public Optional<String> waitForSupplementalInformation(
            String mfaId, long waitFor, TimeUnit unit, String initiator, String market) {
        SupplementalInformationWaiterFinalStatus finalStatus =
                SupplementalInformationWaiterFinalStatus.NONE;
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
                        finalStatus = SupplementalInformationWaiterFinalStatus.CANCELLED;
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
                        finalStatus = SupplementalInformationWaiterFinalStatus.FINISHED_WITH_EMPTY;
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
                        finalStatus = SupplementalInformationWaiterFinalStatus.FINISHED;
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
            finalStatus = SupplementalInformationWaiterFinalStatus.TIMED_OUT;
            // Did not get lock, release anyways and return.
            lock.removeBarrier();
        } catch (SupplementalInfoException e) {
            logger.debug(
                    "triggerRollbackIfOperationIsCancelled triggered a rollback by throwing exception");
            finalStatus = SupplementalInformationWaiterFinalStatus.CANCELLED_NXGEN;
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
            finalStatus = SupplementalInformationWaiterFinalStatus.ERROR;
        } finally {
            // Always clean up the supplemental information
            Credentials credentials = request.getCredentials();
            credentials.setSupplementalInformation(null);
            stopwatch.stop();
            if (overheadTime.isRunning()) {
                overheadTime.stop();
            }
            SupplementalInformationMetrics.observeTotalTime(
                    metricRegistry,
                    stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000,
                    initiator,
                    getClass().getName(),
                    market,
                    finalStatus);
            SupplementalInformationMetrics.observeOverheadTime(
                    metricRegistry,
                    overheadTime.elapsed(TimeUnit.MILLISECONDS),
                    initiator,
                    getClass().getName(),
                    finalStatus);
        }
        logger.info("Supplemental information (empty) will be returned");
        return Optional.empty();
    }

    private void triggerRollbackIfOperationIsCancelled(DistributedBarrier lock) throws Exception {
        // TODO (AAP-1301): We will use requestId when the Payments team is ready
        String requestId = request.getCredentials().getId();
        RequestStatus requestStatus =
                requestStatusManager
                        .get(requestId)
                        .orElseGet(
                                () -> {
                                    logger.error("Request status does not exist");
                                    return RequestStatus.STARTED;
                                });

        if (RequestStatus.STARTED.equals(requestStatus)) {
            return;
        }

        if (RequestStatus.IMPOSSIBLE_TO_ABORT.equals(requestStatus)) {
            logger.debug(
                    "Waiting for supplemental information cannot be aborted because status is {}",
                    RequestStatus.IMPOSSIBLE_TO_ABORT);
            return;
        }

        if (RequestStatus.TRYING_TO_ABORT.equals(requestStatus)) {
            lock.removeBarrier();
            requestStatusManager.set(requestId, RequestStatus.ABORTING);
            logger.info("Status is set to ABORTING, throwing ABORTED exception");
            throw SupplementalInfoError.ABORTED.exception();
        }

        logger.warn("Unexpected request status {}", requestStatus);
    }
}
