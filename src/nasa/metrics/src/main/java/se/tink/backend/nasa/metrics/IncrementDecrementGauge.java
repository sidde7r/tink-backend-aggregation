package se.tink.backend.nasa.metrics;

import java.util.concurrent.atomic.LongAdder;

public class IncrementDecrementGauge implements Metric {
    private final LongAdder value = new LongAdder();

    @Override
    public void register(MetricCollector exporter, MetricId id) {
        exporter.register(id, new Gauge() {
            @Override
            public double getValue() {
                return value.doubleValue();
            }
        });
    }

    public void increment() {
        value.increment();
    }

    public void decrement() {
        value.decrement();
    }

    public Number getValue() {
        return value;
    }
}
