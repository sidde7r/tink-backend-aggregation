package se.tink.backend.nasa.metrics;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricRegistry {

    private final MetricCollector[] collectors;
    private final ConcurrentMap<MetricId, Metric> metrics = Maps.newConcurrentMap();
    private final static Logger LOG = LoggerFactory.getLogger(MetricRegistry.class);

    @Inject
    public MetricRegistry(MetricCollector metricCollector) {
        this(new MetricCollector[]{metricCollector});
    }

    // Note that this method must also support receiving zero collectors.
    public MetricRegistry(MetricCollector... collectors) {
        this.collectors = collectors;
    }

    /**
     * Register a metric.
     * <p>
     * This method assumes that nothing else has registered with the same name. <emph>If someone else has registered a
     * metric with the same name, this method will not have any effect and your metrics _will_ be lost.</emph> It is
     * recommended to use factory methods such as {@link #histogram(MetricId)}, {@link #timer(MetricId)},
     * {@link #lastUpdateGauge(MetricId)}, {@link #minGauge(MetricId)} or {@link #maxGauge(MetricId)} instead.
     *
     * @param id     id of the metric to be registered
     * @param metric the metric to be registered
     * @param <T>    the type of the metric to be registered
     * @return the previously registered metric, or metric if it was registered.
     */
    public <T extends Metric> T registerSingleton(@Nonnull MetricId id, @Nonnull T metric) {
        Optional<T> preexistingMetric = registerOrReturnPreexistingMetric(id, metric);
        preexistingMetric.ifPresent(k -> {
            // Majority of these cases are Gauges which are reregistered. I've already discovered a few cases of these.
            LOG.warn(String.format(
                    "Metric was already registered. Likely a bug. If Gauge, please use specialized Gauge instead. Metric id: %s",
                    id));
        });
        return preexistingMetric.orElse(metric);
    }

    private <T extends Metric> Optional<T> registerOrReturnPreexistingMetric(@Nonnull MetricId id, @Nonnull T metric) {
        Preconditions.checkNotNull(id);

        // Important as we otherwise risk getting `null` values in `metrics`, which means `ConcurrentMap#putIfAbsent`
        // returning `null` could mean that value was either absent _or_ present.
        Preconditions.checkNotNull(metric);

        // We assume that a metric with the exact same MetricId is of the same type. If not, a ClassCastException will
        // be thrown.
        Optional<T> preexistingMetric = Optional.ofNullable((T) metrics.putIfAbsent(id, metric));

        // This is the only way to check if a new value was inserted above.
        final boolean metricWasInserted = !preexistingMetric.isPresent();

        if (metricWasInserted) {
            for (MetricCollector collector : collectors) {
                metric.register(collector, id);
            }
        }

        return preexistingMetric;
    }

    private <T extends Metric> T registerSafe(@Nonnull MetricId id, @Nonnull T metric) {
        return registerOrReturnPreexistingMetric(id, metric).orElse(metric);
    }

    /**
     * Remove a previously registered metric. Does not nothing if no metric with the id exists.
     * <p>
     * This method is mostly only useful if you use {@link #registerSingleton(MetricId, Metric)}.
     *
     * @param id the id to be removed
     */
    public void remove(@Nonnull MetricId id) {
        Metric removedObject = metrics.remove(id);

        if (removedObject == null) {
            LOG.warn("Tried to remove non-existent metric {}, this is a bug", id.toString());
            return;
        }

        for (MetricCollector collector : collectors) {
            collector.remove(id);
        }
    }

    public Histogram histogram(@Nonnull MetricId id) {
        return histogram(id, MetricBuckets.STANDARD_BUCKETS);
    }

    public Histogram histogram(@Nonnull MetricId id, @Nonnull List<? extends Number> buckets) {
        return registerSafe(id, new Histogram(new MetricBuckets(buckets)));
    }

    public Counter meter(@Nonnull MetricId id) {
        return registerSafe(id, new Counter());
    }

    public Timer timer(@Nonnull MetricId id) {
        return timer(id, MetricBuckets.STANDARD_BUCKETS);
    }

    public Timer timer(@Nonnull MetricId id, @Nonnull List<? extends Number> buckets) {
        return registerSafe(id, new Timer(new MetricBuckets(buckets)));
    }

    public MaxGauge maxGauge(@Nonnull MetricId id) {
        return registerSafe(id, new MaxGauge());
    }

    public MinGauge minGauge(@Nonnull MetricId id) {
        return registerSafe(id, new MinGauge());
    }

    public LastUpdateGauge lastUpdateGauge(@Nonnull MetricId id) {
        return registerSafe(id, new LastUpdateGauge());
    }

    public IncrementDecrementGauge incrementDecrementGauge(@Nonnull MetricId id) {
        return registerSafe(id, new IncrementDecrementGauge());
    }

    public Metric get(@Nonnull MetricId id) {
        return metrics.get(id);
    }

    public ImmutableMap<MetricId, Metric> getMetrics() {
        return ImmutableMap.copyOf(metrics);
    }
}
