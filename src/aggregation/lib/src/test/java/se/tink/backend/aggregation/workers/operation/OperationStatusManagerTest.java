package se.tink.backend.aggregation.workers.operation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.workers.operation.OperationStatusManager.LOCK_PATH_TEMPLATE;
import static se.tink.backend.aggregation.workers.operation.OperationStatusManager.OPERATION_STATUS_TTL;

import java.util.concurrent.CompletableFuture;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.libraries.cache.CacheClient;
import se.tink.libraries.cache.CacheScope;

@RunWith(MockitoJUnitRunner.class)
public class OperationStatusManagerTest {

    @Mock private CacheClient cacheClient;
    @Mock private LockSupplier lockSupplier;
    @Mock private InterProcessLock lock;
    @InjectMocks private OperationStatusManager statusManager;

    @Test
    public void setIfEmptyWhenIsEmptyThenSetSuccessfully() {
        // given
        String operationId = "0339b720-3352-4797-b955-ca78bbf5016a";
        when(lockSupplier.getLock(eq(String.format(LOCK_PATH_TEMPLATE, operationId))))
                .thenReturn(lock);
        when(cacheClient.get(eq(CacheScope.OPERATION_STATUS_BY_OPERATION_ID), eq(operationId)))
                .thenReturn(null);
        when(cacheClient.set(any(), any(), anyInt(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        // when
        boolean set = statusManager.setIfEmpty(operationId, OperationStatus.STARTED);

        // then
        assertTrue(set);
        verify(cacheClient)
                .set(
                        eq(CacheScope.OPERATION_STATUS_BY_OPERATION_ID),
                        eq(operationId),
                        eq(OPERATION_STATUS_TTL),
                        eq(OperationStatus.STARTED.name()));
        verifyLockUsage();
    }

    @Test
    public void setIfEmptyWhenIsNotEmptyThenNotSet() {
        // given
        String operationId = "0339b720-3352-4797-b955-ca78bbf5016a";
        when(lockSupplier.getLock(eq(String.format(LOCK_PATH_TEMPLATE, operationId))))
                .thenReturn(lock);
        when(cacheClient.get(eq(CacheScope.OPERATION_STATUS_BY_OPERATION_ID), eq(operationId)))
                .thenReturn(OperationStatus.STARTED.name());

        // when
        boolean set = statusManager.setIfEmpty(operationId, OperationStatus.STARTED);

        // then
        assertFalse(set);
        verify(cacheClient, never()).set(any(), any(), anyInt(), any());
        verifyLockUsage();
    }

    @Test
    public void compareAndSetWhenStatusIsExpectedThenSetSuccessfully() {
        // given
        String operationId = "0339b720-3352-4797-b955-ca78bbf5016a";
        when(lockSupplier.getLock(eq(String.format(LOCK_PATH_TEMPLATE, operationId))))
                .thenReturn(lock);
        when(cacheClient.get(eq(CacheScope.OPERATION_STATUS_BY_OPERATION_ID), eq(operationId)))
                .thenReturn(OperationStatus.STARTED.name());
        when(cacheClient.set(any(), any(), anyInt(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        // when
        boolean set =
                statusManager.compareAndSet(
                        operationId, OperationStatus.STARTED, OperationStatus.TRYING_TO_ABORT);

        // then
        assertTrue(set);
        verify(cacheClient)
                .set(
                        eq(CacheScope.OPERATION_STATUS_BY_OPERATION_ID),
                        eq(operationId),
                        eq(OPERATION_STATUS_TTL),
                        eq(OperationStatus.TRYING_TO_ABORT.name()));
        verifyLockUsage();
    }

    @Test
    public void compareAndSetWhenStatusIsNotExpectedThenNotSet() {
        // given
        String operationId = "0339b720-3352-4797-b955-ca78bbf5016a";
        when(lockSupplier.getLock(eq(String.format(LOCK_PATH_TEMPLATE, operationId))))
                .thenReturn(lock);
        when(cacheClient.get(eq(CacheScope.OPERATION_STATUS_BY_OPERATION_ID), eq(operationId)))
                .thenReturn(OperationStatus.ABORTING.name());

        // when
        boolean set =
                statusManager.compareAndSet(
                        operationId, OperationStatus.STARTED, OperationStatus.TRYING_TO_ABORT);

        // then
        assertFalse(set);
        verify(cacheClient, never()).set(any(), any(), anyInt(), any());
        verifyLockUsage();
    }

    private void verifyLockUsage() {
        try {
            verify(lock).acquire();
            verify(lock).release();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
