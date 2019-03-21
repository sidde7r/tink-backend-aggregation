package se.tink.backend.nasa.metrics;

import java.util.concurrent.atomic.AtomicReference;

public class LastUpdateGauge implements Metric {
    private AtomicReference<Number> value = new AtomicReference<>(0);

    @Override
    public void register(MetricCollector exporter, MetricId id) {
        exporter.register(id, new Gauge() {
            @Override
            public double getValue() {
                return value.get().doubleValue();
            }
        });
    }

    public void update(Number value) {
        this.value.set(value);
    }
}
