package se.tink.libraries.metrics;

import com.google.common.annotations.VisibleForTesting;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LowHeapMemoryMonitor {
    private static final Logger log = LoggerFactory.getLogger(LowHeapMemoryMonitor.class);
    private static final double LOW_MEMORY_THRESHOLD = 0.95;

    @Inject
    public LowHeapMemoryMonitor(@Nonnull MetricRegistry metricRegistry) {
        Counter thresholdExceededCounter =
                metricRegistry.meter(MetricId.newId("heap_threshold_exceeded"));
        MemoryPoolMXBean tenuredGenerationMemoryPool =
                findTenuredGenerationMemoryPool()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Cannot initialize heap tenured generation memory monitoring. Cannot find the memory pool."));

        updateMemoryPoolThresholds(tenuredGenerationMemoryPool, LOW_MEMORY_THRESHOLD);
        subscribeToMemoryPoolThreshold(
                tenuredGenerationMemoryPool, LOW_MEMORY_THRESHOLD, thresholdExceededCounter);

        log.debug("Started heap memory monitoring successfully");
    }

    private static void subscribeToMemoryPoolThreshold(
            MemoryPoolMXBean memoryPoolMxBean, double threshold, Counter counter) {
        NotificationEmitter notificationEmitter =
                (NotificationEmitter) ManagementFactory.getMemoryMXBean();
        notificationEmitter.addNotificationListener(
                (notification, handback) ->
                        onThresholdExceeded(notification, memoryPoolMxBean, threshold, counter),
                null,
                null);
    }

    @VisibleForTesting
    static void onThresholdExceeded(
            Notification notification,
            MemoryPoolMXBean memoryPoolMxBean,
            double threshold,
            Counter counter) {
        MemoryUsage usage = memoryPoolMxBean.getUsage();
        double relativeMemoryUsage = (double) usage.getUsed() / usage.getMax();
        // get usage notification early to update collection threshold in case it hash changed
        if (MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED.equals(notification.getType())) {
            log.debug(
                    "Tenured generation memory pool usage {}% exceeds threshold, adjusting collection threshold",
                    String.format("%.4f", relativeMemoryUsage));
            updateMemoryPoolThresholds(memoryPoolMxBean, threshold);
        } else if (MemoryNotificationInfo.MEMORY_COLLECTION_THRESHOLD_EXCEEDED.equals(
                notification.getType())) {
            log.error(
                    "Tenured generation memory pool usage {}% after garbage collection exceeded threshold, "
                            + "running low on memory",
                    String.format("%.4f", relativeMemoryUsage));
            counter.inc();
        }
    }

    @VisibleForTesting
    static void updateMemoryPoolThresholds(MemoryPoolMXBean memoryPoolMxBean, double threshold) {
        MemoryUsage usage = memoryPoolMxBean.getUsage();
        memoryPoolMxBean.setCollectionUsageThreshold((int) Math.floor(usage.getMax() * threshold));
        memoryPoolMxBean.setUsageThreshold((int) Math.floor(usage.getMax() * threshold));
    }

    @VisibleForTesting
    static Optional<MemoryPoolMXBean> findTenuredGenerationMemoryPool() {
        return ManagementFactory.getMemoryPoolMXBeans().stream()
                .filter(pool -> pool.getType() == MemoryType.HEAP)
                .filter(MemoryPoolMXBean::isUsageThresholdSupported)
                .findFirst();
    }
}
