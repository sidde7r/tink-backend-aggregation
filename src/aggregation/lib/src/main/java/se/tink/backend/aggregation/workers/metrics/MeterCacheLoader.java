package se.tink.backend.aggregation.workers.metrics;

import com.google.common.cache.CacheLoader;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.types.counters.Counter;

public class MeterCacheLoader extends CacheLoader<MetricId.MetricLabels, Counter> {
    private MetricRegistry metricRegistry;
    private MetricId metric;

    public MeterCacheLoader(MetricRegistry metricRegistry, String metric) {
        this.metricRegistry = metricRegistry;
        this.metric = MetricId.newId(metric);
    }

    @Override
    public Counter load(MetricId.MetricLabels labels) throws Exception {
        return metricRegistry.meter(metric.label(labels));
    }
}
