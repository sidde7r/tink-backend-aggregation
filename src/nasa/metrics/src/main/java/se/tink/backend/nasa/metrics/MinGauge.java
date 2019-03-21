package se.tink.backend.nasa.metrics;

import java.util.concurrent.atomic.AtomicReference;

public class MinGauge implements Metric {
    private AtomicReference<Number> minValue = new AtomicReference<>(Integer.MAX_VALUE);

    @Override
    public void register(MetricCollector exporter, MetricId id) {
        exporter.register(id, new Gauge() {
            @Override
            public double getValue() {
                return minValue.get().doubleValue();
            }
        });
    }

    public void update(Number value) {
        while (true) {
            Number currentMin = minValue.get();
            if (currentMin != null && currentMin.doubleValue() <= value.doubleValue()) {
                break;
            }
            // We have found a smaller value.
            if (minValue.compareAndSet(currentMin, value)) {
                break;
            }
            // If we came here, somebody beat us to setting the minValue.
        }
    }
}
