package se.tink.libraries.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import javax.management.Notification;
import org.junit.Test;

public class LowHeapMemoryMonitorTest {
    @Test
    public void ableToFindTenuredGenerationMemoryPool() {
        assertNotNull(LowHeapMemoryMonitor.findTenuredGenerationMemoryPool());
    }

    @Test
    public void updateMemoryPoolThresholds() {
        MemoryUsage memoryUsage = mock(MemoryUsage.class);
        when(memoryUsage.getMax()).thenReturn(4L);

        MemoryPoolMXBean memoryPoolMxBean = mock(MemoryPoolMXBean.class);
        when(memoryPoolMxBean.getUsage()).thenReturn(memoryUsage);

        double threshold = 0.5;
        LowHeapMemoryMonitor.updateMemoryPoolThresholds(memoryPoolMxBean, threshold);

        verify(memoryPoolMxBean).setCollectionUsageThreshold(2);
        verify(memoryPoolMxBean).setUsageThreshold(2);
    }

    @Test
    public void handleCollectionThresholdExceededNotification() {
        Notification notification = mock(Notification.class);
        when(notification.getType())
                .thenReturn(MemoryNotificationInfo.MEMORY_COLLECTION_THRESHOLD_EXCEEDED);
        MemoryPoolMXBean memoryPoolMxBean = mock(MemoryPoolMXBean.class, RETURNS_DEEP_STUBS);
        Counter counter = new Counter();
        double threshold = 0.5;

        LowHeapMemoryMonitor.onThresholdExceeded(
                notification, memoryPoolMxBean, threshold, counter);

        assertEquals(
                "Collection threshold notification should update the metric",
                1,
                (int) counter.getValue());
    }

    @Test
    public void handleUsageThresholdExceededNotification() {
        Notification notification = mock(Notification.class);
        when(notification.getType()).thenReturn(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED);
        MemoryUsage usage = mock(MemoryUsage.class);
        when(usage.getMax()).thenReturn(10L);
        MemoryPoolMXBean memoryPoolMxBean = mock(MemoryPoolMXBean.class);
        when(memoryPoolMxBean.getUsage()).thenReturn(usage);

        Counter counter = new Counter();
        double threshold = 0.5;

        LowHeapMemoryMonitor.onThresholdExceeded(
                notification, memoryPoolMxBean, threshold, counter);

        assertEquals(
                "Usage threshold notification should not update the metric",
                0,
                (int) counter.getValue());
        verify(memoryPoolMxBean).setCollectionUsageThreshold(5);
        verify(memoryPoolMxBean).setUsageThreshold(5);
    }
}
