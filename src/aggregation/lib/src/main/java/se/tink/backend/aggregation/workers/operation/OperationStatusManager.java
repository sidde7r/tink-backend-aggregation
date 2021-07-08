package se.tink.backend.aggregation.workers.operation;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import se.tink.libraries.cache.CacheClient;
import se.tink.libraries.cache.CacheScope;

public class OperationStatusManager {

    @VisibleForTesting
    static final String LOCK_PATH_TEMPLATE = "/locks/aggregation/OperationStatusManager/%s";

    @VisibleForTesting static final int OPERATION_STATUS_TTL = (int) TimeUnit.MINUTES.toSeconds(20);

    private final CacheClient cacheClient;
    private final LockSupplier lockSupplier;

    @Inject
    public OperationStatusManager(CacheClient cacheClient, LockSupplier lockSupplier) {
        this.cacheClient = cacheClient;
        this.lockSupplier = lockSupplier;
    }

    public boolean setIfEmpty(String operationId, OperationStatus status) {
        Objects.requireNonNull(operationId);
        Objects.requireNonNull(status);
        try {
            return callWithLock(
                    getLock(operationId),
                    () -> {
                        Optional<OperationStatus> optionalStatus = getStatusFromCache(operationId);
                        if (optionalStatus.isPresent()) {
                            return false;
                        }
                        setStatusToCache(operationId, status);
                        return true;
                    });
        } catch (Exception e) {
            throw new OperationStatusManagerException("Could not set the status", e);
        }
    }

    public boolean set(String operationId, OperationStatus status) {
        Objects.requireNonNull(operationId);
        Objects.requireNonNull(status);
        try {
            return callWithLock(
                    getLock(operationId),
                    () -> {
                        setStatusToCache(operationId, status);
                        return true;
                    });
        } catch (Exception e) {
            throw new OperationStatusManagerException("Could not set the status", e);
        }
    }

    public boolean compareAndSet(String operationId, UnaryOperator<OperationStatus> mapper) {
        Objects.requireNonNull(operationId);
        Objects.requireNonNull(mapper);
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
                        return true;
                    });
        } catch (Exception e) {
            throw new OperationStatusManagerException("Could not set the status", e);
        }
    }

    public boolean compareAndSet(
            String operationId, OperationStatus expected, OperationStatus newValue) {
        Objects.requireNonNull(operationId);
        Objects.requireNonNull(expected);
        Objects.requireNonNull(newValue);
        try {
            return callWithLock(
                    getLock(operationId),
                    () -> {
                        Optional<OperationStatus> optionalStatus = getStatusFromCache(operationId);
                        if (!optionalStatus.isPresent() || expected != optionalStatus.get()) {
                            return false;
                        }
                        setStatusToCache(operationId, newValue);
                        return true;
                    });
        } catch (Exception e) {
            throw new OperationStatusManagerException("Could not set the status", e);
        }
    }

    public Optional<OperationStatus> get(String operationId) {
        return getStatusFromCache(operationId);
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
}
