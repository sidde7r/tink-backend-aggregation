package se.tink.backend.aggregation.cluster.identification;

import com.google.common.base.Strings;
import com.sun.jersey.spi.container.ContainerRequest;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import se.tink.libraries.metrics.MetricId;

public class ClusterId {
    private static final String CLUSTER_NAME_HEADER = "x-tink-cluster-name";
    private static final String CLUSTER_ENVIRONMENT_HEADER = "x-tink-cluster-environment";
    private static final String AGGREGATOR_NAME_HEADER = "x-tink-aggregator-header";

    private final String name;
    private final String environment;
    private final Aggregator aggregator;

    private ClusterId(String name, String environment, Aggregator aggregator) {
        this.name = name;
        this.environment = environment;
        this.aggregator = aggregator;
    }

    private static Aggregator initAggregator(String aggregatorHeader){
        if(!(Objects.isNull(aggregatorHeader) || aggregatorHeader.equals(""))){
            return Aggregator.of(aggregatorHeader);
        }

        return  Aggregator.of(Aggregator.DEFAULT);
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


    public static String createClustertId(HttpServletRequest request) {
        String clusterName = request.getHeader(CLUSTER_NAME_HEADER);
        String clusterEnvironment = request.getHeader(CLUSTER_ENVIRONMENT_HEADER);
        return String.format("%s-%s", clusterName, clusterEnvironment);
    }

    public Aggregator getAggregator(){
        return aggregator;
    }


    public static ClusterId createFromRequest(HttpServletRequest request) {
        if (Objects.isNull(request)) {
            return createEmpty();
        }

        String clusterName = request.getHeader(CLUSTER_NAME_HEADER);
        String clusterEnvironment = request.getHeader(CLUSTER_ENVIRONMENT_HEADER);
        String aggregatorName = request.getHeader(AGGREGATOR_NAME_HEADER);

        return create(clusterName, clusterEnvironment, initAggregator(aggregatorName));
    }

    public static ClusterId createFromContainerRequest(ContainerRequest request) {
        if (Objects.isNull(request)) {
            return createEmpty();
        }

        String clusterName = request.getHeaderValue(CLUSTER_NAME_HEADER);
        String clusterEnvironment = request.getHeaderValue(CLUSTER_ENVIRONMENT_HEADER);
        String aggregatorName = request.getHeaderValue(AGGREGATOR_NAME_HEADER);

        return create(clusterName, clusterEnvironment, initAggregator(aggregatorName));
    }

    public static ClusterId createEmpty() {
        return new ClusterId(null, null, null);
    }

    public static ClusterId create(String name, String environment, Aggregator aggregator) {
        return new ClusterId(name, environment, aggregator);
    }
}
