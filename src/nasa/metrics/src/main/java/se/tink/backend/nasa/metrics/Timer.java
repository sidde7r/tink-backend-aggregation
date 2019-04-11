package se.tink.backend.nasa.metrics;

import com.google.common.base.MoreObjects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

public class Timer implements Metric {

    private final LongAdder count = new LongAdder();
    private final DoubleAdder sumSeconds = new DoubleAdder();
    private final MetricBuckets buckets;

    public Timer(MetricBuckets buckets) {
        this.buckets = buckets;
    }

    public static class Context {
        private Timer parent;
        private long startNs;

        public Context(Timer parent) {
            // TODO: Replace this with Clock when we have Java 8
            this.parent = parent;
            this.startNs = System.nanoTime();
        }

        public Context() {
            this(null);
        }

        public long stop() {
            long delta = System.nanoTime() - this.startNs;
            if (this.parent != null) {
                this.parent.update(delta, TimeUnit.NANOSECONDS);
            }
            return delta;
        }
    }

    public Context time() {
        return new Context(this);
    }

    public Context time(Context context) {
        context.parent = this;
        return context;
    }

    public void update(long duration, TimeUnit unit) {
        // Count, sum and buckets could be inconsistent, but that's also the
        // case in
        // https://github.com/prometheus/client_golang/blob/master/prometheus/histogram.go#L240.

        double inc = (double) unit.toMicros(duration) / 1000000L;
        count.increment();
        sumSeconds.add(inc);
        buckets.update(inc);
    }

    public long getCount() {
        return this.count.sum();
    }

    public double getSumSeconds() {
        return this.sumSeconds.sum();
    }

    public MetricBuckets getBuckets() {
        return this.buckets;
    }

    @Override
    public void register(MetricCollector collector, MetricId id) {
        collector.register(id.suffix("seconds"), this);
    }

    private class TimedRunnable implements Runnable {
        private final Runnable delegate;
        private final boolean measureFailures;

        public TimedRunnable(Runnable delegate, boolean measureFailures) {
            this.delegate = delegate;
            this.measureFailures = measureFailures;
        }

        @Override
        public void run() {
            final Context context = time();
            try {
                delegate.run();
            } catch (Exception e) {
                if (measureFailures) {
                    context.stop();
                }
                throw e;
            }
            context.stop();
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("delegate", delegate).toString();
        }
    }

    private class TimedCallable<V> implements Callable<V> {
        private final Callable<V> delegate;
        private final boolean measureFailures;

        public TimedCallable(Callable<V> delegate, boolean measureFailures) {
            this.delegate = delegate;
            this.measureFailures = measureFailures;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("delegate", delegate).toString();
        }

        @Override
        public V call() throws Exception {
            final Context context = time();
            V result;
            try {
                result = delegate.call();
            } catch (Exception e) {
                if (measureFailures) {
                    context.stop();
                }
                throw e;
            }
            context.stop();
            return result;
        }
    }

    public Runnable wrap(Runnable r) {
        return wrap(r, false);
    }

    public Runnable wrap(Runnable r, boolean measureFailures) {
        return new TimedRunnable(r, measureFailures);
    }

    public <V> Callable<V> wrap(Callable<V> callable) {
        return wrap(callable, false);
    }

    public <V> Callable<V> wrap(Callable<V> callable, boolean measureFailures) {
        return new TimedCallable<V>(callable, measureFailures);
    }
}
