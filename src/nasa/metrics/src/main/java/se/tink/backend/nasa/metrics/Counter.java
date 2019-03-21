package se.tink.backend.nasa.metrics;

import java.util.concurrent.atomic.LongAdder;

public class Counter implements Metric {
    private LongAdder count = new LongAdder();

    public void inc() {
        this.count.increment();
    }

    public void inc(long n) {
        this.count.add(n);
    }

    public double getValue() {
        return count.sum();
    }

    public long getCount() {
        return count.sum();
    }

    @Override
    public void register(MetricCollector exporter, MetricId id) {
        exporter.register(id.suffix("total"), this);
    }
}
