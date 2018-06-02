package se.tink.libraries.metrics;

public interface Metric {
    void register(MetricCollector exporter, MetricId id);
}
