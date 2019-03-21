package se.tink.backend.nasa.metrics;

import com.google.common.cache.CacheLoader;

public class CounterCacheLoader extends CacheLoader<MetricId.MetricLabels, Counter> {
    private final MetricRegistry metricRegistry;
    private final MetricId meterPrefix;

    public CounterCacheLoader(MetricRegistry metricRegistry, MetricId meterPrefix) {
        this.metricRegistry = metricRegistry;
        this.meterPrefix = meterPrefix;
    }

    @Override
    public Counter load(MetricId.MetricLabels labels) throws Exception {
        return metricRegistry.meter(meterPrefix.label(labels));
    }
}
