package se.tink.backend.nasa.metrics;

/**
 * Try to use an implementation of {@link MaxGauge}, {@link MinGauge}, {@link
 * IncrementDecrementGauge} or @{@link LastUpdateGauge} instead.
 */
@Deprecated
public abstract class Gauge implements Metric {

    public abstract double getValue();

    @Override
    public void register(MetricCollector exporter, MetricId id) {
        exporter.register(id, this);
    }
}
