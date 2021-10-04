package se.tink.backend.aggregation.workers.operation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import se.tink.libraries.cache.CacheClient;
import se.tink.libraries.cache.CacheScope;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;

@Slf4j
public class RequestStatusManager {

    @VisibleForTesting
    static final String LOCK_PATH_TEMPLATE = "/locks/aggregation/RequestStatusManager/%s-%s";

    @VisibleForTesting static final int REQUEST_STATUS_TTL = (int) TimeUnit.MINUTES.toSeconds(20);

    private static final MetricId READ_AND_WRITE_DURATION =
            MetricId.newId("request_status_manager_read_and_set_duration");
    private static final MetricId READ_DURATION =
            MetricId.newId("request_status_manager_read_duration");
    private static final MetricId WRITE_DURATION =
            MetricId.newId("request_status_manager_write_duration");
    private static final List<Integer> BUCKETS_IN_MILLISECONDS =
            Arrays.asList(
                    0, 10, 20, 30, 40, 50, 60, 80, 100, 120, 240, 270, 300, 360, 420, 480, 600);

    private final CacheClient cacheClient;
    private final LockSupplier lockSupplier;
    private final MetricRegistry metricRegistry;

    @Inject
    public RequestStatusManager(
            CacheClient cacheClient, LockSupplier lockSupplier, MetricRegistry metricRegistry) {
        this.cacheClient = cacheClient;
        this.lockSupplier = lockSupplier;
        this.metricRegistry = metricRegistry;
    }

    public boolean setByCredentialsId(String credentialsId, RequestStatus newStatus) {
        Objects.requireNonNull(credentialsId);
        Objects.requireNonNull(newStatus);
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            return callWithLock(
                    getLock(credentialsId, CacheScope.REQUEST_STATUS_BY_CREDENTIALS_ID),
                    () -> {
                        setStatusToCache(
                                credentialsId,
                                CacheScope.REQUEST_STATUS_BY_CREDENTIALS_ID,
                                newStatus);
                        log.info("[RequestStatusManager] Set status to {}", newStatus);
                        return true;
                    });
        } catch (Exception e) {
            throw new RequestStatusManagerException("Could not set the status", e);
        } finally {
            stopwatch.stop();
            updateDurationInfo(WRITE_DURATION, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    public boolean compareAndSetByCredentialsId(
            String credentialsId, UnaryOperator<RequestStatus> mapper) {
        Objects.requireNonNull(credentialsId);
        Objects.requireNonNull(mapper);
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            return callWithLock(
                    getLock(credentialsId, CacheScope.REQUEST_STATUS_BY_CREDENTIALS_ID),
                    () -> {
                        Optional<RequestStatus> status =
                                getStatusFromCache(
                                        credentialsId, CacheScope.REQUEST_STATUS_BY_CREDENTIALS_ID);
                        if (!status.isPresent()) {
                            log.info("[RequestStatusManager] Cache miss!");
                            return false;
                        }
                        RequestStatus newStatus = mapper.apply(status.get());
                        setStatusToCache(
                                credentialsId,
                                CacheScope.REQUEST_STATUS_BY_CREDENTIALS_ID,
                                newStatus);
                        log.info("[RequestStatusManager] Set status to {}", newStatus);
                        return true;
                    });
        } catch (Exception e) {
            throw new RequestStatusManagerException("Could not set the status", e);
        } finally {
            stopwatch.stop();
            updateDurationInfo(READ_AND_WRITE_DURATION, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    public boolean compareAndSetByCredentialsId(
            String credentialsId, RequestStatus expected, RequestStatus newStatus) {
        Objects.requireNonNull(credentialsId);
        Objects.requireNonNull(expected);
        Objects.requireNonNull(newStatus);
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            return callWithLock(
                    getLock(credentialsId, CacheScope.REQUEST_STATUS_BY_CREDENTIALS_ID),
                    () -> {
                        Optional<RequestStatus> status =
                                getStatusFromCache(
                                        credentialsId, CacheScope.REQUEST_STATUS_BY_CREDENTIALS_ID);
                        if (!status.isPresent() || expected != status.get()) {
                            return false;
                        }
                        setStatusToCache(
                                credentialsId,
                                CacheScope.REQUEST_STATUS_BY_CREDENTIALS_ID,
                                newStatus);
                        log.info("[RequestStatusManager] Set status to {}", newStatus);
                        return true;
                    });
        } catch (Exception e) {
            throw new RequestStatusManagerException("Could not set the status", e);
        } finally {
            stopwatch.stop();
            updateDurationInfo(WRITE_DURATION, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    public Optional<RequestStatus> getByCredentialsId(String credentialsId) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            Optional<RequestStatus> status =
                    getStatusFromCache(credentialsId, CacheScope.REQUEST_STATUS_BY_CREDENTIALS_ID);
            if (!status.isPresent()) {
                log.info("[RequestStatusManager] Cache miss!");
            }
            return status;
        } finally {
            stopwatch.stop();
            updateDurationInfo(READ_DURATION, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    private Optional<RequestStatus> getStatusFromCache(
            String identifier, CacheScope identifierType) {
        return Optional.ofNullable(
                        cacheClient.get(CacheScope.valueOf(identifierType.name()), identifier))
                .map(cachedInteger -> RequestStatus.getStatus((Integer) cachedInteger));
    }

    private void setStatusToCache(
            String identifier, CacheScope identifierType, RequestStatus status)
            throws ExecutionException, InterruptedException {
        cacheClient
                .set(
                        CacheScope.valueOf(identifierType.name()),
                        identifier,
                        REQUEST_STATUS_TTL,
                        status.getIntValue())
                .get();
    }

    private InterProcessLock getLock(String identifier, CacheScope identifierType) {
        String lockPath = String.format(LOCK_PATH_TEMPLATE, identifier, identifierType);
        return lockSupplier.getLock(lockPath);
    }

    private static <T> T callWithLock(InterProcessLock lock, Callable<T> callable)
            throws Exception {
        try {
            lock.acquire();
            return callable.call();
        } finally {
            lock.release();
        }
    }

    private void updateDurationInfo(MetricId histogram, long duration) {
        this.metricRegistry.histogram(histogram, BUCKETS_IN_MILLISECONDS).update(duration);
    }
}
