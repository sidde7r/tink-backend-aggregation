package se.tink.backend.aggregation.workers.metrics;

import com.google.common.cache.CacheLoader;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;

public class TimerCacheLoader extends CacheLoader<MetricId.MetricLabels, Timer> {
    private MetricRegistry metricRegistry;
    private MetricId metric;

    public TimerCacheLoader(MetricRegistry metricRegistry, String metric) {
        this.metricRegistry = metricRegistry;
        this.metric = MetricId.newId(metric);
    }

    @Override
    public Timer load(MetricId.MetricLabels meterPrefix) throws Exception {
        return metricRegistry.timer(metric.label(meterPrefix));
    }
}
