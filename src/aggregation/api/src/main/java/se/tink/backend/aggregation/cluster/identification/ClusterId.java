package se.tink.backend.aggregation.cluster.identification;

import com.google.common.base.Strings;
import se.tink.libraries.metrics.MetricId;

public class ClusterId {
    public static final String CLUSTER_NAME_HEADER = "x-tink-cluster-name";
    public static final String CLUSTER_ENVIRONMENT_HEADER = "x-tink-cluster-environment";

    private final String name;
    private final String environment;

    private ClusterId(String name, String environment) {
        this.name = name;
        this.environment = environment;
    }

    public MetricId.MetricLabels metricLabels() {
        if (!isValidId()) {
            return MetricId.MetricLabels.createEmpty();
        }

        return new MetricId.MetricLabels()
                .add("request_cluster", name)
                .add("request_environment", environment);
    }

    public boolean isValidId() {
        return !(Strings.isNullOrEmpty(name) || Strings.isNullOrEmpty(environment));
    }

    public String getName() {
        return name;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getId() {
        return String.format("%s-%s", name, environment);
    }

    public static ClusterId createEmpty() {
        return new ClusterId(null, null);
    }

    public static ClusterId of(String name, String environment) {
        return new ClusterId(name, environment);
    }
}
