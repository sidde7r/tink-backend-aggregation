package se.tink.libraries.metrics;

/**
 * Try to use an implementation of {@link MaxGauge}, {@link MinGauge}, {@link IncrementDecrementGauge} or
 * @{@link LastUpdateGauge} instead.
 */
@Deprecated
public abstract class Gauge implements Metric {

    abstract public double getValue();

    @Override
    public void register(MetricCollector exporter, MetricId id) {
        exporter.register(id, this);
    }
}
