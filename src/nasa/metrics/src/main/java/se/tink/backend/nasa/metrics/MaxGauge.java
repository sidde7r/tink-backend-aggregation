package se.tink.backend.nasa.metrics;

import java.util.concurrent.atomic.AtomicReference;

public class MaxGauge implements Metric {
    private AtomicReference<Number> maxValue = new AtomicReference<>(Integer.MAX_VALUE);

    @Override
    public void register(MetricCollector exporter, MetricId id) {
        exporter.register(
                id,
                new Gauge() {
                    @Override
                    public double getValue() {
                        return maxValue.get().doubleValue();
                    }
                });
    }

    public void update(Number value) {
        while (true) {
            Number currentMax = maxValue.get();
            if (currentMax != null && currentMax.doubleValue() >= value.doubleValue()) {
                break;
            }
            // We have found a larger value.
            if (maxValue.compareAndSet(currentMax, value)) {
                break;
            }
            // If we came here, somebody beat us to setting the maxValue.
        }
    }
}
