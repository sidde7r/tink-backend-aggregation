package se.tink.backend.nasa.metrics;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import io.prometheus.client.Collector;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricCollector extends Collector {

    private final static Logger log = LoggerFactory.getLogger(MetricCollector.class);
    private static final String[] EMPTY_STRING_ARRAY = { "" };
    private static final Pattern METRIC_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]+$");

    private final SortedMap<String, MetricGroup> metrics = new TreeMap<>();

    interface MetricSampler {
        MetricSample sample();
    }

    interface MetricSample {
        double[] sample();

        String[] suffixes();

        Type type();
    }

    private class MetricInstance {
        private MetricId id;
        private MetricSampler sampler;

        public MetricInstance(MetricId id, MetricSampler sampler) {
            this.id = id;
            this.sampler = sampler;
        }
    }

    private class MetricGroup {
        private LinkedHashMap<MetricId, MetricInstance> instances;
        private Class<? extends Metric> cls;

        public MetricGroup(Class<? extends Metric> cls) {
            this.cls = cls;
            this.instances = new LinkedHashMap<>();
        }
    }

    private static class StaticMetricSampler implements MetricSampler {
        private final MetricSample sample;

        private StaticMetricSampler(MetricSample sample) {
            this.sample = sample;
        }

        @Override
        public MetricSample sample() {
            return sample;
        }
    }

    private void register(final MetricId id, Class<? extends Metric> cls, final MetricSample sample) {
        register(id, cls, new StaticMetricSampler(sample));
    }

    private synchronized void register(final MetricId id, Class<? extends Metric> cls, final MetricSampler sampler) {
        Preconditions.checkArgument(METRIC_NAME_PATTERN.matcher(id.getMetricName()).matches(),
                String.format("Illegal Prometheus metric name ( %s )", id.getMetricName()));

        for (String labelName : id.getLabels().keySet()) {
            Preconditions.checkArgument(METRIC_NAME_PATTERN.matcher(labelName).matches(),
                    String.format("Illegal Prometheus metric label name ( %s )", labelName));
        }

        MetricGroup group = metrics.get(id.getMetricName());
        if (group == null) {
            group = new MetricGroup(cls);
            metrics.put(id.getMetricName(), group);
        }

        if (group.cls != cls) {
            log.warn("Metric {} does not match metric class of other types in same group ({} vs {})",
                    id, cls.getName(), group.cls.getName());
            return;
        }

        MetricInstance instance = new MetricInstance(id, sampler);
        group.instances.put(id, instance);
    }

    public void register(final MetricId id, final Counter metric) {
        register(id, Counter.class, new MetricSample() {
            @Override
            public double[] sample() {
                return new double[] { metric.getValue() };
            }

            @Override
            public String[] suffixes() {
                return MetricCollector.EMPTY_STRING_ARRAY;
            }

            @Override
            public Type type() {
                return Type.COUNTER;
            }
        });
    }

    public void register(final MetricId id, final Gauge metric) {
        register(id, Gauge.class, new MetricSample() {
            @Override
            public double[] sample() {
                return new double[] { metric.getValue() };
            }

            @Override
            public String[] suffixes() {
                return MetricCollector.EMPTY_STRING_ARRAY;
            }

            @Override
            public Type type() {
                return Type.GAUGE;
            }
        });
    }

    private static class OptionalMetricSampler implements MetricSampler {
        private static final String[] EMPTY_STRING_ARRAY = new String[0];
        private static final double[] EMPTY_DOUBLE_ARRAY = new double[0];

        private final Supplier<Optional<Number>> valueSupplier;

        private OptionalMetricSampler(Supplier<Optional<Number>> valueSupplier) {
            this.valueSupplier = valueSupplier;
        }

        @Override
        public MetricSample sample() {
            Optional<Number> value = valueSupplier.get();
            return new MetricSample() {
                @Override
                public double[] sample() {
                    return value.map(Number::doubleValue).map(v -> new double[] { v }).orElse(EMPTY_DOUBLE_ARRAY);
                }

                @Override
                public String[] suffixes() {
                    return value.map(v -> MetricCollector.EMPTY_STRING_ARRAY).orElse(EMPTY_STRING_ARRAY);
                }

                @Override
                public Type type() {
                    return Type.GAUGE;
                }
            };
        }
    }

    public void register(final MetricId id, final Histogram metric) {
        register(id, Histogram.class, new MetricSample() {
            @Override
            public double[] sample() {
                return new double[] { metric.getCount(), metric.getSum() };
            }

            @Override
            public String[] suffixes() {
                return new String[] { "_count", "_sum" };
            }

            @Override
            public Type type() {
                return Type.HISTOGRAM;
            }
        });
        register(id.suffix("_min"), Gauge.class, new OptionalMetricSampler(() -> metric.getMinValue()));
        register(id.suffix("_max"), Gauge.class, new OptionalMetricSampler(() -> metric.getMaxValue()));

        registerBuckets(id, Histogram.class, metric.getBuckets());
    }

    public void register(final MetricId id, final Timer metric) {
        register(id, Timer.class, new MetricSample() {
            @Override
            public double[] sample() {
                return new double[] { metric.getCount(), metric.getSumSeconds() };
            }

            @Override
            public String[] suffixes() {
                return new String[] { "_count", "_sum" };
            }

            @Override
            public Type type() {
                return Type.HISTOGRAM;
            }
        });

        registerBuckets(id, Timer.class, metric.getBuckets());
    }

    private void registerBuckets(MetricId id, Class<? extends Metric> cls, final MetricBuckets buckets) {
        for (final Double limit : buckets.getLimits()) {
            register(id.label("le", limit.isInfinite() ? "+Inf" : limit.toString()), cls, new MetricSample() {
                @Override
                public double[] sample() {
                    return new double[] { buckets.getBucket(limit) };
                }

                @Override
                public String[] suffixes() {
                    return new String[] { "_bucket" };
                }

                @Override
                public Type type() {
                    return Type.HISTOGRAM;
                }
            });
        }
    }

    public synchronized void remove(final MetricId id) {
        MetricGroup group = metrics.get(id.getMetricName());

        if (group == null) {
            log.warn("Tried to remove non-existent metric group {}, this is a bug", id.toString());
            return;
        }

        group.instances.remove(id);
        if (group.instances.isEmpty()) {
            metrics.remove(id.getMetricName());
        }
    }

    @Override
    public synchronized List<MetricFamilySamples> collect() {
        /*
         * This code creates a structure like this:
         *
         *  # HELP tink_execution_time_duration_seconds TODO: No help available
         *  # TYPE tink_execution_time_duration_seconds summary
         *  tink_execution_time_duration_seconds_count{class="SendNotificationsJob",} 4.0
         *  tink_execution_time_duration_seconds_sum{class="SendNotificationsJob",} 0.034161000000000004
         *  tink_execution_time_duration_seconds_count{class="AutomaticRefreshJob",} 1.0
         *  tink_execution_time_duration_seconds_sum{class="AutomaticRefreshJob",} 0.010852
         *  tink_execution_time_duration_seconds_count{class="SendFraudReminderJob",} 1.0
         *  tink_execution_time_duration_seconds_sum{class="SendFraudReminderJob",} 8.93E-4

         *  The metric group above is "tink_execution_time_duration_seconds" and it has 3 instances in it.
         *  Finally, every instance has two metrics: count and sum.
         */
        LinkedList<MetricFamilySamples> samplesList = new LinkedList<>();
        for (MetricGroup group : this.metrics.values()) {
            /* Calculate common metric properties from the reference (first) metric instance */
            MetricInstance referenceMetric = group.instances.values().iterator().next();
            Type metricType = referenceMetric.sampler.sample().type();
            String metricHelp = referenceMetric.id.getMetricHelp();
            String metricName = "tink_" + referenceMetric.id.getMetricName();

            List<MetricFamilySamples.Sample> sampleList = collectMetricGroup(metricName, group.instances.values());
            MetricFamilySamples samples = new MetricFamilySamples(metricName, metricType, metricHelp, sampleList);
            samplesList.add(samples);
        }
        return samplesList;
    }

    private List<MetricFamilySamples.Sample> collectMetricGroup(String metricName,
            Collection<MetricInstance> instances) {
        List<MetricFamilySamples.Sample> sampleList = new LinkedList<>();
        for (MetricInstance instance : instances) {

            MetricSample snapshot = instance.sampler.sample();
            String[] suffix = snapshot.suffixes();
            double[] values = snapshot.sample();

            List<String> labelKeys = Lists.newArrayList(instance.id.getLabels().keySet().iterator());
            List<String> labelValues = Lists.newArrayList(instance.id.getLabels().values().iterator());

            for (int sub = 0; sub < values.length; sub++) {
                MetricFamilySamples.Sample sample = new MetricFamilySamples.Sample(
                        metricName + suffix[sub], labelKeys, labelValues, values[sub]);
                sampleList.add(sample);
            }
        }
        return sampleList;
    }
}
