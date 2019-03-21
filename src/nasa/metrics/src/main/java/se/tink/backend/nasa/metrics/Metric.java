package se.tink.backend.nasa.metrics;

public interface Metric {
    void register(MetricCollector exporter, MetricId id);
}
