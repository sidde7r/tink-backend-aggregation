package se.tink.backend.nasa.metrics;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;

public class MeterFactory {
    private final MetricRegistry metricRegistry;

    @Inject
    public MeterFactory(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public Counter getCounter(MetricId id) {
        return metricRegistry.meter(id);
    }

    public Counter getCounter(String name) {
        return getCounter(MetricId.newId(name));
    }

    public Histogram getHistogram(MetricId id) {
        return metricRegistry.histogram(id);
    }

    public Histogram getHistogram(String name) {
        return getHistogram(MetricId.newId(name));
    }

    public LoadingCache<MetricId.MetricLabels, Counter> createLoadingCache(MetricId meterPrefix) {
        return CacheBuilder.newBuilder().build(new CounterCacheLoader(metricRegistry, meterPrefix));
    }
}
