package se.tink.backend.aggregation.cli;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.GaugeMetricFamily;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToIntFunction;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

/**
 * This is a copy of QueuedThreadPoolStatisticsCollector from io.prometheus:simpleclient_jetty_jdk8
 * This library expects jetty 9.4.x where method QueuedThreadPool.getQueueSize exists. However, we
 * are running jetty 9.0.7 from Dropwizard and the method doesn't exist yet. This copy strips the
 * use of that method (effectively removing one gauge).
 */
public class QueuedThreadPoolStatisticsCollector extends Collector {

    private static final List<String> LABEL_NAMES = Collections.singletonList("unit");

    private final Map<String, QueuedThreadPool> queuedThreadPoolMap = new ConcurrentHashMap<>();

    public QueuedThreadPoolStatisticsCollector() {}

    public QueuedThreadPoolStatisticsCollector(QueuedThreadPool queuedThreadPool, String name) {
        add(queuedThreadPool, name);
    }

    public QueuedThreadPoolStatisticsCollector add(QueuedThreadPool queuedThreadPool, String name) {
        queuedThreadPoolMap.put(name, queuedThreadPool);
        return this;
    }

    @Override
    public List<MetricFamilySamples> collect() {
        return Arrays.asList(
                buildGauge(
                        "jetty_queued_thread_pool_threads",
                        "Number of total threads",
                        QueuedThreadPool::getThreads),
                buildGauge(
                        "jetty_queued_thread_pool_threads_idle",
                        "Number of idle threads",
                        QueuedThreadPool::getIdleThreads),
                buildGauge(
                        "jetty_queued_thread_pool_threads_max",
                        "Max size of thread pool",
                        QueuedThreadPool::getMaxThreads));
    }

    @Override
    public <T extends Collector> T register(CollectorRegistry registry) {
        if (queuedThreadPoolMap.isEmpty()) {
            throw new IllegalStateException("You must register at least one QueuedThreadPool.");
        }
        return super.register(registry);
    }

    private GaugeMetricFamily buildGauge(
            String metric, String help, ToIntFunction<QueuedThreadPool> metricValueProvider) {
        final GaugeMetricFamily metricFamily = new GaugeMetricFamily(metric, help, LABEL_NAMES);
        queuedThreadPoolMap.forEach(
                (key, value) ->
                        metricFamily.addMetric(
                                Collections.singletonList(key),
                                metricValueProvider.applyAsInt(value)));
        return metricFamily;
    }
}
