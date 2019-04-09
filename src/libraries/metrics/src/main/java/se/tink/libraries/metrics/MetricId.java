package se.tink.libraries.metrics;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Objects;

/** Immutable (and thread-safe) metric id. */
public class MetricId {

    /** Immutable (and thread-safe) metric labels. */
    public static class MetricLabels {
        private final ImmutableMap<String, String> values;

        public MetricLabels() {
            this.values = ImmutableMap.of();
        }

        public static MetricLabels createEmpty() {
            return new MetricLabels();
        }

        public MetricLabels(Map<String, String> values) {
            this.values = ImmutableMap.copyOf(values);
        }

        public MetricId.MetricLabels add(String key, String label) {
            return new MetricLabels(
                    ImmutableMap.<String, String>builder().putAll(values).put(key, label).build());
        }

        public MetricId.MetricLabels addAll(MetricLabels moreLabels) {
            return new MetricLabels(
                    ImmutableMap.<String, String>builder()
                            .putAll(values)
                            .putAll(moreLabels.values)
                            .build());
        }

        private ImmutableMap<String, String> getValues() {
            return ImmutableMap.copyOf(values);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            MetricLabels that = (MetricLabels) o;
            return com.google.common.base.Objects.equal(values, that.values);
        }

        @Override
        public int hashCode() {
            return com.google.common.base.Objects.hashCode(values);
        }
    }

    private final MetricLabels labels;
    private final String metric;

    private MetricId(String metric, MetricLabels labels) {
        this.labels = labels;
        this.metric = metric;
    }

    public static MetricId newId(String metric) {
        return new MetricId(metric, new MetricLabels());
    }

    /* Add a label to the metric */
    public MetricId label(String key, String label) {
        MetricLabels newLabels = this.labels.add(key, label == null ? "null" : label);
        return new MetricId(metric, newLabels);
    }

    public MetricId label(String key, boolean label) {
        return label(key, String.valueOf(label));
    }

    /* Add a set of metric labels to the existing metric */
    public MetricId label(MetricLabels otherLabels) {
        MetricLabels newLabels = this.labels.addAll(otherLabels);
        return new MetricId(metric, newLabels);
    }

    public MetricId suffix(String suffix) {
        return new MetricId(metric + "_" + suffix, this.labels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metric, labels);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MetricId rhs = (MetricId) obj;
        return Objects.equals(rhs.metric, metric) && Objects.equals(rhs.labels, labels);
    }

    @Override
    public String toString() {
        MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(metric);
        for (Map.Entry<String, String> label : labels.getValues().entrySet()) {
            helper.add(label.getKey(), label.getValue());
        }
        return helper.toString();
    }

    public String getMetricName() {
        return metric;
    }

    public String getMetricHelp() {
        return "TODO: No help available";
    }

    public Map<String, String> getLabels() {
        return labels.getValues();
    }
}
