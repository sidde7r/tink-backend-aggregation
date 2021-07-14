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
import se.tink.backend.aggregation.controllers.SupplementalInformationController;
import se.tink.backend.aggregation.locks.BarrierName;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.metrics.registry.MetricRegistry;

public class LegacySupplementalInformationWaiter implements SupplementalInformationWaiter {

    private static final Logger logger =
            LoggerFactory.getLogger(LegacySupplementalInformationWaiter.class);

    private final MetricRegistry metricRegistry;
    private final CredentialsRequest request;
    private final CuratorFramework coordinationClient;
    private final String clusterId;
    private final String appId;
    private final SupplementalInformationController supplementalInformationController;

    public LegacySupplementalInformationWaiter(
            MetricRegistry metricRegistry,
            CredentialsRequest request,
            CuratorFramework coordinationClient,
            String clusterId,
            String appId,
            SupplementalInformationController supplementalInformationController) {
        this.metricRegistry = metricRegistry;
        this.request = request;
        this.coordinationClient = coordinationClient;
        this.clusterId = clusterId;
        this.appId = appId;
        this.supplementalInformationController = supplementalInformationController;
    }

    @Override
    public Optional<String> waitForSupplementalInformation(
            String mfaId, long waitFor, TimeUnit unit, String initiator) {
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
        try {
            // Reset barrier.
            lock.removeBarrier();
            lock.setBarrier();
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
            if (lock.waitOnBarrier(waitFor, unit)) {
                String result = supplementalInformationController.getSupplementalInformation(mfaId);

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
            } else {
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
            }
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
            SupplementalInformationMetrics.observe(
                    metricRegistry,
                    SupplementalInformationMetrics.duration,
                    stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000,
                    initiator,
                    getClass().getName(),
                    finalStatus);
        }
        logger.info("Supplemental information (empty) will be returned");
        return Optional.empty();
    }
}
