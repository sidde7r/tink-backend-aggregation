package se.tink.backend.aggregation.workers.operation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.workers.operation.RequestStatusManager.LOCK_PATH_TEMPLATE;
import static se.tink.backend.aggregation.workers.operation.RequestStatusManager.REQUEST_STATUS_TTL;

import java.util.concurrent.CompletableFuture;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.libraries.cache.CacheClient;
import se.tink.libraries.cache.CacheScope;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.metrics.types.histograms.Histogram;

@RunWith(MockitoJUnitRunner.class)
public class RequestStatusManagerTest {

    @Mock private CacheClient cacheClient;
    @Mock private LockSupplier lockSupplier;
    @Mock private InterProcessLock lock;
    @Mock private MetricRegistry metricRegistry;
    @InjectMocks private RequestStatusManager statusManager;

    @Before
    public void setup() {
        when(metricRegistry.histogram(any(), any())).thenReturn(mock(Histogram.class));
    }

    @Test
    public void setWhenIsEmptyThenSetSuccessfully() {
        // given
        String requestId = "0339b720-3352-4797-b955-ca78bbf5016a";
        when(lockSupplier.getLock(eq(String.format(LOCK_PATH_TEMPLATE, requestId))))
                .thenReturn(lock);
        when(cacheClient.set(any(), any(), anyInt(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        // when
        boolean set = statusManager.set(requestId, RequestStatus.STARTED);

        // then
        assertTrue(set);
        verify(cacheClient)
                .set(
                        eq(CacheScope.REQUEST_STATUS_BY_REQUEST_ID),
                        eq(requestId),
                        eq(REQUEST_STATUS_TTL),
                        eq(RequestStatus.STARTED.getIntValue()));
        verifyLockUsage();
    }

    @Test
    public void compareAndSetWhenStatusIsExpectedThenSetSuccessfully() {
        // given
        String requestId = "0339b720-3352-4797-b955-ca78bbf5016a";
        when(lockSupplier.getLock(eq(String.format(LOCK_PATH_TEMPLATE, requestId))))
                .thenReturn(lock);
        when(cacheClient.get(eq(CacheScope.REQUEST_STATUS_BY_REQUEST_ID), eq(requestId)))
                .thenReturn(RequestStatus.STARTED.getIntValue());
        when(cacheClient.set(any(), any(), anyInt(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        // when
        boolean set =
                statusManager.compareAndSet(
                        requestId, RequestStatus.STARTED, RequestStatus.TRYING_TO_ABORT);

        // then
        assertTrue(set);
        verify(cacheClient)
                .set(
                        eq(CacheScope.REQUEST_STATUS_BY_REQUEST_ID),
                        eq(requestId),
                        eq(REQUEST_STATUS_TTL),
                        eq(RequestStatus.TRYING_TO_ABORT.getIntValue()));
        verifyLockUsage();
    }

    @Test
    public void compareAndSetWhenStatusIsNotExpectedThenNotSet() {
        // given
        String requestId = "0339b720-3352-4797-b955-ca78bbf5016a";
        when(lockSupplier.getLock(eq(String.format(LOCK_PATH_TEMPLATE, requestId))))
                .thenReturn(lock);
        when(cacheClient.get(eq(CacheScope.REQUEST_STATUS_BY_REQUEST_ID), eq(requestId)))
                .thenReturn(RequestStatus.ABORTING.getIntValue());

        // when
        boolean set =
                statusManager.compareAndSet(
                        requestId, RequestStatus.STARTED, RequestStatus.TRYING_TO_ABORT);

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
