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
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.cache.CacheClient;
import se.tink.libraries.cache.CacheScope;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;

public class OperationStatusManager {

    @VisibleForTesting
    static final String LOCK_PATH_TEMPLATE = "/locks/aggregation/OperationStatusManager/%s";

    @VisibleForTesting static final int OPERATION_STATUS_TTL = (int) TimeUnit.MINUTES.toSeconds(20);

    private static final Logger logger = LoggerFactory.getLogger(OperationStatusManager.class);

    private static final MetricId READ_AND_WRITE_DURATION =
            MetricId.newId("operation_status_manager_read_and_set_duration");
    private static final MetricId READ_DURATION =
            MetricId.newId("operation_status_manager_read_duration");
    private static final MetricId WRITE_DURATION =
            MetricId.newId("operation_status_manager_write_duration");
    private static final List<Integer> BUCKETS_IN_MILLISECONDS =
            Arrays.asList(
                    0, 10, 20, 30, 40, 50, 60, 80, 100, 120, 240, 270, 300, 360, 420, 480, 600);

    private final CacheClient cacheClient;
    private final LockSupplier lockSupplier;
    private final MetricRegistry metricRegistry;

    @Inject
    public OperationStatusManager(
            CacheClient cacheClient, LockSupplier lockSupplier, MetricRegistry metricRegistry) {
        this.cacheClient = cacheClient;
        this.lockSupplier = lockSupplier;
        this.metricRegistry = metricRegistry;
    }

    public boolean setIfEmpty(String operationId, OperationStatus newStatus) {
        Objects.requireNonNull(operationId);
        Objects.requireNonNull(newStatus);
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            return callWithLock(
                    getLock(operationId),
                    () -> {
                        Optional<OperationStatus> optionalStatus = getStatusFromCache(operationId);
                        if (optionalStatus.isPresent()) {
                            return false;
                        }
                        setStatusToCache(operationId, newStatus);
                        logger.info("[OperationStatusManager] Set status to {}", newStatus);
                        return true;
                    });
        } catch (Exception e) {
            throw new OperationStatusManagerException("Could not set the status", e);
        } finally {
            stopwatch.stop();
            updateDurationInfo(READ_AND_WRITE_DURATION, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    public boolean set(String operationId, OperationStatus newStatus) {
        Objects.requireNonNull(operationId);
        Objects.requireNonNull(newStatus);
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            return callWithLock(
                    getLock(operationId),
                    () -> {
                        setStatusToCache(operationId, newStatus);
                        logger.info("[OperationStatusManager] Set status to {}", newStatus);
                        return true;
                    });
        } catch (Exception e) {
            throw new OperationStatusManagerException("Could not set the status", e);
        } finally {
            stopwatch.stop();
            updateDurationInfo(WRITE_DURATION, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    public boolean compareAndSet(String operationId, UnaryOperator<OperationStatus> mapper) {
        Objects.requireNonNull(operationId);
        Objects.requireNonNull(mapper);
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            return callWithLock(
                    getLock(operationId),
                    () -> {
                        Optional<OperationStatus> optionalStatus = getStatusFromCache(operationId);
                        if (!optionalStatus.isPresent()) {
                            return false;
                        }
                        OperationStatus newStatus = mapper.apply(optionalStatus.get());
                        setStatusToCache(operationId, newStatus);
                        logger.info("[OperationStatusManager] Set status to {}", newStatus);
                        return true;
                    });
        } catch (Exception e) {
            throw new OperationStatusManagerException("Could not set the status", e);
        } finally {
            stopwatch.stop();
            updateDurationInfo(READ_AND_WRITE_DURATION, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    public boolean compareAndSet(
            String operationId, OperationStatus expected, OperationStatus newStatus) {
        Objects.requireNonNull(operationId);
        Objects.requireNonNull(expected);
        Objects.requireNonNull(newStatus);
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            return callWithLock(
                    getLock(operationId),
                    () -> {
                        Optional<OperationStatus> optionalStatus = getStatusFromCache(operationId);
                        if (!optionalStatus.isPresent() || expected != optionalStatus.get()) {
                            return false;
                        }
                        setStatusToCache(operationId, newStatus);
                        logger.info("[OperationStatusManager] Set status to {}", newStatus);
                        return true;
                    });
        } catch (Exception e) {
            throw new OperationStatusManagerException("Could not set the status", e);
        } finally {
            stopwatch.stop();
            updateDurationInfo(WRITE_DURATION, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    public Optional<OperationStatus> get(String operationId) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            return getStatusFromCache(operationId);
        } finally {
            stopwatch.stop();
            updateDurationInfo(READ_DURATION, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    private Optional<OperationStatus> getStatusFromCache(String operationId) {
        return Optional.ofNullable(
                        cacheClient.get(CacheScope.OPERATION_STATUS_BY_OPERATION_ID, operationId))
                .map(cachedString -> OperationStatus.valueOf((String) cachedString));
    }

    private void setStatusToCache(String operationId, OperationStatus status)
            throws ExecutionException, InterruptedException {
        cacheClient
                .set(
                        CacheScope.OPERATION_STATUS_BY_OPERATION_ID,
                        operationId,
                        OPERATION_STATUS_TTL,
                        status.name())
                .get();
    }

    private InterProcessLock getLock(String operationId) {
        String lockPath = String.format(LOCK_PATH_TEMPLATE, operationId);
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
