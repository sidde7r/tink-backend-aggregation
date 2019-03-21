package se.tink.backend.nasa.metrics;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

public class Histogram implements Metric {

    private LongAdder count = new LongAdder();

    private DoubleAdder sum = new DoubleAdder();
    private MetricBuckets buckets;
    private AtomicReference<Number> minValue = new AtomicReference<>();
    private AtomicReference<Number> maxValue = new AtomicReference<>();

    public Histogram(MetricBuckets buckets) {
        this.buckets = buckets;
    }

    public void update(Number value) {
        // Since this method isn't synchronized, count, sum and buckets could be inconsistent, but that's also the
        // case in https://github.com/prometheus/client_golang/blob/master/prometheus/histogram.go#L240 so probably
        // okay.

        this.count.increment();
        this.sum.add(value.doubleValue());
        buckets.update(value.doubleValue());
        updateMaxValue(value);
        updateMinValue(value);
    }

    private void updateMaxValue(Number value) {
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

    private void updateMinValue(Number value) {
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

    public long getCount() {
        return this.count.sum();
    }

    public double getSum() {
        return this.sum.sum();
    }

    public MetricBuckets getBuckets() {
        return this.buckets;
    }

    public Optional<Number> getMinValue() {
        return Optional.ofNullable(minValue.get());
    }

    public Optional<Number> getMaxValue() {
        return Optional.ofNullable(maxValue.get());
    }

    @Override
    public void register(MetricCollector exporter, MetricId id) {
        exporter.register(id, this);
    }
}
